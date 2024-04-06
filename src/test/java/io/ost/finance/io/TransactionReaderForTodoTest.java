/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package io.ost.finance.io;

import com.sun.source.doctree.SystemPropertyTree;
import io.ost.finance.CashTransaction;
import io.ost.finance.parser.TransactionParser;
import io.ost.finance.parser.UnifiedCsvParser;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author joostmeulenkamp
 */
public class TransactionReaderForTodoTest {

//    public TransactionReaderForTodoTest() {
//    }
//
//    @org.junit.jupiter.api.BeforeAll
//    public static void setUpClass() throws Exception {
//    }
//
//    @org.junit.jupiter.api.AfterAll
//    public static void tearDownClass() throws Exception {
//    }
//
//    @org.junit.jupiter.api.BeforeEach
//    public void setUp() throws Exception {
//    }
//
//    @org.junit.jupiter.api.AfterEach
//    public void tearDown() throws Exception {
//    }
//
//    /**
//     * Test of getPerFile method, of class TransactionReaderForTodo.
//     */
//    @org.junit.jupiter.api.Test
//    public void testGetPerFile() {
//        System.out.println("getPerFile");
//        TransactionReaderForTodo instance = new TransactionReaderForTodo();
//        Map<String, List<CashTransaction>> expResult = null;
//        Map<String, List<CashTransaction>> result = instance.getPerFile();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getAsList method, of class TransactionReaderForTodo.
//     */
//    @org.junit.jupiter.api.Test
//    public void testGetAsList() {
//        System.out.println("getAsList");
//        TransactionReaderForTodo instance = new TransactionReaderForTodo();
//        List<CashTransaction> expResult = null;
//        List<CashTransaction> result = instance.getAsList();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of read method, of class TransactionReaderForTodo.
//     */
//    @org.junit.jupiter.api.Test
//    public void testRead() {
//        System.out.println("read");
//        TransactionReaderForTodo instance = new TransactionReaderForTodo();
//        TransactionReaderForTodo expResult = null;
//        TransactionReaderForTodo result = instance.read();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
    /**
     * Test of read method, of class TransactionReaderForTodo.
     */
//    @org.junit.jupiter.api.Test
//    public void testGetUniqueTransactionsOnly() {
//        System.out.println("getUniqueTransactionsOnly");
//        TransactionReaderForTodo instance = new TransactionReaderForTodo();
//
//        // Create test data        
//        List<CashTransaction> accountXyzAll = generateTransactionsForAccountWithinTimespan("xyz", LocalDate.now().minusDays(15), LocalDate.now());
//        List<CashTransaction> accountAbcOld = generateTransactionsForAccountWithinTimespan("abc", LocalDate.now().minusDays(15), LocalDate.now().minusDays(5));
//        List<CashTransaction> accountAbcNew = generateTransactionsForAccountWithinTimespan("abc", LocalDate.now().minusDays(10), LocalDate.now());
//        List<CashTransaction> accountAbcDuplicates = filterTransactionsWithinTimespan(accountAbcOld, LocalDate.now().minusDays(10), LocalDate.now());
//        accountAbcNew.addAll(accountAbcDuplicates);
//        sortTransactionsByAscendingDate(accountAbcNew);
//
//        for (CashTransaction transaction : accountAbcOld) {
//            System.out.println(transaction.toString());
//        }
//
//        System.out.println();
//
//        for (CashTransaction transaction : accountAbcNew) {
//            System.out.println(transaction.toString());
//        }
//
//        instance.todoTransactionsPerFile.put("accountXyzAll.csv", accountXyzAll);
//        instance.todoTransactionsPerFile.put("accountAbcOld.csv", accountAbcOld);
//        instance.todoTransactionsPerFile.put("accountAbcNew.csv", accountAbcNew);
//
//        // Perform test
////        int expectedResult = accountAbcOld.size() + accountAbcNew.size() - accountAbcDuplicates.size();
//        int expectedResult = accountXyzAll.size() + accountAbcOld.size() + accountAbcNew.size() - accountAbcDuplicates.size();
//        List<CashTransaction> uniqueTransactions = instance.getUniqueTransactionsOnly();
//        int result = uniqueTransactions.size();
//        System.out.println("accountXyzAll " + accountXyzAll.size());
//        System.out.println("accountAbcOld " + accountAbcOld.size());
//        System.out.println("accountAbcNew " + accountAbcNew.size());
//        System.out.println("Duplicates    " + accountAbcDuplicates.size());
//        System.out.println(expectedResult + " " + result);
//
//        // Evaluate result
//        assertEquals(expectedResult, result);
//
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    private List<CashTransaction> generateTransactionsForAccountWithinTimespan(String account, LocalDate from, LocalDate to) {
        List<LocalDate> dates = generateDatesForTimespan(from, to);
        List<CashTransaction> transactions = generateTransactionsforDates(account, dates);
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

    private List<CashTransaction> generateTransactionsforDates(String account, List<LocalDate> dates) {
        List<CashTransaction> transactions = new ArrayList<>();
        for (LocalDate date : dates) {
            int count = ThreadLocalRandom.current().nextInt(1, 3);
            for (int i = 0; i < count; i++) {
                CashTransaction transaction = generateOneTransaction(account, date);
                transactions.add(transaction);
            }
        }
        return transactions;
    }

    private List<CashTransaction> filterTransactionsWithinTimespan(List<CashTransaction> transactions, LocalDate from, LocalDate to) {
        List<CashTransaction> list = new ArrayList<>();
        for (CashTransaction transaction : transactions) {
            LocalDate date = LocalDate.parse(transaction.getDate());
            if (date.isBefore(from)) {
                continue;
            }
            list.add(transaction);
        }
        return list;
    }

    private void sortTransactionsByAscendingDate(List<CashTransaction> transactions) {
        int n = transactions.size();
        CashTransaction temp = null;
        for (int i = 0; i < n; i++) {
            for (int j = 1; j < (n - i); j++) {
                if (LocalDate.parse(transactions.get(j - 1).getDate()).isAfter(LocalDate.parse(transactions.get(j).getDate()))) {
                    //swap elements  
                    temp = transactions.get(j - 1);
                    transactions.set(j - 1, transactions.get(j));
                    transactions.set(j, temp);
                }
            }
        }
    }

    private CashTransaction generateOneTransaction(String account, LocalDate date) {
        CashTransaction transaction = new CashTransaction();
        transaction.setAccountName(account);
        transaction.setDate(date.toString());
        transaction.setAmount(-10.);
        int expenditure = ThreadLocalRandom.current().nextInt(0, SAMPLE_EXPENDITURES.length - 1);
        transaction.setContraAccountName(SAMPLE_EXPENDITURES[expenditure]);
        return transaction;
    }

    private final String[] SAMPLE_EXPENDITURES = {
        "Cozy Crafts",
        "Sparkle Shoes",
        "Wholesome Wear",
        "Fancy Foods",
        "Stylish Suits",
        "Rustic Rugs",
        "Tasty Treats",
        "Snug Sweaters",
        "Green Groceries",
        "Chic Chairs",
        "Trendy Toys",
        "Marvelous Market",
        "Mighty Mall",
        "Bountiful Bazaar",
        "Swift Study",
        "Sparkle Spa",
        "Sustainable Study",
        "Scholarly Store",
        "Handy Hardware",
        "Hardy Hammers",
        "Stellar Skies",
        "Fast Fuel",
        "Trailblazer Terminal"
    };

}
