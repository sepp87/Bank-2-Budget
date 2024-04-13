/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package io.ost.finance;

import io.ost.finance.parser.TransactionParser;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.RepeatedTest;

/**
 *
 * @author joostmeulenkamp
 */
public class AccountTest {

    @org.junit.jupiter.api.AfterEach
    public void removeAllAccounts() {
        Account.removeAllAccounts();
    }

    /**
     * Test of addTransactionsToAccounts method, of class Account.
     */
    @org.junit.jupiter.api.Test
    //    @RepeatedTest(100)
    public void testAddTransactionsToAccounts_WhenNewOfTwoAcccounts_ThenAddBoth() {
        System.out.println("testAddTransactionsToAccounts_WhenNewOfTwoAcccounts_ThenAddBoth");

        // Create test data        
        List<CashTransaction> accountXyzAll = generateTransactionsForAccountWithinTimespan("xyz", LocalDate.parse("2024-03-01"), LocalDate.parse("2024-04-30"), null);
        List<CashTransaction> accountAbcAll = generateTransactionsForAccountWithinTimespan("abc", LocalDate.parse("2024-03-01"), LocalDate.parse("2024-03-31"), null);

        // Perform test
        Account.addTransactionsToAccounts(accountXyzAll);
        Account.addTransactionsToAccounts(accountAbcAll);

        // Prepare results
        int expected = accountXyzAll.size() + accountAbcAll.size();
        int result = Account.getAccountBy("xyz").getAllTransactions().size() + Account.getAccountBy("abc").getAllTransactions().size();

        // Evaluate result
        assertEquals(expected, result, "Expected number of unique transactions did not match the actual obtained number of transactions");

        System.out.println("Expected: " + expected + "\tResult: " + result);
    }

    private List<CashTransaction> generateTransactionsForAccountWithinTimespan(String account, LocalDate from, LocalDate to, String label) {
        List<LocalDate> dates = generateDatesForTimespan(from, to);
        List<CashTransaction> transactions = generateTransactionforEachDate(account, dates, label);
        return transactions;
    }

    private List<LocalDate> generateDatesForTimespan(LocalDate from, LocalDate to) {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate nextDate = from;
        while (nextDate.isBefore(to)) {
            dates.add(nextDate);
            nextDate = nextDate.plusDays(1);
        }
        dates.add(to);
        return dates;
    }

    private List<CashTransaction> generateTransactionforEachDate(String account, List<LocalDate> dates, String label) {
        List<CashTransaction> transactions = new ArrayList<>();
        for (LocalDate date : dates) {
            CashTransaction transaction = CashTransactionTest.generateOneTransaction(account, date, label);
            transactions.add(transaction);
//            int count = ThreadLocalRandom.current().nextInt(1, 3);
//            for (int i = 0; i < count; i++) {
//                CashTransaction transaction = CashTransactionTest.generateOneTransaction(account, date);
//                transactions.add(transaction);
//            }
        }
        TransactionParser.generateTransactionNumberAndDeriveLastOfDay(transactions);
        return transactions;
    }

    /**
     * Test of addTransactionsToAccounts method, of class Account.
     */
    @org.junit.jupiter.api.Test
    public void testAddTransactionsToAccounts_WhenOldAddNewWithUnorderedOverlap_ThenAddUniqueOnlyAndOldDoNotExistWithinNew() {
        System.out.println("testAddTransactionsToAccounts_WhenOldAddNewWithUnorderedOverlap_ThenAddUniqueOnlyAndOldNotExistWithinNew");

        // Create test data        
        List<CashTransaction> transactionsOld = generateTransactionsForAccountWithinTimespan("abc", LocalDate.parse("2024-03-01"), LocalDate.parse("2024-03-31"), "Some label");
        List<CashTransaction> transactionsNew = generateTransactionsForAccountWithinTimespanWithOverlap("abc", LocalDate.parse("2024-03-22"), LocalDate.parse("2024-04-30"), null, transactionsOld, false);

        // Perform test
        Account.addTransactionsToAccounts(transactionsOld);
        Account.addTransactionsToAccounts(transactionsNew);

        // Prepare results
        List<CashTransaction> transactionsOldDuplicates = filterTransactionsWithinTimespan(transactionsOld, LocalDate.parse("2024-03-22"), LocalDate.parse("2024-04-30"));
        int expected = transactionsOld.size() + transactionsNew.size() - transactionsOldDuplicates.size();
        int result = Account.getAccountBy("abc").getAllTransactions().size();

        // Evaluate result
        assertEquals(expected, result, "Expected number of unique transactions did not match the actual obtained number of transactions");
        for (CashTransaction duplicate : transactionsOldDuplicates) {
            CashTransaction transaction = Account.getAccountBy("abc").getTransactionBy(duplicate.getTransactionNumber());
            boolean sameTransaction = transaction.equals(duplicate);
            assertFalse(sameTransaction, "Expected reimported transactions should NOT exist with given transaction number");
        }

//        for (CashTransaction t : Account.getAccountBy("abc").getAllTransactions()) {
//            System.out.println(t.toString());
//        }

        System.out.println("Expected: " + expected + "\tResult: " + result);
    }

