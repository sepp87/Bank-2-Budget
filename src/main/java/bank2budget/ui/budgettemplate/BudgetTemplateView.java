package bank2budget.ui.budgettemplate;

import bank2budget.ui.transaction.TransactionTableView;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 *
 * @author joostmeulenkamp
 */
public class BudgetTemplateView extends VBox {

    private final BudgetTemplateTableView budgetTemplateTableView;
    private final Button cancelButton;
    private final Button finishButton;

    public BudgetTemplateView() {
        this.finishButton = new Button("Finish");
        this.cancelButton = new Button("Cancel");
        HBox controls = new HBox(finishButton, cancelButton);
        controls.setAlignment(Pos.CENTER_RIGHT);
        
        Label firstLabel = new Label("First of Month:");
        Spinner<Integer> firstSpinner = new Spinner<>();
        HBox firstRoot = new HBox(firstLabel, firstSpinner);

        this.budgetTemplateTableView = new BudgetTemplateTableView();
        VBox.setVgrow(budgetTemplateTableView, Priority.ALWAYS);
        VBox.setVgrow(this, Priority.ALWAYS);

        this.getChildren().addAll(controls, firstRoot, budgetTemplateTableView);
        this.getStyleClass().add("budget-template");

    }

    public BudgetTemplateTableView getBudgetTemplateTableView() {
        return budgetTemplateTableView;
    }


    public Button getFinishButton() {
        return finishButton;
    }

    public Button getCancelButton() {
        return cancelButton;
    }
}
