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

    /**
     *
     * @return the date ISO format yyyy-MM-dd
     */
    public LocalDate getOldestTransactionDate() {
        // By default, TreeMap sorts all its entries according to their natural ordering
        return transactionsIndex.firstEntry().getValue().date();
    }

    /**
     *
     * @return the date ISO format yyyy-MM-dd
     */
    public LocalDate getNewestTransactionDate() {
        // By default, TreeMap sorts all its entries according to their natural ordering
        return transactionsIndex.lastEntry().getValue().date();
    }

    public Account merge(Account incoming) {
        return Account.this.merge(incoming, false);
    }

    public Account merge(Account incoming, boolean overwriteCategories) {
        return evaluateOverlapAndMerge(incoming.transactionsAscending(), overwriteCategories);
    }

    private Account evaluateOverlapAndMerge(List<CashTransaction> incoming, boolean overwriteCategories) {
        var result = new TreeMap<Integer, CashTransaction>(transactionsIndex);

        // Evaluate overlap
        LocalDate[] overlap = CashTransactionDomainLogic.findOverlap(incoming, transactionsAscending());

        // no overlap, so add all incoming
        if (overlap == null) {
            incoming.forEach(e -> result.put(e.transactionNumber(), e));

            // merge overlap
        } else {
            LocalDate from = overlap[0];
            LocalDate to = overlap[1];
            var merged = Account.this.merge(incoming, from, to, overwriteCategories);
            merged.forEach(e -> result.put(e.transactionNumber(), e));

            // finally add non-overlapping incoming transactions
            var otherIncoming = CashTransactionDomainLogic.filterByTimespanInverted(incoming, overlap[0], overlap[1]);
            otherIncoming.forEach(e -> result.put(e.transactionNumber(), e));
        }

        return new Account(this.accountNumber, result.values());
    }

    // decide which transactions to add, discard and add categories to
    private Collection<CashTransaction> merge(List<CashTransaction> incoming, LocalDate from, LocalDate to, boolean overwriteCategories) {
        var result = new TreeMap<Integer, CashTransaction>();
        var existingOverlap = CashTransactionDomainLogic.filterByTimespan(transactionsAscending(), from, to);
        var incomingOverlap = CashTransactionDomainLogic.filterByTimespan(incoming, from, to);
        List<LocalDate> range = DateUtil.dateRange(from, to);

        Map<LocalDate, List<CashTransaction>> existingByDays = CashTransactionDomainLogic.groupByDays(range, existingOverlap);
        Map<LocalDate, List<CashTransaction>> incomingByDays = CashTransactionDomainLogic.groupByDays(range, incomingOverlap);

        existingOverlap.forEach(e -> result.put(e.transactionNumber(), e));

        for (LocalDate day : range) {
            var existingDay = existingByDays.get(day);
            var incomingDay = incomingByDays.get(day);

            // if incoming transactions contains more entries, the existing ones should be replaced
            if (incomingDay.size() > existingDay.size()) {
                incomingDay.forEach(e -> result.put(e.transactionNumber(), e));
                var enriched = enrichCategories(existingDay, true); // existing categories should overwrite new categories
                enriched.forEach(e -> result.put(e.transactionNumber(), e));

            } else {
                var enriched = enrichCategories(incomingDay, overwriteCategories); // new categories should enrich existing categories
                enriched.forEach(e -> result.put(e.transactionNumber(), e));

            }
        }
        return result.values();
    }

    private List<CashTransaction> enrichCategories(List<CashTransaction> transactions, boolean overwriteCategories) {
        var result = new ArrayList<CashTransaction>();
        for (var incoming : transactions) {
            int number = incoming.transactionNumber();
            if (transactionsIndex.containsKey(number)) {
                CashTransaction existing = transactionsIndex.get(number);
                boolean isSame = CashTransactionDomainLogic.areSame(incoming, existing);
                if (isSame && incoming.category() != null && (existing.category() == null || overwriteCategories)) {
                    var updated = existing.withCategory(incoming.category());
                    result.add(updated);
                    transactionsIndex.put(number, updated);
                }

            } else {
                System.out.println("trying to enrich existing transactions with re-imported transactions, but transaction does not exist, should not be a case to right?");
                System.out.println(incoming);
                System.out.println();
            }
        }
        return result;
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
