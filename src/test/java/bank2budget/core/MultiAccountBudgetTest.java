package bank2budget.core;

import bank2budget.core.CashTransaction;
import bank2budget.core.Account;
import bank2budget.core.MultiAccountBudget;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author joost
 */
public class MultiAccountBudgetTest {

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
    public void testGetMonthlyBudgets_WhenFirstOfMonthIsTenth_ThenReturnTwoMonthlyBudgets() {
        System.out.println("testGetMonthlyBudgets_WhenFirstOfMonthIsTenth_ThenReturnTwoMonthlyBudgets");

        // Create test data
        List<CashTransaction> transactions = CashTransactionTest.generateTransactionsForAccountWithinTimespan("ABC", LocalDate.parse("2024-01-01"), LocalDate.parse("2024-01-31"), "GROCERIES", -1.);
        MultiAccountBudget budget = generateBudget(10, transactions);

        // Perform test
        int result = budget.getMonthlyBudgets().size();

        // Prepare results
        int expected = 2;

        // Evaluate result
        assertEquals(expected, result);

        // Print result
        UtilTest.printResult(expected, result);
    }

    public static MultiAccountBudget generateBudget(int firstOfMonth, List<CashTransaction> transactions) {
        MultiAccountBudget budget = new MultiAccountBudget();
        MultiAccountBudget.firstOfMonth = firstOfMonth;
        MultiAccountBudget.budgetedForCategory = generateBudgetedForCategory();

        Account.addTransactionsToAccounts(transactions);
        budget.setAccounts(Account.getAccounts());

        return budget;
    }

    private static Map<String, Double> generateBudgetedForCategory() {
        Map<String, Double> budgetedForCategory = new TreeMap<>();
        budgetedForCategory.put("GROCERIES", 100.);
        return budgetedForCategory;
    }
}
