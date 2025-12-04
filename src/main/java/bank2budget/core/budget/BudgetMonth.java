package bank2budget.core.budget;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author joostmeulenkamp
 */
public class BudgetMonth<T> {

    private final LocalDate firstOfMonth;
    private final Map<String, BudgetMonthCategory<T>> operatingCategories = new TreeMap<>();
    private final BudgetMonthCategory<T> unappliedIncome;
    private final BudgetMonthCategory<T> unappliedExpenses;

    public BudgetMonth(
            LocalDate firstOfMonth,
            List<BudgetMonthCategory<T>> categories,
            BudgetMonthCategory<T> unappliedIncome,
            BudgetMonthCategory<T> unappliedExpenses) {

        this.firstOfMonth = firstOfMonth;
        for (var category : categories) {
            this.operatingCategories.put(category.name(), category);
        }
        this.unappliedIncome = unappliedIncome;
        this.unappliedExpenses = unappliedExpenses;
    }

    public BudgetMonth(LocalDate firstOfMonth, List<BudgetMonthCategory<T>> operating) {
        this(firstOfMonth);
        for (var category : operating) {
            operatingCategories.put(category.name(), category);
        }
    }

    public BudgetMonth(LocalDate firstOfMonth) {
        this.firstOfMonth = firstOfMonth;
        BigDecimal zero = BigDecimal.ZERO;
        this.unappliedIncome = BudgetMonthCategory.createUnappliedIncome(firstOfMonth, zero, zero, zero, Collections.emptyList());
        this.unappliedExpenses = BudgetMonthCategory.createUnappliedExpenses(firstOfMonth, zero, zero, zero, Collections.emptyList());
    }

    public LocalDate firstOfMonth() {
        return firstOfMonth;
    }

    public BudgetMonthCategory<T> unappliedIncome() {
        return unappliedIncome;
    }

    public BudgetMonthCategory<T> unappliedExpenses() {
        return unappliedExpenses;
    }

    /**
     *
     * @return User-defined categories. Categories involved in day-to-day
     * revenue/expense activity.
     */
    public List<BudgetMonthCategory<T>> operatingCategories() {
        return operatingCategories.values().stream().toList();
    }

    /**
     *
     * @return System categories (unapplied income/expenses). System-held
     * categories used for internal reconciliation, holding unapplied or
     * temporary amounts.
     */
    public List<BudgetMonthCategory<T>> controlCategories() {
        return List.of(unappliedIncome, unappliedExpenses);
    }

    public BudgetMonthCategory<T> operatingCategory(String name) {
        return operatingCategories.get(name);
    }

}
