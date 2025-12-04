package bank2budget.adapters.parser;

import bank2budget.adapters.parser.TransactionParser;
import bank2budget.core.Account;
import bank2budget.core.AccountTest;
import bank2budget.core.CashTransaction;
import bank2budget.core.CashTransactionTest;
import bank2budget.core.UtilTest;
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
        List<RawCashTransaction> transactions = generateTenRawTransactions("abc", LocalDate.parse("2024-03-01"));

        // Perform test
        TransactionParser.generateTransactionNumberAndDeriveLastOfDay(transactions);

        // Prepare results
        int expectedTransactionNumber = 240301001;
        int expectedPositionOfDay = 1;
        boolean expectedIsLastOfDay = false;
        for (RawCashTransaction transaction : transactions) {
            if (expectedPositionOfDay == 10) {
                expectedIsLastOfDay = true;
            }

            // Evaluate result
            assertEquals(expectedTransactionNumber, transaction.transactionNumber);
            assertEquals(expectedPositionOfDay, transaction.positionOfDay);
            assertEquals(expectedIsLastOfDay, transaction.lastOfDay);

            UtilTest.printResult(expectedTransactionNumber, transaction.transactionNumber);
            UtilTest.printResult(expectedPositionOfDay, transaction.positionOfDay);
            UtilTest.printResult(expectedIsLastOfDay, transaction.lastOfDay);

            expectedTransactionNumber++;
            expectedPositionOfDay++;
        }
    }

//      TEST has become obsolete since, last of day is loaded directly from transactions xlsx    
//    @Test 
//    public void testDeriveLastOfDay_WhenTenUnsortedTransactions_ThenTenIsLastOfDay() {
//        System.out.println("testDeriveLastOfDay_WhenTenUnsortedTransactions_ThenTenIsLastOfDay");
//
//        // Create test data        
//        List<RawCashTransaction> transactions = generateTenRawTransactions("abc", LocalDate.parse("2024-03-01"));
//        TransactionParser.generateTransactionNumberAndDeriveLastOfDay(transactions);
//        transactions.getLast().setLastOfDay(false);
//        Collections.shuffle(transactions);
//
//        // Perform test
//        TransactionParser.deriveLastOfDay(transactions);
//
//        // Prepare results
//        boolean expected = false;
//        for (CashTransaction transaction : transactions) {
//            if (transaction.getPositionOfDay() == 10) {
//                expected = true;
//            } else {
//                expected = false;
//            }
//
//            // Evaluate result
//            assertEquals(expected, transaction.isLastOfDay());
//
//            UtilTest.printResult(expected, transaction.isLastOfDay());
//        }
//    }

    private List<RawCashTransaction> generateTenRawTransactions(String account, LocalDate date) {
        List<RawCashTransaction> list = new ArrayList<>();
        int i = 0;
        while (i < 10) {
            RawCashTransaction transaction = CashTransactionTest.generateOneRawTransaction(account, date, null, null);
            list.add(transaction);
            i++;
        }
        return list;
    }

}
