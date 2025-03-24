package io.ost.finance.io;

import io.ost.finance.App;
import io.ost.finance.CashTransaction;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author joostmeulenkamp
 */
public class TransactionWriterForSqlite extends TransactionWriter {

    public static void test() {
        String path = App.getDatabaseDirectory() + "bank-2-budget.db";
        Connection connection = getConnection(path);
        if (connection == null) {
            return;
        }

    }

    public static Connection getConnection(String path) {
        String url = "jdbc:sqlite:" + path;
        try (Connection connection = DriverManager.getConnection(url)) {
            return connection;
        } catch (SQLException ex) {
            Logger.getLogger(TransactionWriterForSqlite.class.getName()).log(Level.SEVERE, "Could NOT connect to {0}", url);
        }
        return null;
    }

    public static void createTable(Connection connection) {
        try {
            Statement statement = connection.createStatement();
            String columns = getColumns();
            String primaryKey = getPrimaryKey("accountNumber", "transactionNumber");
            String table = "CREATE TABLE IF NOT EXISTS transactions ("
                    + columns
                    + primaryKey
                    + ");";

            statement.executeUpdate(table);
        } catch (SQLException ex) {
            Logger.getLogger(TransactionWriterForSqlite.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static void addTransaction(Connection connection, CashTransaction transaction) {
        try {
            Statement statement = connection.createStatement();
            String columns = removeLastTwoChars(getColumns());
            String values = getValues(transaction);
            String row = "INSERT INTO transactions (" + columns + ")"
                    + "VALUES (" + values + ")";
            statement.executeUpdate(row);
        } catch (SQLException ex) {
            Logger.getLogger(TransactionWriterForSqlite.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static String getValues(CashTransaction transaction) {
        Object[] array = getObjectArrayFrom(transaction);
        return arrayToCommaSeparatedString(array);
    }

    public static String arrayToCommaSeparatedString(Object[] array) {
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

    public static String getColumns() {
        String result = "";
        for (String column : HEADER) {
            try {
                Field propertyField = CashTransaction.class.getDeclaredField(column);
                propertyField.setAccessible(true);
                Class<?> type = propertyField.getType();
                String dbType = determineDatabaseTypeFromClass(type);
                result += column + " " + dbType + ", ";
            } catch (NoSuchFieldException | SecurityException ex) {
                Logger.getLogger(TransactionWriterForSqlite.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
        }
//        result = removeLastTwoChars(result); // remove the trailing comma and space ", "
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
