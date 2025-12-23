package bank2budget.ui.dashboard;

import bank2budget.app.BudgetReportService;
import bank2budget.app.BudgetService;
import bank2budget.app.report.BudgetReportRow;
import bank2budget.app.report.CategoryRow;
import bank2budget.app.report.SortBy;
import bank2budget.app.report.SortType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import javafx.event.EventHandler;

/**
 *
 * @author joostmeulenkamp
 */
public class BudgetedVsActualController {

    private final BudgetedVsActualView view;
    private final BudgetReportService reportService;
    private final BudgetService budgetService;
    private SortBy sortBy = SortBy.LABEL;
    private SortType sortType = SortType.ASCENDING;
    private LocalDate selected;
    private EventHandler onEditedHandler;

    public BudgetedVsActualController(BudgetedVsActualView budgetedVsActualView, BudgetReportService budgetReportService, BudgetService budgetService) {
        this.view = budgetedVsActualView;
        this.reportService = budgetReportService;
        this.budgetService = budgetService;

        view.onBudgetedEdited(this::onBudgetedEdit);
    }

    public void reload() {
        load(selected);
    }

    public final void load(LocalDate firstOfMonth) {
        selected = firstOfMonth;
        List<BudgetReportRow> rows = reportService.getBudgetedVsActual(firstOfMonth, sortBy, sortType);
        view.getItems().setAll(rows);
    }

    private void onBudgetedEdit(BudgetReportRow row, BigDecimal newValue) {
        if (row instanceof CategoryRow c) {
            budgetService.setBudgetedForCategory(selected, c.name(), newValue);
            load(selected);
        }
        if (onEditedHandler != null) {
            onEditedHandler.handle(null);
        }
        // issue command here
        // budgetCommandService.updateAdjustment(...);
    }

    public void setOnEdited(EventHandler eh) {
        this.onEditedHandler = eh;
    }
}
