package io.ost.finance.io;

import io.ost.finance.App;
import io.ost.finance.CashTransaction;
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

public class TransactionWriterForDone {

    public static final String DONE_DIRECTORY = "done";

    public void write(Map<String, List<CashTransaction>> transactionsPerFile) {
        File doneDirectory = new File(App.getRootDirectory() + DONE_DIRECTORY);

        if (doneDirectory.exists() && doneDirectory.isDirectory()) {
            for (Entry<String, List<CashTransaction>> entry : transactionsPerFile.entrySet()) {
                String filename = entry.getKey();
                List<CashTransaction> transactions = entry.getValue();
                String filenameWithoutExtension = filename.substring(0, filename.length() - 4);
                File file = new File(doneDirectory.getPath() + File.separatorChar + filenameWithoutExtension + " cleaned.csv");
                saveTransactionsToDoneDirectory(transactions, file);
            }
        } else {
            Logger.getLogger(App.class.getName()).log(Level.INFO, "Could NOT find \"done\" directory, creating {0}", doneDirectory.getPath());
            doneDirectory.mkdir();
            write(transactionsPerFile);
        }
    }

    private static void saveTransactionsToDoneDirectory(Collection<CashTransaction> transactions, File toCsvFile) {
//        try (CSVPrinter printer = new CSVPrinter(new FileWriter(toCsvFile, StandardCharsets.UTF_8), CSVFormat.DEFAULT.withQuoteMode(QuoteMode.ALL))) {
        CSVFormat csvFormat = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.ALL).withQuote('"');
        try (CSVPrinter printer = new CSVPrinter(new FileWriter(toCsvFile, StandardCharsets.UTF_8), csvFormat)) {
            printer.printRecord(CashTransaction.getHeader());
            for (CashTransaction transaction : transactions) {
                printer.printRecord(transaction.toRecord());
            }
        } catch (IOException ex) {
            Logger.getLogger(TransactionWriterForDone.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
