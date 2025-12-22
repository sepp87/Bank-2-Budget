package bank2budget.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 *
 * @author joostmeulenkamp
 */
public class TransactionReviewView extends VBox {

    private final TransactionTableView transactionTableView;
    private final Button cancelButton;
    private final Button finishButton;

    public TransactionReviewView() {

        finishButton = new Button("Finish");
        cancelButton = new Button("Cancel");
        HBox controls = new HBox(finishButton, cancelButton);
        controls.setAlignment(Pos.CENTER_RIGHT);

        transactionTableView = new TransactionTableView();
        VBox.setVgrow(transactionTableView, Priority.ALWAYS);
        VBox.setVgrow(this, Priority.ALWAYS);

        this.getChildren().addAll(controls, transactionTableView);
        this.getStyleClass().add("transaction-review");
    }

    public TransactionTableView getTransactionTableView() {
        return transactionTableView;
    }

    public Button getFinishButton() {
        return finishButton;
    }

    public Button getCancelButton() {
        return cancelButton;
    }
}
