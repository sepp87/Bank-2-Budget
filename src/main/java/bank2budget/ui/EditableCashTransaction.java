package bank2budget.ui;

import bank2budget.core.CashTransaction;
import bank2budget.core.CashTransaction.TransactionType;
import bank2budget.core.CreditInstitution;
import bank2budget.core.Util;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * CashTransaction is parsed from one single Record in a CSV file. It checks
 * account names and numbers against the config to derive account names and
 * numbers. It also determines - by the same means - if the transaction is
 * internal, meaning moving cash between personal accounts e.g. from a savings
 * account to a running.
 *
 *
 * @author joost
 */
public class EditableCashTransaction {

    private CashTransaction transaction;

    public EditableCashTransaction(CashTransaction transaction) {
        this.transaction = transaction;
    }

    public EditableCashTransaction(EditableCashTransaction cashTransaction) {
        this.transaction = cashTransaction.transaction();
    }

    // SETTERS
    public void setCategory(String category) {
        transaction = transaction.withCategory(category);
    }

    public void setNotes(String notes) {
        transaction = transaction.withNotes(notes);
    }

    // GETTERS
    public CashTransaction transaction() {
        return transaction;
    }

    public String accountNumber() {
        return transaction.accountNumber();
    }

    public String accountName() {
        return transaction.accountName();
    }

    public BigDecimal accountBalance() {
        return transaction.accountBalance();
    }

    public BigDecimal accountBalanceBefore() {
        return transaction.accountBalanceBefore();
    }

    public CreditInstitution accountInstitution() {
        return transaction.accountInstitution();
    }

    public String contraAccountNumber() {
        return transaction.contraAccountNumber();
    }

    public String contraAccountName() {
        return transaction.contraAccountName();
    }

    public BigDecimal amount() {
        return transaction.amount();
    }

    public LocalDate date() {
        return transaction.date();
    }

    public boolean internal() {
        return transaction.internal();
    }

    public String category() {
        return transaction.category();
    }

    public int transactionNumber() {
        return transaction.transactionNumber();
    }

    public String description() {
        return transaction.description();
    }

    public TransactionType transactionType() {
        return transaction.transactionType();
    }

    public boolean lastOfDay() {
        return transaction.lastOfDay();
    }

    public int positionOfDay() {
        return transaction.positionOfDay();
    }

    public String notes() {
        return transaction.notes();
    }

    @Override
    public String toString() {
        String result = transaction.transactionNumber() + "\t" + transaction.lastOfDay() + "\t" + transaction.date() + "\t" + Util.padWithTabs("€" + transaction.amount(), 2) + Util.padWithTabs(transaction.accountName(), 4) + Util.padWithTabs(transaction.contraAccountName(), 4) + Util.padWithTabs(transaction.category(), 4);
        return result;
    }

}
