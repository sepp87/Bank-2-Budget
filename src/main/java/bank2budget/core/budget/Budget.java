package bank2budget.core.budget;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @author joostmeulenkamp
 */
public class Budget {

    private final int firstOfMonth;
    private final TreeMap<LocalDate, BudgetMonth> months = new TreeMap<>();
    private final Map<String, BigDecimal> template = new TreeMap<>();

    public Budget(int firstOfMonth) {
        this.firstOfMonth = firstOfMonth;
    }

    public void addMonth(BudgetMonth month) {
        months.put(month.firstOfMonth(), month);
    }

    /**
     *
     * @return all months in ascending date order.
     */
    public List<BudgetMonth> months() {
        return months.values().stream().toList();
    }

    public List<BudgetMonth> replace(List<BudgetMonth> updated) {
        List<BudgetMonth> replaced = new ArrayList<>();
        for (var month : updated) {
            var existing = months.replace(month.firstOfMonth(), month);
            if (existing != null) {
                replaced.add(existing);
            }
        }
        return replaced;
    }

    public Set<LocalDate> monthKeys() {
        return Collections.unmodifiableSet(months.keySet());
    }
}
