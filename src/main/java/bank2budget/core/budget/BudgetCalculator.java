package bank2budget.core.budget;

import bank2budget.core.Account;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;
import java.util.function.Function;

/**
 *
 * @author joostmeulenkamp
 */
public class BudgetCalculator {

    public List<BudgetMonth> create(BudgetTemplate template, Budget budget, Collection<Account> accounts) {
        BudgetRangeCalculator rangeCalculator = new BudgetRangeCalculator(template, budget, accounts);

        List<BudgetMonth> before = rangeCalculator.createBeforeBudget();
        List<BudgetMonth> updated = rangeCalculator.createUpdatedBudget();
        List<BudgetMonth> after = rangeCalculator.createAfterBudget();

        List<BudgetMonth> result = new ArrayList<>();
        result.addAll(before);
        result.addAll(updated);
        result.addAll(after);

        return result;
    }

    public List<BudgetMonth> updateAdjustments(Budget budget, LocalDate firstOfMonth, String category, BigDecimal adjustments) {
        BudgetMonthCategory toUpdate = budget.month(firstOfMonth).operatingCategory(category);
        return updateCategoryWithAmount(budget, firstOfMonth, toUpdate::withAdjustments, adjustments);
    }

    public List<BudgetMonth> updateBudgeted(Budget budget, LocalDate firstOfMonth, String category, BigDecimal budgeted) {
        BudgetMonthCategory toUpdate = budget.month(firstOfMonth).operatingCategory(category);
        return updateCategoryWithAmount(budget, firstOfMonth, toUpdate::withBudgeted, budgeted);
    }

    private List<BudgetMonth> updateCategoryWithAmount(Budget budget, LocalDate firstOfMonth, Function<BigDecimal, BudgetMonthCategory> withMethod, BigDecimal value) {
        List<BudgetMonth> result = new ArrayList<>();

        var timeline = new TreeSet<>(budget.monthKeys());

        BudgetMonth month = budget.month(firstOfMonth);
        var updatedCategory = withMethod.apply(value);
        var category = updatedCategory.name();
        var updatedMonth = month.withOperatingCategory(updatedCategory);
        result.add(updatedMonth);

        var next = timeline.higher(firstOfMonth);

        while (next != null) {
            BudgetMonth nextMonth = budget.month(next);
            boolean nextDoesNotContainCategory = nextMonth.operatingCategory(category) == null;
            boolean updatedClosingIsNotZero = updatedCategory.closing().compareTo(BigDecimal.ZERO) != 0;
            BudgetMonthCategory nextUpdatedCategory;
            if (nextDoesNotContainCategory && updatedClosingIsNotZero) {
                /**
                 * Operating categories that are not declared in the budget
                 * template are treated as month-scoped.
                 *
                 * Such categories are only carried forward into the next month
                 * when their closing balance is non-zero.
                 *
                 * If such a category has a non-zero closing balance but is
                 * absent from the next month, it is recreated explicitly.
                 *
                 * The previous month's closing balance becomes both the opening
                 * and closing balance of the recreated category. Since the
                 * category was absent, it has no activity (budgeted amount,
                 * actual amount, adjustments, or transactions)
                 */
                nextUpdatedCategory = new BudgetMonthCategory(
                        next,
                        category,
                        updatedCategory.closing(),
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        updatedCategory.closing(),
                        List.of());
            } else {
                nextUpdatedCategory = nextMonth.operatingCategory(category).withOpening(updatedCategory.closing());
            }
            var nextUpdatedMonth = nextMonth.withOperatingCategory(nextUpdatedCategory);
            result.add(nextUpdatedMonth);

            // prep for next loop
            next = timeline.higher(next);
            updatedCategory = nextUpdatedCategory;
        }

        return result;
    }
}
