package bank2budget.ui;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

/**
 *
 * @author joostmeulenkamp
 */
public class TransactionReviewController {

    private final TransactionReviewView view;
    private final TransactionTableController transactionTableController;

    public TransactionReviewController(TransactionReviewView transactionReviewView) {
        this.view = transactionReviewView;
        this.transactionTableController = new TransactionTableController(view.getTransactionTableView());
    }

    public ObservableList<EditableCashTransaction> transactions() {
        return transactionTableController.transactions();
    }

    public void load(ObservableList<EditableCashTransaction> transactions) {
        transactionTableController.load(transactions);
    }

    public void setOnReviewFinished(EventHandler<ActionEvent> eh) {
        view.getFinishButton().setOnAction(eh);
    }

    public void setOnReviewCanceled(EventHandler<ActionEvent> eh) {
        view.getCancelButton().setOnAction(eh);

    }

}
