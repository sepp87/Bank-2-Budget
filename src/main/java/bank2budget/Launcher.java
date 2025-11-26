package bank2budget;

import bank2budget.cli.CliAppRunner;
import bank2budget.cli.CommandLineArgs;
import bank2budget.ui.UiAppRunner;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import picocli.CommandLine;

// TODO evaluate if Util methods belong in corresponding class
// TODO TEST myAccount
// TODO find out why more rows are written for budget transactions, even though there are none
// TODO add notes field
// TODO Enable budgetting for users that did not start with a new account e.g.
//          UC1: there is already an account balance, before the user started exporting transactions and budgetting with bank-2-budget 
//              > should work for multiple accounts. for each account the first transaction needs to be taken for the initial balance
//          UC2: user wants to run his budget on a yearly basis and does not want to drag older budgets along
//          UC3: user lost old transactions but not the budget
//          AC: handle case where account balance of old budget does not match transactions backed budget
//          Places of Interest: MultiAccountBudget.calculateAllMonthlyBudgets() overwrite expenses and remainder 
//
// DONE FIX account balance integrity check for imported DKB transactions
//      > check balance history integrity for all accounts after import before saving 
// DONE implement derive contra account number with other accounts index
//      > add only (contra) account numbers if (contra) account number is null
// DONE FIX contraAccountName not changed to my alias specified in my-accounts.txt. contraAccountName must be set before contraAccountNumber otherwise the name is not changed according to the settings provided in my-accounts.txt
//      > (contra) account name is overwritten in post processing of transactions, 
//      > it is also overwritten for transactions from transaction.xlsx, since it is used to compare transaction to enrich categories
//      > Thought - do NOT compare account names, to conclude if two transactions are the same, if the data is subject to change at each import
//      > Thought - do NOT forget to overwrite account names, should they be read from further places
//      > Thought - do NOT change original account names, only add aliases e.g. in the UI
/**
 * Bank-to-Budget app reads CSV files from the todo directory and command line.
 * It saves the cash transactions - parsed from the CSV files - to the done
 * directory. It is also responsible for loading the config for the parsers.
 *
 * @author joost
 */
public class Launcher {

    public static boolean log_transactions = false;

    public static void main(String[] args) throws Exception {

        boolean devMode = Boolean.getBoolean("bank2budget.dev") || "dev".equalsIgnoreCase(System.getenv("BANK2BUDGET_MODE"));
        boolean hasConsole = System.console() != null;
        boolean isHeadless = GraphicsEnvironment.isHeadless();

        if (devMode) {
            runCli(args);
            runUi(args);
        } else if (hasConsole || isHeadless) {
            runCli(args);
        }
    }

    private static void runUi(String[] args) throws IOException {
        CommandLineArgs cliArgs = new CommandLineArgs();
        int exitCode = new CommandLine(cliArgs).execute(args);

        AppPaths paths = new AppPaths();
        App app = new App(paths, cliArgs.getDecimalSeparatorChar(), cliArgs.useSqlite());
        UiAppRunner runner = new UiAppRunner(app);
        runner.run();

        System.exit(exitCode);
    }

    private static void runCli(String[] args) throws IOException {
        CommandLineArgs cliArgs = new CommandLineArgs();
        int exitCode = new CommandLine(cliArgs).execute(args);

        AppPaths paths = new AppPaths();
        App app = new App(paths, cliArgs.getDecimalSeparatorChar(), cliArgs.useSqlite());
        CliAppRunner runner = new CliAppRunner(app, cliArgs, paths);
        runner.run();

        System.exit(exitCode);
    }
}
