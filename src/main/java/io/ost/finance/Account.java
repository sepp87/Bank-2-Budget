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

    // including from-date and excluding to-date
    public List<CashTransaction> getTransactionsBetween(LocalDate start, LocalDate end) {
        List<CashTransaction> result = new ArrayList<>();
        start = ChronoUnit.DAYS.addTo(start, -1);
        for (CashTransaction transaction : allTransactionsIndex.values()) {
            LocalDate date = LocalDate.parse(transaction.date);
            if (date.isAfter(start) && date.isBefore(end)) {
                result.add(transaction);
            }
        }
        return result;
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

    private void addTransaction(CashTransaction transaction) {
        // as we near the retrieval date of a bank statement, some transactions might still be missing, 
        // because the bank did not yet process them. As soon as these transactions get processed
        // these transactions might change the ordering. Meaning, newer transactions from
        // newer bank statements get prejudice.
        // 
        // different transactions, but with duplicate ids should ONLY replace already registered transactions
        // when they are the same and have a label. Because when they are the same the order of the older transaction
        // was correct and the label of this transaction should remain and not be removed or replaced, since
        // it is assumed that this label was double checked and intentional.

        if (allTransactionsIndex.containsKey(transaction.transactionNumber)) {
            CashTransaction indexed = allTransactionsIndex.get(transaction.transactionNumber);
            boolean isSame = areValuesSameBetween(transaction, indexed);
            if (isSame && transaction.getLabel() != null) {
                allTransactionsIndex.put(transaction.transactionNumber, transaction);
//                Logger.getLogger(Account.class.getName()).log(Level.INFO, "Transaction numbers {0} matched, please check if NOT duplicate: \n\t{1}\n\t{2}\n", new Object[]{transaction.transactionNumber, indexed.toString(), transaction.toString()});
            }
            return;
        }
        allTransactionsIndex.put(transaction.transactionNumber, transaction);
    }

    private boolean areValuesSameBetween(CashTransaction a, CashTransaction b) {
        String accountNameNumberAndDescriptionA = a.accountName + " " + a.accountNumber + " " + a.description;
        String accountNameNumberAndDescriptionB = b.accountName + " " + b.accountNumber + " " + b.description;
        return accountNameNumberAndDescriptionA.equals(accountNameNumberAndDescriptionB);
    }

    // TODO replaces old method
//    public static void addTransactionsToAccounts(List<CashTransaction> transactions) {
//        // Split up the transactions by account
//        Map<String, List<CashTransaction>> transactionsByAccountNumber = new TreeMap<>();
//        for (CashTransaction transaction : transactions) {
//            String accountNumber = transaction.getAccountNumber();
//            if (transactionsByAccountNumber.containsKey(accountNumber)) {
//                transactionsByAccountNumber.get(accountNumber).add(transaction);
//            } else {
//                List<CashTransaction> list = new ArrayList<>();
//                list.add(transaction);
//                transactionsByAccountNumber.put(accountNumber, list);
//            }
//        }
//
//        // Check if the accounts exist, if yes, compare, remove old incomplete daily records and add new non-existent transactions 
//        for (String accountNumber : transactionsByAccountNumber.keySet()) {
//            if (accounts.containsKey(accountNumber)) {
////                getOverlappingTimespan()
//            } else {
//                Account account = new Account(accountNumber);
//                account.addTransactions(transactions);
//                accounts.put(accountNumber, account);
//            }
//        }
//    }
//
//    private void addTransactions(List<CashTransaction> transactions) {
//        for (CashTransaction transaction : transactions) {
//            allTransactionsIndex.put(transaction.getTransactionNumber(), transaction);
//        }
//    }

    
    public static Collection<Account> addTransactionsToAccounts(List<CashTransaction> transactions) {
        for (CashTransaction transaction : transactions) {
            if (accounts.containsKey(transaction.accountNumber)) {
                Account account = accounts.get(transaction.accountNumber);
                account.addTransaction(transaction);
            } else {
                Account account = new Account(transaction.accountNumber);
                account.addTransaction(transaction);
                accounts.put(transaction.accountNumber, account);
            }
        }
        // TBD maybe want to bubble sort all transactions per account by age ascending here
        return accounts.values();
    }
    public static Collection<Account> getAccounts() {
        return accounts.values();
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
