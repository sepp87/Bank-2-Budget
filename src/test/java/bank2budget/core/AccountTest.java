package bank2budget.core;

import static bank2budget.core.CashTransactionTest.newTx;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 *
 * @author joost
 */
public class AccountTest {

    @org.junit.jupiter.api.BeforeAll
    @org.junit.jupiter.api.AfterAll
    public static void printLine() {
        System.out.println();
    }

    @Test
    public void TestMerge_WhenMergingImported_ThenPreferDatesWithMostCtx_Ordered() {
        System.out.println("TestMerge_WhenMergingImported_ThenPreferDatesWithMostCtx_Ordered");

        // Create test data        
        List<CashTransaction> existingTx = List.of(
                newTx("2025-01-01", 1, false, 10, 10, "abc", "xyz", "Aye"),
                newTx("2025-01-01", 2, true, 10, 20, "abc", "xyz", "Aye"),
                newTx("2025-01-02", 1, false, 10, 30, "abc", "xyz", "Aye"),
                newTx("2025-01-02", 2, true, 10, 40, "abc", "xyz", "Aye"),
                newTx("2025-01-03", 1, true, 20, 60, "abc", "xyz", "Aye") // <--- incomplete day 1
        );

        List<CashTransaction> importedTx1 = List.of(
                newTx("2025-01-03", 1, false, 10, 50, "abc", "xyz", "Bee"), // <--- complete day 1
                newTx("2025-01-03", 2, true, 20, 70, "abc", "xyz", "Bee"), // <--- complete day 1
                newTx("2025-01-04", 1, false, 10, 80, "abc", "xyz", "Bee"),
                newTx("2025-01-04", 2, true, 10, 90, "abc", "xyz", "Bee"),
                newTx("2025-01-05", 1, true, 20, 110, "abc", "xyz", "Bee") // <--- incomplete day 2
        );

        List<CashTransaction> importedTx2 = List.of(
                newTx("2025-01-05", 1, false, 10, 100, "abc", "xyz", "Cee"), // <--- complete day 2
                newTx("2025-01-05", 2, true, 20, 120, "abc", "xyz", "Cee"), // <--- complete day 2
                newTx("2025-01-06", 1, false, 10, 130, "abc", "xyz", "Cee"),
                newTx("2025-01-06", 2, true, 10, 140, "abc", "xyz", "Cee"),
                newTx("2025-01-07", 1, true, 10, 150, "abc", "xyz", "Cee")
        );

        Account existing = new Account("abc", existingTx);
        Account imported1 = new Account("abc", importedTx1);
        Account imported2 = new Account("abc", importedTx2);

        // Perform test 
        existing.merge(imported1);
        existing.merge(imported2);

        // Prepare results
        int expected = 13;
        int result = existing.transactionsAscending().size();

        // Evaluate results
        assertEquals(expected, result, "Expected number of transactions did not match the actual obtained number of transactions");

        UtilTest.printResult(expected, result);
    }

    @Test
    public void TestMerge_WhenMergingImported_ThenPreferDatesWithMostCtx_Unordered() {
        System.out.println("TestMerge_WhenMergingImported_ThenPreferDatesWithMostCtx_Unordered");

        // Create test data        
        List<CashTransaction> existingTx = List.of(
                newTx("2025-01-01", 1, false, 10, 10, "abc", "xyz", "Aye"),
                newTx("2025-01-01", 2, true, 10, 20, "abc", "xyz", "Aye"),
                newTx("2025-01-02", 1, false, 10, 30, "abc", "xyz", "Aye"),
                newTx("2025-01-02", 2, true, 10, 40, "abc", "xyz", "Aye"),
                newTx("2025-01-03", 1, true, 20, 60, "abc", "xyz", "Aye") // <--- incomplete day 1
        );

        List<CashTransaction> importedTx1 = List.of(
                newTx("2025-01-03", 1, false, 10, 50, "abc", "xyz", "Bee"), // <--- complete day 1
                newTx("2025-01-03", 2, true, 20, 70, "abc", "xyz", "Bee"), // <--- complete day 1
                newTx("2025-01-04", 1, false, 10, 80, "abc", "xyz", "Bee"),
                newTx("2025-01-04", 2, true, 10, 90, "abc", "xyz", "Bee"),
                newTx("2025-01-05", 1, true, 20, 110, "abc", "xyz", "Bee") // <--- incomplete day 2
        );

        List<CashTransaction> importedTx2 = List.of(
                newTx("2025-01-05", 1, false, 10, 100, "abc", "xyz", "Cee"), // <--- complete day 2
                newTx("2025-01-05", 2, true, 20, 120, "abc", "xyz", "Cee"), // <--- complete day 2
                newTx("2025-01-06", 1, false, 10, 130, "abc", "xyz", "Cee"),
                newTx("2025-01-06", 2, true, 10, 140, "abc", "xyz", "Cee"),
                newTx("2025-01-07", 1, true, 10, 150, "abc", "xyz", "Cee")
        );

        Account existing = new Account("abc", existingTx);
        Account imported1 = new Account("abc", importedTx1);
        Account imported2 = new Account("abc", importedTx2);

        // Perform test 
        existing.merge(imported2);
        existing.merge(imported1);

        // Prepare results
        int expected = 13;
        int result = existing.transactionsAscending().size();

        // Evaluate results
        assertEquals(expected, result, "Expected number of transactions did not match the actual obtained number of transactions");

        UtilTest.printResult(expected, result);
    }

