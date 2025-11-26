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

    private final static Map<String, Account> accounts = new TreeMap<>();

    private final String accountNumber;
    private final TreeMap<Integer, CashTransaction> allTransactionsIndex = new TreeMap<>();


    public Account(String accountNumber, List<CashTransaction> transactions) {
        this.accountNumber = accountNumber;
        for (CashTransaction t : transactions) {
            allTransactionsIndex.put(t.getTransactionNumber(), t);
        }
    }

    public List<CashTransaction> getTransactions(LocalDate from, LocalDate to) {
        List<CashTransaction> transactions = getAllTransactionsAscending();
        List<CashTransaction> result = CashTransaction.filterByTimespan(transactions, from, to);
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
        return allTransactionsIndex.firstEntry().getValue().getDate();
    }

    /**
     *
     * @return the date ISO format yyyy-MM-dd
     */
    public LocalDate getNewestTransactionDate() {
        // By default, TreeMap sorts all its entries according to their natural ordering
        return allTransactionsIndex.lastEntry().getValue().getDate();
    }



    public void merge(Account imported) {
        merge(imported, false);
    }

    public void merge(Account imported, boolean overwriteExistingCategories) {
        evaluateAndAddTransactions(imported.getAllTransactionsAscending(), overwriteExistingCategories);
    }

    private void evaluateAndAddTransactions(List<CashTransaction> transactions, boolean overwriteExistingCategories) {
        // Evaluate which transactions to add, discard and add categories to
        LocalDate[] overlap = CashTransaction.findOverlap(transactions, getAllTransactionsAscending());

        if (overlap == null) {
            addTransactions(transactions);

        } else {
            List<CashTransaction> existingOverlappingTransactions = getTransactions(overlap[0], overlap[1]);
            List<CashTransaction> newOverlappingTransactions = CashTransaction.filterByTimespan(transactions, overlap[0], overlap[1]);

            // if the newly imported list of transactions contains more entries (during the overlapping timespan), the existing ones should be replaced
            if (newOverlappingTransactions.size() > existingOverlappingTransactions.size()) {
                removeTransactions(existingOverlappingTransactions);
                addTransactions(transactions);

                // the categories (of the existing list of transactions) can be used to enrich the new ones as long as they are actually the same transaction
                addCategoriesToExistingTransactionsFrom(existingOverlappingTransactions, overwriteExistingCategories);

            } else {
                // the categories (of the newly imported list of transactions) can be used to enrich the existing ones as long as they are actually the same transaction
                addCategoriesToExistingTransactionsFrom(newOverlappingTransactions, overwriteExistingCategories);

                List<CashTransaction> otherNewTransactions = CashTransaction.filterByTimespan(transactions, overlap[0], overlap[1], true);
                addTransactions(otherNewTransactions);
            }
        }
    }

    private void addTransactions(List<CashTransaction> transactions) {
        for (CashTransaction transaction : transactions) {
            allTransactionsIndex.put(transaction.getTransactionNumber(), transaction);
        }
    }

    private void removeTransactions(List<CashTransaction> transactions) {
        for (CashTransaction transaction : transactions) {
            int number = transaction.getTransactionNumber();
            allTransactionsIndex.remove(number);
        }
    }

    private void addCategoriesToExistingTransactionsFrom(List<CashTransaction> transactions, boolean overwriteExistingCategories) {
        for (CashTransaction imported : transactions) {
            int number = imported.getTransactionNumber();
            if (allTransactionsIndex.containsKey(number)) {
                CashTransaction existing = allTransactionsIndex.get(number);
                boolean isSame = imported.equals(existing);
                if (isSame && imported.getCategory() != null && (existing.getCategory() == null || overwriteExistingCategories)) {
                    existing.setCategory(imported.getCategory());
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

    public String getAccountNumber() {
        return accountNumber;
    }

}
