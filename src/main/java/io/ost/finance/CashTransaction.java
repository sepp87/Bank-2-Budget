package io.ost.finance;

import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import static io.ost.finance.App.get;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
//    private final int number;
    public String label;
    public double amount;
    /**
     * The transaction number is a unique number bound to this transaction.
     * Every time this cash transaction is parsed this number should remain the
     * same. Then together with account number it can be used as primary key.
     */
    public int transactionNumber;
    public String date;
    public long dateUnix;
    public Double accountBalance;
    public String accountNumber;
    public String accountName;
    public CreditInstitution accountInstitution;
    public String contraAccountNumber;
    public String contraAccountName;
    public Boolean internal;

    private Boolean lastOfDay;
    private int positionOfDay;

    private Collection<String> originalRecord;
    public TransactionType transactionType;
    public String description;

    public CashTransaction() {
        originalRecord = new HashSet<>();
        internal = false;
//        number = ++count;
    }

//    public int getNumber() {
//        return number;
//    }
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
//                String name = getAccountNameFrom(accountNumber);
//                setAccountName(name);
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
        String name = get().myAccounts.getProperty(accountNumber);
        if (name == null) {
            name = get().otherAccounts.getProperty(accountNumber);
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

    /**
     *
     * @return the date ISO format yyyy-MM-dd
     */
    public String getDate() {
        return date;
    }

    /**
     *
     * @param date the date ISO format yyyy-MM-dd
     */
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

    @Override
    public String toString() {
        if (transactionType == TransactionType.DEBIT) {
            return "on " + date + "\t€" + Math.abs(amount) + "\t from " + accountName + "\tto " + contraAccountName + "\tlabeled " + label;
        }

        return "on " + date + "\t€" + Math.abs(amount) + "\t from " + contraAccountName + "\tto " + accountName + "\tlabeled " + label;
    }

    public static String[] getHeader() {
        return new String[]{
            "label",
            "amount",
            "transactionNumber",
            "date",
            "accountBalance",
            "accountInstitution",
            "accountNumber",
            "accountName",
            "contraAccountNumber",
            "contraAccountName",
            "internal",
            "transactionType",
            "description"
        };
    }

    public String[] toRecord() {
        Object[] values = toObjectRecord();
        String[] stringValues = new String[values.length];
        int i = 0;
        for (Object value : values) {
            stringValues[i] = valueToString(value);
            i++;
        }
        return stringValues;
    }

    public Object[] toObjectRecord() {
        String[] header = getHeader();
        Object[] values = new Object[header.length];
        try {
            int i = 0;
            for (String column : header) {
                Object value = CashTransaction.class.getField(column).get(this);
                values[i] = value;
                i++;
            }
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            Logger.getLogger(CashTransaction.class.getName()).log(Level.SEVERE, null, ex);
        }
        return values;
    }

    private String valueToString(Object value) {
        if (value == null) {
            return null;
        } else if (value instanceof String) {
            return value.toString();
        } else if (value instanceof Number) {
            return (value + "").replace('.', Config.getDecimalSeperator());
        }
        return value.toString();
    }

    public enum TransactionType {
        DEBIT,
        CREDIT,
    }

    public static List<CashTransaction> sortAscending(List<CashTransaction> transactions) {
        int n = transactions.size();
        CashTransaction temp = null;
        for (int i = 0; i < n; i++) {
            for (int j = 1; j < (n - i); j++) {
                if (transactions.get(j - 1).transactionNumber > transactions.get(j).transactionNumber) {
                    //swap elements  
                    temp = transactions.get(j - 1);
                    transactions.set(j - 1, transactions.get(j));
                    transactions.set(j, temp);
                }
            }
        }
        return transactions;
    }

    /**
     *
     * @param transactions
     * @param from
     * @param to
     * @return
     */
    public static List<CashTransaction> filterByTimespan(List<CashTransaction> transactions, LocalDate from, LocalDate to) {
        return filterByTimespan(transactions, from, to, false);
    }

    /**
     *
     * @param transactions
     * @param from
     * @param to
     * @param inverted
     * @return
     */
    public static List<CashTransaction> filterByTimespan(List<CashTransaction> transactions, LocalDate from, LocalDate to, boolean inverted) {
        List<CashTransaction> result = new ArrayList<>();
        from = from.minusDays(1);
        to = to.plusDays(1);
        for (CashTransaction transaction : transactions) {
            LocalDate date = LocalDate.parse(transaction.getDate());
            boolean withinTimespan = date.isAfter(from) && date.isBefore(to);
            // Adding or excluding transactions based on 'inverted' flag and whether they are within the time span
            if ((withinTimespan && !inverted) || (!withinTimespan && inverted)) {
                result.add(transaction);
            }
        }
        return result;
    }

    /**
     *
     * @param list1 sorted list in ascending order by date
     * @param list2 sorted list in ascending order by date
     * @return
     */
    public static LocalDate[] findOverlap(List<CashTransaction> list1, List<CashTransaction> list2) {
        List<LocalDate> dates1 = getDatesFrom(list1);
        List<LocalDate> dates2 = getDatesFrom(list2);
        return Util.findOverlap(dates1, dates2);
    }

    private static List<LocalDate> getDatesFrom(List<CashTransaction> list) {
        List<LocalDate> result = new ArrayList<>();
        for (CashTransaction transaction : list) {
            LocalDate date = LocalDate.parse(transaction.getDate());
            result.add(date);
        }
        return result;
    }

}
