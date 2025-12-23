package bank2budget.core.budget;

import java.math.BigDecimal;

/**
 *
 * @author joostmeulenkamp
 */
public record BudgetTemplateCategory(
        EntryType type,
        String name,
        BigDecimal budgeted) {

    public enum EntryType {
        INCOME,
        EXPENSE
    }

    public BudgetTemplateCategory withType(EntryType updated) {
        return new BudgetTemplateCategory(updated, name, budgeted);
    }

    public BudgetTemplateCategory withName(String updated) {
        return new BudgetTemplateCategory(type, updated, budgeted);
    }

    public BudgetTemplateCategory withBudgeted(BigDecimal updated) {
        return new BudgetTemplateCategory(type, name, updated);
    }
}
