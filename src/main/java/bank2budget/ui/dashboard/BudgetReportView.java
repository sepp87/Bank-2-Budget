package bank2budget.ui.dashboard;

import bank2budget.app.report.BudgetReportRow;
import bank2budget.app.report.CategoryRow;
import javafx.beans.binding.Bindings;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableView;

/**
 *
 * @author joostmeulenkamp
 */
public abstract class BudgetReportView extends TableView<BudgetReportRow> {

    public BudgetReportView() {
        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        setEditable(true);
        getSelectionModel().setCellSelectionEnabled(true);
        getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        setRowFactory(tv -> new BudgetReportRowFactory());

        prefHeightProperty().bind(Bindings.createDoubleBinding(this::calculateHeight, getItems()));

    }

    private double calculateHeight() {
        int totalRowCount = getItems().size();
        int normalRowCount = getItems().stream().filter(e -> e instanceof CategoryRow).mapToInt(e -> 1).sum();
        int headerRowCount = totalRowCount - normalRowCount;
        double columnHeader = 28;
        return normalRowCount * 24 + headerRowCount * 34 + columnHeader;

    }
}
