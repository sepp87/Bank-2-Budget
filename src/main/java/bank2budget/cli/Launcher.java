package bank2budget.cli;

import bank2budget.adapters.reader.BudgetSettingsReader;
import bank2budget.core.Account;
import bank2budget.core.CashTransaction;
import bank2budget.core.IntegrityChecker;
import bank2budget.core.MultiAccountBudget;
import bank2budget.core.Rule;
import bank2budget.adapters.reader.RuleReaderForJson;
import bank2budget.core.Util;
import static bank2budget.cli.Config.Mode.BUDGET;
import bank2budget.adapters.db.BudgetDatabase;
import bank2budget.adapters.reader.BudgetReaderForXlsx;
import static bank2budget.adapters.reader.RuleReaderForJson.PROCESSING_RULES;
import bank2budget.adapters.writer.BudgetWriterForXlsx;
import bank2budget.adapters.writer.TransactionWriterForXlsx;
import bank2budget.adapters.reader.TransactionReaderForXlsxDone;
import bank2budget.adapters.reader.TransactionReaderForCsvTodo;
import bank2budget.adapters.writer.TransactionWriterForCsv;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
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
//      > it is also overwritten for transactions from transaction.xlsx, since it is used to compare transaction to enrich labels
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

    private static Launcher app;
    private static boolean compiling = true;
    public static boolean log_transactions = false;

    private static final String CONFIG_DIRECTORY = "config";
    private static final String DONE_DIRECTORY = "done";
    private static final String DATABASE_DIRECTORY = "db";
    private static final String BUILD_DIRECTORY = "build";
    private static final String MY_ACCOUNTS_PROPERTIES = "my-accounts.txt";
    private static final String OTHER_ACCOUNTS_PROPERTIES = "other-accounts.txt";
    public static final String BUDGET_SETTINGS = "budget-settings.txt";

    public final String appRootDirectory;

    public boolean hasBudget;
    public Properties myAccounts;
    public Properties otherAccounts;
    public Collection<Rule> rules;

    public static void main(String[] args) throws Exception {

//        CommandLineArgs cliArgs = new CommandLineArgs();
//        new CommandLine(cliArgs).execute(args);
//
//        AppPaths paths = new AppPaths();
//        App app = new App(paths);
//        CliAppRunner runner = new CliAppRunner(app, cliArgs, paths);
//        runner.run();
//
//        if (true) {
//            return;
//        }

        compiling = false;

        int exitCode = new CommandLine(Config.get()).execute(args);

        TransactionReaderForCsvTodo todoTransactions = new TransactionReaderForCsvTodo().read();
        for (List<CashTransaction> transactions : todoTransactions.getPerFile().values()) {
            Account.addTransactionsToAccounts(transactions);
        }

        Config.Mode mode = Config.getMode();
        switch (mode) {
            case CSV:
                new TransactionWriterForCsv().write(todoTransactions.getPerFile());
                break;
            case XLSX:
            case BUDGET:

                TransactionReaderForXlsxDone oldXlsxTransactions = new TransactionReaderForXlsxDone().read();
//                for (CashTransaction t : oldXlsxTransactions.getAsList()) {
//                    System.out.println(t);
//                }
                Account.addTransactionsToAccounts(oldXlsxTransactions.getAsList(), true);

                boolean isValid = IntegrityChecker.check(Account.getAccounts());
                if (!isValid) {
                    Logger.getLogger(Launcher.class.getName()).log(Level.SEVERE, "Import aborted.");
                    return;
                }
//                // TODO Remove
//                if (true) {
//                    return;
//                }
                new TransactionWriterForXlsx().write(Account.getAccounts());

                BudgetDatabase database = null;
                if (Config.hasSqlite()) {
                    database = new BudgetDatabase(Launcher.getDatabaseDirectory() + "bank-2-budget.db");
                    for (Account account : Account.getAccounts()) {
                        database.insertTransactions(account.getAllTransactionsAscending());
                    }
                }

                if (mode != BUDGET) {
                    break;
                }
                MultiAccountBudget budget = new BudgetReaderForXlsx().read();
                budget.setAccounts(Account.getAccounts());
                new BudgetWriterForXlsx().write(budget);

                if (database != null) {
                    database.insertMonthlyBudgets(budget.getMonthlyBudgets().values());
                }

                break;

        }

        if (Config.isClearTodo()) {
            Logger.getLogger(Launcher.class.getName()).log(Level.INFO, "Clear \"todo\" folder not implemented.");

        }

        System.exit(exitCode);

    }

    private Launcher() {
        app = this;
        appRootDirectory = Util.getAppRootDirectory(this, BUILD_DIRECTORY);
        if (compiling) {
            initializeWithoutConfig();
        } else {
            loadConfig();
        }
    }

    private void initializeWithoutConfig() {
        myAccounts = new Properties();
        otherAccounts = new Properties();
        rules = Collections.emptySet();
    }

    private void loadConfig() {
        myAccounts = readProperties(getConfigDirectory() + MY_ACCOUNTS_PROPERTIES);
        otherAccounts = readProperties(getConfigDirectory() + OTHER_ACCOUNTS_PROPERTIES);
        rules = new RuleReaderForJson(new File(Launcher.getConfigDirectory() + PROCESSING_RULES)).read();
        new BudgetSettingsReader(new File(Launcher.getConfigDirectory() + BUDGET_SETTINGS)).read();
    }

    private Properties readProperties(String path) {
        File file = new File(path);
        try (FileReader reader = new FileReader(file)) {
            Properties properties = new Properties();
            properties.load(reader);
            return properties;
        } catch (IOException ex) {
            Logger.getLogger(Launcher.class.getName()).log(Level.INFO, "Could NOT read file, proceeding without {0}", file.getPath());
        }
        return new Properties();
    }

    public static Launcher get() {
        if (app == null) {
            app = new Launcher();
        }
        return app;
    }

    public static String getRootDirectory() {
        return Launcher.get().appRootDirectory;
    }

    public static String getConfigDirectory() {
        String path = Launcher.get().appRootDirectory + CONFIG_DIRECTORY + File.separatorChar;
        getDirectory(path);
        return path;

    }

    public static String getDoneDirectory() {
        String path = Launcher.get().appRootDirectory + DONE_DIRECTORY + File.separatorChar;
        getDirectory(path);
        return path;
    }

    public static String getDatabaseDirectory() {
        String path = Launcher.get().appRootDirectory + DATABASE_DIRECTORY + File.separatorChar;
        getDirectory(path);
        return path;
    }

    private static File getDirectory(String path) {
        File directory = new File(path);
        if (!directory.exists() || !directory.isDirectory()) {
            Logger.getLogger(Launcher.class.getName()).log(Level.INFO, "Could NOT find \"" + directory.getName() + "\" directory, creating {0}", directory.getPath());
            directory.mkdir();
        }
        return directory;
    }

}