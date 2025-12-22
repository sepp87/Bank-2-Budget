package bank2budget.ui;

import bank2budget.ui.platform.ModifierKey;
import bank2budget.ui.tableview.TableViewClipboardSupport;
import bank2budget.ui.tableview.TableViewEditSupport;
import bank2budget.ui.tableview.TableViewNavigationSupport;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;
import static javafx.scene.input.KeyCode.C;
import static javafx.scene.input.KeyCode.V;
import javafx.scene.input.KeyEvent;

/**
 *
 * @author joostmeulenkamp
 */
public class TransactionTableController {

    private final TransactionTableView view;
    private final ObservableList<EditableCashTransaction> transactions = FXCollections.observableArrayList();

    public TransactionTableController(TransactionTableView transactionsView) {
        this.view = transactionsView;
        
        view.addEventFilter(KeyEvent.KEY_TYPED, e -> {
            TableViewEditSupport.onKeyTyped(e, view);;
        });

        view.setOnKeyPressed((e) -> {
            handleShortcutTriggered(e, view);
        });
    }

    public void load(ObservableList<EditableCashTransaction> transactions) {
        this.transactions.clear();
        this.transactions.addAll(transactions);
        this.view.load(transactions);
    }

    public ObservableList<EditableCashTransaction> transactions() {
        return transactions;
    }

    public static void handleShortcutTriggered(KeyEvent event, TableView<EditableCashTransaction> view) {

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
