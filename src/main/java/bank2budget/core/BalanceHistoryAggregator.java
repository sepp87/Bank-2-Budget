package bank2budget.core;

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
        NavigableMap<LocalDate, Double> balances = new TreeMap<>();

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
            balances.put(date, 0.);
            for (BalanceHistory history : histories) {
                Double balance = history.getBalanceOn(date);
                if (balance != null) {
                    balances.merge(date, balance, BalanceHistoryAggregator::add);
                }
            }
            date = date.plusDays(1);
        }

        return new BalanceHistory(null, balances);
    }

    private static double add(double a, double b) {
        return a + b;
    }

}
