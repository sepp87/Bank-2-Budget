package bank2budget.ui;

import bank2budget.app.report.BudgetReportRow;
import bank2budget.app.report.SectionRow;
import bank2budget.app.report.TotalRow;
import javafx.scene.control.TableRow;

/**
 *
 * @author joostmeulenkamp
 */
class BudgetReportRowFactory extends TableRow<BudgetReportRow> {

    @Override
    protected void updateItem(BudgetReportRow row, boolean empty) {
        super.updateItem(row, empty);
        getStyleClass().removeAll("section", "total");
        setEditable(true);

        if (empty || row == null) {
            return;
        }

        if (row instanceof SectionRow) {
            getStyleClass().add("section");
            setEditable(false);
        }

        if (row instanceof TotalRow) {
            getStyleClass().add("total");
            setEditable(false);
        }
    }
}
