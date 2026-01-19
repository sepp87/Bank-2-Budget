package bank2budget.core;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author joost
 */
public class Account {

    private final String accountNumber;
    
    // By default, TreeMap sorts all its entries according to their natural ordering; meaning ascending by transaction number
    private final TreeMap<Integer, CashTransaction> transactionsIndex = new TreeMap<>(); 

    public Account(String accountNumber, Collection<CashTransaction> transactions) {
        this.accountNumber = accountNumber;
        for (var t : transactions) {
            transactionsIndex.put(t.transactionNumber(), t);
        }
    }
    
    public BigDecimal getCurrentBalance() {
        return transactionsIndex.lastEntry().getValue().accountBalance();
    }
    
    public BigDecimal getOpeningBalance() {
        return transactionsIndex.firstEntry().getValue().accountBalanceBefore();
    }

    public BigDecimal getOpeningBalanceOn(LocalDate date) {
        int boundary = lowerDayBoundary(date);
        var transaction = transactionsIndex.ceilingEntry(boundary);

        /**
         * if null there were no new transactions after the specified date. In
         * this case the most current transaction is the opening balance.
         */
        return transaction == null ? getCurrentBalance() : transaction.getValue().accountBalanceBefore();
    }

    private int lowerDayBoundary(LocalDate date) {
        return getDayBoundary(date, false);
    }

    public BigDecimal getClosingBalanceOn(LocalDate date) {
        int boundary = upperDayBoundary(date);
        var transaction = transactionsIndex.floorEntry(boundary);
        return transaction == null ? BigDecimal.ZERO : transaction.getValue().accountBalance();
    }

    private int upperDayBoundary(LocalDate date) {
        return getDayBoundary(date, true);
    }

    private int getDayBoundary(LocalDate date, boolean isUpper) {
        // turns the date into a fictive transaction number on the next day e.g. 2025-01-01 to 250102000
        // since transaction numbers never end with a zero, this boundary will yield the last transaction of the given date e.g. 250101007
        date = isUpper ? date.plusDays(1) : date;
        String raw = date.format(DateTimeFormatter.BASIC_ISO_DATE).substring(2) + "000";
        return Integer.parseInt(raw);
    }

    public List<CashTransaction> getTransactions(LocalDate from, LocalDate to) {
        List<CashTransaction> transactions = transactionsAscending();
        List<CashTransaction> result = CashTransactionDomainLogic.filterByTimespan(transactions, from, to);
        return result;
    }

    public CashTransaction getTransactionBy(int transactionNumber) {
        return transactionsIndex.get(transactionNumber);
    }


    public LocalDate getOldestTransactionDate() {
        return transactionsIndex.firstEntry().getValue().date();
    }

    public LocalDate getNewestTransactionDate() {
        return transactionsIndex.lastEntry().getValue().date();
    }

    /**
     *
     * @return list containing all transactions of this account, sorted in
     * ascending order, starting from oldest to most current
     */
    public List<CashTransaction> transactionsAscending() {
        return transactionsIndex.values().stream().toList();
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public Account withUpdatedTransactions(List<CashTransaction> transactions) {
        Map<Integer, CashTransaction> result = new TreeMap<>(transactionsIndex);
        for (var updated : transactions) {
            var replaced = result.put(updated.transactionNumber(), updated);
            if (replaced == null) {
                System.out.println("ERROR REPLACED WAS NOT AVAILABLE");
            }
        }
        return new Account(accountNumber, result.values());
    }


}
