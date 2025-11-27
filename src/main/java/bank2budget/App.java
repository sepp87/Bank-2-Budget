package bank2budget;

import bank2budget.adapters.reader.AccountImporter;
import bank2budget.adapters.repository.AnalyticsDatabase;
import bank2budget.adapters.reader.BudgetReaderForXlsx;
import bank2budget.adapters.reader.ConfigReader;
import bank2budget.adapters.reader.AccountReader;
import bank2budget.adapters.reader.TransactionReaderFactory;
import bank2budget.adapters.reader.TransactionReaderForCsv;
import bank2budget.adapters.repository.AccountXlsxRepository;
import bank2budget.adapters.repository.BudgetXlsxRepository;
import bank2budget.adapters.writer.BudgetWriterForXlsx;
import bank2budget.adapters.writer.TransactionWriterForCsv;
import bank2budget.adapters.writer.AccountWriter;
import bank2budget.ports.AccountImporterPort;
import bank2budget.ports.AccountRepositoryPort;
import bank2budget.application.AccountService;
import bank2budget.application.AnalyticsExportService;
import bank2budget.application.BudgetService;
import bank2budget.core.Config;
import bank2budget.application.CsvCleanupService;
import bank2budget.application.NoOpAnalyticsExportService;
import bank2budget.core.RuleEngine;
import bank2budget.ports.AnalyticsExportPort;
import bank2budget.ports.BudgetRepositoryPort;

/**
 *
 * @author joostmeulenkamp
 */
public class App {

    private final AccountReader accountReader;
    private final AccountWriter accountWriter;
    private final BudgetReaderForXlsx budgetReaderForXlsx;
    private final BudgetWriterForXlsx budgetWriterForXlsx;
    private final AnalyticsDatabase analyticsDatabase;
    private final RuleEngine ruleEngine;
    private final CsvCleanupService csvCleanupService;
    private final AccountRepositoryPort accountRepository;
    private final AccountImporterPort accountImporter;
    private final AccountService accountService;
    private final AnalyticsExportPort analyticsExportService;
    private final BudgetRepositoryPort budgetRepository;
    private final BudgetService budgetService;

    public App(AppPaths paths, char decimalSeparatorChar, boolean useSqlite) {

        Config config = new ConfigReader(paths).getConfig();

        this.accountReader = new AccountReader(paths.getTransactionsFile());
        this.accountWriter = new AccountWriter(paths.getTransactionsFile());
        this.budgetReaderForXlsx = new BudgetReaderForXlsx(paths.getBudgetFile());
        this.budgetWriterForXlsx = new BudgetWriterForXlsx(paths.getBudgetFile());
        this.analyticsDatabase = useSqlite ? new AnalyticsDatabase(paths.getDatabaseFile().toString()) : null;
        this.ruleEngine = new RuleEngine(config.rules(), config.myAccounts(), config.otherAccounts());

        this.csvCleanupService = new CsvCleanupService(paths, ruleEngine, decimalSeparatorChar);
        this.accountRepository = new AccountXlsxRepository(accountReader, accountWriter);
        this.accountImporter = new AccountImporter(paths);
        this.accountService = new AccountService(accountRepository, accountImporter, ruleEngine);
        this.analyticsExportService = useSqlite ? new AnalyticsExportService(analyticsDatabase) : new NoOpAnalyticsExportService();
        this.budgetRepository = new BudgetXlsxRepository(budgetReaderForXlsx, budgetWriterForXlsx);
        this.budgetService = new BudgetService(budgetRepository, accountService);

    }

    public CsvCleanupService getCsvCleanupService() {
        return csvCleanupService;
    }

    public AccountService getAccountService() {
        return accountService;
    }
    
    public AnalyticsExportPort getAnalyticsExportService() {
        return analyticsExportService;
    }
    
    public BudgetService getBudgetService() {
        return budgetService;
    }

}
