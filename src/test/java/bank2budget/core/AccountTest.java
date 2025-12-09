package bank2budget.core;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
                newCtx("2025-01-01", 1, false, 10, 10, "abc", "xyz", "Aye"),
                newCtx("2025-01-01", 2, true, 10, 20, "abc", "xyz", "Aye"),
                newCtx("2025-01-02", 1, false, 10, 30, "abc", "xyz", "Aye"),
                newCtx("2025-01-02", 2, true, 10, 40, "abc", "xyz", "Aye"),
                newCtx("2025-01-03", 1, true, 20, 60, "abc", "xyz", "Aye") // <--- incomplete day 1
        );

        List<CashTransaction> importedTx1 = List.of(
                newCtx("2025-01-03", 1, false, 10, 50, "abc", "xyz", "Bee"), // <--- complete day 1
                newCtx("2025-01-03", 2, true, 20, 70, "abc", "xyz", "Bee"), // <--- complete day 1
                newCtx("2025-01-04", 1, false, 10, 80, "abc", "xyz", "Bee"),
                newCtx("2025-01-04", 2, true, 10, 90, "abc", "xyz", "Bee"),
                newCtx("2025-01-05", 1, true, 20, 110, "abc", "xyz", "Bee") // <--- incomplete day 2
        );

        List<CashTransaction> importedTx2 = List.of(
                newCtx("2025-01-05", 1, false, 10, 100, "abc", "xyz", "Cee"), // <--- complete day 2
                newCtx("2025-01-05", 2, true, 20, 120, "abc", "xyz", "Cee"), // <--- complete day 2
                newCtx("2025-01-06", 1, false, 10, 130, "abc", "xyz", "Cee"),
                newCtx("2025-01-06", 2, true, 10, 140, "abc", "xyz", "Cee"),
                newCtx("2025-01-07", 1, true, 10, 150, "abc", "xyz", "Cee")
        );

        Account existing = new Account("abc", existingTx);
        Account imported1 = new Account("abc", importedTx1);
        Account imported2 = new Account("abc", importedTx2);

        // Perform test 
        existing.merge(imported1);
        existing.merge(imported2);

        // Prepare results
        int expected = 13;
        int result = existing.getAllTransactionsAscending().size();

        // Evaluate results
        assertEquals(expected, result, "Expected number of transactions did not match the actual obtained number of transactions");

        UtilTest.printResult(expected, result);
    }

    @Test
    public void TestMerge_WhenMergingImported_ThenPreferDatesWithMostCtx_Unordered() {
        System.out.println("TestMerge_WhenMergingImported_ThenPreferDatesWithMostCtx_Unordered");

        // Create test data        
        List<CashTransaction> existingTx = List.of(
                newCtx("2025-01-01", 1, false, 10, 10, "abc", "xyz", "Aye"),
                newCtx("2025-01-01", 2, true, 10, 20, "abc", "xyz", "Aye"),
                newCtx("2025-01-02", 1, false, 10, 30, "abc", "xyz", "Aye"),
                newCtx("2025-01-02", 2, true, 10, 40, "abc", "xyz", "Aye"),
                newCtx("2025-01-03", 1, true, 20, 60, "abc", "xyz", "Aye") // <--- incomplete day 1
        );

        List<CashTransaction> importedTx1 = List.of(
                newCtx("2025-01-03", 1, false, 10, 50, "abc", "xyz", "Bee"), // <--- complete day 1
                newCtx("2025-01-03", 2, true, 20, 70, "abc", "xyz", "Bee"), // <--- complete day 1
                newCtx("2025-01-04", 1, false, 10, 80, "abc", "xyz", "Bee"),
                newCtx("2025-01-04", 2, true, 10, 90, "abc", "xyz", "Bee"),
                newCtx("2025-01-05", 1, true, 20, 110, "abc", "xyz", "Bee") // <--- incomplete day 2
        );

        List<CashTransaction> importedTx2 = List.of(
                newCtx("2025-01-05", 1, false, 10, 100, "abc", "xyz", "Cee"), // <--- complete day 2
                newCtx("2025-01-05", 2, true, 20, 120, "abc", "xyz", "Cee"), // <--- complete day 2
                newCtx("2025-01-06", 1, false, 10, 130, "abc", "xyz", "Cee"),
                newCtx("2025-01-06", 2, true, 10, 140, "abc", "xyz", "Cee"),
                newCtx("2025-01-07", 1, true, 10, 150, "abc", "xyz", "Cee")
        );

        Account existing = new Account("abc", existingTx);
        Account imported1 = new Account("abc", importedTx1);
        Account imported2 = new Account("abc", importedTx2);

        // Perform test 
        existing.merge(imported2);
        existing.merge(imported1);

//        existing.getAllTransactionsAscending().stream().forEach(System.out::println);
        // Prepare results
        int expected = 13;
        int result = existing.getAllTransactionsAscending().size();

        // Evaluate results
        assertEquals(expected, result, "Expected number of transactions did not match the actual obtained number of transactions");

        UtilTest.printResult(expected, result);
    }

    public static CashTransaction newCtx(String isoDate, int positionOfDay, boolean lastOfDay, double amount, double balance, String account, String contraAccount, String category) {

        Transaction transaction = newTx(isoDate, positionOfDay, lastOfDay, amount, balance, account, contraAccount, category);
        CashTransaction ctx = new CashTransaction(transaction);

        return ctx;
    }

    public static Transaction newTx(String isoDate, int positionOfDay, boolean lastOfDay, double amount, double balance, String account, String contraAccount, String category) {
        LocalDate date = LocalDate.parse(isoDate);
        int txNumber = getTxNumber(date, positionOfDay);
        Transaction.TransactionType txType = amount > 0 ? Transaction.TransactionType.CREDIT : Transaction.TransactionType.DEBIT;

        return new Transaction(
                txNumber,
                date,
                positionOfDay, // positionOfDay
                lastOfDay, // lastOfDay
                BigDecimal.valueOf(amount),
                BigDecimal.valueOf(balance),
                account, // accountNumber
                account, // accountName
                CreditInstitution.COMDIRECT,
                contraAccount, // contraAccountNumber
                contraAccount, // contraAccountName
                false, // isInternal
                txType,
                null, // description
                category,
                null // notes
        );
    }

    private static int getTxNumber(LocalDate date, int positionOfDay) {
        return Integer.parseInt(date.format(DateTimeFormatter.ofPattern("uuMMdd"))) * 1000 + positionOfDay;
    }

    private List<CashTransaction> filterTransactionsWithinTimespan(List<CashTransaction> list, LocalDate from, LocalDate to) {
        List<CashTransaction> result = new ArrayList<>();
        for (CashTransaction transaction : list) {
            LocalDate date = transaction.date();
            if (date.isBefore(from)) {
                continue;
            }
            result.add(transaction);
        }
        return result;
    }

    private List<CashTransaction> copyTransactions(List<CashTransaction> list) {
        List<CashTransaction> result = new ArrayList<>();
        for (CashTransaction transaction : list) {
            result.add(new CashTransaction(transaction));
        }
        return result;
    }

    private void setCategories(List<CashTransaction> list, String category) {
        for (CashTransaction transaction : list) {
            transaction.setCategory(category);
        }
    }

    private void sortTransactionsByAscendingDate(List<CashTransaction> list) {
        int n = list.size();
        CashTransaction temp = null;
        for (int i = 0; i < n; i++) {
            for (int j = 1; j < (n - i); j++) {
                if (list.get(j - 1).date().isAfter(list.get(j).date())) {
                    //swap elements  
                    temp = list.get(j - 1);
                    list.set(j - 1, list.get(j));
                    list.set(j, temp);
                }
            }
        }
    }

    /**
     * Test of addTransactionsToAccounts method, of class Account.
     */
