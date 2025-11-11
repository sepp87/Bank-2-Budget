package bank2budget.adapters.writer;

import bank2budget.Launcher;
import bank2budget.core.CashTransaction;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.csv.*;

/**
 *
 * @author joost
 */
public class TransactionWriterForCsv extends TransactionWriter {

    private final Path doneDirectory;
    private final char decimalSeparator;

    public TransactionWriterForCsv(Path doneDirectory, char decimalSeparator) {
        this.doneDirectory = doneDirectory;
        this.decimalSeparator = decimalSeparator;
    }

    public void write(Map<String, List<CashTransaction>> transactionsPerFile) {
        File doneDirectory = this.doneDirectory.toFile();
        for (Entry<String, List<CashTransaction>> entry : transactionsPerFile.entrySet()) {
            String filename = entry.getKey();
            List<CashTransaction> transactions = entry.getValue();
            String filenameWithoutExtension = filename.substring(0, filename.length() - 4);
            File file = new File(doneDirectory.getPath() + File.separatorChar + filenameWithoutExtension + " cleaned.csv");
            saveTransactionsToDoneDirectory(transactions, file);
        }
    }

    private void saveTransactionsToDoneDirectory(Collection<CashTransaction> transactions, File toCsvFile) {
//        try (CSVPrinter printer = new CSVPrinter(new FileWriter(toCsvFile, StandardCharsets.UTF_8), CSVFormat.DEFAULT.withQuoteMode(QuoteMode.ALL))) {
        CSVFormat csvFormat = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.ALL).withQuote('"');
        try (CSVPrinter printer = new CSVPrinter(new FileWriter(toCsvFile, StandardCharsets.UTF_8), csvFormat)) {
            printer.printRecord((Object[]) HEADER);
            for (CashTransaction transaction : transactions) {
                printer.printRecord((Object[]) getStringArrayFrom(transaction));
            }
        } catch (IOException ex) {
            Logger.getLogger(TransactionWriterForCsv.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private String[] getStringArrayFrom(CashTransaction transaction) {
        Object[] values = getObjectArrayFrom(transaction);
        String[] stringValues = new String[values.length];
        int i = 0;
        for (Object value : values) {
            stringValues[i] = valueToString(value);
            i++;
        }
        return stringValues;
    }

    private String valueToString(Object value) {
        if (value == null) {
            return null;
        } else if (value instanceof String) {
            return value.toString();
        } else if (value instanceof Number) {
            return (value + "").replace('.', decimalSeparator);
        }
        return value.toString();
    }

}
