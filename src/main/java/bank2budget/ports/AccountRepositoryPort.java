package bank2budget.ports;

import bank2budget.core.Account;
import java.util.Collection;
import java.util.Map;

/**
 *
 * @author joostmeulenkamp
 */
public interface AccountRepositoryPort {

    Map<String, Account> load();

    void save(Collection<Account> accounts);
}
