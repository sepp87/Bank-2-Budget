package bank2budget.core;

import java.math.BigDecimal;
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
        NavigableMap<LocalDate, BigDecimal> balances = new TreeMap<>();

        var allTransactions = account.transactionsAscending();

        // filter last of day
        List<CashTransaction> lastOfDay = new ArrayList<>();
        for (var transaction : allTransactions) {
            if (transaction.lastOfDay()) {
                lastOfDay.add(transaction);
            }
        }

        // generate initial balance
        CashTransaction first = allTransactions.get(0);
        LocalDate initialDate = first.date().minusDays(1);
        BigDecimal initialBalance = first.accountBalanceBefore();
        balances.put(initialDate, initialBalance);

        // generate balances day by day
        int last = lastOfDay.size() - 1;
        for (int i = 0; i < last; i++) {
            var current = lastOfDay.get(i);
            var next = lastOfDay.get(i + 1);

            LocalDate currentDate = current.date();
            LocalDate nextDate = next.date();

            while (currentDate.isBefore(nextDate)) {
                balances.put(currentDate, current.accountBalance());
                currentDate = currentDate.plusDays(1);
            }
        }

        // If there's only one transaction, the loop  above is skipped but the 
        // section below ensures that day's balance is still recorded.
        var lastTransaction = lastOfDay.get(last);
        LocalDate lastDate = lastTransaction.date();
        balances.put(lastDate, lastTransaction.accountBalance());

        return new BalanceHistory(account, balances);
    }

}
