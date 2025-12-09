package bank2budget;

import bank2budget.adapters.reader.AccountImporter;
import bank2budget.adapters.repository.AnalyticsDatabase;
import bank2budget.adapters.reader.BudgetReaderForXlsx;
import bank2budget.adapters.reader.ConfigReader;
import bank2budget.adapters.reader.AccountReader;
import bank2budget.adapters.reader.BudgetReaderNew;
import bank2budget.adapters.reader.RuleFactory;
import bank2budget.adapters.repository.AccountXlsxRepository;
import bank2budget.adapters.repository.BudgetRepositoryNew;
import bank2budget.adapters.repository.BudgetXlsxRepository;
import bank2budget.adapters.writer.BudgetWriterForXlsx;
import bank2budget.adapters.writer.AccountWriter;
import bank2budget.adapters.writer.BudgetWriterNew;
import bank2budget.application.AccountService;
import bank2budget.application.AnalyticsExportService;
import bank2budget.application.BudgetService;
import bank2budget.core.Config;
import bank2budget.application.CsvCleanupService;
import bank2budget.application.NoOpAnalyticsExportService;
import bank2budget.core.budget.BudgetCalculator;
import bank2budget.core.rule.RuleEngine;
import bank2budget.ports.AnalyticsExportPort;

/**
 *
 * @author joostmeulenkamp
 */
public class App {

    public static boolean GO_RECORD = false;
    
    private final CsvCleanupService csvCleanupService;
    private final AccountService accountService;
    private final AnalyticsExportPort analyticsExportService;
    private final BudgetService budgetService;

    public App(AppPaths paths, char decimalSeparatorChar, boolean useSqlite) {

        Config config = new ConfigReader(paths).getConfig();

        var accountReader = new AccountReader(paths.getTransactionsFile());
        var accountWriter = new AccountWriter(paths.getTransactionsFile());
        var budgetReaderForXlsx = new BudgetReaderForXlsx(paths.getBudgetFile());
        var budgetWriterForXlsx = new BudgetWriterForXlsx(paths.getBudgetFile());
        var analyticsDatabase = useSqlite ? new AnalyticsDatabase(paths.getDatabaseFile().toString()) : null;
        var rules = config.ruleConfigs().stream().map(RuleFactory::create).toList();
        var ruleEngine = new RuleEngine<>(rules, config.myAccounts(), config.otherAccounts());

        this.csvCleanupService = new CsvCleanupService(paths, ruleEngine, decimalSeparatorChar);
        
        var accountRepository = new AccountXlsxRepository(accountReader, accountWriter);
        var accountImporter = new AccountImporter(paths);
        this.accountService = new AccountService(accountRepository, accountImporter, ruleEngine);
        
        this.analyticsExportService = useSqlite ? new AnalyticsExportService(analyticsDatabase) : new NoOpAnalyticsExportService();
        
        var budgetRepository = new BudgetXlsxRepository(budgetReaderForXlsx, budgetWriterForXlsx);
        var budgetReader = new BudgetReaderNew(paths.getBudgetFile());
        var budgetWriter = new BudgetWriterNew(paths.getBudgetFileNew());
        var newBudgetRepository = new BudgetRepositoryNew(budgetReader, budgetWriter);
        var budgetCalculator = new BudgetCalculator();
        this.budgetService = new BudgetService(budgetRepository, accountService, newBudgetRepository, budgetCalculator, config.budgetTemplate());

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
