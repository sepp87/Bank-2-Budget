/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package io.ost.finance;

import io.ost.finance.CashTransaction.TransactionType;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
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
public class CashTransactionTest {

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
    }

    /**
     * Test of setAmount method, of class CashTransaction.
     */
    @Test
    public void testSetAmount_WhenAmountPositive_ThenTransactionTypeCredit() {
        System.out.println("testSetAmount_WhenAmountPositive_ThenTransactionTypeCredit");

        CashTransaction transaction = new CashTransaction();
        transaction.setAmount(10.);

        TransactionType expResult = TransactionType.CREDIT;
        TransactionType result = transaction.getTransactionType();
        assertEquals(expResult, result);
    }

    /**
     * Test of equals method, of class CashTransaction.
     */
    @Test
    public void testEquals_WhenExactlyTheSame_ThenReturnTrue() {
        System.out.println("testEquals_WhenExactlyTheSame_ThenReturnTrue");
        CashTransaction transaction = generateOneTransaction("abc", LocalDate.now());
        CashTransaction duplicate = new CashTransaction(transaction);
        boolean expected = true;
        boolean result = transaction.equals(duplicate);
        assertEquals(expected, result);
    }

    /**
     * Test of equals method, of class CashTransaction.
     */
    @Test
    public void testEquals_WhenLabelsDiffer_ThenReturnTrue() {
        System.out.println("testEquals_WhenLabelsDiffer_ThenReturnTrue");
        CashTransaction transaction = generateOneTransaction("abc", LocalDate.now());
        CashTransaction duplicate = new CashTransaction(transaction);
        duplicate.setLabel("something");
        boolean expected = true;
        boolean result = transaction.equals(duplicate);
        assertEquals(expected, result);
    }

    public static CashTransaction generateOneTransaction(String account, LocalDate date) {
        CashTransaction transaction = new CashTransaction();
        transaction.setAccountNumber(account);
        transaction.setAccountName(account);
        transaction.setDate(date.toString());
        transaction.setAmount(Math.floor(ThreadLocalRandom.current().nextDouble(-100, 100) * 100)/100);
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
