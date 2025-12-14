package bank2budget.ui;

import bank2budget.app.report.BudgetReportRow;
import bank2budget.app.report.CategoryRow;
import bank2budget.app.report.SectionRow;
import bank2budget.app.report.TotalRow;
import java.math.BigDecimal;
import java.util.function.BiConsumer;
import javafx.scene.control.TableColumn;
import javafx.util.StringConverter;

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
        return TableViewUtil.buildColumn(
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

        var converter = new StringConverter<BigDecimal>() {

            private BigDecimal previous;

            @Override
            public String toString(BigDecimal value) {
                previous = value;
                return value != null ? value.toPlainString() : "";
            }

            @Override
            public BigDecimal fromString(String string) {
                if (string == null) {
                    return previous;
                }

                string = string.trim().replace(",", ".");

                if (string.matches("^[+-]?[0-9]+(\\.[0-9]+)?$")) {
                    BigDecimal value = new BigDecimal(string);
                    return value.compareTo(previous) == 0 ? previous : value;
                }

                return previous;
            }
        };

        return TableViewUtil.buildEditableAmountColumn("Budgeted",
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
                converter
        );
    }

    private TableColumn<BudgetReportRow, BigDecimal> actualColumn() {
        return TableViewUtil.buildAmountColumn(
                "Actual",
                row -> (row instanceof CategoryRow c)
                        ? c.actual()
                        : null
        );
    }

    private TableColumn<BudgetReportRow, BigDecimal> varianceColumn() {
        return TableViewUtil.buildAmountColumn(
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
