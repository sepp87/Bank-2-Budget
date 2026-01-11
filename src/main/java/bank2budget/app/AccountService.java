package bank2budget.app;

import bank2budget.core.Account;
import bank2budget.core.AccountDomainLogic;
import bank2budget.core.CashTransaction;
import bank2budget.core.CashTransactionDomainLogic;
import bank2budget.core.IntegrityChecker;
import bank2budget.ports.AccountRepositoryPort;
import bank2budget.ports.AccountImporterPort;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 *
 * @author joostmeulenkamp
 */
public class AccountService {

    private final AccountRepositoryPort repository;
    private final AccountImporterPort importer;
    private final RuleService ruleService;
    private Map<String, Account> accountIndex = new TreeMap<>();

    public AccountService(AccountRepositoryPort repository, AccountImporterPort importer, RuleService ruleService) {
        this.repository = repository;
        this.importer = importer;
        this.ruleService = ruleService;
        load();
    }

    private void load() {
        applyRules(repository.load().values()).forEach(e -> accountIndex.put(e.getAccountNumber(), e));
    }

    private final List<Runnable> onAccountsUpdatedListeners = new ArrayList<>();

    public void setOnAccountsUpdated(Runnable r) {
        onAccountsUpdatedListeners.add(r);
    }

    private void onAccountsUpdated() {
        onAccountsUpdatedListeners.forEach(Runnable::run);
    }

    public void updateAccounts(List<CashTransaction> transactions) {
        var grouped = CashTransactionDomainLogic.groupByAccountNumber(transactions);
        for (var entry : grouped.entrySet()) {
            String number = entry.getKey();
            if (accountIndex.containsKey(number)) {
                var updated = accountIndex.get(number).withUpdatedTransactions(entry.getValue());
                accountIndex.put(number, updated);
            }
        }
        onAccountsUpdated();
    }

    public Collection<Account> getAccounts() {
        return accountIndex.values();
    }

    public boolean importFromFiles(List<File> files) {
        List<Account> imported = importer.importFromFiles(files);

        var applied = applyRules(imported);
        var newAccounts = applied.stream().filter(e -> accountIndex.containsKey(e.getAccountNumber())).toList();
        var toMerge = applied.stream().filter(e -> !accountIndex.containsKey(e.getAccountNumber())).toList();
        var merged = merge(toMerge);

        var processed = Stream.concat(newAccounts.stream(), merged.stream()).toList();

        boolean hasValidAccounts = IntegrityChecker.check(processed);
        if (hasValidAccounts) {
            processed.forEach(e -> accountIndex.put(e.getAccountNumber(), e));
            onAccountsUpdated();
            return true;
        }
        Logger.getLogger(AccountService.class.getName()).log(Level.SEVERE, "Import aborted.");
        return false;
    }

    public boolean importFromTodoAndSave() {
        List<Account> imported = importer.importFromTodo();

        var applied = applyRules(imported);
        var newAccounts = applied.stream().filter(e -> accountIndex.containsKey(e.getAccountNumber())).toList();
        var toMerge = applied.stream().filter(e -> !accountIndex.containsKey(e.getAccountNumber())).toList();
        var merged = merge(toMerge);

        var processed = Stream.concat(newAccounts.stream(), merged.stream()).toList();

        boolean hasValidAccounts = IntegrityChecker.check(processed);
        if (hasValidAccounts) {
            processed.forEach(e -> accountIndex.put(e.getAccountNumber(), e));
            save();
            onAccountsUpdated();
            return true;
        }
        return false;
    }

    private List<Account> applyRules(Collection<Account> accounts) {
        List<Account> result = new ArrayList<>();
        for (Account account : accounts) {
            var updated = ruleService.applyRules(account.transactionsAscending());
            var updatedAccount = account.withUpdatedTransactions(updated);
            result.add(updatedAccount);
            System.out.println("Tx: " + account.transactionsAscending().size() + "\tupdatedTx: " + updated.size());
        }
        return result;
    }

    public void save() {
        repository.save(accountIndex.values());
    }

    private List<Account> merge(List<Account> imported) {
        var result = new ArrayList<Account>();
        for (Account account : imported) {

            var number = account.getAccountNumber();
            if (!accountIndex.containsKey(number)) {
                continue;
            }

            Account existing = accountIndex.get(number);
            var merged = existing.merge(account);
            result.add(merged);
        }
        return result;
    }

    public LocalDate getLastExportDate() {
        return AccountDomainLogic.getLastExportDate(getAccounts());
    }

    public BigDecimal getTotalBalanceOn(LocalDate date) {
        return AccountDomainLogic.getTotalBalanceOn(getAccounts(), date);
    }

}
