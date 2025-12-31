package bank2budget.ui.transaction;

import bank2budget.app.AccountService;
import bank2budget.core.CashTransaction;
import java.util.List;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

/**
 *
 * @author joostmeulenkamp
 */
public class TransactionReviewController {

    private final TransactionReviewView view;
    private final AccountService accountService;
    private final TransactionTableController transactionTableController;

    public TransactionReviewController(TransactionReviewView transactionReviewView, AccountService accountService) {
        this.view = transactionReviewView;
        this.accountService = accountService;
        this.transactionTableController = new TransactionTableController(view.getTransactionTableView());

    }

    public void load(ObservableList<EditableCashTransaction> transactions) {
        transactionTableController.load(transactions);
    }

    public void setOnFinished(EventHandler<ActionEvent> eh) {
        view.getFinishButton().setOnAction(eh);
    }

    public void setOnCanceled(EventHandler<ActionEvent> eh) {
        view.getCancelButton().setOnAction(eh);

    }

    public void commitChanges() {
        List<CashTransaction> transactions = transactionTableController.transactions().stream().map(EditableCashTransaction::toDomain).toList();
        accountService.updateAccounts(transactions);
    }

}
