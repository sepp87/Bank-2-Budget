package bank2budget.ui.tableview;

import javafx.scene.control.TableView;

/**
 *
 * @author joostmeulenkamp
 */
public class TableViewNavigationSupport {

    public static <S> void onEnterPressed(TableView<S> view) {
        var selectedCells = view.getSelectionModel().getSelectedCells();
        if (selectedCells.size() == 1) {

            var pos = selectedCells.get(0);
            int row = pos.getRow() + 1;
            var column = pos.getTableColumn();

            view.getSelectionModel().clearAndSelect(row, column);
        }
    }

    public static <S> void onTabPressed(TableView<S> view) {
        var selectedCells = view.getSelectionModel().getSelectedCells();
        if (selectedCells.size() == 1) {

            var pos = selectedCells.get(0);
            int row = pos.getRow();

            var columns = view.getColumns();
            int index = columns.indexOf(pos.getTableColumn());

            if (index < 0 || index + 1 >= columns.size()) {
                index = -1;
                row = row + 1;
            }

            view.getSelectionModel().clearAndSelect(row, columns.get(index + 1));
            view.requestFocus();
        }
    }
}
