package bank2budget.core;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author joostmeulenkamp
 */
public class IntegrityChecker {

    public static boolean check(Collection<Account> accounts) {
        boolean allValid = true;
        for (Account account : accounts) {
            boolean isValid = checkAccountBalanceHistory(account);
            if (!isValid) {
                allValid = false;
            }
        }
        return allValid;

    }

    public static boolean checkAccountBalanceHistory(Account account) {
        boolean allValid = true;
        var transactions = account.transactionsAscending();
        int limit = transactions.size() - 1;
        for (int i = 0; i < limit; i++) {
            var transaction = transactions.get(i);
            var next = transactions.get(i + 1);
            boolean isValid = compareBalanceBetween(transaction, next);
            if (!isValid) {
                allValid = false;
            }
        }
        return allValid;
    }

    private static boolean compareBalanceBetween(CashTransaction transaction, CashTransaction next) {
        boolean isSame = transaction.accountBalance().compareTo(next.accountBalanceBefore()) == 0;
        if (!isSame) {
            Logger.getLogger(IntegrityChecker.class.getName()).log(
                    Level.WARNING,
                    "Missing or incomplete bank statements. Account balance history broken for transactionNumber {0}",
                    next.transactionNumber() + "");
        }
        return isSame;
    }
}
