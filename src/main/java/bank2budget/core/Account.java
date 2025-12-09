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
    private final TreeMap<Integer, CashTransaction> allTransactionsIndex = new TreeMap<>();

    public Account(String accountNumber, List<Transaction> transactions) {
        this.accountNumber = accountNumber;
        for (var t : transactions) {
            allTransactionsIndex.put(t.transactionNumber(), new CashTransaction(t));
        }
    }

    public List<CashTransaction> getTransactions(LocalDate from, LocalDate to) {
        List<CashTransaction> transactions = getAllTransactionsAscending();
        List<CashTransaction> result = CashTransactionDomainLogic.filterByTimespan(transactions, from, to);
        return result;
    }

    public CashTransaction getTransactionBy(int transactionNumber) {
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
        evaluateOverlapAndMerge(incoming.getAllTransactionsAscending(), overwriteCategories);
    }

    private void evaluateOverlapAndMerge(List<CashTransaction> incoming, boolean overwriteCategories) {
        // Evaluate overlap
        LocalDate[] overlap = CashTransactionDomainLogic.findOverlap(incoming, getAllTransactionsAscending());

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
        List<CashTransaction> existingOverlap = getTransactions(from, to);
        List<CashTransaction> incomingOverlap = CashTransactionDomainLogic.filterByTimespan(incoming, from, to);
        List<LocalDate> range = DateUtil.dateRange(from, to);

        Map<LocalDate, List<CashTransaction>> existingByDays = groupByDays(range, existingOverlap);
        Map<LocalDate, List<CashTransaction>> incomingByDays = groupByDays(range, incomingOverlap);

        for (LocalDate day : range) {
            List<CashTransaction> existingDay = existingByDays.get(day);
            List<CashTransaction> incomingDay = incomingByDays.get(day);

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
            allTransactionsIndex.put(transaction.transactionNumber(), transaction);
        }
    }

    private void removeTransactions(List<CashTransaction> transactions) {
        for (var transaction : transactions) {
            int number = transaction.transactionNumber();
            allTransactionsIndex.remove(number);
        }
    }

    private void enrichCategories(List<CashTransaction> transactions, boolean overwriteCategories) {
        for (var incoming : transactions) {
            int number = incoming.transactionNumber();
            if (allTransactionsIndex.containsKey(number)) {
                CashTransaction existing = allTransactionsIndex.get(number);
                boolean isSame = CashTransactionDomainLogic.areSame(incoming.getTransaction(), existing.getTransaction());
                if (isSame && incoming.category() != null && (existing.category() == null || overwriteCategories)) {
                    existing.setCategory(incoming.category());
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
            }
        }
    }

    /**
     *
     * @return list containing all transactions of this account, sorted in
     * ascending order, starting from oldest to most current
     */
    public List<CashTransaction> getAllTransactionsAscending() {
        List<CashTransaction> allTransactions = new ArrayList<>();
        allTransactions.addAll(allTransactionsIndex.values());
        return allTransactions;
    }

    /**
     *
     * @return list containing all transactions of this account, sorted in
     * ascending order, starting from oldest to most current
     */
    public List<Transaction> transactionsAscending() {
        return allTransactionsIndex.values().stream().map(CashTransaction::getTransaction).toList();
    }

    public String getAccountNumber() {
        return accountNumber;
    }

}
