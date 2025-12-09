package bank2budget.core;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 *
 * @author joostmeulenkamp
 */
public class BalanceHistoryAggregator {

    public static BalanceHistory combine(Collection<BalanceHistory> histories) {
        NavigableMap<LocalDate, BigDecimal> balances = new TreeMap<>();

        LocalDate oldest = null;
        LocalDate newest = null;
        for (BalanceHistory h : histories) {
            if (oldest == null || h.getOldestDate().isBefore(oldest)) {
                oldest = h.getOldestDate();
            }
            if (newest == null || h.getNewestDate().isAfter(newest)) {
                newest = h.getNewestDate();
            }
        }

        LocalDate date = oldest;
        LocalDate limit = newest.plusDays(1);
        while (date.isBefore(limit)) {
            balances.put(date, BigDecimal.ZERO);
            for (BalanceHistory history : histories) {
                BigDecimal balance = history.getBalanceOn(date);
                if (balance != null) {
                    balances.merge(date, balance, BalanceHistoryAggregator::add);
                }
            }
            date = date.plusDays(1);
        }

        return new BalanceHistory(null, balances);
    }

    private static BigDecimal add(BigDecimal a, BigDecimal b) {
        return a.add(b);
    }

}
