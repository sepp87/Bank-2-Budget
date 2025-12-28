package bank2budget.adapter.budget;

import bank2budget.core.Util;
import bank2budget.core.budget.BudgetTemplate;
import bank2budget.core.budget.BudgetTemplateCategory;
import static bank2budget.core.budget.BudgetTemplateCategory.EntryType.EXPENSE;
import static bank2budget.core.budget.BudgetTemplateCategory.EntryType.INCOME;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author joostmeulenkamp
 */
public class BudgetTemplateWriter {

    private final static String FIRST_OF_MONTH_INFO = "# First of month should be between 1 and 28, default is 1";
    private final static String INCOME_EXPENSE_INFO = """
                                   # Define income/expense categories e.g. EXPENSE;GROCERIES;100
                                   # Decimal values should be separated by a dot e.g. 99.95
                                   # INCOME/EXPENSE;CATEGORY;BUDGETED
                                   """;

    private final Path budgetTemplateFile;

    public BudgetTemplateWriter(Path budgetTemplateFile) {
        this.budgetTemplateFile = budgetTemplateFile;
    }

    public void write(BudgetTemplate template) {

        List<String> result = new ArrayList<>();
        result.add(FIRST_OF_MONTH_INFO);
        result.add("FIRST_OF_MONTH=" + template.firstOfMonth());
        result.add("");
        result.add(INCOME_EXPENSE_INFO);

        result.add("");
        var expense = template.operatingCategories().values().stream().filter(e -> e.type() == EXPENSE).toList();
        result.addAll(serializeCategories(expense));
        
        result.add("");
        var income = template.operatingCategories().values().stream().filter(e -> e.type() == INCOME).toList();
        result.addAll(serializeCategories(income));

        try {
            Files.write(budgetTemplateFile, result, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            Logger.getLogger(BudgetTemplateWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private List<String> serializeCategories(List<BudgetTemplateCategory> categories) {
        return categories.stream()
                .map(e -> {
                    String incomeExpense = Util.padWithTabs(e.type().toString() + ";", 3);
                    String category = Util.padWithTabs(e.name() + ";", 5);
                    String budgeted = e.budgeted().toPlainString();
                    return incomeExpense + category + budgeted;
                })
                .toList();
    }

}
