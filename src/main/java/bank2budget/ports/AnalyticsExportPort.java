package bank2budget.ports;

import bank2budget.core.Account;
import bank2budget.core.budget.Budget;
import java.util.Collection;

/**
 *
 * @author joostmeulenkamp
 */
public interface AnalyticsExportPort {
    
    void exportAccounts(Collection<Account> accounts) ;
    void exportBudget(Budget budget);
}
