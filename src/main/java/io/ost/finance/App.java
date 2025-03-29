package io.ost.finance;

import static io.ost.finance.Config.Mode.BUDGET;
import io.ost.finance.io.BudgetReaderForXlsx;
import io.ost.finance.io.BudgetWriterForSqlite;
import io.ost.finance.io.BudgetWriterForXlsx;
import io.ost.finance.io.TransactionWriterForXlsx;
import io.ost.finance.io.TransactionReaderForXlsxDone;
import io.ost.finance.io.TransactionReaderForCsvTodo;
import io.ost.finance.io.TransactionWriterForCsv;
import io.ost.finance.io.TransactionWriterForSqlite;
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
// TODO implement derive contra account number with other accounts index
// TODO find out why more rows are written for budget transactions, even though there are none
/**
 * Bank-to-Budget app reads CSV files from the todo directory and command line.
 * It saves the cash transactions - parsed from the CSV files - to the done
 * directory. It is also responsible for loading the config for the parsers.
 *
 * @author joost
 */
public class App {

    private static App app;
    private static boolean compiling = true;

    private static final String CONFIG_DIRECTORY = "config";
    private static final String DONE_DIRECTORY = "done";
    private static final String DATABASE_DIRECTORY = "db";
    private static final String BUILD_DIRECTORY = "build";
    private static final String MY_ACCOUNTS_PROPERTIES = "my-accounts.txt";
    private static final String OTHER_ACCOUNTS_PROPERTIES = "other-accounts.txt";

    public final String appRootDirectory;

    public boolean hasBudget;
    public Properties myAccounts;
    public Properties otherAccounts;
    public Collection<Rule> rules;

    public static void main(String[] args) throws Exception {
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
                Account.addTransactionsToAccounts(oldXlsxTransactions.getAsList(), true);

//                // TODO Remove
//                if (true) {
//                    return;
//                }
                new TransactionWriterForXlsx().write(Account.getAccounts());

                if (Config.hasSqlite()) {
                    new TransactionWriterForSqlite().write(Account.getAccounts());
                }

                if (mode != BUDGET) {
                    break;
                }
                MultiAccountBudget budget = new BudgetReaderForXlsx().read();
                budget.setAccounts(Account.getAccounts());
                new BudgetWriterForXlsx().write(budget);

                if (Config.hasSqlite()) {
                    new BudgetWriterForSqlite().write(budget);
                }

                break;

        }

        if (Config.isClearTodo()) {
            Logger.getLogger(App.class.getName()).log(Level.INFO, "Clear \"todo\" folder not implemented.");

        }

        System.exit(exitCode);

    }

    private App() {
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
        rules = new RuleReader().read();
        File budgetSettingsFile = new File(getConfigDirectory() + BudgetSettingsReader.BUDGET_SETTINGS);
        hasBudget = budgetSettingsFile.exists();

    }

    private Properties readProperties(String path) {
        File file = new File(path);
        try (FileReader reader = new FileReader(file)) {
            Properties properties = new Properties();
            properties.load(reader);
            return properties;
        } catch (IOException ex) {
            Logger.getLogger(App.class.getName()).log(Level.INFO, "Could NOT read file, proceeding without {0}", file.getPath());
        }
        return new Properties();
    }

    public static App get() {
        if (app == null) {
            app = new App();
        }
        return app;
    }

    public static String getRootDirectory() {
        return App.get().appRootDirectory;
    }

    public static String getConfigDirectory() {
        String path = App.get().appRootDirectory + CONFIG_DIRECTORY + File.separatorChar;
        getDirectory(path);
        return path;

    }

    public static String getDoneDirectory() {
        String path = App.get().appRootDirectory + DONE_DIRECTORY + File.separatorChar;
        getDirectory(path);
        return path;
    }

    public static String getDatabaseDirectory() {
        String path = App.get().appRootDirectory + DATABASE_DIRECTORY + File.separatorChar;
        getDirectory(path);
        return path;
    }

    private static File getDirectory(String path) {
        File directory = new File(path);
        if (!directory.exists() || !directory.isDirectory()) {
            Logger.getLogger(App.class.getName()).log(Level.INFO, "Could NOT find \"" + directory.getName() + "\" directory, creating {0}", directory.getPath());
            directory.mkdir();
        }
        return directory;
    }

    public static boolean hasBudget() {
        return App.get().hasBudget;
    }

}
