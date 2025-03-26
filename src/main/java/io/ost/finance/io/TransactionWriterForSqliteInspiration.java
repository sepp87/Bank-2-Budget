package io.ost.finance.io;

import io.ost.finance.Account;
import io.ost.finance.CashTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages writing transactions to a SQLite database with improved flexibility
 * and error handling.
 */
public class TransactionWriterForSqliteInspiration extends TransactionWriter {

    private static final Logger logger = LoggerFactory.getLogger(TransactionWriterForSqlite.class);

    private final ConnectionProvider connectionProvider;
    private final DatabaseConfiguration configuration;

    /**
     * Constructor for dependency injection of connection provider and
     * configuration.
     *
     * @param connectionProvider Provides database connections
     * @param configuration Database and writing configuration
     */
    public TransactionWriterForSqliteInspiration(ConnectionProvider connectionProvider, DatabaseConfiguration configuration) {
        this.connectionProvider = connectionProvider;
        this.configuration = configuration;
    }

    /**
     * Writes transactions for multiple accounts to the database.
     *
     * @param accounts Collection of accounts to persist
     * @throws DatabaseWriteException If any database write operation fails
     */
    public void persist(Collection<Account> accounts) throws DatabaseWriteException {
        try (Connection connection = connectionProvider.getConnection()) {
            connection.setAutoCommit(false);

            try {
                prepareDatabase(connection);
                writeTransactions(connection, accounts);
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new DatabaseWriteException("Failed to write transactions", e);
            }
        } catch (SQLException e) {
            throw new DatabaseWriteException("Database connection error", e);
        }
    }

    /**
     * Prepares the database by creating table if not exists and clearing
     * existing data.
     *
     * @param connection Active database connection
     * @throws SQLException If database preparation fails
     */
    private void prepareDatabase(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(generateCreateTableStatement());
            statement.execute("DELETE FROM transactions");
        }
    }

    /**
     * Writes transactions in batches for performance.
     *
     * @param connection Active database connection
     * @param accounts Accounts to write transactions for
     * @throws SQLException If batch insert fails
     */
    private void writeTransactions(Connection connection, Collection<Account> accounts) throws SQLException {
        String insertSql = generateInsertStatement();

        try (PreparedStatement preparedStatement = connection.prepareStatement(insertSql)) {
            for (Account account : accounts) {
                for (CashTransaction transaction : account.getAllTransactions()) {
                    setStatementParameters(preparedStatement, transaction);
                    preparedStatement.addBatch();
                }
            }

            int[] batchResults = preparedStatement.executeBatch();
            logger.info("Inserted {} transactions", Arrays.stream(batchResults).sum());
        }
    }

    /**
     * Sets parameters for prepared statement based on transaction data.
     *
     * @param statement Prepared statement to set parameters for
     * @param transaction Transaction to extract data from
     * @throws SQLException If parameter setting fails
     */
    private void setStatementParameters(PreparedStatement statement, CashTransaction transaction) throws SQLException {
        Object[] values = TransactionWriter.getObjectArrayFrom(transaction);

        for (int i = 0; i < values.length; i++) {
            setParameterSafely(statement, i + 1, values[i]);
        }
    }

    /**
     * Safely sets a parameter in a prepared statement, handling null and type
     * conversion.
     *
     * @param statement Prepared statement
     * @param index Parameter index
     * @param value Value to set
     * @throws SQLException If parameter setting fails
     */
    private void setParameterSafely(PreparedStatement statement, int index, Object value) throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.VARCHAR);
            return;
        }

        Class<?> type = value.getClass();

        if (type == String.class) {
            statement.setString(index, (String) value);
        } else if (type == Integer.class || type == int.class) {
            statement.setInt(index, (Integer) value);
        } else if (type == Double.class || type == double.class) {
            statement.setDouble(index, (Double) value);
        } else if (type == Boolean.class || type == boolean.class) {
            statement.setBoolean(index, (Boolean) value);
        } else {
            statement.setString(index, value.toString());
        }
    }

    /**
     * Generates CREATE TABLE statement dynamically based on transaction fields.
     *
     * @return SQL CREATE TABLE statement
     */
    private String generateCreateTableStatement() {
        String columns = String.join(", ", Arrays.asList(HEADER));
        String primaryKey = "PRIMARY KEY (accountNumber, transactionNumber)";

        return String.format("CREATE TABLE IF NOT EXISTS transactions (%s, %s)", columns, primaryKey);
    }

    /**
     * Maps a column to its SQLite type definition.
     *
     * @param column Column name
     * @return Column definition with type
     */
    private String mapColumnDefinition(String column) {
        Class<?> type = getColumnType(column);
        return column + " " + determineSqliteType(type);
    }

    /**
     * Determines SQLite type for a given Java class.
     *
     * @param type Java class
     * @return SQLite type as string
     */
    private String determineSqliteType(Class<?> type) {
        if (type == Integer.class || type == int.class
                || type == Long.class || type == long.class
                || type == Boolean.class || type == boolean.class) {
            return "INTEGER";
        } else if (type == Double.class || type == double.class
                || type == Float.class || type == float.class) {
            return "REAL";
        } else {
            return "TEXT";
        }
    }

    /**
     * Retrieves column type via reflection.
     *
     * @param column Column name
     * @return Column's Java type
     */
    private Class<?> getColumnType(String column) {
        try {
            return CashTransaction.class.getDeclaredField(column).getType();
        } catch (NoSuchFieldException e) {
            logger.error("Could not find field: {}", column, e);
            return String.class;  // Fallback type
        }
    }

    /**
     * Generates INSERT statement dynamically.
     *
     * @return Parameterized INSERT SQL statement
     */
    private String generateInsertStatement() {
        String columns = String.join(", ", TransactionWriter.HEADER);
        String placeholders = String.join(", ", Collections.nCopies(HEADER.length, "?"));

        return String.format("INSERT INTO transactions (%s) VALUES (%s)", columns, placeholders);
    }

    // Supporting interfaces for dependency injection and configuration
    public interface ConnectionProvider {

        Connection getConnection() throws SQLException;
    }

    public static class DatabaseConfiguration {

        private String databasePath;
        private int batchSize = 1000;

        // Getters and setters
    }

    // Custom exception for database write operations
    public static class DatabaseWriteException extends Exception {

        public DatabaseWriteException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
