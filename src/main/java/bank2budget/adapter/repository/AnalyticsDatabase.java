package bank2budget.adapter.repository;

import bank2budget.core.CashTransaction;
import bank2budget.core.budget.BudgetMonth;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AnalyticsDatabase {

    private static final Logger LOGGER = Logger.getLogger(AnalyticsDatabase.class.getName());
    private final String dbPath;

    public AnalyticsDatabase(String dbPath, int firstOfMonth) {
        this.dbPath = dbPath;
        createSchema();
        createAnalyticsViews(firstOfMonth);
    }

    private void createSchema() {

        try (Connection c = SqliteUtil.getConnection(dbPath); Statement s = c.createStatement()) {

            // Drop the table if it already exists (development convenience)
            s.executeUpdate("DROP TABLE IF EXISTS transactions");
            LOGGER.log(Level.INFO, "Dropped transactions table");

            s.executeUpdate("DROP TABLE IF EXISTS budgets_by_month");
            LOGGER.log(Level.INFO, "Dropped monthly budgets table");

            String transactionsSchema = """
            CREATE TABLE IF NOT EXISTS transactions (
              accountNumber TEXT NOT NULL,
              transactionNumber INTEGER NOT NULL,
              date TEXT NOT NULL,
              amount REAL NOT NULL,
              balanceAfter REAL,
              transactionType TEXT,
              description TEXT,
              contraAccountName TEXT,
              contraAccountNumber TEXT,
              internal INTEGER,
              category TEXT,
              lastOfDay INTEGER,
              positionOfDay INTEGER,
              notes TEXT,
              PRIMARY KEY (accountNumber, transactionNumber)
            );
        """;

            s.executeUpdate(transactionsSchema);
            LOGGER.log(Level.INFO, "Created transactions table");

            String monthlyBudgetsSchema = """
            CREATE TABLE IF NOT EXISTS budgets_by_month (
              firstOfMonth TEXT NOT NULL,
              category TEXT NOT NULL,
              opening REAL NOT NULL,
              actual REAL NOT NULL,
              budgeted REAL NOT NULL,
              adjustments REAL NOT NULL,  
              closing REAL NOT NULL,  
              PRIMARY KEY (firstOfMonth, category)
            );
        """;

            s.executeUpdate(monthlyBudgetsSchema);
            LOGGER.log(Level.INFO, "Created monthly budgets table");

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating schema", e);
        }
    }

    public void insertTransactions(Collection<CashTransaction> transactions) {
        String sql = """
            INSERT OR REPLACE INTO transactions
            (accountNumber, transactionNumber, date, amount, balanceAfter,
             transactionType, description, contraAccountName, contraAccountNumber,
             internal, category, lastOfDay, positionOfDay, notes)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
        """;

        try (Connection c = SqliteUtil.getConnection(dbPath); PreparedStatement ps = c.prepareStatement(sql)) {
            c.setAutoCommit(false);

            for (var t : transactions) {
                ps.setString(1, t.accountNumber());
                ps.setInt(2, t.transactionNumber());
                ps.setString(3, t.date().toString());
                ps.setDouble(4, t.amount().doubleValue());
                ps.setObject(5, t.accountBalance());
                ps.setString(6, t.transactionType() != null ? t.transactionType().name() : null);
                ps.setString(7, t.description());
                ps.setString(8, t.contraAccountName());
                ps.setString(9, t.contraAccountNumber());
                ps.setInt(10, t.internal() ? 1 : 0);
                ps.setString(11, t.category());
                ps.setInt(12, t.lastOfDay() ? 1 : 0);
                ps.setInt(13, t.positionOfDay());
                ps.setString(14, t.notes());
                ps.addBatch();
            }

            ps.executeBatch();
            c.commit();

            LOGGER.log(Level.INFO, "Inserted {0} transactions", transactions.size());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to insert transactions", e);
        }
    }

    public void insertMonthlyBudgets(Collection<BudgetMonth> months) {
        String sql = """
            INSERT OR REPLACE INTO budgets_by_month
            (firstOfMonth, category, opening, actual, budgeted, adjustments, closing)
            VALUES (?, ?, ?, ?, ?, ?, ?);
        """;

        try (Connection c = SqliteUtil.getConnection(dbPath); PreparedStatement ps = c.prepareStatement(sql)) {
            c.setAutoCommit(false);

            for (var month : months) {

                LocalDate firstOfMonth = month.firstOfMonth();

                for (var category : month.operatingCategories()) {
                    ps.setString(1, firstOfMonth.toString());
                    ps.setString(2, category.name());
                    ps.setDouble(3, category.opening().doubleValue());
                    ps.setDouble(4, category.actual().doubleValue());
                    ps.setDouble(5, category.budgeted().doubleValue());
                    ps.setDouble(6, category.adjustments().doubleValue());
                    ps.setDouble(7, category.closing().doubleValue());
                    ps.addBatch();
                }

                var unappliedExpenses = month.unappliedExpenses();
                ps.setString(1, firstOfMonth.toString());
                ps.setString(2, unappliedExpenses.name());
                ps.setDouble(3, unappliedExpenses.opening().doubleValue());
                ps.setDouble(4, unappliedExpenses.actual().doubleValue());
                ps.setDouble(5, unappliedExpenses.budgeted().doubleValue()); // zero
                ps.setDouble(6, unappliedExpenses.adjustments().doubleValue()); // zero
                ps.setDouble(7, unappliedExpenses.closing().doubleValue());
                ps.addBatch();

                var unappliedIncome = month.unappliedIncome();
                ps.setString(1, firstOfMonth.toString());
                ps.setString(2, unappliedIncome.name());
                ps.setDouble(3, unappliedIncome.opening().doubleValue());
                ps.setDouble(4, unappliedIncome.actual().doubleValue());
                ps.setDouble(5, unappliedIncome.budgeted().doubleValue()); // zero
                ps.setDouble(6, unappliedIncome.adjustments().doubleValue()); // zero
                ps.setDouble(7, unappliedIncome.closing().doubleValue());
                ps.addBatch();

            }

            ps.executeBatch();
            c.commit();

            LOGGER.log(Level.INFO, "Inserted {0} monthly budgets", months.size());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to insert monthly budgets", e);
        }
    }

    private void createAnalyticsViews(int firstOfMonth) {

        try (Connection c = SqliteUtil.getConnection(dbPath); Statement s = c.createStatement(); ResultSet rs = s.executeQuery("select sqlite_version();")) {
            if (rs.next()) {
                LOGGER.info("SQLite version: " + rs.getString(1));
            }
        } catch (SQLException ex) {
            Logger.getLogger(AnalyticsDatabase.class.getName()).log(Level.SEVERE, null, ex);
        }

        try (Connection c = SqliteUtil.getConnection(dbPath); Statement s = c.createStatement()) {

            s.executeUpdate("DROP VIEW IF EXISTS balance_history");
            s.executeUpdate("DROP VIEW IF EXISTS balance_history_combined");
            s.executeUpdate("DROP VIEW IF EXISTS latest_account_balance");
            s.executeUpdate("DROP VIEW IF EXISTS latest_overall_balance");
            s.executeUpdate("DROP VIEW IF EXISTS monthly_expenses");
            s.executeUpdate("DROP VIEW IF EXISTS current_month");
            s.executeUpdate("DROP VIEW IF EXISTS total_expenses");
            s.executeUpdate("DROP VIEW IF EXISTS average_expenses");
            s.executeUpdate("DROP VIEW IF EXISTS average_expenses_last_3_months");
            s.executeUpdate("DROP VIEW IF EXISTS average_expenses_last_12_months");
            s.executeUpdate("DROP VIEW IF EXISTS average_expenses_last_year");
            s.executeUpdate("DROP VIEW IF EXISTS incoming_vs_outgoing_by_month");
            s.executeUpdate("DROP VIEW IF EXISTS incoming_vs_outgoing_current_month");
            s.executeUpdate("DROP VIEW IF EXISTS monthly_budgets_current_month");
            s.executeUpdate("DROP VIEW IF EXISTS monthly_remainder");

            s.executeUpdate("DROP VIEW IF EXISTS balance_history_by_account");
            s.executeUpdate("DROP VIEW IF EXISTS balance_history_total");
            s.executeUpdate("DROP VIEW IF EXISTS balance_current_by_account");
            s.executeUpdate("DROP VIEW IF EXISTS balance_current_total");
            s.executeUpdate("DROP VIEW IF EXISTS expenses_by_month");
            s.executeUpdate("DROP VIEW IF EXISTS expenses_current_month");
            s.executeUpdate("DROP VIEW IF EXISTS expenses_total"); //
            s.executeUpdate("DROP VIEW IF EXISTS average_expenses");
            s.executeUpdate("DROP VIEW IF EXISTS average_expenses_total");
            s.executeUpdate("DROP VIEW IF EXISTS average_expenses_last_3_months");
            s.executeUpdate("DROP VIEW IF EXISTS average_expenses_last_12_months");
            s.executeUpdate("DROP VIEW IF EXISTS average_expenses_last_year");
            s.executeUpdate("DROP VIEW IF EXISTS incoming_vs_outgoing_by_month");
            s.executeUpdate("DROP VIEW IF EXISTS incoming_vs_outgoing_current_month");
            s.executeUpdate("DROP VIEW IF EXISTS budgets_current_month");

            s.executeUpdate("DROP VIEW IF EXISTS average_expenses_by_year");
            s.executeUpdate("DROP VIEW IF EXISTS budgets_current_month_without_cumulative_remainders");
            s.executeUpdate("DROP VIEW IF EXISTS budgets_current_month_savings_and_deficits");
            s.executeUpdate("DROP VIEW IF EXISTS last_export");
            s.executeUpdate("DROP VIEW IF EXISTS average_expenses_by_year_negated_amount");
            s.executeUpdate("DROP VIEW IF EXISTS transactions_negated_amount");

            s.executeUpdate("CREATE INDEX IF NOT EXISTS index_transactions_account_date ON transactions(accountNumber, date)");

            s.executeUpdate("""
            CREATE VIEW IF NOT EXISTS balance_history_by_account AS
            WITH RECURSIVE all_dates AS (
              SELECT MIN(date) AS date FROM transactions
              UNION ALL
              SELECT DATE(date, '+1 day')
              FROM all_dates
              WHERE date < (SELECT MAX(date) FROM transactions)
            ),
            balances AS (
              SELECT accountNumber AS account,
                     date,
                     balanceAfter AS balance
              FROM transactions
            ),
            filled AS (
              SELECT
                d.date,
                a.account,
                (
                  SELECT b.balance
                  FROM balances b
                  WHERE b.account = a.account
                    AND b.date <= d.date
                  ORDER BY b.date DESC
                  LIMIT 1
                ) AS balance
              FROM all_dates d
              CROSS JOIN (SELECT DISTINCT account FROM balances) a
            )
            SELECT
              date,
              account,
              balance
            FROM filled
            WHERE balance IS NOT NULL
            ORDER BY date, account
            """);

            s.executeUpdate("""
            CREATE VIEW IF NOT EXISTS balance_history_total AS
            SELECT date, SUM(balance) AS balance
            FROM balance_history_by_account
            GROUP BY date
            ORDER BY date
            """);

            s.executeUpdate("""
            CREATE VIEW IF NOT EXISTS balance_current_by_account AS
            SELECT
              bh.account,
              bh.date,
              bh.balance
            FROM balance_history_by_account bh
            WHERE bh.date = (
              SELECT MAX(bh2.date)
              FROM balance_history_by_account bh2
              WHERE bh2.account = bh.account
            )
            ORDER BY bh.account
            """);

            s.executeUpdate("""
            CREATE VIEW IF NOT EXISTS balance_current_total AS
            SELECT
              date,
              balance
            FROM balance_history_total
            ORDER BY date DESC
            LIMIT 1
            """);

            s.executeUpdate("""
            CREATE VIEW IF NOT EXISTS expenses_by_month AS
            WITH tx AS (
              SELECT *,
                     CAST(strftime('%%d', date) AS INTEGER) AS day
              FROM transactions
            )
            SELECT
              strftime('%%Y-%%m-%%d',
                CASE
                  WHEN day >= %1$d
                    THEN date(date, 'start of month', '+%2$d day')            -- same month
                  ELSE date(date, 'start of month', '+%2$d day', '-1 month')  -- previous month
                END
              ) AS firstOfMonth,
              category,
              SUM(amount) AS amount
            FROM tx
            GROUP BY firstOfMonth, category
            ORDER BY firstOfMonth, category;
            """.formatted(firstOfMonth, firstOfMonth - 1)
            );

            s.executeUpdate("""
            CREATE VIEW IF NOT EXISTS expenses_current_month AS
            WITH latest AS (
              SELECT MAX(firstOfMonth) AS current_month FROM expenses_by_month
            )
            SELECT
              category,
              SUM(amount) AS amount
            FROM expenses_by_month
            WHERE firstOfMonth = (SELECT current_month FROM latest)
            GROUP BY category
            ORDER BY amount;
            """);

            s.executeUpdate("""
            CREATE VIEW IF NOT EXISTS expenses_total AS
            SELECT
              category,
              SUM(amount) AS amount
            FROM transactions
            GROUP BY category
            ORDER BY amount;
            """);

            s.executeUpdate("""
            CREATE VIEW IF NOT EXISTS average_expenses_total AS
            SELECT
              category,
              SUM(amount) / (SELECT COUNT(DISTINCT firstOfMonth) FROM expenses_by_month) AS amount
            FROM expenses_by_month
            GROUP BY category
            ORDER BY amount;
            """);

            s.executeUpdate(getAverageExpensesLastNMonths(3));
            s.executeUpdate(getAverageExpensesLastNMonths(12));
            s.executeUpdate(getAverageExpensesLastYear());
            s.executeUpdate(getAverageExpensesByYear());

            s.executeUpdate("""
            CREATE VIEW IF NOT EXISTS incoming_vs_outgoing_by_month AS
            WITH tx AS (
              SELECT *,
                     CAST(strftime('%%d', date) AS INTEGER) AS day
              FROM transactions
            )
            SELECT
              strftime('%%Y-%%m-%%d',
                CASE
                  WHEN day >= %1$d
                    THEN date(date, 'start of month', '+%2$d day')           -- same month
                  ELSE date(date, 'start of month', '+%2$d day', '-1 month')  -- previous month
                END
              ) AS firstOfMonth,
                    SUM(CASE WHEN amount > 0 THEN amount ELSE 0 END) AS incoming,
                    SUM(CASE WHEN amount < 0 THEN amount ELSE 0 END) AS outgoing,
                    SUM(amount) AS total
            FROM tx
            GROUP BY firstOfMonth
            ORDER BY firstOfMonth;
            """.formatted(firstOfMonth, firstOfMonth - 1)
            );

            s.executeUpdate("""
            CREATE VIEW IF NOT EXISTS incoming_vs_outgoing_current_month AS
            WITH latest AS (
              SELECT MAX(firstOfMonth) AS max_month FROM incoming_vs_outgoing_by_month
            )
            SELECT * FROM incoming_vs_outgoing_by_month
            WHERE firstOfMonth = (SELECT max_month FROM latest)
            """.formatted(firstOfMonth)
            );

            s.executeUpdate("""
            CREATE VIEW IF NOT EXISTS budgets_current_month AS
            WITH latest AS (
              SELECT MAX(firstOfMonth) AS current_month FROM budgets_by_month
            )
            SELECT * FROM budgets_by_month
            WHERE firstOfMonth = (SELECT current_month FROM latest)
            ORDER BY category;
            """);

            s.executeUpdate("""
            CREATE VIEW IF NOT EXISTS budgets_current_month_without_cumulative_remainders AS
            SELECT category, budgeted, expenses, budgeted + expenses AS remainder FROM budgets_current_month
            WHERE budgeted <> 0 OR expenses <> 0
            """);

            s.executeUpdate("""
            CREATE VIEW IF NOT EXISTS budgets_current_month_savings_and_deficits AS
            SELECT category, remainder AS saved FROM budgets_current_month
            WHERE remainder <> 0
            ORDER BY remainder
            """);

            s.executeUpdate("""
            CREATE VIEW IF NOT EXISTS last_export AS
            SELECT MAX(date) FROM transactions
            """);

            s.executeUpdate("""
            CREATE VIEW IF NOT EXISTS current_month AS
            WITH latest AS (
              SELECT firstOfMonth FROM budgets_current_month LIMIT 1
            )
            SELECT
              CASE CAST(strftime('%m', date(firstOfMonth)) AS INTEGER)
                WHEN 1  THEN 'January'
                WHEN 2  THEN 'February'
                WHEN 3  THEN 'March'
                WHEN 4  THEN 'April'
                WHEN 5  THEN 'May'
                WHEN 6  THEN 'June'
                WHEN 7  THEN 'July'
                WHEN 8  THEN 'August'
                WHEN 9  THEN 'September'
                WHEN 10 THEN 'October'
                WHEN 11 THEN 'November'
                WHEN 12 THEN 'December'
              END AS month_name
            FROM (
              SELECT
                CASE
                  WHEN CAST(strftime('%d', date(firstOfMonth)) AS INTEGER) > 16
                    THEN date(firstOfMonth, '+1 month')
                  ELSE date(firstOfMonth)
                END AS firstOfMonth
              FROM latest
            );
            """);

            s.executeUpdate("""
            CREATE VIEW IF NOT EXISTS average_expenses_by_year_negated_amount AS
            SELECT *, -amount AS negatedAmount FROM average_expenses_by_year
            """);

            s.executeUpdate("""
            CREATE VIEW IF NOT EXISTS transactions_negated_amount AS
            SELECT *, -amount AS negatedAmount FROM transactions
            """);

            LOGGER.log(Level.INFO, "Created analytics views");

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating analytics views", e);
        }
    }

    private String getAverageExpensesLastNMonths(int n) {
        return """
            CREATE VIEW IF NOT EXISTS average_expenses_last_%1$d_months AS
            WITH latest AS (
              SELECT MAX(firstOfMonth) AS current_month FROM expenses_by_month
            ),
            recent AS (
              SELECT DISTINCT firstOfMonth
              FROM expenses_by_month, latest
              WHERE firstOfMonth < (SELECT current_month FROM latest)
              ORDER BY firstOfMonth DESC
              LIMIT %1$d
            )
            SELECT
              category,
              SUM(amount) / (SELECT COUNT(*) FROM recent) AS amount
            FROM expenses_by_month
            WHERE firstOfMonth IN (SELECT firstOfMonth FROM recent)
            GROUP BY category
            ORDER BY amount;
        """.formatted(n);
    }

    private String getAverageExpensesLastYear() {
        return """
        CREATE VIEW IF NOT EXISTS average_expenses_last_year AS
        WITH year AS (
          SELECT strftime('%Y', date(MAX(firstOfMonth), '-1 year')) AS value
          FROM expenses_by_month
        )
        SELECT
          category,
          SUM(amount) / COUNT(DISTINCT firstOfMonth) AS amount
        FROM expenses_by_month
        WHERE substr(firstOfMonth, 1, 4) = (SELECT value FROM year)
        GROUP BY category
        ORDER BY amount
        """;
    }

    private String getAverageExpensesByYear() {
        return """
        CREATE VIEW IF NOT EXISTS average_expenses_by_year AS
        WITH bounds AS (
          SELECT
            MIN(date(firstOfMonth)) AS first_month,
            MAX(date(firstOfMonth)) AS last_month,
            strftime('%Y', MIN(date(firstOfMonth))) AS first_year,
            strftime('%Y', MAX(date(firstOfMonth))) AS last_year
          FROM expenses_by_month
        ),
        month_counts AS (
          SELECT
            y.year,
            CASE
              WHEN y.year = b.first_year THEN 12 - CAST(strftime('%m', b.first_month) AS INTEGER) + 1
              WHEN y.year = b.last_year  THEN CAST(strftime('%m', b.last_month) AS INTEGER) - 1
              ELSE 12
            END AS divisor
          FROM (
            SELECT DISTINCT strftime('%Y', date(firstOfMonth)) AS year
            FROM expenses_by_month
          ) y
          CROSS JOIN bounds b
        )
        SELECT
          e.category,
          strftime('%Y', date(e.firstOfMonth)) AS year,
          ROUND(SUM(e.amount) / m.divisor, 2) AS amount
        FROM expenses_by_month e
        JOIN month_counts m
          ON m.year = strftime('%Y', date(e.firstOfMonth))
        GROUP BY e.category, year
        ORDER BY e.category, year;
        """;
    }

    public List<CashTransaction> getAllTransactions() {
        // optional: implement later when you want to pull data back into the app
        return List.of();
    }
}
