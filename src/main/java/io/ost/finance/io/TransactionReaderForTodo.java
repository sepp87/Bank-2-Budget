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
    private final Map<String, List<CashTransaction>> todoTransactionsPerFile;

    public TransactionReaderForTodo() {
        todoTransactions = new ArrayList<>();
        todoTransactionsPerFile = new TreeMap<>();
    }

    public Map<String, List<CashTransaction>> getPerFile() {
        return todoTransactionsPerFile;
    }

    public List<CashTransaction> getAsList() {
        return todoTransactions;
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

        // Transactions can contain duplicates, because of the below statement we need a cleanup routine here
        // as we near the retrieval date of a bank statement, some transactions might still be missing, 
        // because the bank did not yet process them. As soon as these transactions get processed
        // these transactions might change the ordering. Meaning, newer transactions from
        // newer bank statements get prejudice.
        // 
        // edge case - a bank statement can contain less transactions for a given day than in another statement
        // we need to figure out which day from which bank statement contains the highest transaction count. 
        // we assume the day with the highest count contains all the transactions that were made that day.
        // the day with the heighest count is added to the total transactions list
        List<CashTransaction> transactions = getUniqueTransactionsOnly();
        CashTransaction.sortAscending(transactions);
        todoTransactions.addAll(transactions);
    }

    private List<CashTransaction> getUniqueTransactionsOnly() {
        List<CashTransaction> result = new ArrayList<>();
        Map<String, Object[]> dateToFileCount = new TreeMap<>();

        for (Entry<String, List<CashTransaction>> entry : todoTransactionsPerFile.entrySet()) {
            int candidateHighScore = 0;
            for (CashTransaction transaction : entry.getValue()) {

                if (!dateToFileCount.containsKey(transaction.date)) {
                    candidateHighScore = 0;
                    Object[] fileCount = {entry.getKey(), candidateHighScore};
                    dateToFileCount.put(transaction.date, fileCount);
                } else {

                    Object[] fileCount = dateToFileCount.get(transaction.date);
                    String currentFile = entry.getKey();
                    String highScoreFile = (String) fileCount[0];
                    if (currentFile.equals(highScoreFile)) {
                        candidateHighScore = 0;
                        int highScore = (int) fileCount[1];
                        fileCount[1] = highScore + 1;

                    } else {
                        candidateHighScore++;
                        int currentHighScore = (int) fileCount[1];
                        if (candidateHighScore > currentHighScore) {
                            Object[] newFileHighScore = {currentFile, currentHighScore};
                            dateToFileCount.put(transaction.date, newFileHighScore);
                        }
                    }
                }
            }
        }

        for (Entry<String, List<CashTransaction>> entry : todoTransactionsPerFile.entrySet()) {

            for (CashTransaction transaction : entry.getValue()) {
                String fileWithMaxCountForDate = (String) dateToFileCount.get(transaction.date)[0];
                String file = entry.getKey();
                if (file.equals(fileWithMaxCountForDate)) {
                    result.add(transaction);
//                    System.out.println(transaction.date + "\t" + file);
                }
            }
        }
        return result;
    }

    private List<CashTransaction> getTransactionsFromCsv(File csvFile) {
        try {
            TransactionParser parser = SimpleParserFactory.createTransactionParser(csvFile);
            List<CashTransaction> transactions = parser.parse();
            return transactions;

        } catch (Exception ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, "Unknown bank, could NOT load transactions from file", ex);
            return Collections.EMPTY_LIST;
        }
    }

}
