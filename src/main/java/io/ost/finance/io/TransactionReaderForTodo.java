package io.ost.finance.io;

import io.ost.finance.App;
import io.ost.finance.CashTransaction;
import io.ost.finance.Config;
import io.ost.finance.SimpleParserFactory;
import io.ost.finance.Util;
import io.ost.finance.parser.TransactionParser;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author joost
 */
public class TransactionReaderForTodo {

    public static final String TODO_DIRECTORY = "todo";
    private final List<CashTransaction> todoTransactions;
    protected final Map<String, List<CashTransaction>> todoTransactionsPerFile;

    public TransactionReaderForTodo() {
        todoTransactions = new ArrayList<>();
        todoTransactionsPerFile = new TreeMap<>();
    }

    public Map<String, List<CashTransaction>> getPerFile() {
        return todoTransactionsPerFile;
    }

    public TransactionReaderForTodo read() {
        File todoDirectory = new File(App.getRootDirectory() + TODO_DIRECTORY);
        if (todoDirectory.exists() && todoDirectory.isDirectory()) {
            File[] files = getFilesFromCommandLineAnd(todoDirectory);
            processCsv(files);
        } else {
            Logger.getLogger(TransactionReaderForTodo.class.getName()).log(Level.INFO, "Could NOT find \"todo\" directory, creating {0}", todoDirectory.getPath());
            todoDirectory.mkdir();
            read();
        }
        return this;
    }

    private File[] getFilesFromCommandLineAnd(File todoDirectory) {
        File[] todoFiles = Util.getFilesByExtensionFrom(todoDirectory, ".csv");
        List<File> commandLineFiles = Config.getCsvFiles();
        for (File file : todoFiles) {
            commandLineFiles.add(file);
        }
        return commandLineFiles.toArray(new File[0]);
    }

    private void processCsv(File... files) {
        for (File csv : files) {
            List<CashTransaction> transactions = getTransactionsFromCsv(csv);
            todoTransactionsPerFile.put(csv.getName(), transactions);
            System.out.println(transactions.size() + " Transactions parsed \n\n");
        }
    }

    private List<CashTransaction> getTransactionsFromCsv(File csvFile) {
        try {
            TransactionParser parser = SimpleParserFactory.createTransactionParser(csvFile);
            List<CashTransaction> transactions = parser.parse();
            return transactions;

        } catch (Exception ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, "Unknown bank, could NOT load transactions from file", ex);
            return Collections.emptyList();
        }
    }

}
