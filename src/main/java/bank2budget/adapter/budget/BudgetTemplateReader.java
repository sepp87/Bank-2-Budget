package bank2budget.adapter.budget;

import bank2budget.core.budget.BudgetTemplate;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author joost
 */
public class BudgetTemplateReader {

    private final Path budgetTemplateFile;

    public BudgetTemplateReader(Path budgetTemplateFile) {
        this.budgetTemplateFile = budgetTemplateFile;
    }

    public BudgetTemplate read() {
        try {
            return readFrom(budgetTemplateFile);
        } catch (IOException ex) {
            Logger.getLogger(BudgetTemplateReader.class.getName()).log(Level.INFO, "Could NOT read file, proceeding without {0}", budgetTemplateFile.getFileName());
        }
        return null;
    }

    private BudgetTemplate readFrom(Path path) throws IOException {
        List<String> lines = Files.readAllLines(path);
        int firstOfMonth = getFirstOfMonth(lines);
        Map<String, BigDecimal> categories = getCategories(lines);
        return new BudgetTemplate(firstOfMonth, categories);
    }

    private int getFirstOfMonth(List<String> lines) {
        int firstOfMonth = 1;
        for (String line : lines) {
            if (line.strip().startsWith("FIRST_OF_MONTH")) {
                try {
                    String raw = line.split("=")[1].strip();
                    firstOfMonth = Integer.parseInt(raw);
                    break;
                } catch (Exception e) {
                    Logger.getLogger(BudgetTemplateReader.class.getName()).log(Level.INFO, "Could NOT read first of month, using fallback: {0}", firstOfMonth);
                }
            }
        }
        return firstOfMonth;
    }

    private Map<String, BigDecimal> getCategories(List<String> lines) {
        Map<String, BigDecimal> result = new TreeMap<>();
        for (String line : lines) {
            try {
                String upper = line.strip().toUpperCase();
                if (upper.startsWith("INCOME")) {
                    String[] parts = line.split(";");
                    String category = parts[1].strip();
                    BigDecimal budgeted = new BigDecimal(parts[2].strip()).negate(); // income is negative, because it is allocated to expenses
                    result.put(category, budgeted);
                } else if (upper.startsWith("EXPENSE")) {
                    String[] parts = line.split(";");
                    String category = parts[1].strip();
                    BigDecimal budgeted = new BigDecimal(parts[2].strip());
                    result.put(category, budgeted);
                }
            } catch (Exception e) {
                Logger.getLogger(BudgetTemplateReader.class.getName()).log(Level.INFO, "Could NOT read category, proceeding without {0}", line);
            }
        }
        return result;
    }

}
