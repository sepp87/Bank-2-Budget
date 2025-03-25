package io.ost.finance.io;

import io.ost.finance.Account;
import io.ost.finance.App;
import io.ost.finance.CashTransaction;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Base64;
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

    private static final String TRANSACTIONS_TABLE = "transactions";
    private static final Map<String, Class<?>> COLUMN_TYPES = new TreeMap<>();

    private final Connection connection;

    public TransactionWriterForSqlite() {
        String path = App.getDatabaseDirectory() + "bank-2-budget.db";
        this.connection = getConnection(path);
    }

    public void write(Collection<Account> accounts) {
        if (connection == null) {
            return;
        }

        // drop table
        // create table
        for (Account account : accounts) {
            for (CashTransaction transaction : account.getAllTransactions()) {

            }
        }
    }

    private static Connection getConnection(String path) {
        String url = "jdbc:sqlite:" + path;
        try (Connection connection = DriverManager.getConnection(url)) {
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

    private static boolean removeAllRows(Connection connection) {
        String truncateTable = "DELETE FROM transactions";
        return executeUpdateStatement(connection, truncateTable);
    }

    private static boolean dropTable(Connection connection) {
        String dropTable = "DROP TABLE IF EXISTS transactions";
        return executeUpdateStatement(connection, dropTable);
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
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(TransactionWriterForSqlite.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static String getPlaceholders() {
        return String.join(", ", Collections.nCopies(HEADER.length, "?"));
    }

    private static boolean insertTransaction(Connection connection, CashTransaction transaction) {
        String columns = getColumns();
        String values = getValues(transaction);
        String insertRow = "INSERT INTO transactions (" + columns + ") "
                + "VALUES (" + values + ")";
        return executeUpdateStatement(connection, insertRow);
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

    private static String getValues(CashTransaction transaction) {
        Object[] array = getObjectArrayFrom(transaction);
        return arrayToCommaSeparatedString(array);
    }

    private static String arrayToCommaSeparatedString(Object[] array) {
        StringBuilder result = new StringBuilder();

        for (Object value : array) {
            if (value == null) {
                result.append("NULL, ");  // For null values, append "NULL"

            } else if (value instanceof String) {
                // Escape single quotes in strings by doubling them, and enclose in single quotes
                result.append("'").append(value.toString().replace("'", "''")).append("', ");

            } else if (value instanceof Boolean) {
                // Boolean values can be inserted directly as TRUE or FALSE
                result.append(value.toString().toUpperCase()).append(", ");

            } else if (value instanceof Number) {
                // Numbers are added directly
                result.append(value.toString()).append(", ");

            } else if (value instanceof java.util.Date || value instanceof java.sql.Date || value instanceof java.sql.Timestamp) {
                // For Date and Timestamp, format as a string (e.g., 'yyyy-MM-dd HH:mm:ss')
                result.append("'").append(value.toString()).append("', ");

            } else if (value instanceof java.sql.Time) {
                // Handle java.sql.Time as 'HH:mm:ss'
                result.append("'").append(value.toString()).append("', ");

            } else if (value instanceof byte[]) {
                // For byte array (BLOB), convert to Base64 string representation
                result.append("'").append(Base64.getEncoder().encodeToString((byte[]) value)).append("', ");

            } else if (value instanceof java.util.UUID) {
                // For UUID, convert it to string representation
                result.append("'").append(value.toString()).append("', ");

            } else {
                // For other objects (fallback), convert to string and escape it
                result.append("'").append(value.toString().replace("'", "''")).append("', ");
            }
        }

        // Remove the trailing comma and space if any
        if (result.length() > 0) {
            result.setLength(result.length() - 2);
        }

        return result.toString();
    }

    private static String getColumns() {
        String result = "";
        for (String column : HEADER) {
            result += column + ", ";
        }
        return removeLastTwoChars(result);
    }

    private static String getColumnsWithTypes() {
        String result = "";
        for (String column : HEADER) {
            try {
                Field propertyField = CashTransaction.class.getDeclaredField(column);
                propertyField.setAccessible(true);
                Class<?> type = propertyField.getType();
                COLUMN_TYPES.put(column, type);
                String dbType = determineDatabaseTypeFromClass(type);
                result += column + " " + dbType + ", ";
            } catch (NoSuchFieldException | SecurityException ex) {
                Logger.getLogger(TransactionWriterForSqlite.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
        }
        return result;
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

    public static String determineDatabaseTypeFromClass(Class<?> type) {
        if (type == Integer.class || type == int.class) {
            return "INTEGER";

        } else if (type == Long.class || type == long.class) {
            return "BIGINT"; // SQLite: INTEGER (64-bit signed)

        } else if (type == Double.class || type == double.class) {
            return "DOUBLE"; // SQLite: REAL

        } else if (type == Float.class || type == float.class) {
            return "REAL";

        } else if (type == Boolean.class || type == boolean.class) {
            return "BOOLEAN"; // SQLite: INTEGER (0 or 1), MySQL: TINYINT(1), PostgreSQL: BOOLEAN

        } else if (type == String.class) {
            return "TEXT"; // MySQL: VARCHAR(255), PostgreSQL: TEXT

        } else if (type == java.util.Date.class || type == java.sql.Timestamp.class) {
            return "DATETIME"; // SQLite: TEXT (ISO 8601) or INTEGER (Unix timestamp), MySQL: DATETIME, PostgreSQL: TIMESTAMP

        } else if (type == java.sql.Date.class) {
            return "DATE"; // SQLite: TEXT (YYYY-MM-DD) or INTEGER

        } else if (type == java.sql.Time.class) {
            return "TIME"; // SQLite: TEXT (HH:MM:SS) or INTEGER

        } else if (type == byte[].class) {
            return "BLOB"; // Binary storage (images, files)

        } else if (type == java.util.UUID.class) {
            return "TEXT"; // Stored as string

        } else {
            return "TEXT"; // Default to TEXT for unknown types
        }
    }

    public static String determineSqliteType(Class<?> type) {
        if (type == Integer.class || type == int.class
                || type == Long.class || type == long.class
                || type == Boolean.class || type == boolean.class) {
            return "INTEGER"; // Boolean stored as 0 or 1

        } else if (type == Double.class || type == double.class
                || type == Float.class || type == float.class) {
            return "REAL";

        } else if (type == String.class
                || type == java.util.Date.class
                || type == java.sql.Timestamp.class
                || type == java.sql.Date.class
                || type == java.sql.Time.class) {
            return "TEXT"; // Dates and time are stored as TEXT

        } else if (type == byte[].class) {
            return "BLOB";

        } else {
            return "TEXT"; // Default fallback
        }
    }

    public static void checkTableSchema() {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:path_to_your_db.db")) {
            String tableName = "your_table_name";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("PRAGMA table_info(" + tableName + ")");

            while (rs.next()) {
                String columnName = rs.getString("name");
                String dataType = rs.getString("type");
                System.out.println("Column: " + columnName + ", Data Type: " + dataType);
            }
        } catch (SQLException ex) {
            Logger.getLogger(TransactionWriterForSqlite.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
