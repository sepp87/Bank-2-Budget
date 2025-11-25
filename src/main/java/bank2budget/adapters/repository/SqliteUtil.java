package bank2budget.adapters.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author joostmeulenkamp
 */
public class SqliteUtil {

    public static Connection getConnection(String path) {
        String url = "jdbc:sqlite:" + path;
        try {
            Connection connection = DriverManager.getConnection(url);
//            connection.setAutoCommit(false);
            return connection;
        } catch (SQLException ex) {
            Logger.getLogger(TransactionWriterForSqlite.class.getName()).log(Level.SEVERE, "Could NOT connect to {0}", url);
        }
        return null;
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

    public static boolean createTable(Connection connection, String table, String columns, String primaryKey) {
        String createTable = "CREATE TABLE IF NOT EXISTS " + table + " ("
                + columns
                + primaryKey
                + ");";
        return SqliteUtil.executeUpdateStatement(connection, createTable);
    }

    public static boolean clearTable(Connection connection, String table) {
        String truncateTable = "DELETE FROM " + table;
        return executeUpdateStatement(connection, truncateTable);
    }

    public static boolean executeUpdateStatement(Connection connection, String sql) {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        } catch (SQLException ex) {
            Logger.getLogger(TransactionWriterForSqlite.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }

    public static String getPlaceholders(int count) {
        return String.join(", ", Collections.nCopies(count, "?"));
    }

}
