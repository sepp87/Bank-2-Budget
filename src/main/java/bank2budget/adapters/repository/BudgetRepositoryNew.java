package bank2budget.adapters.repository;

import bank2budget.adapters.reader.BudgetReaderNew;
import bank2budget.adapters.writer.BudgetWriterNew;
import bank2budget.core.budget.Budget;
import bank2budget.ports.BudgetRepositoryPort;

/**
 *
 * @author joostmeulenkamp
 */
public class BudgetRepositoryNew implements BudgetRepositoryPort<Budget> {

    private final BudgetReaderNew reader;
    private final BudgetWriterNew writer;

    public BudgetRepositoryNew(BudgetReaderNew reader, BudgetWriterNew writer) {
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