//    @Test
    public void testAddTransactionsToAccounts_WhenOldWithCategoriesAddNewWithoutCategories_ThenPersistOldCategories() {
        /**
         * old with categories should not be overwritten by new without
         * categories
         */
        System.out.println("testAddTransactionsToAccounts_WhenOldWithCategoriesAddNewWithoutCategories_ThenPersistOldCategories");

        // Create test data        
        String accountNumber = "abc";
        List<CashTransaction> transactionsOld = CashTransactionTest.generateTransactionsForAccountWithinTimespan(accountNumber, LocalDate.parse("2024-03-01"), LocalDate.parse("2024-03-31"), "Some category");
        List<CashTransaction> transactionsNew = copyTransactionsAndSetCategories(transactionsOld, null);

        // Perform test
        Account existing = new Account(accountNumber, transactionsOld);
        Account imported = new Account(accountNumber, transactionsNew);
        existing.merge(imported);

        // Prepare results
        List<CashTransaction> transactionsAll = existing.getAllTransactionsAscending();
        int expected = transactionsOld.size();
        int result = transactionsAll.size();

        // Evaluate result
        assertEquals(expected, result, "Expected number of unique transactions did not match the actual obtained number of transactions");
        for (CashTransaction transaction : transactionsAll) {
            assertEquals("Some category", transaction.category());
        }

        UtilTest.printResult(expected, result);
    }

    private List<CashTransaction> copyTransactionsAndSetCategories(List<CashTransaction> list, String category) {
        List<CashTransaction> result = copyTransactions(list);
        setCategories(result, category);
        return result;
    }

    /**
     * Test of addTransactionsToAccounts method, of class Account.
     */
