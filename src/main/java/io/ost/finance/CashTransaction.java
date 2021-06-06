package io.ost.finance;

import java.util.Collection;
import java.util.HashSet;
import static io.ost.finance.TransactionManager.getTransactionManager;

/**
 * CashTransaction is parsed from one single Record in a CSV file. It checks
 * account names and numbers against the config to derive account names and
 * numbers. It also determines - by the same means - if the transaction is
 * internal, meaning moving cash between personal accounts e.g. from a savings
 * account to a running.
 *
 * TODO post process function feels more appropriate in TransactionParser, where
 * it is also initiated in the first places.
 *
 * @author joost
 */
public class CashTransaction {

    private static int count = 0;
    private final int number;
    public String label;
    public Double amount;
    /**
     * The transaction number is a unique number bound to this transaction.
     * Everytime this cash transaction is parsed this number should remain the
     * same. Then together with account number it can be used as primary key.
     */
    public int transactionNumber;
    public String date;
    public long dateUnix;
    public double accountBalance;
    public String accountNumber;
    public String accountName;
    public CreditInstitution accountInstitution;
    public String contraAccountNumber;
    public String contraAccountName;
    public Boolean internal;

    private Collection<String> originalRecord;
    public TransactionType transactionType;
    public String description;

    public CashTransaction() {
        originalRecord = new HashSet<>();
        internal = false;
        number = ++count;
    }

    public int getNumber() {
        return number;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.equals("")) {
            return;
        }
        this.accountNumber = accountNumber;
        if (Util.isMyAccountNumber(accountNumber)) {
            if (!Util.isMyAccountName(accountName)) {
                accountName = getAccountNameFrom(accountNumber);
            }
        }
        this.internal = Util.isMyAccountNumber(accountNumber) && Util.isMyAccountNumber(contraAccountNumber);
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        if (accountName.equals("")) {
            return;
        }
        this.accountName = accountName;
    }

    public double getAccountBalance() {
        return accountBalance;
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
        if (contraAccountNumber == null || contraAccountNumber.equals("")) {
            return;
        }
        this.contraAccountNumber = contraAccountNumber;
        this.internal = Util.isMyAccountNumber(accountNumber) && Util.isMyAccountNumber(contraAccountNumber);
        if (Util.isMyAccountNumber(contraAccountNumber)) {
            this.contraAccountName = getAccountNameFrom(contraAccountNumber);
        }
    }

    private String getAccountNameFrom(String accountNumber) {
        if (accountNumber == null) {
            return null;
        }
        String name = getTransactionManager().myAccounts.getProperty(accountNumber);
        if (name == null) {
            name = getTransactionManager().otherAccounts.getProperty(accountNumber);
        }
        return name;
    }

    public String getContraAccountName() {
        return contraAccountName;
    }

    public void setContraAccountName(String contraAccountName) {
        if (contraAccountName == null || contraAccountName.equals("")) {
            return;
        }
        this.contraAccountName = contraAccountName;
        if (Util.isMyAccountName(contraAccountName)) {
            this.contraAccountNumber = Util.getMyAccountNumberFrom(contraAccountName);
        }
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        setTransactionType(amount > 0 ? TransactionType.CREDIT : TransactionType.DEBIT);
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        if (date == null || date.equals("")) {
            return;
        }
        this.date = date;
    }

    public long getDateUnix() {
        return dateUnix;
    }

    public void setDateUnix(long dateUnix) {
        this.dateUnix = dateUnix;
    }

    public Boolean isInternal() {
        return internal;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        if (label == null || label.equals("")) {
            return;
        }
        this.label = label;
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
         if (description == null || description.equals("")) {
            return;
        }
        this.description = description;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    @Override
    public String toString() {
        if (transactionType == TransactionType.DEBIT) {
            return "on " + date + "\t€" + Math.abs(amount) + "\t from " + accountName + "\tto " + contraAccountName;
        }

        return "on " + date + "\t€" + Math.abs(amount) + "\t from " + contraAccountName + "\tto " + accountName;
    }

    public static String[] getHeader() {
        return new String[]{
            "label",
            "amount",
            "transactionNumber",
            "date",
            "accountBalance",
            "accountNumber",
            "accountName",
            "contraAccountNumber",
            "contraAccountName",
            "internal",
            "transactionType",
            "description",
            "accountInstitution"
        };
    }

    public String[] toRecord() {

        return new String[]{
            label,
            (amount + "").replace('.', TransactionManager.DECIMAL_SEPERATOR),
            transactionNumber + "",
            date,
            (accountBalance + "").replace('.', TransactionManager.DECIMAL_SEPERATOR),
            accountNumber,
            accountName,
            contraAccountNumber,
            contraAccountName,
            internal.toString(),
            transactionType.toString(),
            description,
            accountInstitution.toString(),};
    }

    public enum TransactionType {
        DEBIT,
        CREDIT,
    }

}
