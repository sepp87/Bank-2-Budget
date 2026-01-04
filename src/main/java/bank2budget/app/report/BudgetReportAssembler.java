package bank2budget.app.report;

import bank2budget.app.report.CategoryRow.CategoryType;

import bank2budget.core.budget.BudgetMonth;
import java.math.BigDecimal;
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

    public List<BudgetReportRow> buildProfitAndLoss(BudgetMonth month, SortBy sortBy, SortType sortType, List<String> exclude) {
        BudgetSectionAssembler sectionAssembler = new BudgetSectionAssembler(sortBy, sortType);
        final var excludeUpper = exclude.stream().map(String::toUpperCase).toList();

        List<BudgetReportRow> result = new ArrayList<>();

        var profitCategories = month.operatingCategories().stream()
                .filter(e -> !excludeUpper.contains(e.name().toUpperCase()))
                .filter(e -> {
                    return e.unadjustedClosing().compareTo(BigDecimal.ZERO) > 0
                            // if unadjustedClosing is ZERO, but there is an positive adjustment
                            || e.unadjustedClosing().compareTo(BigDecimal.ZERO) == 0 && e.adjustments().compareTo(BigDecimal.ZERO) > 0;
                })
                .toList();
        if (!profitCategories.isEmpty()) {
            var profitSection = sectionAssembler.build("Profitable Categories", CategoryType.OPERATING_PROFIT, profitCategories);
            result.addAll(profitSection);
        }

        var lossCategories = month.operatingCategories().stream()
                .filter(e -> !excludeUpper.contains(e.name().toUpperCase()))
                .filter(e -> {
                    return e.unadjustedClosing().compareTo(BigDecimal.ZERO) < 0
                            // if unadjustedClosing is ZERO, but there is an negative adjustment
                            || e.unadjustedClosing().compareTo(BigDecimal.ZERO) == 0 && e.adjustments().compareTo(BigDecimal.ZERO) < 0;
                })
                .toList();
        if (!lossCategories.isEmpty()) {
            var lossSection = sectionAssembler.build("Loss-making Categories", CategoryType.OPERATING_LOSS, lossCategories);
            result.addAll(lossSection);
        }
        var controlCategories = month.controlCategories().stream()
                .filter(e -> e.unadjustedClosing().compareTo(BigDecimal.ZERO) != 0)
                .toList();
        if (!controlCategories.isEmpty()) {
            var controlSection = sectionAssembler.build("Control Categories", CategoryType.CONTROL, controlCategories);
            result.addAll(controlSection);
        }

        var filteredTotals = buildFilteredSubtotalRow(month, excludeUpper);
        result.add(filteredTotals);

        var totals = buildTotalRow(month);
        result.add(totals);

        return result;
    }

    private TotalRow buildFilteredSubtotalRow(BudgetMonth month, List<String> excludeUpper) {

        var excluded = month.operatingCategories().stream().filter(e -> excludeUpper.contains(e.name().toUpperCase())).toList();
        var exOpening = excluded.stream()
                .map(e -> e.opening())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        var exActual = excluded.stream()
                .map(e -> e.actual())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        var exBudgeted = excluded.stream()
                .map(e -> e.budgeted())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        var exVariance = excluded.stream()
                .map(e -> e.variance())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        var exAdjustments = excluded.stream()
                .map(e -> e.adjustments())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        var exClosing = excluded.stream()
                .map(e -> e.closing())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        TotalRow totals = new TotalRow(
                "Subtotal (above categories only)",
                month.opening().subtract(exOpening),
                month.actual().subtract(exActual),
                month.budgeted().subtract(exBudgeted),
                month.variance().subtract(exVariance),
                month.adjustments().subtract(exAdjustments),
                month.closing().subtract(exClosing)
        );
        return totals;
    }
}
