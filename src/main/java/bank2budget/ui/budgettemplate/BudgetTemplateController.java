package bank2budget.ui.budgettemplate;

import bank2budget.app.BudgetTemplateService;
import bank2budget.core.budget.BudgetTemplate;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

/**
 *
 * @author joostmeulenkamp
 */
public class BudgetTemplateController {

    private final BudgetTemplateView view;
    private final BudgetTemplateService service;

    public BudgetTemplateController(BudgetTemplateView view, BudgetTemplateService budgetTemplateService) {
        this.view = view;
        this.service = budgetTemplateService;
    }

    public void reload() {
        load(service.getTemplate());
    }

    private void load(BudgetTemplate template) {
        var categories = template.operatingCategories().values().stream().map(EditableBudgetTemplateCategory::new).toList();
        loadCategories(categories);
        view.setFirstOfMonth(template.firstOfMonth());
    }

    private void loadCategories(List<EditableBudgetTemplateCategory> categories) {
        view.getBudgetTemplateTableView().getItems().setAll(categories);
        view.getBudgetTemplateTableView().sort();
    }

    public void setOnFinished(EventHandler<ActionEvent> eh) {
        view.getFinishButton().setOnAction(eh);
    }

    public void setOnCanceled(EventHandler<ActionEvent> eh) {
        view.getCancelButton().setOnAction(eh);
    }

    public void commitChanges() {

        var template = service.getTemplate();

        var firstOfMonth = view.getFirstOfMonth();
        if (firstOfMonth != template.firstOfMonth()) {
            service.setFirstOfMonth(firstOfMonth);
        }

        var categories = view.getBudgetTemplateTableView().getItems().stream().map(EditableBudgetTemplateCategory::toDomain).toList();
        var existing = template.operatingCategories().values();

        if (categories.size() != existing.size() || !categories.containsAll(existing)) {
            service.setCategories(categories);
        } 
    }

}
