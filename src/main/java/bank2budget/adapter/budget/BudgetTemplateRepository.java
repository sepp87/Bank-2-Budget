package bank2budget.adapter.budget;

import bank2budget.core.budget.BudgetTemplate;
import bank2budget.ports.BudgetTemplateRepositoryPort;

/**
 *
 * @author joostmeulenkamp
 */
public class BudgetTemplateRepository implements BudgetTemplateRepositoryPort {

    private final BudgetTemplateReader reader;
    private final BudgetTemplateWriter writer;

    public BudgetTemplateRepository(BudgetTemplateReader reader, BudgetTemplateWriter writer) {
        this.reader = reader;
        this.writer = writer;
    }

    @Override
    public BudgetTemplate load() {
        return reader.read();
    }

    @Override
    public void save(BudgetTemplate template) {
        writer.write(template);
    }

}
