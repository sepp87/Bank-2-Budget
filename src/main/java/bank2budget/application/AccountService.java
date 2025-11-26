package bank2budget.application;

import bank2budget.adapters.repository.BudgetDatabase;
import bank2budget.core.Account;
import bank2budget.core.CashTransaction;
import bank2budget.core.IntegrityChecker;
import bank2budget.core.RuleEngine;
import bank2budget.ports.AccountRepositoryPort;
import bank2budget.ports.AccountImporterPort;
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
    private final RuleEngine ruleEngine;
    private final BudgetDatabase database;
    private final Map<String, Account> accountsIndex;

    public AccountService(AccountRepositoryPort repository, AccountImporterPort importer, RuleEngine ruleEngine, BudgetDatabase database) {
        this.repository = repository;
        this.importer = importer;
        this.ruleEngine = ruleEngine;
        this.database = database;
        this.accountsIndex = repository.load();
        normalize(accountsIndex.values());
    }

    public Collection<Account> getAccounts() {
        return accountsIndex.values();
    }

    public void importAccounts(List<Account> imported) {
        normalize(imported);
        applyRules(imported);
        merge(imported);
    }

    public boolean importAndSave() {
        List<Account> imported = importer.importAccounts();
        normalize(imported);
        applyRules(imported);
        merge(imported);
//        if(true) {
//            return false;
//        }
        if (hasValidAccounts()) {
            saveAccounts();
            return true;
        }
        return false;
    }

    private void normalize(Collection<Account> accounts) {
        for (Account account : accounts) {
            ruleEngine.overwriteAccountNames(account.getAllTransactionsAscending());
//            ruleEngine.addMissingAccountNumbers(transactions);
            ruleEngine.determineInternalTransactions(account.getAllTransactionsAscending());
        }
    }

    private void applyRules(Collection<Account> accounts) {
        for (Account account : accounts) {
            ruleEngine.applyRules(account.getAllTransactionsAscending());
        }
    }

    private boolean hasValidAccounts() {
        boolean isValid = IntegrityChecker.check(accountsIndex.values());
        if (!isValid) {
            Logger.getLogger(AccountService.class.getName()).log(Level.SEVERE, "Import aborted.");
        }
        return isValid;
    }

    private void saveAccounts() {
        repository.save(accountsIndex.values());
        if (database == null) {
            return;
        }
        for (Account account : accountsIndex.values()) {
            database.insertTransactions(account.getAllTransactionsAscending());
        }
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

    /**
     *
     * @return the date of the latest transaction across all accounts. Returns
     * null if there are no accounts respectively no transactions available.
     */
    public LocalDate getLastExportDate() {
        LocalDate result = null;
        for (Account a : getAccounts()) {
            LocalDate candidate = a.getNewestTransactionDate();
            if (result == null) {
                result = candidate;
            }
            if (candidate.isAfter(result)) {
                result = candidate;
            }
        }
        return result;
    }

    /**
     *
     * @return the balance of all accounts put together.
     */
    public BigDecimal getTotalBalance() {
        return getTotalBalanceOn(null);
    }

    /**
     *
     * @return the balance of all accounts put together.
     */
    public BigDecimal getTotalBalanceOn(LocalDate date) {
        if (date == null) {
            date = getLastExportDate();
        }
        BigDecimal result = BigDecimal.ZERO;
        for (Account a : getAccounts()) {
            List<CashTransaction> transactions = a.getAllTransactionsAscending();
            CashTransaction newest = null;
            for (CashTransaction transaction : transactions) {
                if (transaction.getDate().isAfter(date)) {
                    break;
                }
                newest = transaction;
            }
            if (newest != null) {
                result = result.add(BigDecimal.valueOf(newest.getAccountBalance()));
            }
        }
        return result;
    }

}
