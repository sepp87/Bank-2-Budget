package bank2budget;

import bank2budget.adapter.account.AccountImporter;
import bank2budget.adapter.repository.AnalyticsDatabase;
import bank2budget.adapter.config.ConfigReader;
import bank2budget.adapter.account.AccountReader;
import bank2budget.core.rule.RuleFactory;
import bank2budget.adapter.account.AccountXlsxRepository;
import bank2budget.adapter.budget.BudgetRepository;
import bank2budget.adapter.account.AccountWriter;
import bank2budget.adapter.budget.BudgetReader;
import bank2budget.adapter.budget.BudgetWriter;
import bank2budget.app.AccountService;
import bank2budget.app.AnalyticsExportService;
import bank2budget.app.BudgetService;
import bank2budget.core.Config;
import bank2budget.app.CsvCleanupService;
import bank2budget.app.NoOpAnalyticsExportService;
import bank2budget.core.budget.BudgetCalculator;
import bank2budget.core.rule.RuleEngine;
import bank2budget.ports.AnalyticsExportPort;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 *
 * @author joostmeulenkamp
 */
public class App {

    private final CsvCleanupService csvCleanupService;
    private final AccountService accountService;
    private final AnalyticsExportPort analyticsExportService;
    private final BudgetService budgetService;

    public App(AppPaths paths, char decimalSeparatorChar, boolean useSqlite) {
        configureLogging();

        Config config = new ConfigReader(paths).getConfig();

        var accountReader = new AccountReader(paths.getTransactionsFile());
        var accountWriter = new AccountWriter(paths.getTransactionsFile());
        var analyticsDatabase = useSqlite ? new AnalyticsDatabase(paths.getDatabaseFile().toString(), config.budgetTemplate().firstOfMonth()) : null;
        var systemRules = RuleFactory.createSystemRules(config.myAccounts());
        var userRules = config.ruleConfigs().stream().map(RuleFactory::create).toList();
        var ruleEngine = new RuleEngine<>(systemRules, userRules);

        this.csvCleanupService = new CsvCleanupService(paths, ruleEngine, decimalSeparatorChar);

        var accountRepository = new AccountXlsxRepository(accountReader, accountWriter);
        var accountImporter = new AccountImporter(paths);
        this.accountService = new AccountService(accountRepository, accountImporter, ruleEngine);

        this.analyticsExportService = useSqlite ? new AnalyticsExportService(analyticsDatabase) : new NoOpAnalyticsExportService();

        var budgetReader = new BudgetReader(paths.getBudgetFile());
        var budgetWriter = new BudgetWriter(paths.getBudgetFile());
        var newBudgetRepository = new BudgetRepository(budgetReader, budgetWriter);
        var budgetCalculator = new BudgetCalculator();
        this.budgetService = new BudgetService(accountService, newBudgetRepository, budgetCalculator, config.budgetTemplate());

    }

    private void configureLogging() {
        Logger root = Logger.getLogger("bank2budget");
        root.setLevel(Level.INFO);
        root.setUseParentHandlers(false);

        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.INFO);
        handler.setFormatter(new Formatter() {

            @Override
            public synchronized String format(LogRecord r) {
                String formattedMessage = formatMessage(r); // <── this expands {0}, {1}, etc.
                String fullName = r.getLoggerName();
                String simpleName = fullName.substring(fullName.lastIndexOf('.') + 1);

                return String.format(
                        "%tF %<tT [%s] %s - %s%n",
                        r.getMillis(),
                        r.getLevel(),
                        simpleName,
                        formattedMessage
                );
            }
        });
        root.addHandler(handler);
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
