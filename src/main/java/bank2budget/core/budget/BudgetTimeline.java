package bank2budget.core.budget;

import bank2budget.core.Account;
import bank2budget.core.AccountDomainLogic;
import java.time.LocalDate;
import java.util.Collection;
import java.util.TreeSet;

/**
 *
 * @author joostmeulenkamp
 */
public class BudgetTimeline {

    private final BudgetTemplate template;
    private final Budget budget;
    private final Collection<Account> accounts;

    private final TreeSet<LocalDate> firstsForGrouping = new TreeSet<>();
    private final TreeSet<LocalDate> firstsBeforeBudget = new TreeSet<>();
    private final TreeSet<LocalDate> firstsOfBudget = new TreeSet<>();
    private final TreeSet<LocalDate> firstsAfterBudget = new TreeSet<>();

    public BudgetTimeline(BudgetTemplate template, Budget budget, Collection<Account> accounts) {
        this.template = template;
        this.budget = budget;
        this.accounts = accounts;

        calculateFirsts();
    }

    public TreeSet<LocalDate> firstsForGrouping() {
        return firstsForGrouping;
    }

    public TreeSet<LocalDate> firstsBeforeBudget() {
        return firstsBeforeBudget;
    }

    public TreeSet<LocalDate> firstsOfBudget() {
        return firstsOfBudget;
    }

    public TreeSet<LocalDate> firstsAfterBudget() {
        return firstsAfterBudget;
    }

    private void calculateFirsts() {
        firstsOfBudget.addAll(budget.monthKeys());
        if (firstsOfBudget.isEmpty()) {
            LocalDate newest = AccountDomainLogic.getNewestTransactionDate(accounts);
            var firstsBefore = firstsTill(newest, true);
            var lastFirst = firstsBefore.removeLast(); // virtual last first needed for grouping only
            firstsBeforeBudget.addAll(firstsBefore);
            firstsForGrouping.addAll(firstsBefore);
            firstsForGrouping.add(lastFirst);

        } else {
            LocalDate firstOfBudget = firstsOfBudget.first();
            LocalDate lastFirstOfBudget = firstsOfBudget.last();

            var firstsBefore = firstsBeforeBudget(firstOfBudget);
            firstsBefore.removeLast(); // virtual last is part of firstOfBudget
            firstsBeforeBudget.addAll(firstsBefore);

            var firstsAfter = firstsAfterBudget(lastFirstOfBudget);
            var lastFirst = firstsAfter.removeLast(); // virtual last first needed for grouping only
            firstsAfterBudget.addAll(firstsAfter);

            firstsForGrouping.addAll(firstsBeforeBudget);
            firstsForGrouping.addAll(firstsOfBudget);
            firstsForGrouping.addAll(firstsAfterBudget);
            firstsForGrouping.add(lastFirst);
        }
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
        while (!first.isAfter(boundary)) {
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
        while (!first.isAfter(newest)) {
            result.add(first);
            first = first.plusMonths(1);
        }

        result.add(first); // add one last first of month so the newest transactions falls within the range

        return result;
    }
}
