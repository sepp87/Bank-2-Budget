package bank2budget.ui.tableview;

import javafx.scene.input.KeyEvent;
import bank2budget.ui.platform.ModifierKey;

/**
 *
 * @author joostmeulenkamp
 */
public class EnhancedTableController {

    private final EnhancedTableView<?> view;

    public <T> EnhancedTableController(EnhancedTableView<T> view) {
        this.view = view;

        view.addEventFilter(KeyEvent.KEY_TYPED, e -> {
            TableViewEditSupport.onKeyTyped(e, view);;
        });

        view.setOnKeyPressed((e) -> {
            handleShortcutTriggered(e, view);
        });
    }

    public static <T> void handleShortcutTriggered(KeyEvent event, EnhancedTableView<T> view) {

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
        }

    }
}
