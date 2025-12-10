package bank2budget.core.budget;

import bank2budget.core.Account;
import bank2budget.core.CashTransactionDomainLogic;
import bank2budget.core.CashTransaction;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 * @author joostmeulenkamp
 */
class BudgetRangeCalculator {

    private final BudgetTemplate template;
    private final Budget budget;

    private final BudgetTimeline timeline;
    private final TransactionGrouper byMonth;

    private final TreeMap<LocalDate, BudgetMonth> months = new TreeMap<>();

    BudgetRangeCalculator(BudgetTemplate template, Budget budget, Collection<Account> accounts) {
        this.template = template;
        this.budget = budget;

        this.timeline = new BudgetTimeline(template, budget, accounts);
        this.byMonth = new TransactionGrouper(accounts, timeline.firstsForGrouping());
    }

    /**
     * create new months (from template) for all transactions
     *
     * @return
     */
    public List<BudgetMonth> createBeforeBudget() {
        return createMonths(timeline.firstsBeforeBudget());
    }

    /**
     * create updated months for all months contained within the budget
     *
     * @return
     */
    public List<BudgetMonth> createUpdatedBudget() {
        return createMonths(timeline.firstsOfBudget());
    }

    /**
     * create new months (from template) for all transactions
     *
     * @return
     */
    public List<BudgetMonth> createAfterBudget() {
        return createMonths(timeline.firstsAfterBudget());
    }

    private List<BudgetMonth> createMonths(TreeSet<LocalDate> selectedFirsts) {
        List<BudgetMonth> result = new ArrayList<>();

        var allFirsts = timeline.firstsForGrouping();
        for (var first : selectedFirsts) {

            LocalDate previousFirst = allFirsts.lower(first);
            BudgetMonth previous = previousFirst == null ? null : months.get(previousFirst); // previous (updated) month to calculate carryover

            BudgetMonth month = existingOrBlank(first);
            List<CashTransaction> selected = byMonth.transactions(first);

            BudgetMonth updated = createMonth(previous, month, selected);

            months.put(first, updated);
            result.add(updated);
        }

        return result;
    }

    private BudgetMonth existingOrBlank(LocalDate first) {
        return budget.month(first) != null ? budget.month(first) : template.createBlank(first);
    }

    private BudgetMonth createMonth(BudgetMonth previous, BudgetMonth current, List<CashTransaction> transactions) {
        
        var categorized = CashTransactionDomainLogic.categorized(transactions);
        var uncategorized = CashTransactionDomainLogic.uncategorized(transactions);

        var operating = CategoryCalculator.createOperatingCategories(template, previous, current, categorized);
        var unappliedIncome = CategoryCalculator.createUnappliedIncome(previous, current, uncategorized);
        var unappliedExpenses = CategoryCalculator.createUnappliedExpenses(previous, current, uncategorized);

        BudgetMonth updated = new BudgetMonth(current.firstOfMonth(), operating, unappliedIncome, unappliedExpenses);

        return updated;
    }

}
