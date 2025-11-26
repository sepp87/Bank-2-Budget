package bank2budget;

import bank2budget.adapters.reader.AccountImporter;
import bank2budget.adapters.repository.BudgetDatabase;
import bank2budget.adapters.reader.BudgetReaderForXlsx;
import bank2budget.adapters.reader.ConfigReader;
import bank2budget.adapters.reader.AccountReader;
import bank2budget.adapters.reader.TransactionReaderForCsv;
import bank2budget.adapters.repository.AccountXlsxRepository;
import bank2budget.adapters.writer.BudgetWriterForXlsx;
import bank2budget.adapters.writer.TransactionWriterForCsv;
import bank2budget.adapters.writer.AccountWriter;
import bank2budget.ports.AccountImporterPort;
import bank2budget.ports.AccountRepositoryPort;
import bank2budget.application.AccountService;
import bank2budget.core.Config;
import bank2budget.application.CsvCleanupService;
import bank2budget.core.RuleEngine;

/**
 *
 * @author joostmeulenkamp
 */
public class App {

    private final AppPaths paths;
    private final AccountReader accountReader;
    private final TransactionReaderForCsv transactionReaderForCsv;
    private final TransactionWriterForCsv transactionWriterForCsv;
    private final AccountWriter accountWriter;
    private final BudgetReaderForXlsx budgetReaderForXlsx;
    private final BudgetWriterForXlsx budgetWriterForXlsx;
    private final BudgetDatabase budgetDatabase;
    private final RuleEngine ruleEngine;
    private final CsvCleanupService csvCleanupService;
    private final AccountRepositoryPort accountRepository;
    private final AccountImporterPort accountImporter;
    private final AccountService accountService;

    public App(AppPaths paths, char decimalSeparatorChar, boolean useSqlite) {
        this.paths = paths;

        Config config = new ConfigReader(paths).getConfig();

        this.accountReader = new AccountReader(paths.getTransactionsFile());
        this.transactionReaderForCsv = new TransactionReaderForCsv(paths.getTodoDirectory());
        this.transactionWriterForCsv = new TransactionWriterForCsv(paths.getDoneDirectory(), decimalSeparatorChar);
        this.accountWriter = new AccountWriter(paths.getTransactionsFile());
        this.budgetReaderForXlsx = new BudgetReaderForXlsx(paths.getBudgetFile());
        this.budgetWriterForXlsx = new BudgetWriterForXlsx(paths.getBudgetFile());
        this.budgetDatabase = useSqlite ? new BudgetDatabase(paths.getDatabaseFile().toString()) : null;
        this.ruleEngine = new RuleEngine(config.rules(), config.myAccounts(), config.otherAccounts());

        this.csvCleanupService = new CsvCleanupService(transactionReaderForCsv, transactionWriterForCsv, ruleEngine);
        this.accountRepository = new AccountXlsxRepository(accountReader, accountWriter);
        this.accountImporter = new AccountImporter(transactionReaderForCsv);
        this.accountService = new AccountService(accountRepository, accountImporter, ruleEngine, budgetDatabase);

    }

    public AccountWriter getAccountWriter() {
        return accountWriter;
    }

    public BudgetReaderForXlsx getBudgetReaderForXlsx() {
        return budgetReaderForXlsx;
    }

    public BudgetWriterForXlsx getBudgetWriterForXlsx() {
        return budgetWriterForXlsx;
    }

    public BudgetDatabase getBudgetDatabase() {
        return budgetDatabase;
    }

    public CsvCleanupService getCsvCleanupService() {
        return csvCleanupService;
    }

    public AccountService getAccountService() {
        return accountService;
    }

}
