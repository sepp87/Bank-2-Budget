package bank2budget.core;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author joost
 */
public class Account {

    private final String accountNumber;
    private final TreeMap<Integer, Transaction> allTransactionsIndex = new TreeMap<>();

    public Account(String accountNumber, List<Transaction> transactions) {
        this.accountNumber = accountNumber;
        for (var t : transactions) {
            allTransactionsIndex.put(t.transactionNumber(), t);
        }
    }

    public List<Transaction> getTransactions(LocalDate from, LocalDate to) {
        List<Transaction> transactions = transactionsAscending();
        List<Transaction> result = CashTransactionDomainLogic.filterByTimespan(transactions, from, to);
        return result;
    }

    public Transaction getTransactionBy(int transactionNumber) {
        return allTransactionsIndex.get(transactionNumber);
    }

    /**
     *
     * @return the date ISO format yyyy-MM-dd
     */
    public LocalDate getOldestTransactionDate() {
        // By default, TreeMap sorts all its entries according to their natural ordering
        return allTransactionsIndex.firstEntry().getValue().date();
    }

    /**
     *
     * @return the date ISO format yyyy-MM-dd
     */
    public LocalDate getNewestTransactionDate() {
        // By default, TreeMap sorts all its entries according to their natural ordering
        return allTransactionsIndex.lastEntry().getValue().date();
    }

    public void merge(Account incoming) {
        merge(incoming, false);
    }

    public void merge(Account incoming, boolean overwriteCategories) {
        evaluateOverlapAndMerge(incoming.transactionsAscending(), overwriteCategories);
    }

    private void evaluateOverlapAndMerge(List<Transaction> incoming, boolean overwriteCategories) {
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
            List<Transaction> otherIncoming = CashTransactionDomainLogic.filterByTimespanInverted(incoming, overlap[0], overlap[1]);
            addTransactions(otherIncoming);
        }
    }

    // decide which transactions to add, discard and add categories to
    private void merge(List<Transaction> incoming, LocalDate from, LocalDate to, boolean overwriteCategories) {
        var existingOverlap = getTransactions(from, to);
        var incomingOverlap = CashTransactionDomainLogic.filterByTimespan(incoming, from, to);
        List<LocalDate> range = DateUtil.dateRange(from, to);

        Map<LocalDate, List<Transaction>> existingByDays = groupByDays(range, existingOverlap);
        Map<LocalDate, List<Transaction>> incomingByDays = groupByDays(range, incomingOverlap);

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

    private Map<LocalDate, List<Transaction>> groupByDays(List<LocalDate> range, List<Transaction> transactions) {
        Map<LocalDate, List<Transaction>> result = new TreeMap<>();
        for (LocalDate date : range) {
            result.put(date, new ArrayList<>());
        }
        transactions.stream().forEach(e -> result.get(e.date()).add(e));
        return result;
    }

    private void addTransactions(List<Transaction> transactions) {
        for (var transaction : transactions) {
            allTransactionsIndex.put(transaction.transactionNumber(), transaction);
        }
    }

    private void removeTransactions(List<Transaction> transactions) {
        for (var transaction : transactions) {
            int number = transaction.transactionNumber();
            allTransactionsIndex.remove(number);
        }
    }

    private void enrichCategories(List<Transaction> transactions, boolean overwriteCategories) {
        for (var incoming : transactions) {
            int number = incoming.transactionNumber();
            if (allTransactionsIndex.containsKey(number)) {
                Transaction existing = allTransactionsIndex.get(number);
                boolean isSame = CashTransactionDomainLogic.areSame(incoming, existing);
                if (isSame && incoming.category() != null && (existing.category() == null || overwriteCategories)) {
                    var updated = existing.withCategory(incoming.category());
                    allTransactionsIndex.put(number, updated);
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
    public List<CashTransaction> getAllTransactionsAscending() {
        return allTransactionsIndex.values().stream().map(CashTransaction::new).toList();
    }

    /**
     *
     * @return list containing all transactions of this account, sorted in
     * ascending order, starting from oldest to most current
     */
    public List<Transaction> transactionsAscending() {
        return allTransactionsIndex.values().stream().toList();
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void replace(List<Transaction> transactions) {
        for (var tx : transactions) {
            var replaced = allTransactionsIndex.put(tx.transactionNumber(), tx);
            if (replaced == null) {
                System.out.println("ERROR REPLACED WAS NOT AVAILABLE");
            }
        }
    }
}
