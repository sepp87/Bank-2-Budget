package bank2budget.core;

import java.util.Collection;
import java.util.HashSet;
import java.io.File;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;

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

    private static int count = 0;
    private String category;
    private double amount;
    /**
     * The transaction number is a unique number bound to this transaction.
     * Every time this cash transaction is parsed this number should remain the
     * same. Then together with account number it can be used as primary key.
     */
    private int transactionNumber;
    private LocalDate date;
    private Double accountBalance;
    private String accountNumber;
    private String accountName;
    private CreditInstitution accountInstitution;
    private String contraAccountNumber;
    private String contraAccountName;
    private boolean internal;

    private boolean lastOfDay;
    private int positionOfDay;

    private Collection<String> originalRecord;
    private TransactionType transactionType;
    private String description;
    private transient File fileOrigin;
    private String notes;

    public CashTransaction(CashTransaction transaction) {
        this();
        this.category = transaction.category;
        this.amount = transaction.amount;
        this.transactionNumber = transaction.transactionNumber;
        this.date = transaction.date;
        this.accountBalance = transaction.accountBalance;
        this.accountNumber = transaction.accountNumber;
        this.accountName = transaction.accountName;
        this.accountInstitution = transaction.accountInstitution;
        this.contraAccountNumber = transaction.contraAccountNumber;
        this.contraAccountName = transaction.contraAccountName;
        this.internal = transaction.internal;
        this.lastOfDay = transaction.lastOfDay;
        this.positionOfDay = transaction.positionOfDay;
        this.originalRecord.addAll(transaction.originalRecord);
        this.transactionType = transaction.transactionType;
        this.description = transaction.description;
    }

    public CashTransaction() {
        originalRecord = new HashSet<>();
        internal = false;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        if ("".equals(accountNumber)) {
            accountNumber = null;
        }
        this.accountNumber = accountNumber;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        if ("".equals(accountName)) {
            accountName = null;
        }
        this.accountName = accountName;
    }

    public double getAccountBalance() {
        return accountBalance;
    }

    public double getAccountBalanceBefore() {
        return accountBalance - amount;
    }

    public void setAccountBalance(double accountBalance) {
        this.accountBalance = accountBalance;
    }

    public CreditInstitution getAccountInstitution() {
        return accountInstitution;
    }

    public void setAccountInstitution(CreditInstitution accountInstitution) {
        this.accountInstitution = accountInstitution;
    }

    public String getContraAccountNumber() {
        return contraAccountNumber;
    }

    public void setContraAccountNumber(String contraAccountNumber) {
        if ("".equals(contraAccountNumber)) {
            contraAccountNumber = null;
        }
        this.contraAccountNumber = contraAccountNumber;
    }

    public String getContraAccountName() {
        return contraAccountName;
    }

    public void setContraAccountName(String contraAccountName) {
        if ("".equals(contraAccountName)) {
            contraAccountName = null;
        }
        this.contraAccountName = contraAccountName;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        setTransactionType(amount > 0 ? TransactionType.CREDIT : TransactionType.DEBIT);
        this.amount = amount;
    }

    /**
     *
     * @return the date ISO format yyyy-MM-dd
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     *
     * @param date the date ISO format yyyy-MM-dd
     */
    public void setDate(LocalDate date) {
        if ("".equals(date)) {
            date = null;
        }
        this.date = date;
    }

    public Boolean isInternal() {
        return internal;
    }

    public void setInternal(boolean internal) {
        this.internal = internal;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        if ("".equals(category)) {
            category = null;
        }
        this.category = category;
    }

    public Collection<String> getOriginalRecord() {
        return originalRecord;
    }

    public void setOriginalRecord(Collection<String> originalRecord) {
        this.originalRecord = originalRecord;
    }

    public int getTransactionNumber() {
        return transactionNumber;
    }

    public void setTransactionNumber(int transactionNumber) {
        this.transactionNumber = transactionNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        if ("".equals(description)) {
            description = null;
        }
        this.description = description;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public Boolean isLastOfDay() {
        return lastOfDay;
    }

    public void setLastOfDay(Boolean lastOfDay) {
        this.lastOfDay = lastOfDay;
    }

    public int getPositionOfDay() {
        return positionOfDay;
    }

    public void setPositionOfDay(int positionOfDay) {
        this.positionOfDay = positionOfDay;
    }

    public File getFileOrigin() {
        return fileOrigin;
    }

    public void setFileOrigin(File file) {
        fileOrigin = file;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        if ("".equals(notes)) {
            notes = null;
        }
        this.notes = notes;
    }

    @Override
    public String toString() {
        String result = transactionNumber + "\t" + lastOfDay + "\t" + date + "\t" + Util.padWithTabs("€" + amount, 2) + Util.padWithTabs(accountName, 4) + Util.padWithTabs(contraAccountName, 4) + Util.padWithTabs(category, 4);
//        String result = date + "\t" + Util.padWithTabs("€" + amount, 3) + Util.padWithTabs(accountName, 4) + Util.padWithTabs(contraAccountName, 5) + Util.padWithTabs(category, 4);
        return result;
    }

    public enum TransactionType {
        DEBIT,
        CREDIT
    }
}