//    @Test
    public void testAddTransactionsToAccounts_WhenOldWithCategoriesAddNewWithCategories_ThenPersistOldCategories() {
        System.out.println("testAddTransactionsToAccounts_WhenOldWithCategoriesAddNewWithCategories_ThenPersistOldCategories");

        // Create test data        
        String accountNumber = "abc";
        List<CashTransaction> transactionsOld = CashTransactionTest.generateTransactionsForAccountWithinTimespan(accountNumber, LocalDate.parse("2024-03-01"), LocalDate.parse("2024-03-31"), "Some category");
        List<CashTransaction> transactionsNew = copyTransactionsAndSetCategories(transactionsOld, "Other category");

        // Perform test
        Account existing = new Account(accountNumber, transactionsOld);
        Account imported = new Account(accountNumber, transactionsNew);
        existing.merge(imported);

        // Prepare results
        List<CashTransaction> transactionsAll = existing.getAllTransactionsAscending();
        int expected = transactionsOld.size();
        int result = transactionsAll.size();

        // Evaluate result
        assertEquals(expected, result, "Expected number of unique transactions did not match the actual obtained number of transactions");
        for (CashTransaction transaction : transactionsAll) {
            assertEquals("Some category", transaction.category());
        }

        UtilTest.printResult(expected, result);
    }

    /**
     * Test of addTransactionsToAccounts method, of class Account.
     */
//    @Test
    public void testAddTransactionsToAccounts_WhenOldWithoutCategoriesAddNewWithCategories_ThenAddNewCategories() {
        System.out.println("testAddTransactionsToAccounts_WhenOldWithoutCategoriesAddNewWithCategories_ThenAddNewCategories");

        // Create test data   
        String accountNumber = "abc";
        List<CashTransaction> transactionsOld = CashTransactionTest.generateTransactionsForAccountWithinTimespan(accountNumber, LocalDate.parse("2024-03-01"), LocalDate.parse("2024-03-31"), null);
        List<CashTransaction> transactionsNew = copyTransactionsAndSetCategories(transactionsOld, "Other category");

        // Perform test
        Account existing = new Account(accountNumber, transactionsOld);
        Account imported = new Account(accountNumber, transactionsNew);
        existing.merge(imported);

        // Prepare results
        List<CashTransaction> transactionsAll = existing.getAllTransactionsAscending();
        int expected = transactionsOld.size();
        int result = transactionsAll.size();

        // Evaluate result
        assertEquals(expected, result, "Expected number of unique transactions did not match the actual obtained number of transactions");
        for (CashTransaction transaction : transactionsAll) {
            assertEquals("Other category", transaction.category());
        }

        UtilTest.printResult(expected, result);
    }

    /**
     * Test of addTransactionsToAccounts method, of class Account.
     */
//    @Test
    public void testAddTransactionsToAccounts_WhenNewWithCategoriesOverwriteOldWithCategories_ThenPersistOldCategories() {
        System.out.println("testAddTransactionsToAccounts_WhenNewWithCategoriesOverwriteOldWithCategories_ThenPersistOldCategories");

        // Create test data       
        String accountNumber = "abc";
        List<CashTransaction> transactionsOld = CashTransactionTest.generateTransactionsForAccountWithinTimespan(accountNumber, LocalDate.parse("2024-03-01"), LocalDate.parse("2024-03-31"), "Some category");
        List<CashTransaction> transactionsNew = copyTransactionsAndSetCategories(transactionsOld, "Other category");

        // Perform test
        Account existing = new Account(accountNumber, transactionsOld);
        Account imported = new Account(accountNumber, transactionsNew);
        imported.merge(existing, true);

        // Prepare results
        List<CashTransaction> transactionsAll = imported.getAllTransactionsAscending();
        int expected = transactionsOld.size();
        int result = transactionsAll.size();

        // Evaluate result
        assertEquals(expected, result, "Expected number of unique transactions did not match the actual obtained number of transactions");
        for (CashTransaction transaction : transactionsAll) {
            assertEquals("Some category", transaction.category());
        }

        UtilTest.printResult(expected, result);
    }

    /**
     * Test of addTransactionsToAccounts method, of class Account.
     */
//    @Test
    public void testAddTransactionsToAccounts_WhenNewWithCategoriesOverwriteOldWithoutCategories_ThenPersistNewCategories() {
        System.out.println("testAddTransactionsToAccounts_WhenNewWithCategoriesOverwriteOldWithoutCategories_ThenPersistNewCategories");

        // Create test data        
        String accountNumber = "abc";
        List<CashTransaction> transactionsOld = CashTransactionTest.generateTransactionsForAccountWithinTimespan(accountNumber, LocalDate.parse("2024-03-01"), LocalDate.parse("2024-03-31"), null);
        List<CashTransaction> transactionsNew = copyTransactionsAndSetCategories(transactionsOld, "Other category");

        // Perform test
        Account existing = new Account(accountNumber, transactionsOld);
        Account imported = new Account(accountNumber, transactionsNew);
        imported.merge(existing, true);

        // Prepare results
        List<CashTransaction> transactionsAll = imported.getAllTransactionsAscending();
        int expected = transactionsOld.size();
        int result = transactionsAll.size();

        // Evaluate result
        assertEquals(expected, result, "Expected number of unique transactions did not match the actual obtained number of transactions");
        for (CashTransaction transaction : transactionsAll) {
            assertEquals("Other category", transaction.category());
        }

        UtilTest.printResult(expected, result);
    }

}
