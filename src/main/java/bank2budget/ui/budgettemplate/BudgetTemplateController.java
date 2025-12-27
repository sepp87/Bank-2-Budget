package bank2budget.ui.budgettemplate;

import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

/**
 *
 * @author joostmeulenkamp
 */
public class BudgetTemplateController {

    private final BudgetTemplateView view;

    public BudgetTemplateController(BudgetTemplateView view) {
        this.view = view;
        
    }
    
    public void setFirstOfMonth(int first) {
        view.setFirstOfMonth(first);
    }

    public void load(List<EditableBudgetTemplateCategory> categories) {
        view.getBudgetTemplateTableView().getItems().setAll(categories);
        view.getBudgetTemplateTableView().sort();
    }

    public List<EditableBudgetTemplateCategory> categories() {
        return List.copyOf(view.getBudgetTemplateTableView().getItems());
    }

    public void setOnFinished(EventHandler<ActionEvent> eh) {
        view.getFinishButton().setOnAction(eh);
    }

    public void setOnCanceled(EventHandler<ActionEvent> eh) {
        view.getCancelButton().setOnAction(eh);
    }

}
