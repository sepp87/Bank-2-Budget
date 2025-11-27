package bank2budget.cli;

import bank2budget.App;
import static bank2budget.cli.CommandLineArgs.Mode.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author joostmeulenkamp
 */
public class CliAppRunner {

    private final static Logger LOGGER = Logger.getLogger(CliAppRunner.class.getName());
    
    private final App app;
    private final CommandLineArgs cliArgs;

    public CliAppRunner(App app, CommandLineArgs cliArgs) {
        this.app = app;
        this.cliArgs = cliArgs;
    }

    public void run() {

        switch (cliArgs.getMode()) {
            case CSV:
                app.getCsvCleanupService().cleanTodoDirectory();
                break;
            case XLSX:
                app.getAccountService().importFromTodoAndSave();
                app.getAnalyticsExportService().exportAccounts(app.getAccountService().getAccounts());
                break;
            case BUDGET:
                app.getBudgetService().importFromTodoAndSave();
                app.getAnalyticsExportService().exportAccounts(app.getAccountService().getAccounts());
                app.getAnalyticsExportService().exportBudget(app.getBudgetService().getBudget());
                break;
        }

        if (cliArgs.shouldClearTodo()) {
            LOGGER.log(Level.INFO, "Clear \"todo\" folder not implemented.");
        }
    }

}
