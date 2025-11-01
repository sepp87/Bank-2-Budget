package bank2budget.core;

import bank2budget.core.Account;
import java.time.LocalDate;
import java.util.Collections;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 *
 * @author joostmeulenkamp
 */
public class BalanceHistory {

    private final Account account;
    private final NavigableMap<LocalDate, Double> balances;

    public BalanceHistory(Account account, NavigableMap<LocalDate, Double> balances) {
        this.account = account;
        this.balances =  Collections.unmodifiableNavigableMap(new TreeMap<>(balances));;
    }

    public LocalDate getOldestDate() {
        return balances.firstKey();
    }

    public LocalDate getNewestDate() {
        return balances.lastKey();
    }

    public Double getBalanceOn(LocalDate date) {
        return balances.get(date);
    }

}
