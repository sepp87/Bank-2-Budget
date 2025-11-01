package bank2budget.core;

import bank2budget.core.CashTransaction;
import bank2budget.core.CashTransaction.TransactionType;
import bank2budget.adapters.parser.TransactionParser;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author joost
 */
public class CashTransactionTest {

    @org.junit.jupiter.api.BeforeAll
    @org.junit.jupiter.api.AfterAll
    public static void printLine() {
        System.out.println();
    }

    /**
     * Test of setAmount method, of class CashTransaction.
     */
    @Test
    public void testSetAmount_WhenAmountNegative_ThenTransactionTypeDebit() {
        System.out.println("testSetAmount_WhenAmountNegative_ThenTransactionTypeDebit");

        CashTransaction transaction = new CashTransaction();
        transaction.setAmount(-10.);

        TransactionType expected = TransactionType.DEBIT;
        TransactionType result = transaction.getTransactionType();
        assertEquals(expected, result);

        UtilTest.printResult(expected, result);
    }

    /**
     * Test of setAmount method, of class CashTransaction.
     */
    @Test
    public void testSetAmount_WhenAmountPositive_ThenTransactionTypeCredit() {
        System.out.println("testSetAmount_WhenAmountPositive_ThenTransactionTypeCredit");

        CashTransaction transaction = new CashTransaction();
        transaction.setAmount(10.);

        TransactionType expected = TransactionType.CREDIT;
        TransactionType result = transaction.getTransactionType();
        assertEquals(expected, result);

        UtilTest.printResult(expected, result);
    }

    /**
     * Test of equals method, of class CashTransaction.
     */
    @Test
    public void testEquals_WhenExactlyTheSame_ThenReturnTrue() {
        System.out.println("testEquals_WhenExactlyTheSame_ThenReturnTrue");

        CashTransaction transaction = generateOneTransaction("abc", LocalDate.now(), "Some label", null);
        CashTransaction duplicate = new CashTransaction(transaction);

        boolean expected = true;
        boolean result = transaction.equals(duplicate);
        assertEquals(expected, result);

        UtilTest.printResult(expected, result);
    }

    /**
     * Test of equals method, of class CashTransaction.
     */
    @Test
    public void testEquals_WhenLabelsDiffer_ThenReturnTrue() {
        System.out.println("testEquals_WhenLabelsDiffer_ThenReturnTrue");

        CashTransaction transaction = generateOneTransaction("abc", LocalDate.now(), "Some label", null);
        CashTransaction duplicate = new CashTransaction(transaction);
        duplicate.setLabel("Other label");

        boolean expected = true;
        boolean result = transaction.equals(duplicate);
        assertEquals(expected, result);

        UtilTest.printResult(expected, result);
    }

    /**
     * Test of equals method, of class CashTransaction.
     */
    @Test
    public void testEquals_WhenLastOfDaysDiffer_ThenReturnTrue() {
        System.out.println("testEquals_WhenLastOfDaysDiffer_ThenReturnTrue");

        CashTransaction transaction = generateOneTransaction("abc", LocalDate.now(), "Some label", null);
        transaction.setLastOfDay(true);
        CashTransaction duplicate = new CashTransaction(transaction);
        duplicate.setLastOfDay(false);

        boolean expected = true;
        boolean result = transaction.equals(duplicate);
        assertEquals(expected, result);

        UtilTest.printResult(expected, result);
    }

    public static List<CashTransaction> generateTransactionsForAccountWithinTimespan(String account, LocalDate from, LocalDate to, String label) {
        return generateTransactionsForAccountWithinTimespan(account, from, to, label, null);
    }

    public static List<CashTransaction> generateTransactionsForAccountWithinTimespan(String account, LocalDate from, LocalDate to, String label, Double amount) {
        List<LocalDate> dates = generateDatesForTimespan(from, to);
        List<CashTransaction> transactions = generateTransactionforEachDate(account, dates, label, amount);
        return transactions;
    }

    private static List<LocalDate> generateDatesForTimespan(LocalDate from, LocalDate to) {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate nextDate = from;
        while (nextDate.isBefore(to)) {
            dates.add(nextDate);
            nextDate = nextDate.plusDays(1);
        }
        dates.add(to);
        return dates;
    }

    private static List<CashTransaction> generateTransactionforEachDate(String account, List<LocalDate> dates, String label, Double amount) {
        List<CashTransaction> transactions = new ArrayList<>();
        for (LocalDate date : dates) {
            CashTransaction transaction = CashTransactionTest.generateOneTransaction(account, date, label, amount);
            transactions.add(transaction);
        }
        TransactionParser.generateTransactionNumberAndDeriveLastOfDay(transactions);
        return transactions;
    }

    public static CashTransaction generateOneTransaction(String account, LocalDate date, String label, Double amount) {
        CashTransaction transaction = new CashTransaction();
        transaction.setAccountNumber(account);
        transaction.setAccountName(account);
        transaction.setDate(date);
        transaction.setLabel(label);
        transaction.setAccountBalance(0);
        if (amount == null) {
            transaction.setAmount(Math.floor(ThreadLocalRandom.current().nextDouble(-100, 100) * 100) / 100);
        } else {
            transaction.setAmount(amount);
        }
        int expenditure = ThreadLocalRandom.current().nextInt(0, SAMPLE_EXPENDITURES.length - 1);
        transaction.setContraAccountName(SAMPLE_EXPENDITURES[expenditure]);
        return transaction;
    }

    private static final String[] SAMPLE_EXPENDITURES = {
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
