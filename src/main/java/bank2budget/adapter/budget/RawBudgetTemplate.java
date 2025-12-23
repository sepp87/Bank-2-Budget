package bank2budget.adapter.budget;

import java.util.List;

/**
 *
 * @author joostmeulenkamp
 */
public record RawBudgetTemplate(
        int firstOfMonth,
        List<RawBudgetTemplateCategory> categories) {

}
