package bank2budget.adapters.reader;

import bank2budget.Launcher;
import bank2budget.adapters.parser.SimpleParserFactory;
import bank2budget.core.CashTransaction;
import bank2budget.adapters.parser.TransactionParser;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author joost
 */
public class TransactionReaderForCsvTodo {

    protected final Map<String, List<CashTransaction>> todoTransactionsPerFile;
    private final Path todoDirectory;
    private final Path[] commandLinePaths;

    public TransactionReaderForCsvTodo(Path todoDirectory, Path... commandLinePaths) {
        todoTransactionsPerFile = new TreeMap<>();
        this.todoDirectory = todoDirectory;
        this.commandLinePaths = commandLinePaths;
    }

    public Map<String, List<CashTransaction>> getPerFile() {
        return todoTransactionsPerFile;
    }

    public TransactionReaderForCsvTodo read() {
        List<Path> csvFiles = new ArrayList<>();
        csvFiles.addAll(FileUtil.filterDirectoryByExtension(".csv", todoDirectory));
        csvFiles.addAll(FileUtil.filterFilesByExtension(".csv", commandLinePaths));   
        processCsv(csvFiles);
        return this;
    }

    private void processCsv(Collection<Path> csvFiles) {
        for (Path csv : csvFiles) {
            List<CashTransaction> transactions = getTransactionsFromCsv(csv.toFile());
            todoTransactionsPerFile.put(csv.getFileName().toString(), transactions);
            System.out.println(transactions.size() + " Transactions parsed \n\n");
        }
    }

    private List<CashTransaction> getTransactionsFromCsv(File csvFile) {
        try {
            TransactionParser parser = SimpleParserFactory.createTransactionParser(csvFile);
            List<CashTransaction> transactions = parser.parse();
            return transactions;

        } catch (Exception ex) {
            Logger.getLogger(Launcher.class.getName()).log(Level.SEVERE, "Unknown bank, could NOT load transactions from file", ex);
            return Collections.emptyList();
        }
    }

}
