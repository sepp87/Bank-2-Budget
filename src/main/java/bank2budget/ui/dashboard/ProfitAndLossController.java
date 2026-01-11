package bank2budget.ui.dashboard;

import bank2budget.app.BudgetReportService;
import bank2budget.app.BudgetService;
import bank2budget.app.ConfigService;
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
public class ProfitAndLossController {

    private final ProfitAndLossView view;
    private final BudgetReportService reportService;
    private final BudgetService budgetService;
    private final ConfigService configService;
    private SortBy sortBy = SortBy.LABEL;
    private SortType sortType = SortType.ASCENDING;
    private LocalDate selected;
    private EventHandler onEditedHandler;
    private boolean hideCategories = true;

    public ProfitAndLossController(ProfitAndLossView profitAndLossView, BudgetReportService budgetReportService, BudgetService budgetService, ConfigService configService) {
        this.view = profitAndLossView;
        this.reportService = budgetReportService;
        this.budgetService = budgetService;
        this.configService = configService;

        view.onAdjustmentEdited(this::onAdjustmentEdit);
    }
    
    public void togglePnlCategories() {
        hideCategories = !hideCategories;
        reload();
    }

    public void reload() {
        load(selected);
    }

    public final void load(LocalDate firstOfMonth) {
        selected = firstOfMonth;
        List<String> excludeCategories = hideCategories ? configService.excludePnlCategories() : List.of();
        List<BudgetReportRow> rows = reportService.getProfitAndLoss(firstOfMonth, sortBy, sortType, excludeCategories);
        view.getItems().setAll(rows);
    }

    private void onAdjustmentEdit(BudgetReportRow row, BigDecimal newValue) {
        if (row instanceof CategoryRow c) {
            budgetService.setAdjustmentsForCategory(selected, c.name(), newValue);
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
