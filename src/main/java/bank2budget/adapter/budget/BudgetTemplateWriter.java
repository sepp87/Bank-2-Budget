package bank2budget.adapter.budget;

import bank2budget.core.budget.BudgetTemplate;
import java.nio.file.Path;

/**
 *
 * @author joostmeulenkamp
 */
public class BudgetTemplateWriter {

    private final Path budgetTemplateFile;

    public BudgetTemplateWriter(Path budgetTemplateFile) {
        this.budgetTemplateFile = budgetTemplateFile;
    }
    
    public void write(BudgetTemplate template) {
        
    }
}
