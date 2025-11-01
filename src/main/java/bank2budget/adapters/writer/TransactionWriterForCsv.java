package bank2budget.adapters.writer;

import bank2budget.cli.Launcher;
import bank2budget.core.CashTransaction;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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

    public void write(Map<String, List<CashTransaction>> transactionsPerFile) {
        File doneDirectory = new File(Launcher.getDoneDirectory());
        for (Entry<String, List<CashTransaction>> entry : transactionsPerFile.entrySet()) {
            String filename = entry.getKey();
            List<CashTransaction> transactions = entry.getValue();
            String filenameWithoutExtension = filename.substring(0, filename.length() - 4);
            File file = new File(doneDirectory.getPath() + File.separatorChar + filenameWithoutExtension + " cleaned.csv");
            saveTransactionsToDoneDirectory(transactions, file);
        }
    }

    private static void saveTransactionsToDoneDirectory(Collection<CashTransaction> transactions, File toCsvFile) {
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

}
