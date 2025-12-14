package bank2budget.app;

import bank2budget.app.report.BudgetReportAssembler;
import bank2budget.app.report.BudgetReportRow;
import bank2budget.app.report.SortBy;
import bank2budget.app.report.SortType;
import bank2budget.core.budget.BudgetMonth;
import java.time.LocalDate;
import java.util.List;

/**
 *
 * @author joostmeulenkamp
 */
public class BudgetReportService {

    private final BudgetService budgetService;
    private final BudgetReportAssembler assembler;

    public BudgetReportService(BudgetService budgetService, BudgetReportAssembler assembler) {
        this.budgetService = budgetService;
        this.assembler = assembler;
    }

    public List<BudgetReportRow> getBudgetedVsActual(LocalDate firstOfMonth, SortBy sortBy, SortType sortType) {
        BudgetMonth month = budgetService.month(firstOfMonth);
        return assembler.buildActualVsBudgeted(month, sortBy, sortType);
    }
    

    public List<BudgetReportRow> getProfitAndLoss(LocalDate firstOfMonth, SortBy sortBy, SortType sortType) {
        BudgetMonth month = budgetService.month(firstOfMonth);
        return assembler.buildProfitAndLoss(month, sortBy, sortType);
    }
    
}
