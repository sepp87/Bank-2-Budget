package bank2budget.core;

import bank2budget.core.Account;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 *
 * @author joostmeulenkamp
 */
public class BalanceHistoryFactory {

    public static BalanceHistory fromAccount(Account account) {
        NavigableMap<LocalDate, Double> balances = new TreeMap<>();

        List<CashTransaction> allTransactions = account.getAllTransactionsAscending();

        // filter last of day
        List<CashTransaction> lastOfDay = new ArrayList<>();
        for (CashTransaction transaction : allTransactions) {
            if (transaction.isLastOfDay()) {
                lastOfDay.add(transaction);
            }
        }

        // generate initial balance
        CashTransaction first = allTransactions.get(0);
        LocalDate initialDate = first.getDate().minusDays(1);
        double initialBalance = first.getAccountBalanceBefore();
        balances.put(initialDate, initialBalance);

        // generate balances day by day
        int last = lastOfDay.size() - 1;
        for (int i = 0; i < last; i++) {
            CashTransaction current = lastOfDay.get(i);
            CashTransaction next = lastOfDay.get(i + 1);

            LocalDate currentDate = current.getDate();
            LocalDate nextDate = next.getDate();

            while (currentDate.isBefore(nextDate)) {
                balances.put(currentDate, current.getAccountBalance());
                currentDate = currentDate.plusDays(1);
            }
        }

        // If there's only one transaction, the loop  above is skipped but the 
        // section below ensures that day's balance is still recorded.
        CashTransaction lastTransaction = lastOfDay.get(last);
        LocalDate lastDate = lastTransaction.getDate();
        balances.put(lastDate, lastTransaction.getAccountBalance());

        return new BalanceHistory(account, balances);
    }

}
