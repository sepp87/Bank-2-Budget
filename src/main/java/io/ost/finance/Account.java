package io.ost.finance;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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

//    public Account(List<CashTransaction> transactions) {
//
//    }
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
        // different transactions, but with duplicate ids should not replace already registered transactions
        // because we assume, the first batch of transactions are new and contains are most complete
        // and we assume, the second batch of transactions was from an earlier point in time

        if (allTransactionsIndex.containsKey(transaction.transactionNumber)) {
            CashTransaction indexed = allTransactionsIndex.get(transaction.transactionNumber);
            boolean isSame = transaction.description.equals(indexed.description);
            if (!isSame) {
//                Logger.getLogger(Account.class.getName()).log(Level.INFO, "Transaction numbers {0} matched, please check if NOT duplicate: \n\t{1}\n\t{2}\n", new Object[]{transaction.transactionNumber, indexed.toString(), transaction.toString()});
                return;
            }
        }
        allTransactionsIndex.put(transaction.transactionNumber, transaction);
    }

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
