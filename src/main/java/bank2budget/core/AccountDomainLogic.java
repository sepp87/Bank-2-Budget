package bank2budget.core;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;

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

    /**
     *
     * @return the date of the latest transaction across all accounts. Returns
     * null if there are no accounts respectively no transactions available.
     */
    public static LocalDate getLastExportDate(Collection<Account> accounts) {
        LocalDate result = null;
        for (Account a : accounts) {
            LocalDate candidate = a.getNewestTransactionDate();
            if (result == null) {
                result = candidate;
            }
            if (candidate.isAfter(result)) {
                result = candidate;
            }
        }
        return result;
    }

    /**
     *
     * @return the balance of all accounts put together.
     */
    public static BigDecimal getTotalBalanceOn(Collection<Account> accounts, LocalDate date) {
        if (date == null) {
            date = getLastExportDate(accounts);
        }
        BigDecimal result = BigDecimal.ZERO;
        for (Account a : accounts) {
            var transactions = a.transactionsAscending();
            CashTransaction newest = null;
            for (var transaction : transactions) {
                if (transaction.date().isAfter(date)) {
                    break;
                }
                newest = transaction;
            }
            if (newest != null) {
                result = result.add(newest.accountBalance());
            }
        }
        return result;
    }

}
