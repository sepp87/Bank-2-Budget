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
public class BudgetMonth {

    private final LocalDate firstOfMonth;
    private final Map<String, BudgetMonthCategory> operatingCategories = new TreeMap<>();
    private final BudgetMonthCategory unappliedIncome;
    private final BudgetMonthCategory unappliedExpenses;

    public BudgetMonth(
            LocalDate firstOfMonth,
            List<BudgetMonthCategory> operating,
            BudgetMonthCategory unappliedIncome,
            BudgetMonthCategory unappliedExpenses) {

        this.firstOfMonth = firstOfMonth;
        for (var category : operating) {
            this.operatingCategories.put(category.name(), category);
        }
        this.unappliedIncome = unappliedIncome;
        this.unappliedExpenses = unappliedExpenses;
    }

    public BudgetMonth(LocalDate firstOfMonth, List<BudgetMonthCategory> operating) {
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

    public BudgetMonthCategory unappliedIncome() {
        return unappliedIncome;
    }

    public BudgetMonthCategory unappliedExpenses() {
        return unappliedExpenses;
    }

    /**
     *
     * @return User-defined categories. Categories involved in day-to-day
     * revenue/expense activity.
     */
    public List<BudgetMonthCategory> operatingCategories() {
        return operatingCategories.values().stream().toList();
    }

    /**
     *
     * @return System categories (unapplied income/expenses). System-held
     * categories used for internal reconciliation, holding unapplied or
     * temporary amounts.
     */
    public List<BudgetMonthCategory> controlCategories() {
        return List.of(unappliedIncome, unappliedExpenses);
    }

    public BudgetMonthCategory operatingCategory(String name) {
        return operatingCategories.get(name);
    }

}
