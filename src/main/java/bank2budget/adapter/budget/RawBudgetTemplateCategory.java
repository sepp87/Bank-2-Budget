package bank2budget.adapter.budget;

import bank2budget.core.budget.BudgetTemplateCategory.EntryType;
import java.math.BigDecimal;

/**
 *
 * @author joostmeulenkamp
 */
public record RawBudgetTemplateCategory(
        EntryType type,
        String name,
        BigDecimal budgeted) {

}
