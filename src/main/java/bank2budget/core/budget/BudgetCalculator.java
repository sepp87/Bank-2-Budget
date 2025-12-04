package bank2budget.core.budget;

import bank2budget.core.Account;
import bank2budget.core.AccountDomainLogic;
import bank2budget.core.CashTransaction;
import bank2budget.core.CashTransactionDomainLogic;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author joostmeulenkamp
 */
public class BudgetCalculator {

    public void calculate(BudgetTemplate template, Budget budget, Collection<Account> accounts) {
        BudgetTimeline timeline = new BudgetTimeline(template, budget, accounts);
    }

    // budget template trickles all the way down to createOperatingCategories, to ensure categories are listed regardless if there are any transactions for the respective month. If budgeted is also 0. for these categories, why calculate these to begin with? it is in that sense just something a user might want to see in the UI, but nothing to persist in the budget.
    public List<BudgetMonth> createMissingAfter() {
        // create new months (from template) for all transactions
        return Collections.emptyList();
    }

    public List<BudgetMonth> createMissingBefore() {
        // create new months (from template) for all transactions
        return Collections.emptyList();
    }

    public List<BudgetMonth> createUpdated(BudgetTemplate template, Budget budget, Collection<Account> accounts) {

        List<CashTransaction> transactions = AccountDomainLogic.getTransactions(accounts);
        LocalDate newest = AccountDomainLogic.getNewestTransactionDate(accounts);
        LocalDate oldest = AccountDomainLogic.getOldestTransactionDate(accounts);

        // nothing to do
        if (transactions.isEmpty()) {
            return Collections.emptyList();
        }

        // Budget loaded - create updated months for all months contained within the budget 
        if (!budget.months().isEmpty() && isDateWithinBudget(oldest, budget, template)) {
            List<BudgetMonth> updated = createUpdatedMonths(template, budget, accounts);
            return updated;
        }

        return Collections.emptyList();
    }

    // create updated months for all months contained within the budget 
    private List<BudgetMonth> createUpdatedMonths(BudgetTemplate template, Budget budget, Collection<Account> accounts) {
        List<BudgetMonth> result = new ArrayList<>();

        var months = budget.months();
        int size = months.size();
        for (int i = 0; i < size; i++) {

            BudgetMonth month = months.get(i);

            LocalDate firstOfMonth = month.firstOfMonth();
            LocalDate lastOfMonth;

            // next month boundary
            if (i + 1 < size) {
                BudgetMonth next = months.get(i + 1);
                lastOfMonth = next.firstOfMonth().minusDays(1);
            } else {
                lastOfMonth = getLastDayOfBudget(budget, template);
            }

            // previous (updated) month
            BudgetMonth previous = (i > 0) ? result.get(i - 1) : null;

            var selected = AccountDomainLogic.getTransactions(accounts, firstOfMonth, lastOfMonth);

            BudgetMonth<CashTransaction> updated = createUpdatedMonth(template, previous, month, selected);

            result.add(updated);
        }
        return result;
    }

    private BudgetMonth<CashTransaction> createUpdatedMonth(BudgetTemplate template, BudgetMonth previous, BudgetMonth month, List<CashTransaction> transactions) {

        var categorized = CashTransactionDomainLogic.categorized(transactions);
        var uncategorized = CashTransactionDomainLogic.uncategorized(transactions);

        var operating = createOperatingCategories(template, previous, month, categorized);
        var unappliedIncome = createUnappliedIncome(previous, month, uncategorized);
        var unappliedExpenses = createUnappliedExpenses(previous, month, uncategorized);

        BudgetMonth<CashTransaction> updated = new BudgetMonth<>(month.firstOfMonth(), operating, unappliedIncome, unappliedExpenses);

        return updated;
    }

    private BudgetMonthCategory createUnappliedIncome(BudgetMonth<CashTransaction> previousMonth, BudgetMonth<CashTransaction> currentMonth, List<CashTransaction> uncategorized) {
        return createUnappliedCategory(previousMonth, currentMonth, uncategorized, true);
    }

    private BudgetMonthCategory createUnappliedExpenses(BudgetMonth<CashTransaction> previousMonth, BudgetMonth<CashTransaction> currentMonth, List<CashTransaction> uncategorized) {
        return createUnappliedCategory(previousMonth, currentMonth, uncategorized, false);
    }

