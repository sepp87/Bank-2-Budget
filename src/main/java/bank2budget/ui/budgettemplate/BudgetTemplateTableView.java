package bank2budget.ui.budgettemplate;

import bank2budget.ui.TableViewUtil;
import bank2budget.ui.tableview.EnhancedTableView;
import java.math.BigDecimal;
import javafx.scene.control.TableColumn;

/**
 *
 * @author joostmeulenkamp
 */
public class BudgetTemplateTableView extends EnhancedTableView<EditableBudgetTemplateCategory> {

    public BudgetTemplateTableView() {
        
//        TableColumn<EditableBudgetTemplateCategory, EntryType> typeColumn = TableViewUtil.buildColumn("Transaction Number", EditableBudgetTemplateCategory::transactionNumber);
//        TableColumn<EditableBudgetTemplateCategory, EntryType> typeColumn = TableViewUtil.buildEditableColumn("Type", EditableBudgetTemplateCategory::type, EditableBudgetTemplateCategory::setType, this::requestFocus);
        TableColumn<EditableBudgetTemplateCategory, String> nameColumn = TableViewUtil.buildEditableTextColumn("Name", EditableBudgetTemplateCategory::name, EditableBudgetTemplateCategory::setName, this::requestFocus);
        TableColumn<EditableBudgetTemplateCategory, BigDecimal> budgetedColumn = TableViewUtil.buildEditableAmountColumn("Budgeted", EditableBudgetTemplateCategory::budgeted, EditableBudgetTemplateCategory::setBudgeted, TableViewUtil.bigDecimalConverter(), this::requestFocus);

//        typeColumn.setPrefWidth(240);
        nameColumn.setPrefWidth(240);
        budgetedColumn.setPrefWidth(240);

//        this.getColumns().add(typeColumn);
        this.getColumns().add(nameColumn);
        this.getColumns().add(budgetedColumn);

//        typeColumn.setSortType(TableColumn.SortType.DESCENDING);
//        this.getSortOrder().add(typeColumn);
        this.sort();
    }
}