    /**
     * Test of addTransactionsToAccounts method, of class Account.
     */
    @org.junit.jupiter.api.Test
    public void testAddTransactionsToAccounts_WhenNewAddOldWithUnorderedOverlap_ThenAddUniqueOnlyAndOldDoNotExistWithinNew() {
        System.out.println("testAddTransactionsToAccounts_WhenNewAddOldWithUnorderedOverlap_ThenAddUniqueOnlyAndOldNotExistWithinNew");

        // Create test data        
        List<CashTransaction> transactionsOld = generateTransactionsForAccountWithinTimespan("abc", LocalDate.parse("2024-03-01"), LocalDate.parse("2024-03-31"), "Some label");
        List<CashTransaction> transactionsNew = generateTransactionsForAccountWithinTimespanWithOverlap("abc", LocalDate.parse("2024-03-22"), LocalDate.parse("2024-04-30"), null, transactionsOld, false);

        // Perform test
        Account.addTransactionsToAccounts(transactionsNew);
        Account.addTransactionsToAccounts(transactionsOld);

        // Prepare results
        List<CashTransaction> transactionsOldDuplicates = filterTransactionsWithinTimespan(transactionsOld, LocalDate.parse("2024-03-22"), LocalDate.parse("2024-04-30"));
        int expected = transactionsOld.size() + transactionsNew.size() - transactionsOldDuplicates.size();
        int result = Account.getAccountBy("abc").getAllTransactions().size();

        // Evaluate result
        assertEquals(expected, result, "Expected number of unique transactions did not match the actual obtained number of transactions");
        for (CashTransaction duplicate : transactionsOldDuplicates) {
            CashTransaction transaction = Account.getAccountBy("abc").getTransactionBy(duplicate.getTransactionNumber());
            boolean sameTransaction = transaction.equals(duplicate);
            assertFalse(sameTransaction, "Expected reimported transactions should NOT exist with given transaction number");
        }

//        for (CashTransaction t : Account.getAccountBy("abc").getAllTransactions()) {
//            System.out.println(t.toString());
//        }

        System.out.println("Expected: " + expected + "\tResult: " + result);
    }

    /**
     * Test of addTransactionsToAccounts method, of class Account.
     */
    @org.junit.jupiter.api.Test
    public void testAddTransactionsToAccounts_WhenOldAddNewWithOrderedOverlap_ThenAddUniqueOnlyAndOldExistWithinNew() {
        System.out.println("testAddTransactionsToAccounts_WhenOldAddNewWithOrderedOverlap_ThenAddUniqueOnlyAndOldExistWithinNew");

        // Create test data        
        List<CashTransaction> transactionsOld = generateTransactionsForAccountWithinTimespan("abc", LocalDate.parse("2024-03-01"), LocalDate.parse("2024-03-31"), "Some label");
        List<CashTransaction> transactionsNew = generateTransactionsForAccountWithinTimespanWithOverlap("abc", LocalDate.parse("2024-03-22"), LocalDate.parse("2024-04-30"), null, transactionsOld, true);

        // Perform test
        Account.addTransactionsToAccounts(transactionsOld);
        Account.addTransactionsToAccounts(transactionsNew);

        // Prepare results
        List<CashTransaction> transactionsOldDuplicates = filterTransactionsWithinTimespan(transactionsOld, LocalDate.parse("2024-03-22"), LocalDate.parse("2024-04-30"));
        int expected = transactionsOld.size() + transactionsNew.size() - transactionsOldDuplicates.size();
        int result = Account.getAccountBy("abc").getAllTransactions().size();

        // Evaluate result
        assertEquals(expected, result, "Expected number of unique transactions did not match the actual obtained number of transactions");
        for (CashTransaction duplicate : transactionsOldDuplicates) {
            CashTransaction transaction = Account.getAccountBy("abc").getTransactionBy(duplicate.getTransactionNumber());
            boolean sameTransaction = transaction.equals(duplicate);
            assertTrue(sameTransaction, "Expected reimported transactions should exist with given transaction number");
        }

//        for (CashTransaction t : Account.getAccountBy("abc").getAllTransactions()) {
//            System.out.println(t.toString());
//        }

        
        System.out.println("Expected: " + expected + "\tResult: " + result);
    }