    private BudgetMonthCategory createUnappliedCategory(BudgetMonth<CashTransaction> previousMonth, BudgetMonth<CashTransaction> currentMonth, List<CashTransaction> uncategorized, boolean wantUnappliedIncome) {

        LocalDate firstOfMonth = currentMonth.firstOfMonth();
        BudgetMonthCategory current = wantUnappliedIncome ? currentMonth.unappliedIncome() : currentMonth.unappliedExpenses();
        BudgetMonthCategory previous = null;
        if (previousMonth != null) {
            previous = wantUnappliedIncome ? previousMonth.unappliedIncome() : previousMonth.unappliedExpenses();
        }

        BigDecimal actual = BigDecimal.ZERO;
        BigDecimal opening = previous != null ? previous.closing() : BigDecimal.ZERO;
        BigDecimal adjustments = current.adjustments();
        List<CashTransaction> transactions = new ArrayList<>();

        for (var transaction : uncategorized) {
            boolean isIncome = transaction.getTransactionType() == CashTransaction.TransactionType.CREDIT;
            if (wantUnappliedIncome == isIncome) {
                actual = actual.add(BigDecimal.valueOf(transaction.getAmount()));
                transactions.add(transaction);
            }
        }

        BigDecimal closing = actual.add(opening).add(adjustments);

        var unapplied = wantUnappliedIncome
                ? BudgetMonthCategory.createUnappliedIncome(firstOfMonth, actual, opening, closing, transactions)
                : BudgetMonthCategory.createUnappliedExpenses(firstOfMonth, actual, opening, closing, transactions);
        return unapplied;
    }

    private List<BudgetMonthCategory> createOperatingCategories(BudgetTemplate template, BudgetMonth<CashTransaction> previousMonth, BudgetMonth<CashTransaction> currentMonth, List<CashTransaction> categorized) {
        List<BudgetMonthCategory> result = new ArrayList<>();
        Map<String, List<CashTransaction>> byCategory = CashTransactionDomainLogic.groupByCategory(categorized);

        Set<String> categories = new HashSet<>();
        categories.addAll(byCategory.keySet());
        categories.addAll(template.operatingCategories().keySet());
        currentMonth.operatingCategories()
                .forEach(e -> categories.add(e.name()));
        if (previousMonth != null) {
            previousMonth.operatingCategories().stream()
                    .filter(e -> e.closing().compareTo(BigDecimal.ZERO) != 0)
                    .forEach(e -> categories.add(e.name()));
        }

        for (String name : categories) {

            LocalDate firstOfMonth = currentMonth.firstOfMonth();
            var previous = previousMonth != null ? previousMonth.operatingCategory(name) : null;
            var category = currentMonth.operatingCategory(name);

            BigDecimal budgeted = null;
            BigDecimal opening = null;
            BigDecimal adjustments = null;

            if (previous != null) {
                opening = previous.closing();
            } else {
                opening = BigDecimal.ZERO;
            }
            if (category != null) {
                budgeted = category.budgeted();
                adjustments = category.adjustments();
            } else {
                budgeted = BigDecimal.ZERO;
                adjustments = BigDecimal.ZERO;
            }

            List<CashTransaction> categoryTransactions = byCategory.getOrDefault(name, new ArrayList<>());
            BigDecimal actual = getActual(categoryTransactions);
            BigDecimal closing = budgeted.add(actual).add(opening).add(adjustments);
            BudgetMonthCategory<CashTransaction> updated = new BudgetMonthCategory<>(firstOfMonth, name, budgeted, actual, opening, closing, adjustments, categoryTransactions);
            result.add(updated);
        }
        return result;
    }

    private BigDecimal getActual(List<CashTransaction> transactions) {
        BigDecimal result = BigDecimal.ZERO;
        for (var transaction : transactions) {
            result = result.add(BigDecimal.valueOf(transaction.getAmount()));
        }
        return result;
    }

    private boolean isDateWithinBudget(LocalDate date, Budget budget, BudgetTemplate template) {
        var months = budget.months();
        if (months.isEmpty()) {
            return false;
        }

        LocalDate firstDay = months.getFirst().firstOfMonth();
        LocalDate lastDay = getLastDayOfBudget(budget, template);

        return !date.isBefore(firstDay) && !date.isAfter(lastDay);
    }

    private LocalDate getLastDayOfBudget(Budget budget, BudgetTemplate template) {
        BudgetMonth newest = budget.months().getLast();
        int actualFirstOfMonth = newest.firstOfMonth().getDayOfMonth();
        if (actualFirstOfMonth == template.firstOfMonth()) {
            // Happy path - first of month did NOT change, so the last of month can be calculated based on the given month
            return newest.firstOfMonth().plusMonths(1).minusDays(1);

        } else {
            if (template.firstOfMonth() < actualFirstOfMonth) {
                return newest.firstOfMonth().plusMonths(1).withDayOfMonth(template.firstOfMonth()).minusDays(1);

            } else {
                return newest.firstOfMonth().withDayOfMonth(template.firstOfMonth()).minusDays(1);

            }
        }
    }

}
