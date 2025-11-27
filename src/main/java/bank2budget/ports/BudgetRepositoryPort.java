package bank2budget.ports;

import bank2budget.core.MultiAccountBudget;

/**
 *
 * @author joostmeulenkamp
 */
public interface BudgetRepositoryPort {
    
    MultiAccountBudget load();
    
    void save(MultiAccountBudget budget);
}
