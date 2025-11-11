package bank2budget.core;

import java.util.Collection;
import java.util.HashSet;
import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

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
        if (accountNumber.equals("")) {
            accountNumber = null;
        }
        this.accountNumber = accountNumber;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        if (accountName.equals("")) {
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
        if (contraAccountNumber.equals("")) {
            contraAccountNumber = null;
        }
        this.contraAccountNumber = contraAccountNumber;
    }

    public String getContraAccountName() {
        return contraAccountName;
    }

    public void setContraAccountName(String contraAccountName) {
        if (contraAccountName.equals("")) {
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
        if (date == null || date.equals("")) {
            return;
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
        if (category == null || category.equals("")) {
            return;
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
        if(notes.equals("")) {
            notes = null;
        }
        this.notes = notes;
    }

    /**
     * When comparing transactions, category and lastOfDay is not compared
     *
     * @param transaction
     * @return
     */
    public boolean equals(CashTransaction transaction) {
        Set<Boolean> result = new HashSet<>();
        result.add(Util.compareMoney(this.amount, transaction.amount));
        result.add(this.transactionNumber == transaction.transactionNumber);
        result.add((this.date == null ? transaction.date == null : this.date.equals(transaction.date)));
        result.add(Util.compareMoney(this.accountBalance, transaction.accountBalance));
        result.add((this.accountNumber == null ? transaction.accountNumber == null : this.accountNumber.equals(transaction.accountNumber)));
        result.add((this.accountName == null ? transaction.accountName == null : this.accountName.equals(transaction.accountName)));
        result.add(this.accountInstitution == transaction.accountInstitution);
        result.add((this.contraAccountNumber == null ? transaction.contraAccountNumber == null : this.contraAccountNumber.equals(transaction.contraAccountNumber)));
        result.add((this.contraAccountName == null ? transaction.contraAccountName == null : this.contraAccountName.equals(transaction.contraAccountName)));
        result.add(Objects.equals(this.internal, transaction.internal));
        result.add(this.positionOfDay == transaction.positionOfDay);
        result.add(this.transactionType == transaction.transactionType);
        result.add((this.description == null ? transaction.description == null : this.description.equals(transaction.description)));

//        System.out.println(Util.compareMoney(this.amount, transaction.amount));
//        System.out.println(this.transactionNumber == transaction.transactionNumber);
//        System.out.println((this.date == null ? transaction.date == null : this.date.equals(transaction.date)));
//        System.out.println(Util.compareMoney(this.accountBalance, transaction.accountBalance) + " " + this.accountBalance + " " + transaction.accountBalance);
//        System.out.println((this.accountNumber == null ? transaction.accountNumber == null : this.accountNumber.equals(transaction.accountNumber)));
//        System.out.println((this.accountName == null ? transaction.accountName == null : this.accountName.equals(transaction.accountName)));
//        System.out.println(this.accountInstitution == transaction.accountInstitution);
//        System.out.println((this.contraAccountNumber == null ? transaction.contraAccountNumber == null : this.contraAccountNumber.equals(transaction.contraAccountNumber)));
//        System.out.println((this.contraAccountName == null ? transaction.contraAccountName == null : this.contraAccountName.equals(transaction.contraAccountName)));
//        System.out.println(Objects.equals(this.internal, transaction.internal));
//        System.out.println(this.positionOfDay == transaction.positionOfDay);
//        System.out.println(this.transactionType == transaction.transactionType);
//        System.out.println((this.description == null ? transaction.description == null : this.description.equals(transaction.description)));
        return !result.contains(false);
    }

    @Override
    public String toString() {
        String result = transactionNumber + "\t" + lastOfDay + "\t" + date + "\t" + Util.padWithTabs("€" + amount, 2) + Util.padWithTabs(accountName, 4) + Util.padWithTabs(contraAccountName, 4) + Util.padWithTabs(category, 4);
//        String result = date + "\t" + Util.padWithTabs("€" + amount, 3) + Util.padWithTabs(accountName, 4) + Util.padWithTabs(contraAccountName, 5) + Util.padWithTabs(category, 4);
        return result;
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

    public static List<CashTransaction> filterByTimespan(List<CashTransaction> transactions, LocalDate from, LocalDate to) {
        return filterByTimespan(transactions, from, to, false);
    }

    public static List<CashTransaction> filterByTimespan(List<CashTransaction> transactions, LocalDate from, LocalDate to, boolean inverted) {
        List<CashTransaction> result = new ArrayList<>();
        from = from.minusDays(1);
        to = to.plusDays(1);
        for (CashTransaction transaction : transactions) {
            LocalDate date = transaction.getDate();
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
            result.add(transaction.getDate());
        }
        return result;
    }

    public enum TransactionType {
        DEBIT,
        CREDIT
    }
}
