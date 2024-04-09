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

/**
 *
 * @author joostmeulenkamp
 */
public class AccountTest {

    /**
     * Test of addTransactionsToAccounts method, of class Account.
     *
     * TODO Rewrite test, because randomized data sometimes yields duplicates,
     * which causes the test to fail. With randomized amount, the tests do not
     * fail anymore, but the test is not deterministic.
     */
    @org.junit.jupiter.api.Test
//    @RepeatedTest(100)
    public void testAddTransactionsToAccounts_WhenBankStatementsOverlap_ThenAddUniqueTransactionsOnly() {
        System.out.println("testAddTransactionsToAccounts_WhenBankStatementsOverlap_ThenAddUniqueTransactionsOnly");

        // Create test data        
        List<CashTransaction> accountXyzAll = generateTransactionsForAccountWithinTimespan("xyz", LocalDate.now().minusDays(15), LocalDate.now());
        List<CashTransaction> accountAbcOld = generateTransactionsForAccountWithinTimespan("abc", LocalDate.now().minusDays(15), LocalDate.now().minusDays(5));
        List<CashTransaction> accountAbcNew = generateTransactionsForAccountWithinTimespan("abc", LocalDate.now().minusDays(10), LocalDate.now());
        List<CashTransaction> accountAbcOldDuplicates = filterTransactionsWithinTimespan(accountAbcOld, LocalDate.now().minusDays(10), LocalDate.now());
        List<CashTransaction> accountAbcNewDuplicates = copyTransactions(accountAbcOldDuplicates);
        accountAbcNew.addAll(accountAbcNewDuplicates);
        sortTransactionsByAscendingDate(accountAbcNew);
        TransactionParser.generateTransactionNumberAndDeriveLastOfDay(accountAbcNew);

        Account.addTransactionsToAccounts(accountXyzAll);
        Account.addTransactionsToAccounts(accountAbcOld);
        Account.addTransactionsToAccounts(accountAbcNew);

//        for (CashTransaction t : accountAbcNew) {
//            System.out.println(t.toString());
//        }
        // Perform test
        int expected = accountXyzAll.size() + accountAbcOld.size() + accountAbcNew.size() - accountAbcOldDuplicates.size();
        int result = Account.getAccountBy("xyz").getAllTransactions().size() + Account.getAccountBy("abc").getAllTransactions().size();

        // Evaluate result
        assertEquals(expected, result, "Expected number of unique transactions did not match the actual obtained number of transactions");
        for (CashTransaction duplicate : accountAbcOldDuplicates) {
            CashTransaction transaction = Account.getAccountBy("abc").getTransactionBy(duplicate.getTransactionNumber());
            boolean sameTransaction = transaction.equals(duplicate);
//            if (sameTransaction) {
//                System.out.println(transaction);
//                System.out.println(duplicate);
//            }

            assertFalse(sameTransaction, "Expected reimported transactions should NOT exist with given transaction number");
        }
        Account.removeAllAccounts();
    }

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
                CashTransaction transaction = CashTransactionTest.generateOneTransaction(account, date);
                transactions.add(transaction);
            }
        }
        TransactionParser.generateTransactionNumberAndDeriveLastOfDay(transactions);
        return transactions;
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

}
