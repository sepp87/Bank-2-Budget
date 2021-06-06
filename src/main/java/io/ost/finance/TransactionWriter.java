package io.ost.finance;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.csv.*;

public class TransactionWriter {

    public static void write(Collection<CashTransaction> transactions, File toCsvFile) {
//        try (CSVPrinter printer = new CSVPrinter(new FileWriter(toCsvFile, StandardCharsets.UTF_8), CSVFormat.DEFAULT.withQuoteMode(QuoteMode.ALL))) {
        CSVFormat csvFormat = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.ALL).withQuote('"');
        try (CSVPrinter printer = new CSVPrinter(new FileWriter(toCsvFile, StandardCharsets.UTF_8), csvFormat)) {
            printer.printRecord(CashTransaction.getHeader());
            for (CashTransaction transaction : transactions) {
                printer.printRecord(transaction.toRecord());
            }
        } catch (IOException ex) {
            Logger.getLogger(TransactionWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
