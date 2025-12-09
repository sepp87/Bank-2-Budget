package bank2budget.core.budget;

import bank2budget.core.Account;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author joostmeulenkamp
 */
public class BudgetCalculator {

    public List<BudgetMonth> create(BudgetTemplate template, Budget budget, Collection<Account> accounts) {
        BudgetRangeCalculator rangeCalculator = new BudgetRangeCalculator(template, budget, accounts);

        List<BudgetMonth> before = rangeCalculator.createBeforeBudget();
        List<BudgetMonth> updated = rangeCalculator.createUpdatedBudget();
        List<BudgetMonth> after = rangeCalculator.createAfterBudget();

        List<BudgetMonth> result = new ArrayList<>();
        result.addAll(before);
        result.addAll(updated);
        result.addAll(after);
        
        return result;
    }

}
