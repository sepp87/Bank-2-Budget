package io.ost.finance.parser;

import io.ost.finance.Account;
import io.ost.finance.AccountTest;
import io.ost.finance.CashTransaction;
import io.ost.finance.CashTransactionTest;
import io.ost.finance.UtilTest;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author joost
 */
public class TransactionParserTest {

    @org.junit.jupiter.api.BeforeAll
    @org.junit.jupiter.api.AfterAll
    public static void printLine() {
        System.out.println();
    }

    @Test
    public void testGenerateTransactionNumberAndDeriveLastOfDay_WhenTenTransactions_ThenTenIsHighestPositionAndLastOfDay() {
        System.out.println("testGenerateTransactionNumberAndDeriveLastOfDay_WhenTenTransactions_ThenTenIsHighestPositionAndLastOfDay");

        // Create test data        
        List<CashTransaction> transactions = generateTenTransactions("abc", LocalDate.parse("2024-03-01"));

        // Perform test
        TransactionParser.generateTransactionNumberAndDeriveLastOfDay(transactions);

        // Prepare results
        int expectedTransactionNumber = 240301001;
        int expectedPositionOfDay = 1;
        boolean expectedIsLastOfDay = false;
        for (CashTransaction transaction : transactions) {
            if (expectedPositionOfDay == 10) {
                expectedIsLastOfDay = true;
            }

            // Evaluate result
            assertEquals(expectedTransactionNumber, transaction.getTransactionNumber());
            assertEquals(expectedPositionOfDay, transaction.getPositionOfDay());
            assertEquals(expectedIsLastOfDay, transaction.isLastOfDay());

            UtilTest.printResult(expectedTransactionNumber, transaction.getTransactionNumber());
            UtilTest.printResult(expectedPositionOfDay, transaction.getPositionOfDay());
            UtilTest.printResult(expectedIsLastOfDay, transaction.isLastOfDay());

            expectedTransactionNumber++;
            expectedPositionOfDay++;
        }
    }

    @Test
    public void testDeriveLastOfDay_WhenTenUnsortedTransactions_ThenTenIsLastOfDay() {
        System.out.println("testDeriveLastOfDay_WhenTenUnsortedTransactions_ThenTenIsLastOfDay");

        // Create test data        
        List<CashTransaction> transactions = generateTenTransactions("abc", LocalDate.parse("2024-03-01"));
        TransactionParser.generateTransactionNumberAndDeriveLastOfDay(transactions);
        transactions.getLast().setLastOfDay(false);
        Collections.shuffle(transactions);

        // Perform test
        TransactionParser.deriveLastOfDay(transactions);

        // Prepare results
        boolean expected = false;
        for (CashTransaction transaction : transactions) {
            if (transaction.getPositionOfDay() == 10) {
                expected = true;
            } else {
                expected = false;
            }

            // Evaluate result
            assertEquals(expected, transaction.isLastOfDay());

            UtilTest.printResult(expected, transaction.isLastOfDay());
        }
    }

    private List<CashTransaction> generateTenTransactions(String account, LocalDate date) {
        List<CashTransaction> list = new ArrayList<>();
        int i = 0;
        while (i < 10) {
            CashTransaction transaction = CashTransactionTest.generateOneTransaction(account, date, null, null);
            list.add(transaction);
            i++;
        }
        return list;
    }

}
