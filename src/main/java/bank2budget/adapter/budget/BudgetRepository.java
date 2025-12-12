package bank2budget.adapter.budget;

import bank2budget.core.budget.Budget;
import bank2budget.ports.BudgetRepositoryPort;

/**
 *
 * @author joostmeulenkamp
 */
public class BudgetRepository implements BudgetRepositoryPort<Budget> {

    private final BudgetReader reader;
    private final BudgetWriter writer;

    public BudgetRepository(BudgetReader reader, BudgetWriter writer) {
        this.reader = reader;
        this.writer = writer;
    }

    @Override
    public Budget load() {
        return reader.read();
    }

    @Override
    public void save(Budget budget) {
        if (writer != null) {
            writer.write(budget);
        }
    }

}
