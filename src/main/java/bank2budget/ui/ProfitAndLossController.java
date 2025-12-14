package bank2budget.ui;

import bank2budget.app.BudgetReportService;
import bank2budget.app.BudgetService;
import bank2budget.app.report.BudgetReportRow;
import bank2budget.app.report.CategoryRow;
import bank2budget.app.report.SortBy;
import bank2budget.app.report.SortType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import javafx.application.Platform;

/**
 *
 * @author joostmeulenkamp
 */
public class ProfitAndLossController {

    private final ProfitAndLossView view;
    private final BudgetReportService reportService;
    private final BudgetService budgetService;
    private SortBy sortBy = SortBy.LABEL;
    private SortType sortType = SortType.ASCENDING;
    private LocalDate selected;

    public ProfitAndLossController(ProfitAndLossView profitAndLossView, BudgetReportService budgetReportService, BudgetService budgetService) {
        this.view = profitAndLossView;
        this.reportService = budgetReportService;
        this.budgetService = budgetService;

        view.onAdjustmentEdited(this::onAdjustmentEdit);
    }

    public final void load(LocalDate firstOfMonth) {
        Platform.runLater(() -> {
            selected = firstOfMonth;
            List<BudgetReportRow> rows = reportService.getProfitAndLoss(firstOfMonth, sortBy, sortType);
            view.getItems().setAll(rows);
        });
    }

    private void onAdjustmentEdit(BudgetReportRow row, BigDecimal newValue) {
        if (row instanceof CategoryRow c) {
            budgetService.setAdjustmentsForCategory(selected, c.name(), newValue);
            load(selected);
        }

        // issue command here
        // budgetCommandService.updateAdjustment(...);
    }
}
