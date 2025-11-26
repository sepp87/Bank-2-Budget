package bank2budget.core;

import bank2budget.core.CashTransaction;
import bank2budget.core.Account;
import bank2budget.adapters.parser.TransactionParser;
import java.time.LocalDate;
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

//    @org.junit.jupiter.api.AfterEach
//    public void removeAllAccounts() {
//        Account.removeAllAccounts();
//    }
//
//    @Test
//    public void testAddTransactionsToAccounts_WhenTwoTransactionsOfDifferentAccountsAddedSimultaneously_ThenReturnTwoAccountsWithOneTransaction() {
//        System.out.println("testAddTransactionsToAccounts_WhenTwoTransactionsOfDifferentAccountsAddedSimultaneously_ThenReturnTwoAccountsWithOneTransaction");
//
//        // Create test data        
//        List<CashTransaction> transactions = CashTransactionTest.generateTransactionsForAccountWithinTimespan("xyz", LocalDate.parse("2024-01-01"), LocalDate.parse("2024-01-01"), null);
//        transactions.addAll(CashTransactionTest.generateTransactionsForAccountWithinTimespan("abc", LocalDate.parse("2024-01-31"), LocalDate.parse("2024-01-31"), null));
//
//        // Perform test
//        Account.addTransactionsToAccounts(transactions);
//
//        // Prepare results
//        int expected = 2;
//        int result = Account.getAccountBy("xyz").getAllTransactionsAscending().size() + Account.getAccountBy("abc").getAllTransactionsAscending().size();
//
//        // Evaluate result
//        assertEquals(expected, result, "Expected number of unique transactions did not match the actual obtained number of transactions");
//
//        UtilTest.printResult(expected, result);
//    }
//
//    /**
//     * Test of addTransactionsToAccounts method, of class Account.
//     */
//    @Test
//    //    @RepeatedTest(100)
//    public void testAddTransactionsToAccounts_WhenNewOfTwoAcccounts_ThenAddBoth() {
//        System.out.println("testAddTransactionsToAccounts_WhenNewOfTwoAcccounts_ThenAddBoth");
//
//        // Create test data        
//        List<CashTransaction> accountXyzAll = CashTransactionTest.generateTransactionsForAccountWithinTimespan("xyz", LocalDate.parse("2024-03-01"), LocalDate.parse("2024-04-30"), null);
//        List<CashTransaction> accountAbcAll = CashTransactionTest.generateTransactionsForAccountWithinTimespan("abc", LocalDate.parse("2024-03-01"), LocalDate.parse("2024-03-31"), null);
//
//        // Perform test
//        Account.addTransactionsToAccounts(accountXyzAll);
//        Account.addTransactionsToAccounts(accountAbcAll);
//
//        // Prepare results
//        int expected = accountXyzAll.size() + accountAbcAll.size();
//        int result = Account.getAccountBy("xyz").getAllTransactionsAscending().size() + Account.getAccountBy("abc").getAllTransactionsAscending().size();
//
//        // Evaluate result
//        assertEquals(expected, result, "Expected number of unique transactions did not match the actual obtained number of transactions");
//
//        UtilTest.printResult(expected, result);
//    }

    /**
     * Test of addTransactionsToAccounts method, of class Account.
     */
    @Test
    public void testAddTransactionsToAccounts_WhenOldAddNewWithUnorderedOverlap_ThenAddUniqueOnlyAndOldDoNotExistWithinNew() {
        System.out.println("testAddTransactionsToAccounts_WhenOldAddNewWithUnorderedOverlap_ThenAddUniqueOnlyAndOldNotExistWithinNew");

        // Create test data        
        String accountNumber = "abc";    
        List<CashTransaction> transactionsOld = CashTransactionTest.generateTransactionsForAccountWithinTimespan(accountNumber, LocalDate.parse("2024-03-01"), LocalDate.parse("2024-03-31"), "Some category");
        List<CashTransaction> transactionsNew = generateTransactionsForAccountWithinTimespanWithOverlap(accountNumber, LocalDate.parse("2024-03-22"), LocalDate.parse("2024-04-30"), null, transactionsOld, false);

        // Perform test
        Account existing = new Account(accountNumber, transactionsOld);
        Account imported = new Account(accountNumber, transactionsNew);
        existing.merge(imported);
//        Account.addTransactionsToAccounts(transactionsOld);
//        Account.addTransactionsToAccounts(transactionsNew);

        // Prepare results
        List<CashTransaction> transactionsOldDuplicates = filterTransactionsWithinTimespan(transactionsOld, LocalDate.parse("2024-03-22"), LocalDate.parse("2024-04-30"));
        int expected = transactionsOld.size() + transactionsNew.size() - transactionsOldDuplicates.size();
        int result = existing.getAllTransactionsAscending().size();

        // Evaluate result
        assertEquals(expected, result, "Expected number of unique transactions did not match the actual obtained number of transactions");
        for (CashTransaction duplicate : transactionsOldDuplicates) {
            CashTransaction transaction = existing.getTransactionBy(duplicate.getTransactionNumber());
            boolean sameTransaction = transaction.equals(duplicate);
            assertFalse(sameTransaction, "Expected reimported transactions should NOT exist with given transaction number");
        }

//        for (CashTransaction t : Account.getAccountBy("abc").getAllTransactions()) {
//            System.out.println(t.toString());
//        }
        UtilTest.printResult(expected, result);
    }

    /**
     * Test of addTransactionsToAccounts method, of class Account.
     */
    @Test
    public void testAddTransactionsToAccounts_WhenNewAddOldWithUnorderedOverlap_ThenAddUniqueOnlyAndOldDoNotExistWithinNew() {
        System.out.println("testAddTransactionsToAccounts_WhenNewAddOldWithUnorderedOverlap_ThenAddUniqueOnlyAndOldNotExistWithinNew");

        // Create test data    
        String accountNumber = "abc";    
        List<CashTransaction> transactionsOld = CashTransactionTest.generateTransactionsForAccountWithinTimespan(accountNumber, LocalDate.parse("2024-03-01"), LocalDate.parse("2024-03-31"), "Some category");
        List<CashTransaction> transactionsNew = generateTransactionsForAccountWithinTimespanWithOverlap(accountNumber, LocalDate.parse("2024-03-22"), LocalDate.parse("2024-04-30"), null, transactionsOld, false);

        // Perform test
        Account existing = new Account(accountNumber, transactionsOld);
        Account imported = new Account(accountNumber, transactionsNew);
        imported.merge(existing);
//        Account.addTransactionsToAccounts(transactionsNew);
//        Account.addTransactionsToAccounts(transactionsOld);

        // Prepare results
        List<CashTransaction> transactionsOldDuplicates = filterTransactionsWithinTimespan(transactionsOld, LocalDate.parse("2024-03-22"), LocalDate.parse("2024-04-30"));
        int expected = transactionsOld.size() + transactionsNew.size() - transactionsOldDuplicates.size();
        int result = imported.getAllTransactionsAscending().size();

        // Evaluate result
        assertEquals(expected, result, "Expected number of unique transactions did not match the actual obtained number of transactions");
        for (CashTransaction duplicate : transactionsOldDuplicates) {
            CashTransaction transaction = imported.getTransactionBy(duplicate.getTransactionNumber());
            boolean sameTransaction = transaction.equals(duplicate);
            assertFalse(sameTransaction, "Expected reimported transactions should NOT exist with given transaction number");
        }

        UtilTest.printResult(expected, result);
    }

    /**
     * Test of addTransactionsToAccounts method, of class Account.
     */
    @Test
    public void testAddTransactionsToAccounts_WhenOldAddNewWithOrderedOverlap_ThenAddUniqueOnlyAndOldExistWithinNew() {
        System.out.println("testAddTransactionsToAccounts_WhenOldAddNewWithOrderedOverlap_ThenAddUniqueOnlyAndOldExistWithinNew");

        // Create test data        
        String accountNumber = "abc";
        List<CashTransaction> transactionsOld = CashTransactionTest.generateTransactionsForAccountWithinTimespan(accountNumber, LocalDate.parse("2024-03-01"), LocalDate.parse("2024-03-31"), "Some category");
        List<CashTransaction> transactionsNew = generateTransactionsForAccountWithinTimespanWithOverlap(accountNumber, LocalDate.parse("2024-03-22"), LocalDate.parse("2024-04-30"), null, transactionsOld, true);

        // Perform test
        Account existing = new Account(accountNumber, transactionsOld);
        Account imported = new Account(accountNumber, transactionsNew);
        existing.merge(imported);
//        Account.addTransactionsToAccounts(transactionsOld);
//        Account.addTransactionsToAccounts(transactionsNew);

        // Prepare results
        List<CashTransaction> transactionsOldDuplicates = filterTransactionsWithinTimespan(transactionsOld, LocalDate.parse("2024-03-22"), LocalDate.parse("2024-04-30"));
        int expected = transactionsOld.size() + transactionsNew.size() - transactionsOldDuplicates.size();
        int result = existing.getAllTransactionsAscending().size();

        // Evaluate result
        assertEquals(expected, result, "Expected number of unique transactions did not match the actual obtained number of transactions");
        for (CashTransaction duplicate : transactionsOldDuplicates) {
            CashTransaction transaction = existing.getTransactionBy(duplicate.getTransactionNumber());
            boolean sameTransaction = transaction.equals(duplicate);
            assertTrue(sameTransaction, "Expected reimported transactions should exist with given transaction number");
        }

        UtilTest.printResult(expected, result);
    }

    /**
     * Test of addTransactionsToAccounts method, of class Account.
     */
    @Test
    public void testAddTransactionsToAccounts_WhenNewAddOldWithOrderedOverlap_ThenAddUniqueOnlyAndOldExistWithinNew() {
        System.out.println("testAddTransactionsToAccounts_WhenNewAddOldWithOrderedOverlap_ThenAddUniqueOnlyAndOldExistWithinNew");

        // Create test data        
        String accountNumber = "abc";
        List<CashTransaction> transactionsOld = CashTransactionTest.generateTransactionsForAccountWithinTimespan(accountNumber, LocalDate.parse("2024-03-01"), LocalDate.parse("2024-03-31"), "Some category");
        List<CashTransaction> transactionsNew = generateTransactionsForAccountWithinTimespanWithOverlap(accountNumber, LocalDate.parse("2024-03-22"), LocalDate.parse("2024-04-30"), null, transactionsOld, true);

        // Perform test
        Account existing = new Account(accountNumber, transactionsOld);
        Account imported = new Account(accountNumber, transactionsNew);
        imported.merge(existing);
//        Account.addTransactionsToAccounts(transactionsNew);
//        Account.addTransactionsToAccounts(transactionsOld);

        // Prepare results
        List<CashTransaction> transactionsOldDuplicates = filterTransactionsWithinTimespan(transactionsOld, LocalDate.parse("2024-03-22"), LocalDate.parse("2024-04-30"));
        int expected = transactionsOld.size() + transactionsNew.size() - transactionsOldDuplicates.size();
        int result = imported.getAllTransactionsAscending().size();

        // Evaluate result
        assertEquals(expected, result, "Expected number of unique transactions did not match the actual obtained number of transactions");
        for (CashTransaction duplicate : transactionsOldDuplicates) {
//            CashTransaction transaction = Account.getAccountBy("abc").getTransactionBy(duplicate.getTransactionNumber());
            CashTransaction transaction = imported.getTransactionBy(duplicate.getTransactionNumber());
            boolean sameTransaction = transaction.equals(duplicate);
            assertTrue(sameTransaction, "Expected reimported transactions should exist with given transaction number");
        }

        UtilTest.printResult(expected, result);
    }

    private List<CashTransaction> generateTransactionsForAccountWithinTimespanWithOverlap(String account, LocalDate from, LocalDate to, String category, List<CashTransaction> transactions, boolean overlappingFirst) {
        List<CashTransaction> result = CashTransactionTest.generateTransactionsForAccountWithinTimespan(account, from, to, category);
        List<CashTransaction> overlap = filterTransactionsWithinTimespan(transactions, from, to);
        List<CashTransaction> copies = copyTransactions(overlap);
        setCategories(copies, category);
        if (overlappingFirst) {
            result.addAll(0, copies);
        } else {
            result.addAll(copies);
        }
        sortTransactionsByAscendingDate(result);
        TransactionParser.generateTransactionNumberAndDeriveLastOfDay(result);
        return result;
    }

    private List<CashTransaction> filterTransactionsWithinTimespan(List<CashTransaction> list, LocalDate from, LocalDate to) {
        List<CashTransaction> result = new ArrayList<>();
        for (CashTransaction transaction : list) {
            LocalDate date = transaction.getDate();
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
                if (list.get(j - 1).getDate().isAfter(list.get(j).getDate())) {
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
    @Test
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
//        Account.addTransactionsToAccounts(transactionsOld);
//        Account.addTransactionsToAccounts(transactionsNew);

        // Prepare results
//        List<CashTransaction> transactionsAll = Account.getAccountBy("abc").getAllTransactionsAscending();
        List<CashTransaction> transactionsAll = existing.getAllTransactionsAscending();
        int expected = transactionsOld.size();
        int result = transactionsAll.size();

        // Evaluate result
        assertEquals(expected, result, "Expected number of unique transactions did not match the actual obtained number of transactions");
        for (CashTransaction transaction : transactionsAll) {
            assertEquals("Some category", transaction.getCategory());
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
    @Test
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
//        Account.addTransactionsToAccounts(transactionsOld);
//        Account.addTransactionsToAccounts(transactionsNew);

        // Prepare results
//        List<CashTransaction> transactionsAll = Account.getAccountBy("abc").getAllTransactionsAscending();
        List<CashTransaction> transactionsAll = existing.getAllTransactionsAscending();
        int expected = transactionsOld.size();
        int result = transactionsAll.size();

        // Evaluate result
        assertEquals(expected, result, "Expected number of unique transactions did not match the actual obtained number of transactions");
        for (CashTransaction transaction : transactionsAll) {
            assertEquals("Some category", transaction.getCategory());
        }

        UtilTest.printResult(expected, result);
    }

    /**
     * Test of addTransactionsToAccounts method, of class Account.
     */
    @Test
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
//        Account.addTransactionsToAccounts(transactionsOld);
//        Account.addTransactionsToAccounts(transactionsNew);

        // Prepare results
//        List<CashTransaction> transactionsAll = Account.getAccountBy("abc").getAllTransactionsAscending();
        List<CashTransaction> transactionsAll = existing.getAllTransactionsAscending();
        int expected = transactionsOld.size();
        int result = transactionsAll.size();

        // Evaluate result
        assertEquals(expected, result, "Expected number of unique transactions did not match the actual obtained number of transactions");
        for (CashTransaction transaction : transactionsAll) {
            assertEquals("Other category", transaction.getCategory());
        }

        UtilTest.printResult(expected, result);
    }

    /**
     * Test of addTransactionsToAccounts method, of class Account.
     */
    @Test
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

//        Account.addTransactionsToAccounts(transactionsNew);
//        Account.addTransactionsToAccounts(transactionsOld, true);
        // Prepare results
        List<CashTransaction> transactionsAll = imported.getAllTransactionsAscending();
//        List<CashTransaction> transactionsAll = Account.getAccountBy("abc").getAllTransactionsAscending();
        int expected = transactionsOld.size();
        int result = transactionsAll.size();

        // Evaluate result
        assertEquals(expected, result, "Expected number of unique transactions did not match the actual obtained number of transactions");
        for (CashTransaction transaction : transactionsAll) {
            assertEquals("Some category", transaction.getCategory());
        }

        UtilTest.printResult(expected, result);
    }

    /**
     * Test of addTransactionsToAccounts method, of class Account.
     */
    @Test
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

//        Account.addTransactionsToAccounts(transactionsNew);
//        Account.addTransactionsToAccounts(transactionsOld, true);
        // Prepare results
        List<CashTransaction> transactionsAll = imported.getAllTransactionsAscending();
//        List<CashTransaction> transactionsAll = Account.getAccountBy("abc").getAllTransactionsAscending();
        int expected = transactionsOld.size();
        int result = transactionsAll.size();

        // Evaluate result
        assertEquals(expected, result, "Expected number of unique transactions did not match the actual obtained number of transactions");
        for (CashTransaction transaction : transactionsAll) {
            assertEquals("Other category", transaction.getCategory());
        }

        UtilTest.printResult(expected, result);
    }

}
