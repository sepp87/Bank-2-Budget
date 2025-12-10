package bank2budget.application;

import bank2budget.core.Account;
import bank2budget.core.budget.Budget;
import bank2budget.ports.AnalyticsExportPort;
import java.util.Collection;

/**
 *
 * @author joostmeulenkamp
 */
public class NoOpAnalyticsExportService implements AnalyticsExportPort {

    @Override
    public void exportAccounts(Collection<Account> accounts) {
    }

    @Override
    public void exportBudget(Budget budget) {
    }


}
