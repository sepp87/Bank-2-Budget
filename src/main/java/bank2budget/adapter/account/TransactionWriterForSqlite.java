package bank2budget.adapter.account;

import bank2budget.adapter.repository.SqliteUtil;
import bank2budget.core.Account;
import bank2budget.adapter.account.TransactionWriter;
import bank2budget.core.CashTransaction;
//import bank2budget.core.CashTransaction;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Note to self: dynamic schema definition, infers SQL column types, dynamically
 * creates the table and inserts data dynamically. In short, too over-engineered
 * and is only kept for reference. Basically, not in use.
 *
 * @author joostmeulenkamp
 */
public class TransactionWriterForSqlite extends TransactionWriter {

    private static final String TRANSACTIONS_TABLE = "transactions";
    private static final Map<String, Class<?>> COLUMN_TYPES = new TreeMap<>();

    private final Path databaseFile;
    private Connection connection;

    public TransactionWriterForSqlite(Path databaseFile) {
        this.databaseFile = databaseFile;
    }

    public void write(Collection<Account> accounts) {
        connection = SqliteUtil.getConnection(databaseFile.toAbsolutePath().toString());
        if (connection == null) {
            return;
        }

        boolean success = true; // Track transaction success
        success &= createTable(connection);
        success &= SqliteUtil.clearTable(connection, TRANSACTIONS_TABLE);

        for (Account account : accounts) {
            success &= insertTransactions(connection, account.transactionsAscending());
        }

        try {

            if (success) {
                connection.commit();  // All good, commit the transaction
            } else {
                connection.rollback();  // Something failed, rollback
            }
        } catch (SQLException ex) {
            try {
                connection.rollback();  // Ensure rollback in case of exception
            } catch (SQLException rollbackEx) {
                Logger.getLogger(TransactionWriterForSqlite.class.getName()).log(Level.SEVERE, "Rollback failed", rollbackEx);
            }
            Logger.getLogger(TransactionWriterForSqlite.class.getName()).log(Level.SEVERE, "Transaction failed", ex);
        } finally {
            try {
                connection.close();  // Ensure the connection is closed
            } catch (SQLException closeEx) {
                Logger.getLogger(TransactionWriterForSqlite.class.getName()).log(Level.SEVERE, "Failed to close connection", closeEx);
            }
            connection = null;
        }
    }

    private static boolean createTable(Connection connection) {
        String columns = getColumnsWithTypes();
        String primaryKey = SqliteUtil.getPrimaryKey("accountNumber", "transactionNumber");
        return SqliteUtil.createTable(connection, TRANSACTIONS_TABLE, columns, primaryKey);
    }

    private static String getColumnsWithTypes() {
        String result = "";
        for (String column : HEADER) {
            try {
                Field propertyField = CashTransaction.class.getDeclaredField(column);
                propertyField.setAccessible(true);
                Class<?> type = propertyField.getType();
                COLUMN_TYPES.put(column, type);
                String dbType = determineSqliteType(type);
                result += column + " " + dbType + ", ";
            } catch (NoSuchFieldException | SecurityException ex) {
                Logger.getLogger(TransactionWriterForSqlite.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
        }
        return result;
    }

    public static String determineSqliteType(Class<?> type) {
        if (type == Integer.class || type == int.class
                || type == Long.class || type == long.class
                || type == Boolean.class || type == boolean.class) {
            return "INTEGER"; // Boolean stored as 0 or 1

        } else if (type == Double.class || type == double.class
                || type == Float.class || type == float.class) {
            return "REAL";

        } else {
            return "TEXT"; // Default fallback
        }
    }

    private static boolean insertTransactions(Connection connection, Collection<CashTransaction> transactions) {
        String columns = getColumns();
        String placeholders = SqliteUtil.getPlaceholders(HEADER.length);
        String insertTransactions = "INSERT INTO " + TRANSACTIONS_TABLE + " (" + columns + ") "
                + "VALUES (" + placeholders + ")";

        try (PreparedStatement statement = connection.prepareStatement(insertTransactions)) {

            for (var transaction : transactions) {
                Object[] values = TransactionWriter.getObjectArrayFrom(transaction);
                int i = 0;
                for (String column : HEADER) {
                    setValueForStatement(i, values[i], statement);
                    i++;
                }
                statement.addBatch();  // Batching improves performance
            }

            statement.executeBatch();  // Executes all inserts at once

        } catch (SQLException ex) {
            Logger.getLogger(TransactionWriterForSqlite.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }

    private static String getColumns() {
        String result = "";
        for (String column : HEADER) {
            result += column + ", ";
        }
        return SqliteUtil.removeLastTwoChars(result);
    }

    private static void setValueForStatement(int index, Object value, PreparedStatement statement) {
        int parameterIndex = index + 1;
        Class<?> type = COLUMN_TYPES.get(HEADER[index]);
        try {
            if (type.equals(String.class)) {
                if (value == null) {
                    statement.setNull(parameterIndex, java.sql.Types.VARCHAR);
                } else {
                    statement.setString(parameterIndex, (String) value);
                }

            } else if (type.equals(Integer.class) || type.equals(int.class)) {
                if (value == null) {
                    statement.setNull(parameterIndex, java.sql.Types.INTEGER);
                } else {
                    statement.setInt(parameterIndex, (int) value);
                }

            } else if (type.equals(Double.class) || type.equals(double.class)) {
                if (value == null) {
                    statement.setNull(parameterIndex, java.sql.Types.DOUBLE);
                } else {
                    statement.setDouble(parameterIndex, (double) value);
                }

            } else if (type.equals(Boolean.class) || type.equals(boolean.class)) {
                if (value == null) {
                    statement.setNull(parameterIndex, java.sql.Types.BOOLEAN);
                } else {
                    statement.setBoolean(parameterIndex, (boolean) value);
                }

            } else {
                if (value == null) {
                    statement.setNull(parameterIndex, java.sql.Types.VARCHAR);
                } else {
                    statement.setString(parameterIndex, value.toString());
                }
            }

        } catch (SQLException ex) {
            Logger.getLogger(TransactionWriterForSqlite.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
