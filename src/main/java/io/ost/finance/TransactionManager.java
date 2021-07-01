package io.ost.finance;

import io.ost.finance.parser.TransactionParser;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * AccountHandler reads CSV files from the todo directory or command line. It
 * saves the cash transactions - parsed from the CSV files - to the done
 * directory. It is also responsible for loading the config for the parsers.
 *
 * TODO AccountHandler class name does not describe the function of the class.
 *
 * @author joost
 */
public class TransactionManager {

    private static TransactionManager transactionManager;

    public String appRootDirectory;

    public static final String TODO_DIRECTORY = "todo";
    public static final String DONE_DIRECTORY = "done";
    public static final String CONFIG_DIRECTORY = "config";
    public static final String MY_ACCOUNTS_PROPERTIES = "my-accounts.txt";
    public static final String OTHER_ACCOUNTS_PROPERTIES = "other-accounts.txt";
    public static final String PROCESSING_RULES = "processing-rules.txt";
    public static final String BUILD_DIRECTORY = "D:\\dev\\Bank-2-Budget\\build\\";
    public static final char DECIMAL_SEPERATOR = new DecimalFormatSymbols(Locale.getDefault()).getDecimalSeparator();

    public Properties myAccounts;
    public Properties otherAccounts;
    public Properties databaseConfig;
    public Collection<Rule> rules;
    public List<CashTransaction> transactions;

    public static void main(String[] args) throws Exception {
        TransactionManager manager = new TransactionManager();
        manager.processTodoDirectory();
        File[] files = getFileFromString(args);
        manager.processCsv(files);
    }

    private static File[] getFileFromString(String... args) {
        File[] files = new File[args.length];
        for (int i = 0; i < args.length; i++) {
            files[i] = new File(args[i]);
        }
        return files;
    }

    private TransactionManager() {
        transactionManager = this;
        appRootDirectory = Util.getAppRootDirectory(this, BUILD_DIRECTORY);
        loadConfig();
        transactions = new ArrayList<>();
    }

    private void loadConfig() {
        File configDirectory = new File(appRootDirectory + CONFIG_DIRECTORY);
        if (configDirectory.exists() && configDirectory.isDirectory()) {
            myAccounts = readProperties(appRootDirectory + CONFIG_DIRECTORY + File.separatorChar + MY_ACCOUNTS_PROPERTIES);
            otherAccounts = readProperties(appRootDirectory + CONFIG_DIRECTORY + File.separatorChar + OTHER_ACCOUNTS_PROPERTIES);
            rules = loadRulesFromFile(appRootDirectory + CONFIG_DIRECTORY + File.separatorChar + PROCESSING_RULES);
        } else {
            Logger.getLogger(TransactionManager.class.getName()).log(Level.INFO, "Could NOT find \"config\" directory, creating {0}", configDirectory.getPath());
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
            Logger.getLogger(TransactionManager.class.getName()).log(Level.INFO, "Could NOT read file, proceeding without {0}", file.getPath());
        }
        return new Properties();
    }

    private Collection<Rule> loadRulesFromFile(String path) {
        File rulesJson = new File(path);
        RuleParser parser = new RuleParser();
        return parser.parse(rulesJson);
    }

    private void processTodoDirectory() {
        File todoDirectory = new File(appRootDirectory + TODO_DIRECTORY);
        if (todoDirectory.exists() && todoDirectory.isDirectory()) {
            File[] files = getCsvFilesFromDirectory(todoDirectory);
            processCsv(files);
        } else {
            Logger.getLogger(TransactionManager.class.getName()).log(Level.INFO, "Could NOT find \"todo\" directory, creating {0}", todoDirectory.getPath());
            todoDirectory.mkdir();
            processTodoDirectory();
        }
    }

    private File[] getCsvFilesFromDirectory(File directory) {
        return directory.listFiles(getCsvFileFilter());
    }

    // https://howtodoinjava.com/java/io/java-filefilter-example/
    private FileFilter getCsvFileFilter() {
        FileFilter filter = new FileFilter() {
            //Override accept method
            public boolean accept(File file) {
                //if the file extension is .csv return true, else false
                return file.getName().toLowerCase().endsWith(".csv");
            }
        };
        return filter;
    }

    private void processCsv(File... files) {
        for (File csv : files) {
            loadTransactionsFromFile(csv);
            saveTransactionsToDoneDirectory(csv.getName());
        }
    }

    private boolean loadTransactionsFromFile(File csvFile) {
        try {
            TransactionParser parser = SimpleParserFactory.createTransactionParser(csvFile);
            List parsedTransactions = parser.parse();
            transactions.addAll(parsedTransactions);
            return true;
        } catch (Exception ex) {
            Logger.getLogger(TransactionManager.class.getName()).log(Level.SEVERE, "Unknown bank, could NOT load transactions from file", ex);
            return false;
        }
    }

    private void saveTransactionsToDoneDirectory(String withOriginalFilename) {
        File doneDirectory = new File(appRootDirectory + DONE_DIRECTORY);
        if (doneDirectory.exists() && doneDirectory.isDirectory()) {
            String originalFilenameWithoutExtension = withOriginalFilename.substring(0, withOriginalFilename.length() - 4);
            File file = new File(doneDirectory.getPath() + File.separatorChar + originalFilenameWithoutExtension + " cleaned.csv");
            TransactionWriter.write(getTransactions(), file);
            clearTransactions();
        } else {
            Logger.getLogger(TransactionManager.class.getName()).log(Level.INFO, "Could NOT find \"done\" directory, creating {0}", doneDirectory.getPath());
            doneDirectory.mkdir();
            saveTransactionsToDoneDirectory(withOriginalFilename);
        }
    }

    public Collection<CashTransaction> getTransactions() {
        System.out.println(transactions.size() + " Transactions parsed");
        return transactions;
    }

    public void clearTransactions() {
        transactions.clear();
    }

    public static TransactionManager getTransactionManager() {
        if (transactionManager == null) {
            transactionManager = new TransactionManager();
        }
        return transactionManager;
    }

}
