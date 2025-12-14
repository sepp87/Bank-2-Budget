package bank2budget.core.budget;

import bank2budget.core.CashTransaction;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 *
 * @author joostmeulenkamp
 */
public record BudgetMonthCategory(
        LocalDate firstOfMonth,
        String name,
        BigDecimal opening,
        BigDecimal actual,
        BigDecimal budgeted,
        BigDecimal adjustments,
        BigDecimal closing,
        List<CashTransaction> transactions) {

    public BigDecimal variance() {
        return actual.add(budgeted);
    }

    public BigDecimal unadjustedClosing() {
        return opening.add(actual).add(budgeted);
    }

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

    public static BudgetMonthCategory createUnappliedIncome(
            LocalDate firstOfMonth,
            BigDecimal opening,
            BigDecimal actual,
            BigDecimal closing,
            List<CashTransaction> transactions) {

        return new BudgetMonthCategory(firstOfMonth, UNAPPLIED_INCOME, opening, actual, BigDecimal.ZERO, BigDecimal.ZERO, closing, transactions);
    }

    public static BudgetMonthCategory createUnappliedExpenses(
            LocalDate firstOfMonth,
            BigDecimal opening,
            BigDecimal actual,
            BigDecimal closing,
            List<CashTransaction> transactions) {

        return new BudgetMonthCategory(firstOfMonth, UNAPPLIED_EXPENSES, opening, actual, BigDecimal.ZERO, BigDecimal.ZERO, closing, transactions);
    }

    public BudgetMonthCategory withBudgeted(BigDecimal updated) {
        BigDecimal newClosing = opening.add(actual).add(updated).add(adjustments);
        return new BudgetMonthCategory(firstOfMonth, name, opening, actual, updated, adjustments, newClosing, transactions);
    }

    public BudgetMonthCategory withAdjustments(BigDecimal updated) {
        BigDecimal newClosing = opening.add(actual).add(budgeted).add(updated);
        return new BudgetMonthCategory(firstOfMonth, name, opening, actual, budgeted, updated, newClosing, transactions);
    }

    public BudgetMonthCategory withOpening(BigDecimal updated) {
        BigDecimal newClosing = updated.add(actual).add(budgeted).add(adjustments);
        return new BudgetMonthCategory(firstOfMonth, name, updated, actual, budgeted, adjustments, newClosing, transactions);
    }
}
