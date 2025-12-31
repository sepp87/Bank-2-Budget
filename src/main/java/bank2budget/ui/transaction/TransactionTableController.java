package bank2budget.ui.transaction;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author joostmeulenkamp
 */
public class TransactionTableController {

    private final TransactionTableView view;
    private final ObservableList<EditableCashTransaction> transactions = FXCollections.observableArrayList();

    public TransactionTableController(TransactionTableView transactionsView) {
        this.view = transactionsView;
    }

    public void load(ObservableList<EditableCashTransaction> transactions) {
        this.transactions.clear();
        this.transactions.addAll(transactions);
        this.view.load(transactions);
    }

    public ObservableList<EditableCashTransaction> transactions() {
        return transactions;
    }

}
