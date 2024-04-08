package io.ost.finance;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

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
        List<CashTransaction> transactions = getAllTransactions();
        List<CashTransaction> result = CashTransaction.filterByTimespan(transactions, from, to);
        return result;
    }

    public CashTransaction getTransactionBy(int transactionNumber){
        return allTransactionsIndex.get(transactionNumber);
    }

    public String getOldestTransactionDate() {
        // TBD maybe want to sort here
        return allTransactionsIndex.values().iterator().next().getDate();
    }

    public String getNewestTransactionDate() {
        // TBD maybe want to sort here
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

    public static void addTransactionsToAccounts(List<CashTransaction> transactions, boolean overwriteExistingLabels) {
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
            // If the accounts exist, evaluate the transactions before adding them
            if (accounts.containsKey(accountNumber)) {
                Account account = accounts.get(accountNumber);
                account.evaluateAndAddTransactions(transactions, overwriteExistingLabels);
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

    private void evaluateAndAddTransactions(List<CashTransaction> transactions, boolean overwriteExistingLabels) {
        // Evaluate which transactions to add, discard and add labels to
        LocalDate[] overlap = CashTransaction.findOverlap(transactions, getAllTransactions());

        if (overlap == null) {
            addTransactions(transactions);
        } else {
            List<CashTransaction> existingOverlappingTransactions = getTransactions(overlap[0], overlap[1]);
            List<CashTransaction> newOverlappingTransactions = CashTransaction.filterByTimespan(transactions, overlap[0], overlap[1]);

            // if the newly imported list of transactions contains more entries (during the overlapping timespan), the existing ones should be replaced
            // TODO Bonus - evaluate when the overlap starts producing uneven lists and re-adjust the timespan
            if (newOverlappingTransactions.size() > existingOverlappingTransactions.size()) {
                removeTransactions(existingOverlappingTransactions);
                addTransactions(transactions);

            } else {
                // if the newly imported list of transactions contains the same or less entries, the labels can be used to enrich the existing ones as long as they are actually the same transaction
                addLabelsToExistingTransactionsFrom(newOverlappingTransactions, overwriteExistingLabels);

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

    private void addLabelsToExistingTransactionsFrom(List<CashTransaction> transactions, boolean overwriteExistingLabels) {
        for (CashTransaction transaction : transactions) {
            int number = transaction.getTransactionNumber();
            if (allTransactionsIndex.containsKey(number)) {
                CashTransaction existing = allTransactionsIndex.get(number);
                boolean isSame = transaction.equals(existing);
                if (isSame && transaction.getLabel() != null && (existing.getLabel() == null || overwriteExistingLabels)) {
                    existing.setLabel(transaction.getLabel());
//                Logger.getLogger(Account.class.getName()).log(Level.INFO, "Transaction numbers {0} matched, please check if NOT duplicate: \n\t{1}\n\t{2}\n", new Object[]{transaction.transactionNumber, indexed.toString(), transaction.toString()});
                }
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

    public List<CashTransaction> getAllTransactions() {
        List<CashTransaction> allTransactions = new ArrayList<>();
        allTransactions.addAll(allTransactionsIndex.values());
        return allTransactions;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

}
