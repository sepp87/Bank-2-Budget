package bank2budget.adapters.reader;

import bank2budget.adapters.parser.TransactionParserFactory;
import bank2budget.adapters.parser.TransactionParser;
import bank2budget.core.Transaction;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author joost
 */
public class TransactionReaderForCsv {

    private final static Logger LOGGER = Logger.getLogger(TransactionReaderForCsv.class.getName());

    private final File file;
    private final List<Transaction> transactions;

    public TransactionReaderForCsv(File file) {
        this.file = file;
        this.transactions = getTransactionsFromCsv(file);
    }

    public List<Transaction> getTransactions() {
        return List.copyOf(transactions);
    }

    private List<Transaction> getTransactionsFromCsv(File csvFile) {
        try {
            TransactionParser parser = TransactionParserFactory.createTransactionParser(csvFile);
            var result = parser.parse();
            LOGGER.log(Level.INFO, "{0}: {1} transactions parsed", new Object[]{file.getName(), result.size()});
            return result;

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Unknown bank, could NOT load transactions from file", ex);
            return Collections.emptyList();
        }
    }

}
