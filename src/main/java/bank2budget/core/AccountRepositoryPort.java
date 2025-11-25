package bank2budget.core;

import java.util.Collection;
import java.util.List;

/**
 *
 * @author joostmeulenkamp
 */
public interface AccountRepositoryPort {

    List<Account> load();

    void save(Collection<Account> accounts);
}
