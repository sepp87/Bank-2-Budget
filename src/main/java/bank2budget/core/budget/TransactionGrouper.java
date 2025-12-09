package bank2budget.core.budget;

import bank2budget.core.Account;
import bank2budget.core.Transaction;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 * @author joostmeulenkamp
 */
public class TransactionGrouper {

    private final TreeSet<LocalDate> firsts;
    private final TreeMap<LocalDate, List<Transaction>> byMonth = new TreeMap<>();

    public TransactionGrouper(Collection<Account> accounts, TreeSet<LocalDate> firsts) {
        this.firsts = firsts;

        for (Account account : accounts) {
            groupAccountByMonths(account);
        }
    }

    public List<Transaction> transactions(LocalDate first) {
        return byMonth.getOrDefault(first, List.of());
    }

    private void groupAccountByMonths(Account account) {

        var transactions = account.transactionsAscending(); // sorted ascending
        var toGroup = new ArrayList<>(transactions); // sorted ascending

        Iterator<LocalDate> iterator = firsts.iterator();
        while (iterator.hasNext()) {
            LocalDate first = iterator.next();
            LocalDate nextFirst = firsts.higher(first);

            if (nextFirst == null) {
                break;
            }

            while (!toGroup.isEmpty()) {
                var tx = toGroup.getFirst();
                LocalDate date = tx.date();
                if (date.isBefore(nextFirst)) {
                    byMonth.computeIfAbsent(first, k -> new ArrayList<>()).add(tx);
                    toGroup.removeFirst();
                } else {
                    break;
                }
            }
        }
    }
}
