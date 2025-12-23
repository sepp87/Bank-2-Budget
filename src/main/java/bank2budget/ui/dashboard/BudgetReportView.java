package bank2budget.ui.dashboard;

import bank2budget.app.report.BudgetReportRow;
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

        //        setFixedCellSize(26); // match your row height
        prefHeightProperty().bind(
                Bindings.size(getItems())
                        .multiply(26)
                        .add(30) // header height (approx)
        );
    }
}
