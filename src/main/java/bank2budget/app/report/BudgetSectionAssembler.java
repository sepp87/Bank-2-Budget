package bank2budget.app.report;

import static bank2budget.app.report.SortBy.ADJUSTMENTS;
import static bank2budget.app.report.SortBy.CLOSING;
import static bank2budget.app.report.SortBy.LABEL;
import static bank2budget.app.report.SortBy.UNADJUSTED_CLOSING;
import bank2budget.core.budget.BudgetMonthCategory;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author joostmeulenkamp
 */
public class BudgetSectionAssembler {

    private final SortBy sortBy;
    private final SortType sortType;

    BudgetSectionAssembler(SortBy sortBy, SortType sortType) {
        this.sortBy = sortBy;
        this.sortType = sortType;
    }

    List<BudgetReportRow> build(String label, CategoryRow.CategoryType type, List<BudgetMonthCategory> categories) {
        List<BudgetReportRow> result = new ArrayList<>();

        SectionRow section = new SectionRow(label);
        result.add(section);

        result.addAll(buildContent(type, categories));

        return result;
    }

    private List<? extends BudgetReportRow> buildContent(CategoryRow.CategoryType type, List<BudgetMonthCategory> categories) {
        List<CategoryRow> result = new ArrayList<>();

        for (var cat : categories) {
            CategoryRow row = new CategoryRow(
                    type,
                    cat.name(),
                    cat.opening(),
                    cat.actual(),
                    cat.budgeted(),
                    cat.variance(),
                    cat.unadjustedClosing(),
                    cat.adjustments(),
                    cat.closing()
            );
            result.add(row);
        }

        return sort(result);
    }

    private List<CategoryRow> sort(List<CategoryRow> rows) {

        Comparator<BigDecimal> amount = Comparator.nullsLast(BigDecimal::compareTo);
        Comparator<CategoryRow> comparator = switch (sortBy) {
            case LABEL ->
                Comparator.comparing(CategoryRow::name);
            case OPENING ->
                Comparator.comparing(CategoryRow::opening, amount);
            case ACTUAL ->
                Comparator.comparing(CategoryRow::actual, amount);
            case BUDGETED ->
                Comparator.comparing(CategoryRow::budgeted, amount);
            case VARIANCE ->
                Comparator.comparing(CategoryRow::variance, amount);
            case UNADJUSTED_CLOSING ->
                Comparator.comparing(CategoryRow::unadjustedClosing, amount);
            case ADJUSTMENTS ->
                Comparator.comparing(CategoryRow::adjustments, amount);
            case CLOSING ->
                Comparator.comparing(CategoryRow::closing, amount);

        };

        var result = rows.stream().sorted(comparator).toList();

        if (sortType == SortType.DESCENDING) {
            return result.reversed();
        }

        return result;
    }
}
