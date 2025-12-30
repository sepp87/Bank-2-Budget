package bank2budget.ui.tableview;

import bank2budget.ui.platform.ModifierKey;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.scene.Node;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;

/**
 *
 * @author joostmeulenkamp
 */
public class TableViewEditSupport {

    public static <S> void onKeyTyped(KeyEvent event, TableView<S> table) {

        if (ModifierKey.isKeyDown(event)) {
            return;
        }

        if (table.getEditingCell() != null) {
            return; // already editing
        }

        String ch = event.getCharacter();
        if (ch == null || ch.isBlank()) {
            return;
        }

        var selectedCells = table.getSelectionModel().getSelectedCells();
        if (selectedCells.size() != 1) {
            return;
        }

        var pos = selectedCells.get(0);
        var row = pos.getRow();
        var column = pos.getTableColumn();

        table.edit(row, column);

        Platform.runLater(() -> {
            Node node = table.lookup(".text-field");
            if (node instanceof TextField textField) {
                textField.setText(ch);
                textField.positionCaret(1);
            }
        });

        event.consume();

    }

    public static <S> void enableEditOnSelect(TableView<S> table) {

        table.getSelectionModel().getSelectedCells().addListener((ListChangeListener<TablePosition>) c -> {
            var selectedCells = table.getSelectionModel().getSelectedCells();
            if (selectedCells.size() != 1) {
                return;
            }

            var pos = selectedCells.get(0);
            var row = pos.getRow();
            var column = pos.getTableColumn();

            if (!table.isEditable() || !column.isEditable()) {
                return;
            }

            if (column.getProperties().containsKey("EDIT_ON_SELECT")) {
                Platform.runLater(() -> table.edit(row, column)); // run later so selection is settled in
            }

        });

    }

}
