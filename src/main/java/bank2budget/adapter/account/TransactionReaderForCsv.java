package bank2budget.adapter.account;

import bank2budget.adapter.parser.TransactionParserFactory;
import bank2budget.adapter.parser.TransactionParser;
import bank2budget.core.CashTransaction;
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
    private final List<CashTransaction> transactions;

    public TransactionReaderForCsv(File file) {
        this.file = file;
        this.transactions = getTransactionsFromCsv(file);
//        System.out.println();
    }

    public List<CashTransaction> getTransactions() {
        return List.copyOf(transactions);
    }

    private List<CashTransaction> getTransactionsFromCsv(File csvFile) {
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
