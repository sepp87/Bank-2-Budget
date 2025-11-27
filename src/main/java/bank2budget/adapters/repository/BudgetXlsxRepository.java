package bank2budget.adapters.repository;

import bank2budget.adapters.reader.BudgetReaderForXlsx;
import bank2budget.adapters.writer.BudgetWriterForXlsx;
import bank2budget.core.MultiAccountBudget;
import bank2budget.ports.BudgetRepositoryPort;

/**
 *
 * @author joostmeulenkamp
 */
public class BudgetXlsxRepository implements BudgetRepositoryPort {

    private final BudgetReaderForXlsx reader;
    private final BudgetWriterForXlsx writer;

    public BudgetXlsxRepository(BudgetReaderForXlsx reader, BudgetWriterForXlsx writer) {
        this.reader = reader;
        this.writer = writer;
    }

    @Override
    public MultiAccountBudget load() {
        return reader.read();
    }

    @Override
    public void save(MultiAccountBudget budget) {
        writer.write(budget);
    }

}
