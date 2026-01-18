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

    public void merge(Account incoming) {
        merge(incoming, false);
    }

    public void merge(Account incoming, boolean overwriteCategories) {
        evaluateOverlapAndMerge(incoming.transactionsAscending(), overwriteCategories);
    }

    private void evaluateOverlapAndMerge(List<CashTransaction> incoming, boolean overwriteCategories) {
        // Evaluate overlap
        LocalDate[] overlap = CashTransactionDomainLogic.findOverlap(incoming, transactionsAscending());

        // no overlap, so add all incoming
        if (overlap == null) {
            addTransactions(incoming);

            // merge overlap
        } else {
            LocalDate from = overlap[0];
            LocalDate to = overlap[1];
            merge(incoming, from, to, overwriteCategories);

            // finally add non-overlapping incoming transactions
            List<CashTransaction> otherIncoming = CashTransactionDomainLogic.filterByTimespanInverted(incoming, overlap[0], overlap[1]);
            addTransactions(otherIncoming);
        }
    }

    // decide which transactions to add, discard and add categories to
    private void merge(List<CashTransaction> incoming, LocalDate from, LocalDate to, boolean overwriteCategories) {
        var existingOverlap = getTransactions(from, to);
        var incomingOverlap = CashTransactionDomainLogic.filterByTimespan(incoming, from, to);
        List<LocalDate> range = DateUtil.dateRange(from, to);

        Map<LocalDate, List<CashTransaction>> existingByDays = groupByDays(range, existingOverlap);
        Map<LocalDate, List<CashTransaction>> incomingByDays = groupByDays(range, incomingOverlap);

        for (LocalDate day : range) {
            var existingDay = existingByDays.get(day);
            var incomingDay = incomingByDays.get(day);

            // if incoming transactions contains more entries, the existing ones should be replaced
            if (incomingDay.size() > existingDay.size()) {
                removeTransactions(existingDay);
                addTransactions(incomingDay);
                enrichCategories(existingDay, true); // existing categories should overwrite new categories

            } else {
                enrichCategories(incomingDay, overwriteCategories); // new categories should enrich existing categories
            }
        }
    }

    private Map<LocalDate, List<CashTransaction>> groupByDays(List<LocalDate> range, List<CashTransaction> transactions) {
        Map<LocalDate, List<CashTransaction>> result = new TreeMap<>();
        for (LocalDate date : range) {
            result.put(date, new ArrayList<>());
        }
        transactions.stream().forEach(e -> result.get(e.date()).add(e));
        return result;
    }

    private void addTransactions(List<CashTransaction> transactions) {
        for (var transaction : transactions) {
            transactionsIndex.put(transaction.transactionNumber(), transaction);
        }
    }

    private void removeTransactions(List<CashTransaction> transactions) {
        for (var transaction : transactions) {
            int number = transaction.transactionNumber();
            transactionsIndex.remove(number);
        }
    }

    private void enrichCategories(List<CashTransaction> transactions, boolean overwriteCategories) {
        for (var incoming : transactions) {
            int number = incoming.transactionNumber();
            if (transactionsIndex.containsKey(number)) {
                CashTransaction existing = transactionsIndex.get(number);
                boolean isSame = CashTransactionDomainLogic.areSame(incoming, existing);
                if (isSame && incoming.category() != null && (existing.category() == null || overwriteCategories)) {
                    var updated = existing.withCategory(incoming.category());
                    transactionsIndex.put(number, updated);
//                Logger.getLogger(Account.class.getName()).log(Level.INFO, "Transaction numbers {0} matched, please check if NOT duplicate: \n\t{1}\n\t{2}\n", new Object[]{transaction.transactionNumber, indexed.toString(), transaction.toString()});
                }
//                else {
//                    System.out.println("isSame " + isSame + "   transaction.getCategory()!=null " + (transaction.getCategory() != null) + "  overwriteExistingCategories " + overwriteExistingCategories);
//                    System.out.println(existing);
//                    System.out.println(transaction);
//                    System.out.println();

//                }
            } else {
                System.out.println("trying to enrich existing transactions with re-imported transactions, but transaction does not exist, should not be a case to right?");
                System.out.println(incoming);
                System.out.println();
            }
        }
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
