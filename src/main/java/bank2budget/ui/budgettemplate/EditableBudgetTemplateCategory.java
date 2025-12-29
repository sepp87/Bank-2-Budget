package bank2budget.ui.budgettemplate;

import bank2budget.core.budget.BudgetTemplateCategory;
import bank2budget.core.budget.BudgetTemplateCategory.EntryType;
import java.math.BigDecimal;

/**
 *
 * @author joostmeulenkamp
 */
public class EditableBudgetTemplateCategory {

    private BudgetTemplateCategory initialValue;
    private BudgetTemplateCategory domain;

    public EditableBudgetTemplateCategory(BudgetTemplateCategory category) {
        this.initialValue = category;
        this.domain = category;
    }

    public BudgetTemplateCategory toDomain() {
        return domain;
    }

    public EntryType type() {
        return domain.type();
    }

    public String name() {
        return domain.name();
    }

    public BigDecimal budgeted() {
        return domain.budgeted();
    }

    public void setType(EntryType type) {
        domain = domain.withType(type);
    }

    public void setName(String name) {
        domain = domain.withName(name);
    }

    public void setBudgeted(BigDecimal budgeted) {
        domain = domain.withBudgeted(budgeted);
    }
    
    public boolean isEdited() {
        return initialValue != domain;
    }
}