    /**
     * Test of addTransactionsToAccounts method, of class Account.
     */
    @org.junit.jupiter.api.Test
    public void testAddTransactionsToAccounts_WhenNewAddOldWithOrderedOverlap_ThenAddUniqueOnlyAndOldExistWithinNew() {
        System.out.println("testAddTransactionsToAccounts_WhenNewAddOldWithOrderedOverlap_ThenAddUniqueOnlyAndOldExistWithinNew");

        // Create test data        
        List<CashTransaction> transactionsOld = generateTransactionsForAccountWithinTimespan("abc", LocalDate.parse("2024-03-01"), LocalDate.parse("2024-03-31"), "Some label");
        List<CashTransaction> transactionsNew = generateTransactionsForAccountWithinTimespanWithOverlap("abc", LocalDate.parse("2024-03-22"), LocalDate.parse("2024-04-30"), null, transactionsOld, true);

        // Perform test
        Account.addTransactionsToAccounts(transactionsNew);
        Account.addTransactionsToAccounts(transactionsOld);

        // Prepare results
        List<CashTransaction> transactionsOldDuplicates = filterTransactionsWithinTimespan(transactionsOld, LocalDate.parse("2024-03-22"), LocalDate.parse("2024-04-30"));
        int expected = transactionsOld.size() + transactionsNew.size() - transactionsOldDuplicates.size();
        int result = Account.getAccountBy("abc").getAllTransactions().size();

        // Evaluate result
        assertEquals(expected, result, "Expected number of unique transactions did not match the actual obtained number of transactions");
        for (CashTransaction duplicate : transactionsOldDuplicates) {
            CashTransaction transaction = Account.getAccountBy("abc").getTransactionBy(duplicate.getTransactionNumber());
            boolean sameTransaction = transaction.equals(duplicate);
            assertTrue(sameTransaction, "Expected reimported transactions should exist with given transaction number");
        }

//        for (CashTransaction t : Account.getAccountBy("abc").getAllTransactions()) {
//            System.out.println(t.toString());
//        }

        System.out.println("Expected: " + expected + "\tResult: " + result);
    }

