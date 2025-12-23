package bank2budget.core.budget;

import bank2budget.core.Account;
import bank2budget.core.UtilTest;
import static bank2budget.core.CashTransactionTest.newTx;
import static bank2budget.core.budget.BudgetTemplateCategory.EntryType.EXPENSE;
import static bank2budget.core.budget.BudgetTest.groupTransactionsByAccount;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author joost
 */
public class BudgetMonthCategoryTest {

    @org.junit.jupiter.api.BeforeAll
    @org.junit.jupiter.api.AfterAll
    public static void printLine() {
        System.out.println();
    }

    @org.junit.jupiter.api.Test
    public void testGetTransactions_WhenFirstOfMonthIsTenth_ThenReturn3And2Transactions() {
        System.out.println("testGetTransactions_WhenFirstOfMonthIsTenth_ThenReturn3And2Transactions");

        // Create test data
        var categories = Map.of("GROCERIES", new BudgetTemplateCategory(EXPENSE, "GROCERIES", BigDecimal.valueOf(100)));
        BudgetTemplate template = new BudgetTemplate(10, categories);

        Budget budget = new Budget();

        var transactions = List.of(
                newTx("2024-01-01", 1, true, -10, -10, "abc", "xyz", "GROCERIES"), // <--- first month
                newTx("2024-01-02", 1, true, -10, -20, "abc", "xyz", "GROCERIES"),
                newTx("2024-01-03", 1, true, -10, -30, "abc", "xyz", "GROCERIES"),
                newTx("2024-01-11", 1, true, -10, -40, "abc", "xyz", "GROCERIES"), // <--- second month
                newTx("2024-01-12", 1, true, -10, -50, "abc", "xyz", "GROCERIES")
        );
        Collection<Account> accounts = groupTransactionsByAccount(transactions);

        BudgetCalculator calculator = new BudgetCalculator();
        List<BudgetMonth> months = calculator.create(template, budget, accounts);

        // Perform test
        int resultDec = months.get(0).operatingCategories().getFirst().transactions().size();
        int resultJan = months.get(1).operatingCategories().getFirst().transactions().size();

        // Prepare results
        int expectedDec = 3;
        int expectedJan = 2;

        // Evaluate result
        assertEquals(expectedDec, resultDec);
        assertEquals(expectedJan, resultJan);

        // Print result
        UtilTest.printResult(expectedDec, resultDec);
        UtilTest.printResult(expectedJan, resultJan);
    }

    @org.junit.jupiter.api.Test
    public void testGetTransactions_WhenBudgeted100AndExpenseIs50_ThenRemainderIs50() {
        System.out.println("testGetTransactions_WhenBudgeted100AndExpenseIs50_ThenRemainderIs50");

        // Create test data
        var categories = Map.of("GROCERIES", new BudgetTemplateCategory(EXPENSE, "GROCERIES", BigDecimal.valueOf(100)));
        BudgetTemplate template = new BudgetTemplate(1, categories); // <--- 1st is first of month!

        Budget budget = new Budget();

        var transactions = List.of( // <--- 5 * -10 = -50 
                newTx("2024-01-01", 1, true, -10, -10, "abc", "xyz", "GROCERIES"),
                newTx("2024-01-02", 1, true, -10, -20, "abc", "xyz", "GROCERIES"),
                newTx("2024-01-03", 1, true, -10, -30, "abc", "xyz", "GROCERIES"),
                newTx("2024-01-11", 1, true, -10, -40, "abc", "xyz", "GROCERIES"),
                newTx("2024-01-12", 1, true, -10, -50, "abc", "xyz", "GROCERIES")
        );
        Collection<Account> accounts = groupTransactionsByAccount(transactions);

        BudgetCalculator calculator = new BudgetCalculator();
        List<BudgetMonth> months = calculator.create(template, budget, accounts);

        // Perform test
        BigDecimal result = months.get(0).operatingCategories().getFirst().closing();

        // Prepare results
        BigDecimal expected = BigDecimal.valueOf(50);

        // Evaluate result
        assertTrue(result.compareTo(expected) == 0);

        // Print result
        UtilTest.printResult(expected, result);
    }

    @org.junit.jupiter.api.Test
    public void testGetTransactions_WhenBudgeted100AndExpense50_ThenNextMonthsRemainderIs150() {
        System.out.println("testGetTransactions_WhenBudgeted100AndExpense50_ThenNextMonthsRemainderIs150");

        // Create test data
        var categories = Map.of("GROCERIES", new BudgetTemplateCategory(EXPENSE, "GROCERIES", BigDecimal.valueOf(100)));
        BudgetTemplate template = new BudgetTemplate(1, categories); // <--- 1st is first of month!

        Budget budget = new Budget();

        var transactions = List.of( // <--- 5 * -10 = -50 
                newTx("2024-01-01", 1, true, -10, -10, "abc", "xyz", "GROCERIES"),
                newTx("2024-01-02", 1, true, -10, -20, "abc", "xyz", "GROCERIES"),
                newTx("2024-01-03", 1, true, -10, -30, "abc", "xyz", "GROCERIES"),
                newTx("2024-01-11", 1, true, -10, -40, "abc", "xyz", "GROCERIES"),
                newTx("2024-01-12", 1, true, -10, -50, "abc", "xyz", "GROCERIES"),
                newTx("2024-02-01", 1, true, 0.0, -50, "abc", "xyz", "GROCERIES") // <--- Transaction without an expense, so a monthly budget for february is generated
        );
        Collection<Account> accounts = groupTransactionsByAccount(transactions);

        BudgetCalculator calculator = new BudgetCalculator();
        List<BudgetMonth> months = calculator.create(template, budget, accounts);

        // Perform test
        BigDecimal result = months.get(1).operatingCategories().getFirst().closing();

        // Prepare results
        BigDecimal expected = BigDecimal.valueOf(150);

        // Evaluate result
        assertTrue(result.compareTo(expected) == 0);

        // Print result
        UtilTest.printResult(expected, result);
    }

