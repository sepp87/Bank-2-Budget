package bank2budget.app.report;

import static bank2budget.app.report.SortBy.ADJUSTMENTS;
import static bank2budget.app.report.SortBy.CLOSING;
import static bank2budget.app.report.SortBy.LABEL;
import bank2budget.core.budget.BudgetMonthCategory;
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
                    cat.adjustments(),
                    cat.closing()
            );
            result.add(row);
        }

        return sort(result);
    }

    private List<CategoryRow> sort(List<CategoryRow> rows) {

        Comparator<CategoryRow> comparator = switch (sortBy) {
            case LABEL ->
                Comparator.comparing(CategoryRow::label);
            case OPENING ->
                Comparator.comparing(CategoryRow::opening);
            case ACTUAL ->
                Comparator.comparing(CategoryRow::actual);
            case BUDGETED ->
                Comparator.comparing(CategoryRow::budgeted);
            case VARIANCE ->
                Comparator.comparing(CategoryRow::variance);
            case ADJUSTMENTS ->
                Comparator.comparing(CategoryRow::adjustments);
            case UNADJUSTED_CLOSING ->
                Comparator.comparing(CategoryRow::unadjustedClosing);
            case CLOSING ->
                Comparator.comparing(CategoryRow::closing);

        };

        var result = rows.stream().sorted(comparator).toList();

        if (sortType == SortType.DESCENDING) {
            return result.reversed();
        }

        return result;
    }
}
