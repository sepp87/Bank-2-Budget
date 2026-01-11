package bank2budget.ui.dashboard;

import bank2budget.app.report.BudgetReportRow;
import bank2budget.app.report.CategoryRow;
import bank2budget.app.report.SectionRow;
import bank2budget.app.report.TotalRow;
import bank2budget.ui.tableview.TableColumnUtil;
import bank2budget.ui.tableview.TableConfigurator;
import java.math.BigDecimal;
import java.util.function.BiConsumer;
import javafx.scene.control.TableColumn;

/**
 *
 * @author joostmeulenkamp
 */
public class ProfitAndLossView extends BudgetReportView {

    public ProfitAndLossView() {

        var configurator = new TableConfigurator<>(this);

        var categoryColumn = categoryColumn();
        var adjustmentsColumn = adjustmentsColumn();
        var closingColumn = closingColumn();

        getColumns().add(categoryColumn);
        getColumns().add(adjustmentsColumn);
        getColumns().add(closingColumn);

        categoryColumn.setMinWidth(140);
        adjustmentsColumn.setMaxWidth(80);
        closingColumn.setMaxWidth(80);

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

    private TableColumn<BudgetReportRow, BigDecimal> unadjustedClosingColumn() {
        return TableColumnUtil.buildAmountColumn(
                "Unadjusted",
                row -> (row instanceof CategoryRow c)
                        ? c.unadjustedClosing()
                        : null
        );
    }

    private BiConsumer<CategoryRow, BigDecimal> adjustmentEditHandler;

    public void onAdjustmentEdited(BiConsumer<CategoryRow, BigDecimal> handler) {
        this.adjustmentEditHandler = handler;
    }

    private TableColumn<BudgetReportRow, BigDecimal> adjustmentsColumn() {

        return TableColumnUtil.buildEditableAmountColumn("Adjustments",
                row -> switch (row) {
            case CategoryRow c ->
                c.adjustments();
            case TotalRow t ->
                t.adjustments();
            default ->
                null;
        },
                (row, value) -> {
                    if (row instanceof CategoryRow c && adjustmentEditHandler != null) {
                        adjustmentEditHandler.accept(c, value);
                    }
                },
                this::requestFocus
        );
    }

    private TableColumn<BudgetReportRow, BigDecimal> closingColumn() {
        return TableColumnUtil.buildAmountColumn(
                "Savings",
                row -> switch (row) {
            case CategoryRow c ->
                c.closing();
            case TotalRow t ->
                t.closing();
            default ->
                null;
        }
        );
    }
//
//    private TableColumn<BudgetReportRow, BigDecimal> withAmountFormatting(TableColumn<BudgetReportRow, BigDecimal> col) {
//        col.setCellFactory(amountCellFactory());
//        return col;
//    }
//
//    private Callback<TableColumn<BudgetReportRow, BigDecimal>, TableCell<BudgetReportRow, BigDecimal>> amountCellFactory() {
//
//        return col -> new TableCell<>() {
//            @Override
//            protected void updateItem(BigDecimal value, boolean empty) {
//                super.updateItem(value, empty);
//
//                if (empty || value == null) {
//                    setText(null);
//                } else {
//                    setText(value.setScale(2, RoundingMode.HALF_UP).toPlainString());
//                }
//
//                setAlignment(Pos.CENTER_RIGHT);
//            }
//        };
//    }

}

//return new TextFieldTableCell<>(converter) {
//
//        @Override
//        public void updateItem(BigDecimal value, boolean empty) {
//            super.updateItem(value, empty);
//
//            if (empty || value == null) {
//                setText(null);
//            } else if (!isEditing()) {
//                setText(value.setScale(2, RoundingMode.HALF_UP).toPlainString());
//            }
//
//            setAlignment(Pos.CENTER_RIGHT);
//        }
//    };
