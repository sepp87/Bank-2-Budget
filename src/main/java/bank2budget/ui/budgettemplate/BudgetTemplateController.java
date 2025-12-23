
package bank2budget.ui.budgettemplate;

import bank2budget.ui.tableview.EnhancedTableController;
import java.util.List;

/**
 *
 * @author joostmeulenkamp
 */
public class BudgetTemplateController extends EnhancedTableController {
    
    private final BudgetTemplateView view;
    
    public BudgetTemplateController(BudgetTemplateView view) {
        super(view.getBudgetTemplateTableView());
        
        this.view = view;
    }
    
    public void load(List<EditableBudgetTemplateCategory> categories) {
        view.getBudgetTemplateTableView().getItems().setAll(categories);
    }
    
    public List<EditableBudgetTemplateCategory> categories() {
        return List.copyOf(view.getBudgetTemplateTableView().getItems());
    }
    
}
