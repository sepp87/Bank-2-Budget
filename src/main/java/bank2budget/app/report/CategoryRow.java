package bank2budget.app.report;

import java.math.BigDecimal;

/**
 *
 * @author joostmeulenkamp
 */
public record CategoryRow(
        CategoryType type,
        String name,
        BigDecimal opening,
        BigDecimal actual,
        BigDecimal budgeted,
        BigDecimal variance,
        BigDecimal unadjustedClosing,
        BigDecimal adjustments,
        BigDecimal closing)
        implements BudgetReportRow {


    public enum CategoryType {
        OPERATING_PROFIT,
        OPERATING_LOSS,
        OPERATING_INCOME,
        OPERATING_EXPENSE,
        CONTROL
    }
}
