package bank2budget.core;

import bank2budget.core.CashTransaction;
import bank2budget.core.Account;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
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

    private static boolean checkAccountBalanceHistory(Account account) {
        boolean allValid = true;
        List<CashTransaction> transactions = account.getAllTransactionsAscending();
        int limit = transactions.size() - 1;
        for (int i = 0; i < limit; i++) {
            CashTransaction transaction = transactions.get(i);
            CashTransaction next = transactions.get(i + 1);
            boolean isValid = compareBalanceBetween(transaction, next);
            if (!isValid) {
                allValid = false;
            }
        }
        return allValid;
    }

    private static boolean compareBalanceBetween(CashTransaction transaction, CashTransaction next) {
        boolean isSame = Util.compareMoney(transaction.getAccountBalance(), next.getAccountBalanceBefore());
        if (!isSame) {
            Logger.getLogger(IntegrityChecker.class.getName()).log(
                    Level.WARNING,
                    "Missing or incomplete bank statements. Account balance history broken for transactionNumber {0} originating from {1}",
                    new Object[]{next.getTransactionNumber(), next.getFileOrigin().getName()});
        }
        return isSame;
    }
}
