package bank2budget;

import bank2budget.adapter.account.AccountImporter;
import bank2budget.adapter.repository.AnalyticsDatabase;
import bank2budget.adapter.config.ConfigReader;
import bank2budget.adapter.account.AccountReader;
import bank2budget.adapter.account.AccountXlsxRepository;
import bank2budget.adapter.budget.BudgetRepository;
import bank2budget.adapter.account.AccountWriter;
import bank2budget.adapter.budget.BudgetReader;
import bank2budget.adapter.budget.BudgetTemplateReader;
import bank2budget.adapter.budget.BudgetTemplateRepository;
import bank2budget.adapter.budget.BudgetTemplateWriter;
import bank2budget.adapter.budget.BudgetWriter;
import bank2budget.adapter.config.ConfigRepository;
import bank2budget.adapter.config.ConfigWriter;
import bank2budget.adapter.rule.RuleReader;
import bank2budget.adapter.rule.RuleRepository;
import bank2budget.adapter.rule.RuleWriter;
import bank2budget.app.AccountService;
import bank2budget.app.AnalyticsExportService;
import bank2budget.app.BudgetReportService;
import bank2budget.app.BudgetService;
import bank2budget.app.BudgetTemplateService;
import bank2budget.app.ConfigService;
import bank2budget.core.Config;
import bank2budget.app.CsvCleanupService;
import bank2budget.app.NoOpAnalyticsExportService;
import bank2budget.app.RuleService;
import bank2budget.app.report.BudgetReportAssembler;
import bank2budget.core.CashTransaction;
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
    private final BudgetReportService budgetReportService;
    private final BudgetTemplateService templateService;
    private final RuleService ruleService;
    private final ConfigService configService;

    public App(AppPaths paths, char decimalSeparatorChar, boolean useSqlite) {
        configureLogging();

        var config = new ConfigReader(paths).read();
        var configReader = new ConfigReader(paths);
        var configWriter = new ConfigWriter(paths);
        var configRepository = new ConfigRepository(configReader, configWriter);
        this.configService = new ConfigService(configRepository);

        var ruleReader = new RuleReader(paths.getCategorizationRulesFile());
        var ruleWriter = new RuleWriter(paths.getCategorizationRulesFile());
        var ruleRepository = new RuleRepository(ruleReader, ruleWriter);
        var ruleEngine = new RuleEngine<CashTransaction>();
        this.ruleService = new RuleService(ruleRepository, ruleEngine, config.myAccounts());

        this.csvCleanupService = new CsvCleanupService(paths, ruleService, decimalSeparatorChar);

        var accountReader = new AccountReader(paths.getTransactionsFile());
        var accountWriter = new AccountWriter(paths.getTransactionsFile());
        var accountRepository = new AccountXlsxRepository(accountReader, accountWriter);
        var accountImporter = new AccountImporter(paths);
        this.accountService = new AccountService(accountRepository, accountImporter, ruleService);

        var budgetReader = new BudgetReader(paths.getBudgetFile());
        var budgetWriter = new BudgetWriter(paths.getBudgetFile());
        var budgetRepository = new BudgetRepository(budgetReader, budgetWriter);
        var budgetCalculator = new BudgetCalculator();
        var templateReader = new BudgetTemplateReader(paths.getBudgetTemplateFile());
        var templateWriter = new BudgetTemplateWriter(paths.getBudgetTemplateFile());
        var templateRepository = new BudgetTemplateRepository(templateReader, templateWriter);
        this.templateService = new BudgetTemplateService(templateRepository);
        this.budgetService = new BudgetService(accountService, budgetRepository, budgetCalculator, templateService);

        var budgetReportAssembler = new BudgetReportAssembler();
        this.budgetReportService = new BudgetReportService(budgetService, budgetReportAssembler);

        var analyticsDatabase = useSqlite ? new AnalyticsDatabase(paths.getDatabaseFile().toString(), templateService.getTemplate().firstOfMonth()) : null;
        this.analyticsExportService = useSqlite ? new AnalyticsExportService(analyticsDatabase) : new NoOpAnalyticsExportService();

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
    
    public ConfigService getConfigService() {
        return configService;
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

    public BudgetReportService getBudgetReportService() {
        return budgetReportService;
    }

    public BudgetTemplateService getTemplateService() {
        return templateService;
    }

    public RuleService getRuleService() {
        return ruleService;
    }

   
    
}
