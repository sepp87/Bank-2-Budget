package bank2budget.ui.budgettemplate;

import bank2budget.core.budget.BudgetTemplateCategory.EntryType;
import bank2budget.ui.tableview.TableConfigurator;
import javafx.collections.FXCollections;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 *
 * @author joostmeulenkamp
 */
public class BudgetTemplateTableView extends TableView<EditableBudgetTemplateCategory> {

    private final TableColumn<EditableBudgetTemplateCategory, String> nameColumn;

    public BudgetTemplateTableView() {

        var configurator = new TableConfigurator<>(this);

        var values = FXCollections.observableArrayList(EntryType.values());
        var typeColumn = configurator.addEditableChoiceColumn("Type", EditableBudgetTemplateCategory::type, EditableBudgetTemplateCategory::setType, values);
        this.nameColumn = configurator.addEditableTextColumn("Name", EditableBudgetTemplateCategory::name, EditableBudgetTemplateCategory::setName);
        var budgetedColumn = configurator.addEditableAmountColumn("Budgeted", EditableBudgetTemplateCategory::budgeted, EditableBudgetTemplateCategory::setBudgeted);
        var removeColumn = configurator.addRemoveButtonColumn();

        typeColumn.setPrefWidth(240);
        nameColumn.setPrefWidth(240);
        budgetedColumn.setPrefWidth(240);
        removeColumn.setMinWidth(34);
        removeColumn.setMaxWidth(34);

        typeColumn.setSortType(TableColumn.SortType.DESCENDING);
        this.getSortOrder().add(typeColumn);
        this.sort();
    }
    
    public TableColumn<EditableBudgetTemplateCategory, String> getNameColumn() {
        return nameColumn;
    }
}
