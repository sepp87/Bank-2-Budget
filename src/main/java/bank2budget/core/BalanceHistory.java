package bank2budget.core;

import bank2budget.core.Account;
import java.math.BigDecimal;
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
    private final NavigableMap<LocalDate, BigDecimal> balances;

    public BalanceHistory(Account account, NavigableMap<LocalDate, BigDecimal> balances) {
        this.account = account;
        this.balances =  Collections.unmodifiableNavigableMap(new TreeMap<>(balances));;
    }

    public LocalDate getOldestDate() {
        return balances.firstKey();
    }

    public LocalDate getNewestDate() {
        return balances.lastKey();
    }

    public BigDecimal getBalanceOn(LocalDate date) {
        return balances.get(date);
    }

}
