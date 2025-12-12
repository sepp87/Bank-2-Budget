package bank2budget.app.report;

import bank2budget.app.report.CategoryRow.CategoryType;

import bank2budget.core.budget.BudgetMonth;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author joostmeulenkamp
 */
public class BudgetReportAssembler {

    public List<BudgetReportRow> buildActualVsBudgeted(BudgetMonth month, SortBy sortBy, SortType sortType) {
        BudgetSectionAssembler sectionAssembler = new BudgetSectionAssembler(sortBy, sortType);
        
        List<BudgetReportRow> result = new ArrayList<>();

        var expenseCategories = month.operatingCategories().stream().filter(e -> e.isExpense()).toList();
        var expenseSection = sectionAssembler.build("Expense Categories", CategoryType.OPERATING_EXPENSE, expenseCategories);
        result.addAll(expenseSection);

        var incomeCategories = month.operatingCategories().stream().filter(e -> e.isIncome()).toList();
        var incomeSection = sectionAssembler.build("Income Categories", CategoryType.OPERATING_INCOME, incomeCategories);
        result.addAll(incomeSection);

        var controlCategories = month.controlCategories();
        var controlSection = sectionAssembler.build("Control Categories", CategoryType.CONTROL, controlCategories);
        result.addAll(controlSection);

        var totals = buildTotalRow(month);
        result.add(totals);
        
        return result;
    }


    private TotalRow buildTotalRow(BudgetMonth month) {
        TotalRow totals = new TotalRow(
                "Total",
                month.opening(),
                month.actual(),
                month.budgeted(),
                month.variance(),
                month.adjustments(),
                month.closing()
        );
        return totals;
    }
}
