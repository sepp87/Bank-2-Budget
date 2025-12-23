package bank2budget.ports;

import bank2budget.core.budget.BudgetTemplate;

/**
 *
 * @author joostmeulenkamp
 */
public interface BudgetTemplateRepositoryPort {

    BudgetTemplate load();

    void save(BudgetTemplate template);
}
