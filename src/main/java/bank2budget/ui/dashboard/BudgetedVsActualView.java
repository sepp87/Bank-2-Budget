package bank2budget.ui.dashboard;

import bank2budget.app.report.BudgetReportRow;
import bank2budget.app.report.CategoryRow;
import bank2budget.app.report.SectionRow;
import bank2budget.app.report.TotalRow;
import bank2budget.ui.tableview.TableColumnUtil;
import java.math.BigDecimal;
import java.util.function.BiConsumer;
import javafx.scene.control.TableColumn;

/**
 *
 * @author joostmeulenkamp
 */
public class BudgetedVsActualView extends BudgetReportView {

    public BudgetedVsActualView() {

        var categoryColumn = categoryColumn();
        var budgetedColumn = budgetedColumn();
        var actualColumn = actualColumn();
        var varianceColumn = varianceColumn();

        getColumns().add(categoryColumn);
        getColumns().add(budgetedColumn);
        getColumns().add(actualColumn);
        getColumns().add(varianceColumn);

        // 200 and 80
        categoryColumn.setMinWidth(140);
//        categoryColumn.setMaxWidth(5f * Integer.MAX_VALUE);
//        budgetedColumn.setMaxWidth(1f * Integer.MAX_VALUE);
//        actualColumn.setMaxWidth(1f * Integer.MAX_VALUE);
//        varianceColumn.setMaxWidth(1f * Integer.MAX_VALUE);

        budgetedColumn.setMaxWidth(80);
        actualColumn.setMaxWidth(80);
        varianceColumn.setMaxWidth(80);
    }

    private TableColumn<BudgetReportRow, String> categoryColumn() {
        return TableColumnUtil.buildColumn(
                "Category",
                row -> switch (row) {
            case SectionRow s ->
                s.label();
            case CategoryRow c ->
                c.name();
            case TotalRow t ->
                t.label();
        }
        );
    }

    private BiConsumer<CategoryRow, BigDecimal> budgetedEditHandler;

    public void onBudgetedEdited(BiConsumer<CategoryRow, BigDecimal> handler) {
        this.budgetedEditHandler = handler;
    }

    private TableColumn<BudgetReportRow, BigDecimal> budgetedColumn() {

        return TableColumnUtil.buildEditableAmountColumn("Budgeted",
                row -> switch (row) {
            case CategoryRow c ->
                c.budgeted();
            case TotalRow t ->
                t.budgeted();
            default ->
                null;
        },
                (row, value) -> {
                    if (row instanceof CategoryRow c && budgetedEditHandler != null) {
                        budgetedEditHandler.accept(c, value);
                    }
                },
                
                this::requestFocus
        );
    }

    private TableColumn<BudgetReportRow, BigDecimal> actualColumn() {
        return TableColumnUtil.buildAmountColumn(
                "Actual",
                row -> (row instanceof CategoryRow c)
                        ? c.actual()
                        : null
        );
    }

    private TableColumn<BudgetReportRow, BigDecimal> varianceColumn() {
        return TableColumnUtil.buildAmountColumn(
                "Remaining",
                row -> switch (row) {
            case CategoryRow c ->
                c.variance();
            case TotalRow t ->
                t.variance();
            default ->
                null;
        }
        );
    }
}
