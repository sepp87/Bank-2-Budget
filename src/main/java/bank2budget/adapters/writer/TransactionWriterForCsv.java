package bank2budget.adapters.writer;

import bank2budget.core.CashTransaction;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.csv.*;

/**
 *
 * @author joost
 */
public class TransactionWriterForCsv extends TransactionWriter {

    private final static Logger LOGGER = Logger.getLogger(TransactionWriterForCsv.class.getName());

    private final File target;
    private final char decimalSeparator;

    public TransactionWriterForCsv(File target, char decimalSeparator) {
        this.target = target;
        this.decimalSeparator = decimalSeparator;
    }
    
    public void write(Collection<CashTransaction> transactions) {
        CSVFormat csvFormat = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.ALL).withQuote('"');
        try (CSVPrinter printer = new CSVPrinter(new FileWriter(target, StandardCharsets.UTF_8), csvFormat)) {
            printer.printRecord((Object[]) HEADER);
            for (CashTransaction transaction : transactions) {
                printer.printRecord((Object[]) getStringArrayFrom(transaction));
            }
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Failed to write CSV file: " + target, ex);
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
