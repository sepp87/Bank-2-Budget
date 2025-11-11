package bank2budget.core;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author joost
 */
public class Account {

    private static Map<String, Account> accounts = new TreeMap<>();

    private final String accountNumber;
    private Map<Integer, CashTransaction> allTransactionsIndex = new TreeMap<>();

    private Account(String accountNumber) {
        this.accountNumber = accountNumber;
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
        return allTransactionsIndex.values().iterator().next().getDate();
    }

    /**
     *
     * @return the date ISO format yyyy-MM-dd
     */
    public LocalDate getNewestTransactionDate() {
        // By default, TreeMap sorts all its entries according to their natural ordering
        Iterator<CashTransaction> i = allTransactionsIndex.values().iterator();
        CashTransaction last = null;
        while (i.hasNext()) {
            last = i.next();
        }
        return last.getDate();
    }

    public static void addTransactionsToAccounts(List<CashTransaction> transactions) {
        addTransactionsToAccounts(transactions, false);
    }

    public static void addTransactionsToAccounts(List<CashTransaction> transactions, boolean overwriteExistingCategories) {
        // Split up the transactions by account
        Map<String, List<CashTransaction>> transactionsByAccountNumber = new TreeMap<>();
        for (CashTransaction transaction : transactions) {
            String accountNumber = transaction.getAccountNumber();
            if (transactionsByAccountNumber.containsKey(accountNumber)) {
                transactionsByAccountNumber.get(accountNumber).add(transaction);
            } else {
                List<CashTransaction> list = new ArrayList<>();
                list.add(transaction);
                transactionsByAccountNumber.put(accountNumber, list);
            }
        }

        // Add the transactions to the corresponding accounts
        for (String accountNumber : transactionsByAccountNumber.keySet()) {
            transactions = transactionsByAccountNumber.get(accountNumber);

            // If the accounts exist, evaluate the transactions before adding them
            if (accounts.containsKey(accountNumber)) {
                Account account = accounts.get(accountNumber);
                account.evaluateAndAddTransactions(transactions, overwriteExistingCategories);
            } else {
                Account account = new Account(accountNumber);
                accounts.put(accountNumber, account);
                account.addTransactions(transactions);
            }
        }

    }

    private void addTransactions(List<CashTransaction> transactions) {
        for (CashTransaction transaction : transactions) {
            allTransactionsIndex.put(transaction.getTransactionNumber(), transaction);
        }
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

    private void removeTransactions(List<CashTransaction> transactions) {
        for (CashTransaction transaction : transactions) {
            int number = transaction.getTransactionNumber();
            allTransactionsIndex.remove(number);
        }
    }

    private void addCategoriesToExistingTransactionsFrom(List<CashTransaction> transactions, boolean overwriteExistingCategories) {
        for (CashTransaction transaction : transactions) {
            int number = transaction.getTransactionNumber();
            if (allTransactionsIndex.containsKey(number)) {
                CashTransaction existing = allTransactionsIndex.get(number);
                boolean isSame = transaction.equals(existing);
                if (isSame && transaction.getCategory() != null && (existing.getCategory() == null || overwriteExistingCategories)) {
                    existing.setCategory(transaction.getCategory());
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

    public static Collection<Account> getAccounts() {
        return accounts.values();
    }

    public static Account getAccountBy(String accountNumber) {
        return accounts.get(accountNumber);
    }

    public static void removeAllAccounts() {
        Account.accounts.clear();
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
