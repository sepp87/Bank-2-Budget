package bank2budget.adapters.writer;

import bank2budget.adapters.db.SqliteUtil;
import bank2budget.adapters.writer.TransactionWriterForSqlite;
import bank2budget.cli.Launcher;
import bank2budget.core.MonthlyBudget;
import bank2budget.core.MultiAccountBudget;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author joostmeulenkamp
 */
public class BudgetWriterForSqlite {

    private static final String DATABASE_PATH = Launcher.getDatabaseDirectory() + "bank-2-budget.db";
    private static final String BUDGETS_TABLE = "monthlyBudgets";

    private Connection connection;

    public void write(MultiAccountBudget budget) {
        connection = SqliteUtil.getConnection(DATABASE_PATH);
        if (connection == null) {
            return;
        }

        boolean success = true; // Track transaction success
        success &= createTable(connection);
        success &= SqliteUtil.clearTable(connection, BUDGETS_TABLE);
        success &= insertMonthlyBudgets(connection, budget.getMonthlyBudgets().values());

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
        String primaryKey = SqliteUtil.getPrimaryKey("firstOfMonth", "category");
        return SqliteUtil.createTable(connection, BUDGETS_TABLE, columns, primaryKey);
    }

    private static String getColumnsWithTypes() {
        String result
                = "firstOfMonth TEXT, "
                + "category TEXT, "
                + "budgeted REAL, "
                + "expenses REAL, "
                + "remainderLastMonth REAL, ";
        return result;
    }

    public static boolean insertMonthlyBudgets(Connection connection, Collection<MonthlyBudget> monthlyBudgets) {
        String columns = getColumns();
        String placeholders = SqliteUtil.getPlaceholders(5);
        String insertTransactions = "INSERT INTO " + BUDGETS_TABLE + " (" + columns + ") "
                + "VALUES (" + placeholders + ")";

        try (PreparedStatement statement = connection.prepareStatement(insertTransactions)) {

            for (MonthlyBudget month : monthlyBudgets) {
                String firstOfMonth = month.getFirstOfMonth();

                Map<String, Double> budgetedForCategory = month.getBudgetedForCategories();
                Map<String, Double> expensesForCategory = month.getExpensesForCategories();
                Map<String, Double> remainderForCategoryLastMonth = month.getRemainderForCategoriesLastMonth();

                for (Map.Entry<String, Double> entry : budgetedForCategory.entrySet()) {

                    String category = entry.getKey();
                    double budgeted = entry.getValue();
                    double expenses = expensesForCategory.get(category);
                    double remainderLastMonth = remainderForCategoryLastMonth.get(category);
                    setRowForStatement(firstOfMonth, category, budgeted, expenses, remainderLastMonth, statement);
                }

                setRowForStatement(firstOfMonth, "UNASSIGNED EXPENSES", 0, month.getUnassignedExpenses(), month.getUnassignedExpensesRemainderLastMonth(), statement);
                setRowForStatement(firstOfMonth, "UNASSIGNED INCOME", 0, month.getUnassignedIncome(), month.getUnassignedIncomeRemainderLastMonth(), statement);
            }

            statement.executeBatch();

        } catch (SQLException ex) {
            Logger.getLogger(TransactionWriterForSqlite.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }

    private static void setRowForStatement(String firstOfMonth, String category, double budgeted, double expenses, double remainderLastMonth, PreparedStatement statement) throws SQLException {
        statement.setString(1, firstOfMonth);
        statement.setString(2, category);
        statement.setDouble(3, budgeted);
        statement.setDouble(4, expenses);
        statement.setDouble(5, remainderLastMonth);
        statement.addBatch();
    }

    private static String getColumns() {
        String result
                = "firstOfMonth, "
                + "category, "
                + "budgeted, "
                + "expenses, "
                + "remainderLastMonth";
        return result;
    }

}
