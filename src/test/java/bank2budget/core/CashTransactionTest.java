package bank2budget.core;

import bank2budget.adapter.parser.RawCashTransaction;
import bank2budget.core.CashTransaction.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

        var transaction = new TransactionBuilder().amount(BigDecimal.valueOf(-10.)).build();

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

        var transaction = new TransactionBuilder().amount(BigDecimal.valueOf(10.)).build();

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

        var tx = newTx("2025-01-01", 1, true, 10, 10, "abc", "def", "Some category");
        var ty = newTx("2025-01-01", 1, true, 10, 10, "abc", "def", "Some category");

        boolean expected = true;
        boolean result = CashTransactionDomainLogic.areSame(tx, ty);
        assertEquals(expected, result);

        UtilTest.printResult(expected, result);
    }

    /**
     * Test of equals method, of class CashTransaction.
     */
    @Test
    public void testEquals_WhenCategoriesDiffer_ThenReturnTrue() {
        System.out.println("testEquals_WhenCategoriesDiffer_ThenReturnTrue");

        var tx = newTx("2025-01-01", 1, true, 10, 10, "abc", "def", "Some category");
        var ty = tx.withCategory("Other category");

        boolean expected = true;
        boolean result = CashTransactionDomainLogic.areSame(tx, ty);
        assertEquals(expected, result);

        UtilTest.printResult(expected, result);
    }

    /**
     * Test of equals method, of class CashTransaction.
     */
    @Test
    public void testEquals_WhenLastOfDaysDiffer_ThenReturnTrue() {
        System.out.println("testEquals_WhenLastOfDaysDiffer_ThenReturnTrue");

        var tx = newTx("2025-01-01", 1, true, 10, 10, "abc", "def", "Some category");
        var ty = tx.withLastOfDay(false);

        boolean expected = true;
        boolean result = CashTransactionDomainLogic.areSame(tx, ty);
        assertEquals(expected, result);

        UtilTest.printResult(expected, result);
    }

    public static CashTransaction newTx(String isoDate, int positionOfDay, boolean lastOfDay, double amount, double balance, String account, String contraAccount, String category) {
        LocalDate date = LocalDate.parse(isoDate);
        int txNumber = getTxNumber(date, positionOfDay);
        CashTransaction.TransactionType txType = amount > 0 ? CashTransaction.TransactionType.CREDIT : CashTransaction.TransactionType.DEBIT;

        return new CashTransaction(
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

    public static RawCashTransaction newRtx(String account, LocalDate date, String category, Double amount) {
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
