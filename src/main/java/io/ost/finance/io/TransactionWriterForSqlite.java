package io.ost.finance.io;

import io.ost.finance.Account;
import io.ost.finance.App;
import io.ost.finance.CashTransaction;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author joostmeulenkamp
 */
public class TransactionWriterForSqlite extends TransactionWriter {

    private static final String DATABASE_PATH = App.getDatabaseDirectory() + "bank-2-budget.db";
    private static final Map<String, Class<?>> COLUMN_TYPES = new TreeMap<>();

    private Connection connection;

    public TransactionWriterForSqlite() {
    }

    public void write(Collection<Account> accounts) {
        connection = getConnection(DATABASE_PATH);
        if (connection == null) {
            return;
        }

        boolean success = true; // Track transaction success
        success &= createTable(connection);
        success &= clearTable(connection);

        for (Account account : accounts) {
            success &= insertTransactions(connection, account.getAllTransactions());
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

    private static Connection getConnection(String path) {
        String url = "jdbc:sqlite:" + path;
        try {
            Connection connection = DriverManager.getConnection(url);
            connection.setAutoCommit(false);
            return connection;
        } catch (SQLException ex) {
            Logger.getLogger(TransactionWriterForSqlite.class.getName()).log(Level.SEVERE, "Could NOT connect to {0}", url);
        }
        return null;
    }

    private static boolean createTable(Connection connection) {
        String columns = getColumnsWithTypes();
        String primaryKey = getPrimaryKey("accountNumber", "transactionNumber");
        String createTable = "CREATE TABLE IF NOT EXISTS transactions ("
                + columns
                + primaryKey
                + ");";
        return executeUpdateStatement(connection, createTable);
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

    public static String getPrimaryKey(String... columns) {
        String result = "PRIMARY KEY (";
        for (String value : columns) {
            result += value + ", ";
        }
        return removeLastTwoChars(result) + ")";
    }

    public static String removeLastTwoChars(String value) {
        return value.substring(0, value.length() - 2); // remove the trailing comma and space ", "
    }

    private static boolean clearTable(Connection connection) {
        String truncateTable = "DELETE FROM transactions";
        return executeUpdateStatement(connection, truncateTable);
    }

    private static boolean executeUpdateStatement(Connection connection, String sql) {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        } catch (SQLException ex) {
            Logger.getLogger(TransactionWriterForSqlite.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }

    private static boolean insertTransactions(Connection connection, Collection<CashTransaction> transactions) {
        String columns = getColumns();
        String placeholders = getPlaceholders();
        String insertTransactions = "INSERT INTO transactions (" + columns + ") "
                + "VALUES (" + placeholders + ")";

        try (PreparedStatement statement = connection.prepareStatement(insertTransactions)) {

            for (CashTransaction transaction : transactions) {
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
        return removeLastTwoChars(result);
    }

    private static String getPlaceholders() {
        return String.join(", ", Collections.nCopies(HEADER.length, "?"));
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
