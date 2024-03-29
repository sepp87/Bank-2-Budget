package io.ost.finance;

import static io.ost.finance.Config.Mode.BUDGET;
import io.ost.finance.io.BudgetReader;
import io.ost.finance.io.BudgetWriter;
import io.ost.finance.io.TransactionWriterForBudget;
import io.ost.finance.io.TransactionReaderForBudget;
import io.ost.finance.io.TransactionReaderForTodo;
import io.ost.finance.io.TransactionWriterForDone;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import picocli.CommandLine;

/**
 * Bank-to-Budget app reads CSV files from the todo directory and command line.
 * It saves the cash transactions - parsed from the CSV files - to the done
 * directory. It is also responsible for loading the config for the parsers.
 *
 * @author joost
 */
public class App {

    private static App app;

    public final String appRootDirectory;

    private static final String CONFIG_DIRECTORY = "config";
    private static final String BUDGET_DIRECTORY = "done";

    private static final String MY_ACCOUNTS_PROPERTIES = "my-accounts.txt";
    private static final String OTHER_ACCOUNTS_PROPERTIES = "other-accounts.txt";

    private static final String BUILD_DIRECTORY = "D:\\dev\\Bank-2-Budget\\build\\";

    public SingleAccountBudget budget;
    public boolean hasBudget;
    public Properties myAccounts;
    public Properties otherAccounts;
    public Collection<Rule> rules;

    public static void main(String[] args) throws Exception {

        int exitCode = new CommandLine(Config.get()).execute(args);

        TransactionReaderForTodo todoTransactions = new TransactionReaderForTodo().read();
        Account.addTransactionsToAccounts(todoTransactions.getAsList());

        Config.Mode mode = Config.getMode();
        switch (mode) {
            case CSV:
                TransactionWriterForDone doneTransactions = new TransactionWriterForDone();
                doneTransactions.write(todoTransactions.getPerFile());
                break;
            case XLSX:
            case BUDGET:
                TransactionReaderForBudget oldBudgetTransactions = new TransactionReaderForBudget().read();
                Account.addTransactionsToAccounts(oldBudgetTransactions.getAsList());

                TransactionWriterForBudget newBudgetTransactions = new TransactionWriterForBudget();
                newBudgetTransactions.write(Account.getAccounts());

                if (mode != BUDGET) {
                    break;
                }

                // for now, budgets can only be managed for a maximum of 1 
                SingleAccountBudget budget = new BudgetReader().read();
                budget.addAccount(Account.getAccounts().iterator().next());
                new BudgetWriter().write(budget);
                break;
        }

        if (Config.isClearTodo()) {
            System.out.println("Clear \"todo\" folder not implemented.");
                   
        }

        System.exit(exitCode);

    }

    private App() {
        app = this;
        appRootDirectory = Util.getAppRootDirectory(this, BUILD_DIRECTORY);
        loadConfig();
    }

    private void loadConfig() {
        File configDirectory = new File(getConfigDirectory());
        if (configDirectory.exists() && configDirectory.isDirectory()) {
            myAccounts = readProperties(getConfigDirectory() + MY_ACCOUNTS_PROPERTIES);
            otherAccounts = readProperties(getConfigDirectory() + OTHER_ACCOUNTS_PROPERTIES);
            rules = new RuleReader().read();
            File budgetSettingsFile = new File(getConfigDirectory() + BudgetSettingsReader.BUDGET_SETTINGS);
            hasBudget = budgetSettingsFile.exists();
        } else {
            Logger.getLogger(App.class.getName()).log(Level.INFO, "Could NOT find \"config\" directory, creating {0}", configDirectory.getPath());
            configDirectory.mkdir();
            loadConfig();
        }
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
        return App.get().appRootDirectory + CONFIG_DIRECTORY + File.separatorChar;
    }

    public static String getBudgetDirectory() {
        return App.get().appRootDirectory + BUDGET_DIRECTORY + File.separatorChar;
    }

    public static boolean hasBudget() {
        return App.get().hasBudget;
    }

}
