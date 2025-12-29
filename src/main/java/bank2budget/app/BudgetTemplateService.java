package bank2budget.app;

import bank2budget.core.budget.BudgetMonthCategory;
import bank2budget.core.budget.BudgetTemplate;
import bank2budget.core.budget.BudgetTemplateCategory;
import bank2budget.ports.BudgetTemplateRepositoryPort;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author joostmeulenkamp
 */
public class BudgetTemplateService {

    private final BudgetTemplateRepositoryPort repository;
    private BudgetTemplate template;
    private boolean hasChanges = false;

    public BudgetTemplateService(BudgetTemplateRepositoryPort repository) {
        this.repository = repository;
        this.template = repository.load();
    }

    public void setFirstOfMonth(int i) {
        hasChanges = true;
        template = new BudgetTemplate(i, template.operatingCategories());
    }

    public void setCategories(List<BudgetTemplateCategory> categories) {
        hasChanges = true;
        var asMap = categories.stream().collect(Collectors.toMap(BudgetTemplateCategory::name, e -> e));
        template = new BudgetTemplate(template.firstOfMonth(), asMap);
    }

    public BudgetTemplate getTemplate() {
        return template;
    }

    public void save() {
        if (hasChanges) {
            repository.save(template);
        }
    }
}
