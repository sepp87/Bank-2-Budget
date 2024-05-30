package io.ost.finance;

import java.time.LocalDate;
import java.util.Iterator;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author joost
 */
public class MonthlyBudgetTest {

    @org.junit.jupiter.api.BeforeAll
    @org.junit.jupiter.api.AfterAll
    public static void printLine() {
        System.out.println();
    }

    @org.junit.jupiter.api.AfterEach
    public void removeAccounts() {
        Account.removeAllAccounts();
    }

    @org.junit.jupiter.api.Test
    public void testGetTransactions_WhenFirstOfMonthIsTenth_ThenReturn9And22Transactions() {
        System.out.println("testGetTransactions_WhenFirstOfMonthIsTenth_ThenReturn9And22Transactions");

        // Create test data
        List<CashTransaction> transactions = CashTransactionTest.generateTransactionsForAccountWithinTimespan("ABC", LocalDate.parse("2024-01-01"), LocalDate.parse("2024-01-31"), "GROCERIES", -1.);
        SingleAccountBudget budget = SingleAccountBudgetTest.generateBudget(10, transactions);

        // Perform test
        Iterator<MonthlyBudget> iterator = budget.getMonthlyBudgets().values().iterator();
        int resultDec = iterator.next().getTransactions().size();
        int resultJan = iterator.next().getTransactions().size();

        // Prepare results
        int expectedDec = 9;
        int expectedJan = 22;

        // Evaluate result
        assertEquals(expectedDec, resultDec);
        assertEquals(expectedJan, resultJan);

        // Print result
        UtilTest.printResult(expectedDec, resultDec);
        UtilTest.printResult(expectedJan, resultJan);
    }

    @org.junit.jupiter.api.Test
    public void testGetTransactions_WhenBudgeted100AndExpenseIs1PerDay_ThenRemainderIs69() {
        System.out.println("testGetTransactions_WhenFirstOfMonthIsTenth_ThenReturn9And22Transactions");

        // Create test data
        List<CashTransaction> transactions = CashTransactionTest.generateTransactionsForAccountWithinTimespan("ABC", LocalDate.parse("2024-01-01"), LocalDate.parse("2024-01-31"), "GROCERIES", -1.);
        SingleAccountBudget budget = SingleAccountBudgetTest.generateBudget(1, transactions);

        // Perform test
        MonthlyBudget monthlyBudget = budget.getMonthlyBudgets().values().iterator().next();
        double result = monthlyBudget.remainderForCategories.get("GROCERIES");

        // Prepare results
        double expected = 69;

        // Evaluate result
        assertEquals(expected, result);

        // Print result
        UtilTest.printResult(expected, result);
    }

    @org.junit.jupiter.api.Test
    public void testGetTransactions_WhenBudgeted100AndExpenseIs1PerDay_ThenNextMonthsRemainderIs169() {
        System.out.println("testGetTransactions_WhenBudgeted100AndExpenseIs1PerDay_ThenNextMonthsRemainderIs169");

        // Create test data
        List<CashTransaction> transactions = CashTransactionTest.generateTransactionsForAccountWithinTimespan("ABC", LocalDate.parse("2024-01-01"), LocalDate.parse("2024-01-31"), "GROCERIES", -1.);
        transactions.add(CashTransactionTest.generateTransactionsForAccountWithinTimespan("ABC", LocalDate.parse("2024-02-01"), LocalDate.parse("2024-02-01"), "GROCERIES", 0.).getFirst()); // Transaction without an expense, so a monthly budget for february is generated
        SingleAccountBudget budget = SingleAccountBudgetTest.generateBudget(1, transactions);

        // Perform test
        MonthlyBudget monthlyBudget = budget.getMonthlyBudgets().get("2024-02-01");
        double result = monthlyBudget.remainderForCategories.get("GROCERIES");

        // Prepare results
        double expected = 169.;

        // Evaluate result
        assertEquals(expected, result);

        // Print result
        UtilTest.printResult(expected, result);
    }

    @org.junit.jupiter.api.Test
    public void testGetTransactions_WhenBudgeted100AndExpenseIs1PerDayFor2Accounts_ThenRemainderIs38() {
        System.out.println("testGetTransactions_WhenBudgeted100AndExpenseIs1PerDayFor2Accounts_ThenRemainderIs38");

        // Create test data
        List<CashTransaction> transactions = CashTransactionTest.generateTransactionsForAccountWithinTimespan("ABC", LocalDate.parse("2024-01-01"), LocalDate.parse("2024-01-31"), "GROCERIES", -1.);
        transactions.addAll(CashTransactionTest.generateTransactionsForAccountWithinTimespan("BCD", LocalDate.parse("2024-01-01"), LocalDate.parse("2024-01-31"), "GROCERIES", -1.));
        MultiAccountBudget budget = MultiAccountBudgetTest.generateBudget(1, transactions);

        // Perform test
        MonthlyBudget monthlyBudget = budget.getMonthlyBudgets().values().iterator().next();
        double result = monthlyBudget.remainderForCategories.get("GROCERIES");

        // Prepare results
        double expected = 38.;

        // Evaluate result
        assertEquals(expected, result);

        // Print result
        UtilTest.printResult(expected, result);
    }

    @org.junit.jupiter.api.Test
    public void testGetTransactions_WhenBudgeted100AndExpenseIs1PerDayFor2Accounts_ThenNextMonthsRemainderIs138() {
        System.out.println("testGetTransactions_WhenBudgeted100AndExpenseIs1PerDayFor2Accounts_ThenNextMonthsRemainderIs138");

        // Create test data
        List<CashTransaction> transactions = CashTransactionTest.generateTransactionsForAccountWithinTimespan("ABC", LocalDate.parse("2024-01-01"), LocalDate.parse("2024-01-31"), "GROCERIES", -1.);
        transactions.addAll(CashTransactionTest.generateTransactionsForAccountWithinTimespan("BCD", LocalDate.parse("2024-01-01"), LocalDate.parse("2024-01-31"), "GROCERIES", -1.));
        transactions.add(CashTransactionTest.generateTransactionsForAccountWithinTimespan("ABC", LocalDate.parse("2024-02-01"), LocalDate.parse("2024-02-01"), "GROCERIES", 0.).getFirst()); // Transaction without an expense, so a monthly budget for february is generated
        MultiAccountBudget budget = MultiAccountBudgetTest.generateBudget(1, transactions);

        // Perform test
        MonthlyBudget monthlyBudget = budget.getMonthlyBudgets().get("2024-02-01");
        double result = monthlyBudget.remainderForCategories.get("GROCERIES");

        // Prepare results
        double expected = 138.;

        // Evaluate result
        assertEquals(expected, result);

        // Print result
        UtilTest.printResult(expected, result);
    }


}
