package bank2budget.application;

import bank2budget.adapters.repository.AnalyticsDatabase;
import bank2budget.core.Account;
import bank2budget.core.MultiAccountBudget;
import bank2budget.ports.AnalyticsExportPort;
import java.util.Collection;

/**
 *
 * @author joostmeulenkamp
 */
public class AnalyticsExportService implements AnalyticsExportPort {

    private final AnalyticsDatabase db;

    public AnalyticsExportService(AnalyticsDatabase db) {
        this.db = db;
    }

    public void exportAccounts(Collection<Account> accounts) {
        for (Account account : accounts) {
            db.insertTransactions(account.getAllTransactionsAscending());
        }
    }

    public void exportBudget(MultiAccountBudget budget) {
        db.insertMonthlyBudgets(budget.getMonthlyBudgets().values());
    }

}
