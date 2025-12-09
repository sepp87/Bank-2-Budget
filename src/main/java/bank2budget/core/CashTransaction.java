package bank2budget.core;

import bank2budget.core.Transaction.TransactionType;
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
public class CashTransaction {

    private Transaction transaction;

    public CashTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public CashTransaction(CashTransaction cashTransaction) {
        this.transaction = cashTransaction.getTransaction();
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public String accountNumber() {
        return transaction.accountNumber();
    }

    public String accountName() {
        return transaction.accountName();
    }

    public void setAccountName(String name) {
        transaction = transaction.withAccountName(name);

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

    public void setContraAccountName(String name) {
        transaction = transaction.withContraAccountName(name);
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

    public void setInternal(boolean internal) {
        transaction = transaction.withInternal(internal);
    }

    public String category() {
        return transaction.category();
    }

    public void setCategory(String category) {
        transaction = transaction.withCategory(category);
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

    public void setLastOfDay(Boolean lastOfDay) {
        transaction = transaction.withLastOfDay(lastOfDay);
    }

    public int positionOfDay() {
        return transaction.positionOfDay();
    }

    public String notes() {
        return transaction.notes();
    }

    public void setNotes(String notes) {
        transaction = transaction.withNotes(notes);
    }

    @Override
    public String toString() {
        String result = transaction.transactionNumber() + "\t" + transaction.lastOfDay() + "\t" + transaction.date() + "\t" + Util.padWithTabs("€" + transaction.amount(), 2) + Util.padWithTabs(transaction.accountName(), 4) + Util.padWithTabs(transaction.contraAccountName(), 4) + Util.padWithTabs(transaction.category(), 4);
        return result;
    }

}
