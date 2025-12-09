package bank2budget.core.budget;

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
    private final Map<String, BigDecimal> operatingCategories;

    public BudgetTemplate(int firstOfMonth, Map<String, BigDecimal> categories) {
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

    public Map<String, BigDecimal> operatingCategories() {
        return Map.copyOf(operatingCategories);
    }

    public BudgetMonth createBlank(LocalDate first) {
        List<BudgetMonthCategory> categories = new ArrayList<>();
        for (var entry : operatingCategories.entrySet()) {
            String name = entry.getKey();
            BigDecimal budgeted = entry.getValue();
            BigDecimal zero = BigDecimal.ZERO;
            var category = new BudgetMonthCategory(first, name, budgeted, zero, zero, zero, zero, Collections.emptyList());
            categories.add(category);
        }
        return new BudgetMonth(first, categories);
    }

}
