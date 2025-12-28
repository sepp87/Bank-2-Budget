package bank2budget.app;

import bank2budget.core.budget.BudgetTemplate;
import bank2budget.ports.BudgetTemplateRepositoryPort;

/**
 *
 * @author joostmeulenkamp
 */
public class BudgetTemplateService {
    
    private final BudgetTemplateRepositoryPort repository;
    private BudgetTemplate template;
    
    public BudgetTemplateService (BudgetTemplateRepositoryPort repository) {
        this.repository = repository;
        this.template = repository.load();
    }
    
    public BudgetTemplate getTemplate() {
        return template;
    }
    
    public void save() {
        repository.save(template);
    }
}
