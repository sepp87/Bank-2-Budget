package bank2budget.core;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author joostmeulenkamp
 */
public class AccountDomainLogic {

    public static LocalDate getOldestTransactionDate(Collection<Account> accounts) {
        LocalDate result = null;
        for (Account account : accounts) {
            LocalDate candidate = account.getOldestTransactionDate();
            if (result == null) {
                result = candidate;
            }
            if (candidate.isBefore(result)) {
                result = candidate;
            }
        }
        return result;
    }

    public static LocalDate getNewestTransactionDate(Collection<Account> accounts) {
        LocalDate result = null;
        for (Account account : accounts) {
            LocalDate candidate = account.getNewestTransactionDate();
            if (result == null) {
                result = candidate;
            }
            if (candidate.isAfter(result)) {
                result = candidate;
            }
        }
        return result;
    }

    public static List<CashTransaction> getTransactions(Collection<Account> accounts) {
        List<CashTransaction> result = new ArrayList<>();
        for (Account account : accounts) {
            result.addAll(account.getAllTransactionsAscending());
        }
        return result;
    }
    
    public static List<CashTransaction> getTransactions(Collection<Account> accounts, LocalDate from, LocalDate to) {
        List<CashTransaction> result = new ArrayList<>();
        for (Account account : accounts) {
            var transactions = account.getTransactions(from, to);
            result.addAll(transactions);
        }
        return result;
    }
}
