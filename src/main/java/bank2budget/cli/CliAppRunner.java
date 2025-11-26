package bank2budget.cli;

import bank2budget.Launcher;
import bank2budget.AppPaths;
import bank2budget.App;
import static bank2budget.cli.CommandLineArgs.Mode.*;
import bank2budget.adapters.repository.BudgetDatabase;
import bank2budget.adapters.reader.TransactionReaderForCsv;
import bank2budget.core.MultiAccountBudget;
import bank2budget.core.RuleEngine;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author joostmeulenkamp
 */
public class CliAppRunner {

    private final App app;
    private final CommandLineArgs cliArgs;
    private final AppPaths paths;

    private final TransactionReaderForCsv todoReader;
    private final BudgetDatabase budgetDatabase;

    public CliAppRunner(App app, CommandLineArgs cliArgs, AppPaths paths) {
        this.app = app;
        this.cliArgs = cliArgs;
        this.paths = paths;

        this.todoReader = new TransactionReaderForCsv(paths.getTodoDirectory());
        this.budgetDatabase = app.getBudgetDatabase();
    }

    public void run() {

        switch (cliArgs.getMode()) {
            case CSV:
                app.getCsvCleanupService().cleanTodoDirectory();
                break;
            case XLSX:
                app.getAccountService().importAndSave();
                break;
            case BUDGET:
                runBudgetMode();
                break;
        }

        if (cliArgs.shouldClearTodo()) {
            Logger.getLogger(Launcher.class.getName()).log(Level.INFO, "Clear \"todo\" folder not implemented.");
        }
    }

    //
    //
    // BUDGET MODE
    private void runBudgetMode() {
        boolean isSucces = app.getAccountService().importAndSave();
        if (isSucces) {
            MultiAccountBudget budget = loadBudget();
            saveBudget(budget);
        }
    }

    private MultiAccountBudget loadBudget() {
        MultiAccountBudget budget = app.getBudgetReaderForXlsx().read();
        budget.setAccounts(app.getAccountService().getAccounts());
        return budget;
    }

    private void saveBudget(MultiAccountBudget budget) {
        app.getBudgetWriterForXlsx().write(budget);

        if (budgetDatabase == null) {
            return;
        }
        budgetDatabase.insertMonthlyBudgets(budget.getMonthlyBudgets().values());
    }

}
