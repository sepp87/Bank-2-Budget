package bank2budget.core.budget;

import static bank2budget.core.budget.BudgetTemplateCategory.EntryType.INCOME;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author joostmeulenkamp
 */
public class BudgetTemplate {

    private static final Logger LOGGER = Logger.getLogger(BudgetTemplate.class.getName());

    private final int firstOfMonth;
    private final Map<String, BudgetTemplateCategory> operatingCategories;

    public BudgetTemplate(int firstOfMonth, Map<String, BudgetTemplateCategory> categories) {
        this.firstOfMonth = validateFirstOfMonth(firstOfMonth);
        this.operatingCategories = categories;
    }

    private int validateFirstOfMonth(int i) {
        if (i > 0 && i < 29) {
            return i;
        } else {
            LOGGER.log(Level.WARNING, "Specified first of month ({0}) does NOT fall within range 1 > 28, using fallback: 1", i);
        }
        return 1;
    }

    public int firstOfMonth() {
        return firstOfMonth;
    }

    public Map<String, BudgetTemplateCategory> operatingCategories() {
        return Map.copyOf(operatingCategories);
    }

    public BudgetMonth createBlank(LocalDate first) {
        List<BudgetMonthCategory> categories = new ArrayList<>();
        for (var entry : operatingCategories.entrySet()) {
            String name = entry.getKey();
            var template = entry.getValue();
            BigDecimal budgeted = template.type() == INCOME ? template.budgeted().negate() : template.budgeted(); // income is negative, because it is allocated to expenses
            BigDecimal zero = BigDecimal.ZERO;
            var category = new BudgetMonthCategory(first, name, zero, zero, budgeted, zero, zero, Collections.emptyList());
            categories.add(category);
        }
        return new BudgetMonth(first, categories);
    }

}
