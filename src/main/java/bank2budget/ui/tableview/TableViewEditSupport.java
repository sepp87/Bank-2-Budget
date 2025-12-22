package bank2budget.ui.tableview;

import bank2budget.ui.platform.ModifierKey;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;

/**
 *
 * @author joostmeulenkamp
 */
public class TableViewEditSupport {

    public static <S> void onKeyTyped(KeyEvent event, TableView<S> view) {

        if (ModifierKey.isKeyDown(event)) {
            return;
        }
        
        if (view.getEditingCell() != null) {
            return; // already editing
        }

        String ch = event.getCharacter();
        if (ch == null || ch.isBlank()) {
            return;
        }

        var selectedCells = view.getSelectionModel().getSelectedCells();
        if (selectedCells.size() != 1) {
            return;
        }

        var pos = selectedCells.get(0);
        var row = pos.getRow();
        var column = pos.getTableColumn();

        view.edit(row, column);

        Platform.runLater(() -> {
            Node node = view.lookup(".text-field");
            if (node instanceof TextField textField) {
                textField.setText(ch);
                textField.positionCaret(1);
            }
        });

        event.consume();

    }
}
