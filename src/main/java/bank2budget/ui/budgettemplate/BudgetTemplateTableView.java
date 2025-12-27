package bank2budget.ui.budgettemplate;

import bank2budget.core.budget.BudgetTemplateCategory.EntryType;
import bank2budget.ui.tableview.TableConfigurator;
import java.math.BigDecimal;
import javafx.collections.FXCollections;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 *
 * @author joostmeulenkamp
 */
public class BudgetTemplateTableView extends TableView<EditableBudgetTemplateCategory> {

    public BudgetTemplateTableView() {

        var configurator = new TableConfigurator<>(this);

        var values = FXCollections.observableArrayList(EntryType.values());
        TableColumn<EditableBudgetTemplateCategory, EntryType> typeColumn = configurator.addEditableChoiceColumn("Type", EditableBudgetTemplateCategory::type, EditableBudgetTemplateCategory::setType, values);
        TableColumn<EditableBudgetTemplateCategory, String> nameColumn = configurator.addEditableTextColumn("Name", EditableBudgetTemplateCategory::name, EditableBudgetTemplateCategory::setName);
        TableColumn<EditableBudgetTemplateCategory, BigDecimal> budgetedColumn = configurator.addEditableAmountColumn("Budgeted", EditableBudgetTemplateCategory::budgeted, EditableBudgetTemplateCategory::setBudgeted);

        typeColumn.setPrefWidth(240);
        nameColumn.setPrefWidth(240);
        budgetedColumn.setPrefWidth(240);

        typeColumn.setSortType(TableColumn.SortType.DESCENDING);
        this.getSortOrder().add(typeColumn);
        this.sort();
    }
}
