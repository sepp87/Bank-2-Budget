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
public class TransactionReaderForCsv {

    protected final Map<String, List<CashTransaction>> transactionsPerFile = new TreeMap<>();

    public TransactionReaderForCsv(Path directory) {
        List<Path> csvFiles = FileUtil.filterDirectoryByExtension(".csv", directory);
        processCsv(csvFiles);
    }

    public TransactionReaderForCsv(Path... files) {
        List<Path> csvFiles = FileUtil.filterFilesByExtension(".csv", files);
        processCsv(csvFiles);
    }

    public TransactionReaderForCsv(Collection<File> files) {
        List<Path> csvFiles = FileUtil.filterFilesByExtension(".csv", files.stream().map(File::toPath).toArray(Path[]::new));
        processCsv(csvFiles);
    }

    public Map<String, List<CashTransaction>> getPerFile() {
        return transactionsPerFile;
    }

    public List<CashTransaction> getAllTransactions() {
        List<CashTransaction> result = new ArrayList<>();
        for(List<CashTransaction> transactions : transactionsPerFile.values()) {
            result.addAll(transactions);
        }
        return result;
    }

    private void processCsv(Collection<Path> csvFiles) {
        for (Path csv : csvFiles) {
            List<CashTransaction> transactions = getTransactionsFromCsv(csv.toFile());
            transactionsPerFile.put(csv.getFileName().toString(), transactions);
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
