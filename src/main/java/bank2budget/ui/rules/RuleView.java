package bank2budget.ui.rules;

import bank2budget.ui.tableview.TableConfigurator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 *
 * @author joostmeulenkamp
 */
public class RuleView extends VBox {

    private final ObservableList<String> categorySuggestions = FXCollections.observableArrayList();
    private final TableView<EditableRuleConfig> ruleTable;
    private final Button cancelButton;
    private final Button finishButton;

    public RuleView() {
        this.finishButton = new Button("Finish");
        this.cancelButton = new Button("Cancel");
        HBox controls = new HBox(finishButton, cancelButton);
        controls.setAlignment(Pos.CENTER_RIGHT);

        this.ruleTable = new TableView<>();
        var configurator = new TableConfigurator<>(ruleTable);
        configurator.addEditableTextColumWithAutocomplete("Category", EditableRuleConfig::resultValue, EditableRuleConfig::setResultValue, categorySuggestions);
        configurator.addColumn("WHEN", EditableRuleConfig::when);
        configurator.addEditableChoiceColumn("Field", EditableRuleConfig::checkField, EditableRuleConfig::setCheckField, categorySuggestions);
        configurator.addColumn("CONTAINS", EditableRuleConfig::operator);
        configurator.addEditableTextColumn("Value", EditableRuleConfig::checkValue, EditableRuleConfig::setCheckValue);
        VBox.setVgrow(ruleTable, Priority.ALWAYS);
        VBox.setVgrow(this, Priority.ALWAYS);

        this.getChildren().addAll(controls, ruleTable);
        this.getStyleClass().add("overlay-modal");
    }

    public void load(List<EditableRuleConfig> rules) {
        loadCategorySuggestions(rules);
        ruleTable.getItems().setAll(rules);
        ruleTable.sort();
    }

    private void loadCategorySuggestions(List<EditableRuleConfig> rules) {
        categorySuggestions.clear();
        Set<String> availableCategories = new HashSet<>();
        for (EditableRuleConfig r : rules) {
            availableCategories.add(r.resultValue());
        }
        categorySuggestions.addAll(availableCategories);
    }

    public TableView<EditableRuleConfig> getRuleTable() {
        return ruleTable;
    }

    public Button getFinishButton() {
        return finishButton;
    }

    public Button getCancelButton() {
        return cancelButton;
    }

}
