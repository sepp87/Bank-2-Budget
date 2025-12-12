package bank2budget.app.report;

import java.math.BigDecimal;

/**
 *
 * @author joostmeulenkamp
 */
public record CategoryRow(
        CategoryType type,
        String label,
        BigDecimal opening,
        BigDecimal actual,
        BigDecimal budgeted,
        BigDecimal variance,
        BigDecimal adjustments,
        BigDecimal closing)
        implements BudgetReportRow {


    public BigDecimal unadjustedClosing() {
        return opening.add(actual).add(budgeted);
    }

    public enum CategoryType {
        OPERATING_INCOME,
        OPERATING_EXPENSE,
        CONTROL
    }
}
