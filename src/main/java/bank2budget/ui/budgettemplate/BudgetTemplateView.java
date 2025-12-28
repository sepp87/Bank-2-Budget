package bank2budget.ui.budgettemplate;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.converter.IntegerStringConverter;

/**
 *
 * @author joostmeulenkamp
 */
public class BudgetTemplateView extends VBox {

    private final Spinner<Integer> firstOfMonthSpinner;
    private final BudgetTemplateTableView budgetTemplateTableView;
    private final Button cancelButton;
    private final Button finishButton;

    public BudgetTemplateView() {
        this.finishButton = new Button("Finish");
        this.cancelButton = new Button("Cancel");
        HBox controls = new HBox(finishButton, cancelButton);
        controls.setAlignment(Pos.CENTER_RIGHT);

        Label firstLabel = new Label("First of Month:");
        this.firstOfMonthSpinner = firstOfMonthSpinner();
        HBox firstRoot = new HBox(firstLabel, firstOfMonthSpinner);
        firstRoot.setAlignment(Pos.CENTER_LEFT);

        this.budgetTemplateTableView = new BudgetTemplateTableView();
        VBox.setVgrow(budgetTemplateTableView, Priority.ALWAYS);
        VBox.setVgrow(this, Priority.ALWAYS);

        this.getChildren().addAll(controls, firstRoot, budgetTemplateTableView);
        this.getStyleClass().add("overlay-modal");

    }

    private Spinner<Integer> firstOfMonthSpinner() {
        Spinner<Integer> spinner = new Spinner<>();
        spinner.setEditable(true);
        TextFormatter<Integer> formatter = new TextFormatter<>(
                new IntegerStringConverter(),
                spinner.getValue(),
                change -> {
                    String text = change.getControlNewText();
                    return text.matches("^(?:[1-9]|1\\d|2[0-8])?$")
                    ? change
                    : null;
                }
        );

        spinner.getEditor().setTextFormatter(formatter);
        spinner.setValueFactory(new SafeIntegerSpinnerValueFactory(1, 28, 1));
        spinner.getValueFactory().setWrapAround(true);
        spinner.getValueFactory().valueProperty().bindBidirectional(formatter.valueProperty());

        spinner.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) {
                spinner.increment(0); // force commit
            }
        });
        return spinner;
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

    public void setFirstOfMonth(int first) {
        firstOfMonthSpinner.getValueFactory().setValue(first);
    }
}
