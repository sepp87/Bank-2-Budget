package bank2budget.core.budget;

import bank2budget.core.Account;
import bank2budget.core.AccountDomainLogic;
import bank2budget.core.CashTransaction;
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
class BudgetTimeline {

    private final BudgetTemplate template;
    private final Budget budget;
    private final Collection<Account> accounts;
    private final TreeMap<LocalDate, List<CashTransaction>> byMonth = new TreeMap<>();

    BudgetTimeline(BudgetTemplate template, Budget budget, Collection<Account> accounts) {
        this.template = template;
        this.budget = budget;
        this.accounts = accounts;
    }

    private void groupByMonths() {

        List<CashTransaction> transactions = new ArrayList<>(); // sorted ascending
        List<CashTransaction> toGroup = List.copyOf(transactions); // sorted ascending

        TreeSet<LocalDate> allFirsts = firsts();
        Iterator<LocalDate> iterator = allFirsts.iterator();
        while (iterator.hasNext()) {
            LocalDate first = iterator.next();
            LocalDate nextFirst = allFirsts.higher(first);

            if (nextFirst == null) {
                break;
            }

            while (!toGroup.isEmpty()) {
                var tx = toGroup.getFirst();
                LocalDate date = tx.getDate();
                if (date.isBefore(nextFirst)) {
                    byMonth.computeIfAbsent(first, k -> new ArrayList<>()).add(tx);
                    toGroup.removeFirst();
                } else {
                    break;
                }
            }
        }
    }

    private TreeSet<LocalDate> firsts() {
        TreeSet<LocalDate> result = new TreeSet<>(); // contains all first
        result.addAll(budget.monthKeys());
        if (result.isEmpty()) {
            LocalDate newest = AccountDomainLogic.getNewestTransactionDate(accounts);
            result.addAll(firstsTill(newest, true));

        } else {
            LocalDate firstOfBudget = result.first();
            LocalDate lastFirstOfBudget = result.last();

            result.addAll(firstsBeforeBudget(firstOfBudget));
            result.addAll(firstsAfterBudget(lastFirstOfBudget));
        }

        return result;
    }

    private TreeSet<LocalDate> firstsBeforeBudget(LocalDate firstOfBudget) {
        return firstsTill(firstOfBudget, false);
    }

    private TreeSet<LocalDate> firstsTill(LocalDate boundary, boolean includeBoundary) {
        LocalDate oldest = AccountDomainLogic.getOldestTransactionDate(accounts);

        LocalDate first = oldest.withDayOfMonth(template.firstOfMonth());
        if (first.isAfter(oldest)) {
            first = first.minusMonths(1);
        }

        TreeSet<LocalDate> result = new TreeSet<>();
        while (first.isBefore(boundary)) {
            result.add(first);
            first = first.plusMonths(1);
        }
        if (includeBoundary) {
            result.add(first); // add one last first of month so the boundary (e.g. newest transaction) falls within the range
        }

        return result;
    }

    private TreeSet<LocalDate> firstsAfterBudget(LocalDate lastFirstOfBudget) {
        LocalDate newest = AccountDomainLogic.getNewestTransactionDate(accounts);

        LocalDate first = lastFirstOfBudget.withDayOfMonth(template.firstOfMonth());
        if (!first.isAfter(lastFirstOfBudget)) {
            first = first.plusMonths(1);
        }

        TreeSet<LocalDate> result = new TreeSet<>();
        while (first.isBefore(newest)) {
            result.add(first);
            first = first.plusMonths(1);
        }
        result.add(first); // add one last first of month so the newest transactions falls within the range

        return result;
    }

}
