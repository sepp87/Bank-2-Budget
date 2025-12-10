package bank2budget.application;

import bank2budget.core.Account;
import bank2budget.core.AccountDomainLogic;
import bank2budget.core.IntegrityChecker;
import bank2budget.core.CashTransaction;
import bank2budget.core.rule.RuleEngine;
import bank2budget.ports.AccountRepositoryPort;
import bank2budget.ports.AccountImporterPort;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author joostmeulenkamp
 */
public class AccountService {

    private final AccountRepositoryPort repository;
    private final AccountImporterPort importer;
    private final RuleEngine<CashTransaction> ruleEngine;
    private final Map<String, Account> accountsIndex;

    public AccountService(AccountRepositoryPort repository, AccountImporterPort importer, RuleEngine<CashTransaction> ruleEngine) {
        this.repository = repository;
        this.importer = importer;
        this.ruleEngine = ruleEngine;
        this.accountsIndex = repository.load();
        applySystemRules(accountsIndex.values());
    }

    public Collection<Account> getAccounts() {
        return accountsIndex.values();
    }

    public void importFromFiles(List<File> files) {
        List<Account> imported = importer.importFromFiles(files);
        applyRulesAndMerge(imported);
    }

    public boolean importFromTodoAndSave() {
        List<Account> imported = importer.importFromTodo();
        applyRulesAndMerge(imported);

//        if(true) {
//            return false;
//        }
        if (hasValidAccounts()) {
            save();
            return true;
        }
        return false;
    }

    private void applyRulesAndMerge(List<Account> imported) {
        applyRules(imported);
        merge(imported);
    }

    private void applySystemRules(Collection<Account> accounts) {
        for (Account account : accounts) {
            var updated = ruleEngine.applySystemRules(account.transactionsAscending());
            account.replace(updated);
            System.out.println("Tx: " + account.transactionsAscending().size() + "\tupdatedTx: " + updated.size());
        }
    }

    private void applyRules(Collection<Account> accounts) {
        for (Account account : accounts) {
            var updated = ruleEngine.applyRules(account.transactionsAscending());
            account.replace(updated);
            System.out.println("Tx: " + account.transactionsAscending().size() + "\tupdatedTx: " + updated.size());
        }
    }

    private boolean hasValidAccounts() {
        boolean isValid = IntegrityChecker.check(accountsIndex.values());
        if (!isValid) {
            Logger.getLogger(AccountService.class.getName()).log(Level.SEVERE, "Import aborted.");
        }
        return isValid;
    }

    public void save() {
        repository.save(accountsIndex.values());
    }

    private void merge(List<Account> importedAccounts) {
        // TODO Implement
        for (Account imported : importedAccounts) {
            if (accountsIndex.containsKey(imported.getAccountNumber())) {
                Account existing = accountsIndex.get(imported.getAccountNumber());
                existing.merge(imported);
            } else {
                accountsIndex.put(imported.getAccountNumber(), imported);
            }
        }
    }

    public LocalDate getLastExportDate() {
        return AccountDomainLogic.getLastExportDate(getAccounts());
    }

    public BigDecimal getTotalBalanceOn(LocalDate date) {
        return AccountDomainLogic.getTotalBalanceOn(getAccounts(), date);
    }

}
