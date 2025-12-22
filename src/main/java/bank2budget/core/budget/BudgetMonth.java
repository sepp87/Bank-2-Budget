package bank2budget.core.budget;

import bank2budget.core.CashTransaction;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

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

    public int financialYear() {
        LocalDate date = firstOfMonth;
        int day = date.getDayOfMonth();
        int month = date.getMonthValue();
        int year = date.getYear();
        if (month == 12 && day > 16) {
            return year + 1;
        }
        return year;
    }

    public int financialMonth() {
        LocalDate date = firstOfMonth;
        if (date.getDayOfMonth() > 16) {
            return date.plusMonths(1).getMonthValue();
        }
        return date.getMonthValue();
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
        return List.copyOf(operatingCategories.values());
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

    public List<BudgetMonthCategory> categories() {
        return Stream.concat(operatingCategories().stream(), controlCategories().stream()).toList();
    }

    public BudgetMonthCategory operatingCategory(String name) {
        return operatingCategories.get(name);
    }

    public BigDecimal opening() {
        return categories().stream()
                .map(BudgetMonthCategory::opening)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal actual() {
        return categories().stream()
                .map(BudgetMonthCategory::actual)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal budgeted() {
        return categories().stream()
                .map(BudgetMonthCategory::budgeted)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal adjustments() {
        return categories().stream()
                .map(BudgetMonthCategory::adjustments)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal closing() {
        return categories().stream()
                .map(BudgetMonthCategory::closing)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal variance() {
        return categories().stream()
                .map(BudgetMonthCategory::variance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<CashTransaction> transactions() {
        return categories().stream()
                .flatMap(category -> category.transactions().stream())
                .sorted(Comparator.comparing(CashTransaction::transactionNumber))
                .toList();
    }

    /**
     * Returns a copy of this month with the provided operating category.
     *
     * @param updated
     * @return
     */
    public BudgetMonth withOperatingCategory(BudgetMonthCategory updated) {
        var updatedMap = new TreeMap<>(operatingCategories);
        updatedMap.put(updated.name(), updated);
        var updatedList = updatedMap.values().stream().toList();
        return new BudgetMonth(firstOfMonth, updatedList, unappliedIncome, unappliedExpenses);
    }

}
