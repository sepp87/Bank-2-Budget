package bank2budget.ports;

import bank2budget.core.Account;
import java.util.List;

/**
 *
 * @author joostmeulenkamp
 */
public interface AccountImporterPort {

    List<Account> importAccounts();
}
