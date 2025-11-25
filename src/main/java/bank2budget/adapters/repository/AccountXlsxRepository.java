package bank2budget.adapters.repository;

import bank2budget.adapters.reader.AccountReader;
import bank2budget.adapters.writer.AccountWriter;
import bank2budget.core.Account;
import bank2budget.core.CashTransaction;
import java.util.Collection;
import java.util.List;
import bank2budget.core.AccountRepositoryPort;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

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
    public List<Account> load() {
        List<Account> accounts = new ArrayList<>();
        Map<String, List<CashTransaction>> accountTransactions = reader.getPerSheet();
        for (Entry<String, List<CashTransaction>> entry : accountTransactions.entrySet()) {
            String accountNumber = entry.getKey();
            List<CashTransaction> transactions = entry.getValue();
            Account a = new Account(accountNumber, transactions);
            accounts.add(a);
        }
        return accounts;
    }

    @Override
    public void save(Collection<Account> accounts) {
        writer.write(accounts);
    }

}
