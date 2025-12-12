package bank2budget.core.budget;

import bank2budget.core.CashTransactionDomainLogic;
import bank2budget.core.CashTransaction;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author joostmeulenkamp
 */
public class CategoryCalculator {

    public static BudgetMonthCategory createUnappliedIncome(BudgetMonth previousMonth, BudgetMonth currentMonth, List<CashTransaction> uncategorized) {
        return createUnappliedCategory(previousMonth, currentMonth, uncategorized, true);
    }

    public static BudgetMonthCategory createUnappliedExpenses(BudgetMonth previousMonth, BudgetMonth currentMonth, List<CashTransaction> uncategorized) {
        return createUnappliedCategory(previousMonth, currentMonth, uncategorized, false);
    }

    private static BudgetMonthCategory createUnappliedCategory(BudgetMonth previousMonth, BudgetMonth currentMonth, List<CashTransaction> uncategorized, boolean wantUnappliedIncome) {

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
            boolean isIncome = transaction.transactionType() == CashTransaction.TransactionType.CREDIT;
            if (wantUnappliedIncome == isIncome) {
                actual = actual.add(transaction.amount());
                transactions.add(transaction);
            }
        }

        BigDecimal closing = actual.add(opening).add(adjustments);

        var unapplied = wantUnappliedIncome
                ? BudgetMonthCategory.createUnappliedIncome(firstOfMonth, opening, actual, closing, transactions)
                : BudgetMonthCategory.createUnappliedExpenses(firstOfMonth, opening, actual, closing, transactions);
        return unapplied;
    }

    public static List<BudgetMonthCategory> createOperatingCategories(BudgetTemplate template, BudgetMonth previousMonth, BudgetMonth currentMonth, List<CashTransaction> categorized) {
        List<BudgetMonthCategory> result = new ArrayList<>();
        var byCategory = CashTransactionDomainLogic.groupByCategory(categorized);

        Set<String> categories = getCategories(template, previousMonth, currentMonth, byCategory);

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
            BudgetMonthCategory updated = new BudgetMonthCategory(firstOfMonth, name, opening, actual, budgeted, adjustments, closing, categoryTransactions);
            result.add(updated);
        }
        return result;
    }

    private static Set<String> getCategories(BudgetTemplate template, BudgetMonth previousMonth, BudgetMonth currentMonth, Map<String, List<CashTransaction>> byCategory) {
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
        return categories;
    }

    private static BigDecimal getActual(List<CashTransaction> transactions) {
        BigDecimal result = BigDecimal.ZERO;
        for (var transaction : transactions) {
            result = result.add(transaction.amount());
        }
        return result;
    }
}
