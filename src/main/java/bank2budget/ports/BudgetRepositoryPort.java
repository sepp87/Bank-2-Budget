package bank2budget.ports;

import bank2budget.core.MultiAccountBudget;

/**
 *
 * @author joostmeulenkamp
 */
public interface BudgetRepositoryPort<T> {
    
    T load();
    
    void save(T budget);
}
