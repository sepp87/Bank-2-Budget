package bank2budget.app.report;

/**
 *
 * @author joostmeulenkamp
 */
public sealed interface BudgetReportRow permits SectionRow, CategoryRow, TotalRow {
    
}