    @org.junit.jupiter.api.Test
    public void testGetTransactions_WhenBudgeted100AndExpense50For2Accounts_ThenRemainderIs0() {
        System.out.println("testGetTransactions_WhenBudgeted100AndExpense50For2Accounts_ThenRemainderIs0");

        // Create test data
        var categories = Map.of("GROCERIES", new BudgetTemplateCategory(EXPENSE, "GROCERIES", BigDecimal.valueOf(100)));
        BudgetTemplate template = new BudgetTemplate(1, categories); // <--- 1st is first of month!

        Budget budget = new Budget();

        var transactions = List.of( // <--- 10 * -10 = -100 
                newTx("2024-01-01", 1, true, -10, -10, "abc", "xyz", "GROCERIES"), // <--- account abc
                newTx("2024-01-02", 1, true, -10, -20, "abc", "xyz", "GROCERIES"),
                newTx("2024-01-03", 1, true, -10, -30, "abc", "xyz", "GROCERIES"),
                newTx("2024-01-11", 1, true, -10, -40, "abc", "xyz", "GROCERIES"),
                newTx("2024-01-12", 1, true, -10, -50, "abc", "xyz", "GROCERIES"),
                newTx("2024-01-01", 1, true, -10, -10, "def", "uvw", "GROCERIES"), // <--- account def
                newTx("2024-01-02", 1, true, -10, -20, "def", "uvw", "GROCERIES"),
                newTx("2024-01-03", 1, true, -10, -30, "def", "uvw", "GROCERIES"),
                newTx("2024-01-11", 1, true, -10, -40, "def", "uvw", "GROCERIES"),
                newTx("2024-01-12", 1, true, -10, -50, "def", "uvw", "GROCERIES")
        );
        Collection<Account> accounts = groupTransactionsByAccount(transactions);

        BudgetCalculator calculator = new BudgetCalculator();
        List<BudgetMonth> months = calculator.create(template, budget, accounts);

        // Perform test
        BigDecimal result = months.getFirst().operatingCategories().getFirst().closing();

        // Prepare results
        BigDecimal expected = BigDecimal.ZERO;

        // Evaluate result
        assertTrue(result.compareTo(expected) == 0);

        // Print result
        UtilTest.printResult(expected, result);
    }

    @org.junit.jupiter.api.Test
    public void testGetTransactions_WhenBudgeted100AndExpense50For2Accounts_ThenNextMonthsRemainderIs100() {
        System.out.println("testGetTransactions_WhenBudgeted100AndExpense50For2Accounts_ThenNextMonthsRemainderIs100");

        // Create test data
        var categories = Map.of("GROCERIES", new BudgetTemplateCategory(EXPENSE, "GROCERIES", BigDecimal.valueOf(100)));
        BudgetTemplate template = new BudgetTemplate(1, categories); // <--- 1st is first of month!

        Budget budget = new Budget();

        var transactions = List.of( // <--- 10 * -10 = -100 
                newTx("2024-01-01", 1, true, -10, -10, "abc", "xyz", "GROCERIES"), // <--- account abc
                newTx("2024-01-02", 1, true, -10, -20, "abc", "xyz", "GROCERIES"),
                newTx("2024-01-03", 1, true, -10, -30, "abc", "xyz", "GROCERIES"),
                newTx("2024-01-11", 1, true, -10, -40, "abc", "xyz", "GROCERIES"),
                newTx("2024-01-12", 1, true, -10, -50, "abc", "xyz", "GROCERIES"),
                newTx("2024-01-01", 1, true, -10, -10, "def", "uvw", "GROCERIES"), // <--- account def
                newTx("2024-01-02", 1, true, -10, -20, "def", "uvw", "GROCERIES"),
                newTx("2024-01-03", 1, true, -10, -30, "def", "uvw", "GROCERIES"),
                newTx("2024-01-11", 1, true, -10, -40, "def", "uvw", "GROCERIES"),
                newTx("2024-01-12", 1, true, -10, -50, "def", "uvw", "GROCERIES"),
                newTx("2024-02-01", 1, true, 0.0, -50, "abc", "xyz", "GROCERIES") // <--- Transaction without an expense, so a monthly budget for february is generated
        );
        Collection<Account> accounts = groupTransactionsByAccount(transactions);

        BudgetCalculator calculator = new BudgetCalculator();
        List<BudgetMonth> months = calculator.create(template, budget, accounts);

        // Perform test
        BigDecimal result = months.get(1).operatingCategories().getFirst().closing();

        // Prepare results
        BigDecimal expected = BigDecimal.valueOf(100);

        // Evaluate result
        assertTrue(result.compareTo(expected) == 0);

        // Print result
        UtilTest.printResult(expected, result);
    }

}
