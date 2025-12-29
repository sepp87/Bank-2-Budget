package bank2budget.ui.tableview;

import bank2budget.ui.platform.ModifierKey;
import javafx.application.Platform;
import javafx.scene.control.TableView;
import static javafx.scene.input.KeyCode.C;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.V;
import javafx.scene.input.KeyEvent;

/**
 *
 * @author joostmeulenkamp
 */
public class TableViewShortcutSupport {

    public static <S> void shortcutPressed(KeyEvent event, TableView<S> view) {

        boolean isModifierDown = ModifierKey.isKeyDown(event);
        switch (event.getCode()) {

            case C:
                if (isModifierDown) {
                    // copy cell value
                    TableViewClipboardSupport.copySelectionToClipboard(view);
                }
                break;

            case V:
                if (isModifierDown) {
                    // paste value to selected cells
                    TableViewClipboardSupport.pasteFromClipboard(view);
                }
                break;

            case ENTER:
                // select next cell
                TableViewNavigationSupport.onEnterPressed(view);
                break;

            case TAB:
                Platform.runLater(() -> {
                    TableViewNavigationSupport.onTabPressed(view);
                    event.consume();

                });
                break;
        }

    }
}