    /**
     * Test of addTransactionsToAccounts method, of class Account.
     */
    @Test
    public void testAddTransactionsToAccounts_WhenOldCategorized_ThenKeepOldCategories() {
        System.out.println("testAddTransactionsToAccounts_WhenOldCategorized_ThenKeepOldCategories");

        // Create test data        
        List<CashTransaction> existingTx = List.of(
                newTx("2025-01-01", 1, true, 10, 10, "abc", "xyz", "Aye"),
                newTx("2025-01-02", 1, true, 10, 20, "abc", "xyz", "Aye"),
                newTx("2025-01-03", 1, true, 10, 30, "abc", "xyz", "Aye")
        );

        List<CashTransaction> importedTx = List.of(
                newTx("2025-01-01", 1, true, 10, 10, "abc", "xyz", "Bee"),
                newTx("2025-01-02", 1, true, 10, 20, "abc", "xyz", "Bee"),
                newTx("2025-01-03", 1, true, 10, 30, "abc", "xyz", "Bee")
        );

        // Perform test
        Account existing = new Account("abc", existingTx);
        Account imported = new Account("abc", importedTx);
        existing.merge(imported);

        // Prepare results
        int expected = 3;
        int result = existing.transactionsAscending().size();

        // Evaluate result
        assertEquals(expected, result, "Expected number of unique transactions did not match the actual obtained number of transactions");
        for (var transaction : existing.transactionsAscending()) {
            assertEquals("Aye", transaction.category());
        }

        UtilTest.printResult(expected, result);
    }

    /**
     * Test of addTransactionsToAccounts method, of class Account.
     */
    @Test
    public void testAddTransactionsToAccounts_WhenOldUncategorized_ThenAddNewCategories() {
        System.out.println("testAddTransactionsToAccounts_WhenOldUncategorized_ThenAddNewCategories");

        // Create test data   
        List<CashTransaction> existingTx = List.of(
                newTx("2025-01-01", 1, true, 10, 10, "abc", "xyz", null),
                newTx("2025-01-02", 1, true, 10, 20, "abc", "xyz", null),
                newTx("2025-01-03", 1, true, 10, 30, "abc", "xyz", null)
        );

        List<CashTransaction> importedTx = List.of(
                newTx("2025-01-01", 1, true, 10, 10, "abc", "xyz", "Bee"),
                newTx("2025-01-02", 1, true, 10, 20, "abc", "xyz", "Bee"),
                newTx("2025-01-03", 1, true, 10, 30, "abc", "xyz", "Bee")
        );

        // Perform test
        Account existing = new Account("abc", existingTx);
        Account imported = new Account("abc", importedTx);
        existing.merge(imported);

        // Prepare results
        int expected = 3;
        int result = existing.transactionsAscending().size();

        // Evaluate result
        assertEquals(expected, result, "Expected number of unique transactions did not match the actual obtained number of transactions");
        for (var transaction : existing.transactionsAscending()) {
            assertEquals("Bee", transaction.category());
        }

        UtilTest.printResult(expected, result);
    }

    /**
     * Test of addTransactionsToAccounts method, of class Account.
     */
    @Test
    public void testAddTransactionsToAccounts_WhenNewOverwriteOld_ThenAddNewCategories() {
        System.out.println("testAddTransactionsToAccounts_WhenNewOverwriteOld_ThenAddNewCategories");

        // Create test data       
        List<CashTransaction> existingTx = List.of(
                newTx("2025-01-01", 1, true, 10, 10, "abc", "xyz", "Aye"),
                newTx("2025-01-02", 1, true, 10, 20, "abc", "xyz", "Aye"),
                newTx("2025-01-03", 1, true, 10, 30, "abc", "xyz", "Aye")
        );

        List<CashTransaction> importedTx = List.of(
                newTx("2025-01-01", 1, true, 10, 10, "abc", "xyz", "Bee"),
                newTx("2025-01-02", 1, true, 10, 20, "abc", "xyz", "Bee"),
                newTx("2025-01-03", 1, true, 10, 30, "abc", "xyz", "Bee")
        );

        // Perform test
        Account existing = new Account("abc", existingTx);
        Account imported = new Account("abc", importedTx);
        existing.merge(imported, true);

        // Prepare results
        int expected = 3;
        int result = existing.transactionsAscending().size();

        // Evaluate result
        assertEquals(expected, result, "Expected number of unique transactions did not match the actual obtained number of transactions");
        for (var transaction : existing.transactionsAscending()) {
            assertEquals("Bee", transaction.category());
        }

        UtilTest.printResult(expected, result);
    }

}
