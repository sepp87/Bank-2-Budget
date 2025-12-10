package bank2budget.ports;


/**
 *
 * @author joostmeulenkamp
 */
public interface BudgetRepositoryPort<T> {
    
    T load();
    
    void save(T budget);
}
