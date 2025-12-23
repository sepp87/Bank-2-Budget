package bank2budget.ui.tableview;

import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableView;

/**
 *
 * @author joostmeulenkamp
 */
public class EnhancedTableView<T> extends TableView<T> {

    public EnhancedTableView() {
        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        this.setEditable(true);
        this.getSelectionModel().setCellSelectionEnabled(true);
        this.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }
}
