package bank2budget.core;

import bank2budget.adapters.parser.RawCashTransaction;
import bank2budget.core.CashTransaction;
import bank2budget.adapters.parser.TransactionParser;
import bank2budget.core.Transaction.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
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

        CashTransaction transaction = new CashTransaction(new TransactionBuilder().amount(BigDecimal.valueOf(-10.)).build());

        TransactionType expected = TransactionType.DEBIT;
        TransactionType result = transaction.transactionType();
        assertEquals(expected, result);

        UtilTest.printResult(expected, result);
    }

    /**
     * Test of setAmount method, of class CashTransaction.
     */
    @Test
    public void testSetAmount_WhenAmountPositive_ThenTransactionTypeCredit() {
        System.out.println("testSetAmount_WhenAmountPositive_ThenTransactionTypeCredit");

        CashTransaction transaction = new CashTransaction(new TransactionBuilder().amount(BigDecimal.valueOf(10.)).build());

        TransactionType expected = TransactionType.CREDIT;
        TransactionType result = transaction.transactionType();
        assertEquals(expected, result);

        UtilTest.printResult(expected, result);
    }

    /**
     * Test of equals method, of class CashTransaction.
     */
    @Test
    public void testEquals_WhenExactlyTheSame_ThenReturnTrue() {
        System.out.println("testEquals_WhenExactlyTheSame_ThenReturnTrue");

        CashTransaction transaction = generateOneTransaction("abc", LocalDate.now(), "Some category", null);
        CashTransaction duplicate = new CashTransaction(transaction);

        boolean expected = true;
        boolean result = CashTransactionDomainLogic.areSame(transaction, duplicate);
        assertEquals(expected, result);

        UtilTest.printResult(expected, result);
    }

    /**
     * Test of equals method, of class CashTransaction.
     */
    @Test
    public void testEquals_WhenCategoriesDiffer_ThenReturnTrue() {
        System.out.println("testEquals_WhenCategoriesDiffer_ThenReturnTrue");

        CashTransaction transaction = generateOneTransaction("abc", LocalDate.now(), "Some category", null);
        CashTransaction duplicate = new CashTransaction(transaction);
        duplicate.setCategory("Other category");

        boolean expected = true;
        boolean result = CashTransactionDomainLogic.areSame(transaction, duplicate);
        assertEquals(expected, result);

        UtilTest.printResult(expected, result);
    }

    /**
     * Test of equals method, of class CashTransaction.
     */
    @Test
    public void testEquals_WhenLastOfDaysDiffer_ThenReturnTrue() {
        System.out.println("testEquals_WhenLastOfDaysDiffer_ThenReturnTrue");

        CashTransaction transaction = generateOneTransaction("abc", LocalDate.now(), "Some category", null);
        transaction.setLastOfDay(true);
        CashTransaction duplicate = new CashTransaction(transaction);
        duplicate.setLastOfDay(false);

        boolean expected = true;
        boolean result = CashTransactionDomainLogic.areSame(transaction, duplicate);
        assertEquals(expected, result);

        UtilTest.printResult(expected, result);
    }

    public static List<CashTransaction> generateTransactionsForAccountWithinTimespan(String account, LocalDate from, LocalDate to, String category) {
        return generateTransactionsForAccountWithinTimespan(account, from, to, category, null);
    }

    public static List<CashTransaction> generateTransactionsForAccountWithinTimespan(String account, LocalDate from, LocalDate to, String category, Double amount) {
        List<LocalDate> dates = generateDatesForTimespan(from, to);
        List<CashTransaction> transactions = generateTransactionforEachDate(account, dates, category, amount);
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

    private static List<CashTransaction> generateTransactionforEachDate(String account, List<LocalDate> dates, String category, Double amount) {
        List<RawCashTransaction> transactions = new ArrayList<>();
        for (LocalDate date : dates) {
            RawCashTransaction transaction = CashTransactionTest.generateOneRawTransaction(account, date, category, amount);
            transactions.add(transaction);
        }
        TransactionParser.generateTransactionNumberAndDeriveLastOfDay(transactions);
        return transactions.stream().map(RawCashTransaction::toCashTransaction).collect(Collectors.toCollection(ArrayList::new));
    }

    public static CashTransaction generateOneTransaction(String account, LocalDate date, String category, Double amount) {
        return generateOneRawTransaction(account, date, category, amount).toCashTransaction();
    }

    public static RawCashTransaction generateOneRawTransaction(String account, LocalDate date, String category, Double amount) {
        RawCashTransaction raw = new RawCashTransaction();
        raw.accountNumber = account;
        raw.accountName = account;
        raw.date = date;
        raw.category = category;
        raw.accountBalance = BigDecimal.ZERO;
        if (amount == null) {
            raw.amount = BigDecimal.valueOf(ThreadLocalRandom.current().nextDouble(-100, 100));
        } else {
            raw.amount = BigDecimal.valueOf(amount);
        }
        int expenditure = ThreadLocalRandom.current().nextInt(0, SAMPLE_EXPENDITURES.length - 1);
        raw.contraAccountName = SAMPLE_EXPENDITURES[expenditure];
        return raw;
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
