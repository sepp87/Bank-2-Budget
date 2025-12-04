package bank2budget.core.budget;

import bank2budget.core.CashTransaction;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 *
 * @author joostmeulenkamp
 */
public record BudgetMonthCategory<T>(
        LocalDate firstOfMonth,
        String name,
        BigDecimal budgeted,
        BigDecimal actual,
        BigDecimal opening,
        BigDecimal closing,
        BigDecimal adjustments,
        List<T> transactions) {

    private final static String UNAPPLIED_INCOME = "UNAPPLIED INCOME";
    private final static String UNAPPLIED_EXPENSES = "UNAPPLIED EXPENSES";

    public boolean isUnappliedIncome() {
        return UNAPPLIED_INCOME.equals(this.name);
    }

    public boolean isUnappliedExpenses() {
        return UNAPPLIED_EXPENSES.equals(this.name);
    }

    public boolean isIncome() {
        return budgeted.compareTo(BigDecimal.ZERO) < 0;
    }

    public boolean isExpense() {
        return !isIncome();
    }

    public static <T> BudgetMonthCategory<T> createUnappliedIncome(
            LocalDate firstOfMonth,
            BigDecimal actual,
            BigDecimal opening,
            BigDecimal closing,
            List<T> transactions) {

        return new BudgetMonthCategory<>(firstOfMonth, UNAPPLIED_INCOME, BigDecimal.ZERO, actual, opening, closing, BigDecimal.ZERO, transactions);
    }

    public static <T> BudgetMonthCategory<T> createUnappliedExpenses(
            LocalDate firstOfMonth,
            BigDecimal actual,
            BigDecimal opening,
            BigDecimal closing,
            List<T> transactions) {

        return new BudgetMonthCategory<>(firstOfMonth, UNAPPLIED_EXPENSES, BigDecimal.ZERO, actual, opening, closing, BigDecimal.ZERO, transactions);
    }

}
