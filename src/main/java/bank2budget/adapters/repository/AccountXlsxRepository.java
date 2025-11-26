package bank2budget.adapters.repository;

import bank2budget.adapters.reader.AccountReader;
import bank2budget.adapters.writer.AccountWriter;
import bank2budget.core.Account;
import java.util.Collection;
import bank2budget.ports.AccountRepositoryPort;
import java.util.Map;

/**
 *
 * @author joostmeulenkamp
 */
public class AccountXlsxRepository implements AccountRepositoryPort {

    private final AccountReader reader;
    private final AccountWriter writer;

    public AccountXlsxRepository(AccountReader reader, AccountWriter writer) {
        this.reader = reader;
        this.writer = writer;
    }

    @Override
    public Map<String, Account> load() {
        return reader.getAccountsIndex();
    }

    @Override
    public void save(Collection<Account> accounts) {
        writer.write(accounts);
    }

}