    private List<CashTransaction> generateTransactionsForAccountWithinTimespanWithOverlap(String account, LocalDate from, LocalDate to, String label, List<CashTransaction> transactions, boolean overlappingFirst) {
        List<CashTransaction> result = generateTransactionsForAccountWithinTimespan(account, from, to, label);
        List<CashTransaction> overlap = filterTransactionsWithinTimespan(transactions, from, to);
        List<CashTransaction> copies = copyTransactions(overlap);
        setLabels(copies, label);
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
            LocalDate date = LocalDate.parse(transaction.getDate());
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

    private void setLabels(List<CashTransaction> list, String label) {
        for (CashTransaction transaction : list) {
            transaction.setLabel(label);
        }
    }

    private void sortTransactionsByAscendingDate(List<CashTransaction> list) {
        int n = list.size();
        CashTransaction temp = null;
        for (int i = 0; i < n; i++) {
            for (int j = 1; j < (n - i); j++) {
                if (LocalDate.parse(list.get(j - 1).getDate()).isAfter(LocalDate.parse(list.get(j).getDate()))) {
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
    @org.junit.jupiter.api.Test
    public void testAddTransactionsToAccounts_WhenOldWithLabelsAddNewWithoutLabels_ThenPersistOldLabels() {
        /**
         * old with labels should not be overwritten by new without labels
         */
        System.out.println("testAddTransactionsToAccounts_WhenOldWithLabelsAddNewWithoutLabels_ThenPersistOldLabels");

        // Create test data        
        List<CashTransaction> transactionsOld = generateTransactionsForAccountWithinTimespan("abc", LocalDate.parse("2024-03-01"), LocalDate.parse("2024-03-31"), "Some label");
        List<CashTransaction> transactionsNew = copyTransactionsAndSetLabels(transactionsOld, null);

        // Perform test
        Account.addTransactionsToAccounts(transactionsOld);
        Account.addTransactionsToAccounts(transactionsNew);

        // Prepare results
        List<CashTransaction> transactionsAll = Account.getAccountBy("abc").getAllTransactions();
        int expected = transactionsOld.size();
        int result = transactionsAll.size();

        // Evaluate result
        assertEquals(expected, result, "Expected number of unique transactions did not match the actual obtained number of transactions");
        for (CashTransaction transaction : transactionsAll) {
            assertEquals("Some label", transaction.getLabel());
        }

        System.out.println("Expected: " + expected + "\tResult: " + result);
    }

    private List<CashTransaction> copyTransactionsAndSetLabels(List<CashTransaction> list, String label) {
        List<CashTransaction> result = copyTransactions(list);
        setLabels(result, label);
        return result;
    }

    /**
     * Test of addTransactionsToAccounts method, of class Account.
     */
    @org.junit.jupiter.api.Test
    public void testAddTransactionsToAccounts_WhenOldWithLabelsAddNewWithLabels_ThenPersistOldLabels() {
        System.out.println("testAddTransactionsToAccounts_WhenOldWithLabelsAddNewWithLabels_ThenPersistOldLabels");

        // Create test data        
        List<CashTransaction> transactionsOld = generateTransactionsForAccountWithinTimespan("abc", LocalDate.parse("2024-03-01"), LocalDate.parse("2024-03-31"), "Some label");
        List<CashTransaction> transactionsNew = copyTransactionsAndSetLabels(transactionsOld, "Other label");

        // Perform test
        Account.addTransactionsToAccounts(transactionsOld);
        Account.addTransactionsToAccounts(transactionsNew);

        // Prepare results
        List<CashTransaction> transactionsAll = Account.getAccountBy("abc").getAllTransactions();
        int expected = transactionsOld.size();
        int result = transactionsAll.size();

        // Evaluate result
        assertEquals(expected, result, "Expected number of unique transactions did not match the actual obtained number of transactions");
        for (CashTransaction transaction : transactionsAll) {
            assertEquals("Some label", transaction.getLabel());
        }

        System.out.println("Expected: " + expected + "\tResult: " + result);
    }

    /**
     * Test of addTransactionsToAccounts method, of class Account.
     */
    @org.junit.jupiter.api.Test
    public void testAddTransactionsToAccounts_WhenOldWithoutLabelsAddNewWithLabels_ThenAddNewLabels() {
        System.out.println("testAddTransactionsToAccounts_WhenOldWithoutLabelsAddNewWithLabels_ThenAddNewLabels");

        // Create test data        
        List<CashTransaction> transactionsOld = generateTransactionsForAccountWithinTimespan("abc", LocalDate.parse("2024-03-01"), LocalDate.parse("2024-03-31"), null);
        List<CashTransaction> transactionsNew = copyTransactionsAndSetLabels(transactionsOld, "Other label");

        // Perform test
        Account.addTransactionsToAccounts(transactionsOld);
        Account.addTransactionsToAccounts(transactionsNew);

        // Prepare results
        List<CashTransaction> transactionsAll = Account.getAccountBy("abc").getAllTransactions();
        int expected = transactionsOld.size();
        int result = transactionsAll.size();

        // Evaluate result
        assertEquals(expected, result, "Expected number of unique transactions did not match the actual obtained number of transactions");
        for (CashTransaction transaction : transactionsAll) {
            assertEquals("Other label", transaction.getLabel());
        }

        System.out.println("Expected: " + expected + "\tResult: " + result);
    }

    /**
     * Test of addTransactionsToAccounts method, of class Account.
     */
    @org.junit.jupiter.api.Test
    public void testAddTransactionsToAccounts_WhenNewWithLabelsAddOldWithLabels_ThenPersistOldLabels() {
        System.out.println("testAddTransactionsToAccounts_WhenNewWithLabelsAddOldWithLabels_ThenPersistOldLabels");

        // Create test data        
        List<CashTransaction> transactionsOld = generateTransactionsForAccountWithinTimespan("abc", LocalDate.parse("2024-03-01"), LocalDate.parse("2024-03-31"), "Some label");
        List<CashTransaction> transactionsNew = copyTransactionsAndSetLabels(transactionsOld, "Other label");

        // Perform test
        Account.addTransactionsToAccounts(transactionsNew);
        Account.addTransactionsToAccounts(transactionsOld, true);

        // Prepare results
        List<CashTransaction> transactionsAll = Account.getAccountBy("abc").getAllTransactions();
        int expected = transactionsOld.size();
        int result = transactionsAll.size();

        // Evaluate result
        assertEquals(expected, result, "Expected number of unique transactions did not match the actual obtained number of transactions");
        for (CashTransaction transaction : transactionsAll) {
            assertEquals("Some label", transaction.getLabel());
        }

        System.out.println("Expected: " + expected + "\tResult: " + result);
    }

}
