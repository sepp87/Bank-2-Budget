package bank2budget.core.budget;

import bank2budget.core.Account;
import static bank2budget.core.CashTransactionTest.newTx;
import bank2budget.core.UtilTest;
import bank2budget.core.CashTransaction;
import static bank2budget.core.budget.BudgetTemplateCategory.EntryType.EXPENSE;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author joost
 */
public class BudgetTest {

    @org.junit.jupiter.api.BeforeAll
    @org.junit.jupiter.api.AfterAll
    public static void printLine() {
        System.out.println();
    }

    @org.junit.jupiter.api.Test
    public void testGetMonthlyBudgets_WhenFirstOfMonthIsTenth_ThenReturnTwoMonthlyBudgets() {
        System.out.println("testGetMonthlyBudgets_WhenFirstOfMonthIsTenth_ThenReturnTwoMonthlyBudgets");

        // Create test data
        var categories = Map.of("GROCERIES", new BudgetTemplateCategory(EXPENSE, "GROCERIES", BigDecimal.valueOf(100)));
        BudgetTemplate template = new BudgetTemplate(10, categories);

        Budget budget = new Budget();

        List<CashTransaction> transactions = List.of(
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
        int result = months.size();

        // Prepare results
        int expected = 2;

        // Evaluate result
        assertEquals(expected, result);

        // Print result
        UtilTest.printResult(expected, result);

    }


    public static List<Account> groupTransactionsByAccount(List<CashTransaction> transactions) {
        Map<String, List<CashTransaction>> temporary = new TreeMap<>();
        for (CashTransaction transaction : transactions) {
            String accountNumber = transaction.accountNumber();
            temporary.computeIfAbsent(accountNumber, k -> new ArrayList<>()).add(transaction);
        }

        List<Account> result = new ArrayList<>();
        for (Map.Entry<String, List<CashTransaction>> entry : temporary.entrySet()) {
            Account account = new Account(entry.getKey(), entry.getValue());
            result.add(account);
        }
        return result;
    }


}
