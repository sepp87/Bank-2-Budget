package bank2budget.ui.rules;

import bank2budget.core.rule.RuleConfig;
import bank2budget.ui.tableview.TableConfigurator;
import java.util.Collection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
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
    private final Button addRuleButton;

    public RuleView() {

        // controls
        this.finishButton = new Button("Finish");
        this.cancelButton = new Button("Cancel");
        HBox controls = new HBox(finishButton, cancelButton);
        controls.setAlignment(Pos.CENTER_RIGHT);

        // add rule
        this.addRuleButton = new Button("Add rule");

        // rule table
        this.ruleTable = new TableView<>();
        var configurator = new TableConfigurator<>(ruleTable);
        var categoryColumn = configurator.addEditableTextColumWithAutocomplete("Category", EditableRuleConfig::resultValue, EditableRuleConfig::setResultValue, categorySuggestions);
        configurator.addColumn("WHEN", EditableRuleConfig::when);
        configurator.addEditableChoiceColumn("Field", EditableRuleConfig::checkField, EditableRuleConfig::setCheckField, FXCollections.observableArrayList("contraAccountName", "description"));
        configurator.addColumn("CONTAINS", EditableRuleConfig::operator);
        configurator.addEditableTextColumn("Value", EditableRuleConfig::checkValue, EditableRuleConfig::setCheckValue);
        var removeColumn = configurator.addRemoveButtonColumn();

        removeColumn.setMinWidth(34);
        removeColumn.setMaxWidth(34);

        VBox.setVgrow(ruleTable, Priority.ALWAYS);
        VBox.setVgrow(this, Priority.ALWAYS);

        // build view
        this.getChildren().addAll(controls, addRuleButton, ruleTable);
        this.getStyleClass().add("overlay-modal");

        addRuleButton.setOnAction(e -> {
            ruleTable.requestFocus();
            var row = new EditableRuleConfig(new RuleConfig("", "", "category", ""));
            ruleTable.getItems().add(row);
            var index = ruleTable.getItems().indexOf(row);
            ruleTable.getSelectionModel().clearAndSelect(index, categoryColumn);
            ruleTable.edit(index, categoryColumn);
            ruleTable.scrollTo(row);
        });

    }

    public TableView<EditableRuleConfig> getRuleTable() {
        return ruleTable;
    }

    public void setCategorySuggestions(Collection<String> suggestions) {
        categorySuggestions.setAll(suggestions);
    }

    public Button getFinishButton() {
        return finishButton;
    }

    public Button getCancelButton() {
        return cancelButton;
    }

}
