package bank2budget.cli;

import static bank2budget.cli.CommandLineArgs.Mode.*;
import bank2budget.adapters.db.BudgetDatabase;
import bank2budget.adapters.reader.TransactionReaderForCsvTodo;
import bank2budget.adapters.reader.TransactionReaderForXlsxDone;
import bank2budget.core.Account;
import bank2budget.core.CashTransaction;
import bank2budget.core.IntegrityChecker;
import bank2budget.core.MultiAccountBudget;
import java.util.List;
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

    public CliAppRunner(App app, CommandLineArgs cliArgs, AppPaths paths) {
        this.app = app;
        this.cliArgs = cliArgs;
        this.paths = paths;
    }

    public void run() {
        TransactionReaderForCsvTodo todoTransactions = app.getTransactionReaderForCsvTodo().read();
        for (List<CashTransaction> transactions : todoTransactions.getPerFile().values()) {
            Account.addTransactionsToAccounts(transactions);
        }
        
        switch (cliArgs.getMode()) {
            case CSV:
                app.getTransactionWriterForCsv().write(todoTransactions.getPerFile());
                break;
            case XLSX:
            case BUDGET:

                TransactionReaderForXlsxDone oldXlsxTransactions = app.getTransactionReaderForXlsxDone().read();
                
                Account.addTransactionsToAccounts(oldXlsxTransactions.getAsList(), true);

                boolean isValid = IntegrityChecker.check(Account.getAccounts());
                if (!isValid) {
                    Logger.getLogger(CliAppRunner.class.getName()).log(Level.SEVERE, "Import aborted.");
                    return;
                }

                app.getTransactionWriterForXlsx().write(Account.getAccounts());

                BudgetDatabase database = null;
                if (Config.hasSqlite()) {
                    database = app.getBudgetDatabase();
                    for (Account account : Account.getAccounts()) {
                        database.insertTransactions(account.getAllTransactionsAscending());
                    }
                }

                if (cliArgs.getMode() != BUDGET) {
                    break;
                }
                MultiAccountBudget budget = app.getBudgetReaderForXlsx().read();
                budget.setAccounts(Account.getAccounts());
                app.getBudgetWriterForXlsx().write(budget);

                if (database != null) {
                    database.insertMonthlyBudgets(budget.getMonthlyBudgets().values());
                }

                break;

        }

    }

}
