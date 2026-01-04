package bank2budget.ui.tableview;

import javafx.application.Platform;
import javafx.scene.control.TableView;

/**
 *
 * @author joostmeulenkamp
 */
public class TableViewNavigationSupport {

    public static <S> void onEnterPressed(TableView<S> table) {
        var selectedCells = table.getSelectionModel().getSelectedCells();
        if (selectedCells.size() != 1) {
            return;

        }

        var pos = selectedCells.get(0);
        int row = pos.getRow() + 1;
        var column = pos.getTableColumn();
        
        Platform.runLater(() -> { // wait for enter to commit changes (if current cell is being edited) before selecting next cell
            table.getSelectionModel().clearAndSelect(row, column);
        });

    }

    public static <S> void onTabPressed(TableView<S> table) {

        var selectedCells = table.getSelectionModel().getSelectedCells();
        if (selectedCells.size() != 1) {
            return;
        }

        var pos = selectedCells.get(0);
        int row = pos.getRow();

        var columns = table.getColumns();
        var column = pos.getTableColumn();
        int index = columns.indexOf(column);

        if (index < 0 || index + 1 >= columns.size()) {
            index = -1;
            row = row + 1;
        }

        var nextColumn = columns.get(index + 1);

        table.getSelectionModel().clearAndSelect(row, nextColumn);
        table.requestFocus();

    }

}
