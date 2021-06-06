package io.ost.finance.parser;

import io.ost.finance.CashTransaction;
import io.ost.finance.ParserConfig;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

/**
 * Comdirect allows clients to export multiple account mutations in one CSV.
 * Because of this and the omission of the account name, balance and number
 * inside each transaction record the parseRecordsWith(CSVParser parser) needed
 * to be overridden to overtake more control over the parsing process.
 *
 * @author joost
 */
public class ComdirectParser extends TransactionParser {

    public ComdirectParser(ParserConfig config) {
        super(config);
    }

    @Override
    public CSVFormat getCsvFormat() {
        String[] header = new String[]{
            "Buchungstag",
            "Wertstellung (Valuta)",
            "Vorgang",
            "Buchungstext",
            "Umsatz in EUR"
        };
        return CSVFormat.DEFAULT.withDelimiter(';').withHeader(header);
    }

    @Override
    protected List<CSVRecord> getTransactionRecordsFrom(List<CSVRecord> records) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected CashTransaction parseCashTransactionFrom(CSVRecord record) throws ParseException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected List<CashTransaction> parseRecordsWith(CSVParser parser) throws IOException {
        List<CashTransaction> transactions = new ArrayList<>();
        List<CSVRecord> records = parser.getRecords();
        List<Batch> batches = new ArrayList<>();
        int i = 0;
        int batchStart = 4;
        for (var record : records) {
            if (isStartingBalanceRecord(record)) {
                int batchEnd = i;
                CSVRecord titleRecord = records.get(batchStart - 3);
                String startingBalance = record.get(1);
                Batch batch = new Batch();
                batch.accountName = getAccountNameFrom(titleRecord);
                batch.currentBalance = getDoubleFrom(startingBalance);
                batch.transactionRecords = records.subList(batchStart, batchEnd);
                batches.add(batch);
                batchStart = getNextBatchStart(i);
            }
            i++;
        }
        for (var batch : batches) {
            Collections.reverse(batch.transactionRecords);
            transactions.addAll(batch.parse());
        }
        return transactions;
    }

    private boolean isStartingBalanceRecord(CSVRecord record) {
        return record.get(0).startsWith("Alter Kontostand");
    }

    private String getAccountNameFrom(CSVRecord record) {
        return record.get(0).substring(8);
    }

    private int getNextBatchStart(int i) {
        return i + 4;
    }

    private class Batch {

        private double currentBalance;
        private String accountName;
        List<CSVRecord> transactionRecords;

        private Batch() {
            transactionRecords = new ArrayList<>();
        }

        private List<CashTransaction> parse() {
            List<CashTransaction> transactions = new ArrayList<>();
            for (var record : transactionRecords) {
                try {
                    CashTransaction transaction = parseCashTransactionFrom(record);
                    postProcess(transaction);
                    transactions.add(transaction);
                } catch (ParseException ex) {
                    Logger.getLogger(ComdirectParser.class.getName()).log(Level.WARNING, "Record skipped since value date was left open, meaning the cash transaction was NOT yet processed by the bank.");
                }
            }
            return transactions;
        }

        private CashTransaction parseCashTransactionFrom(CSVRecord record) throws ParseException {
            CashTransaction transaction = new CashTransaction();
            transaction.setOriginalRecord(record.toMap().values());
            transaction.setAccountName(accountName);
            transaction.setAccountNumber(accountName);
            if (accountName.equals("Visa-Karte (Kreditkarte)")) {
                transaction.setAmount(getDoubleFrom(record.get(5)));
                transaction.setDescription(record.get(4));

            } else {
                transaction.setAmount(getDoubleFrom(record.get("Umsatz in EUR")));
                transaction.setDescription(record.get("Buchungstext"));
            }
            parseDateFrom(record.get("Buchungstag"), transaction);
            calculateBalanceAfter(transaction);
            generateTransactionNumber(transaction);
            filterContraAccountNameFromDescription(transaction);
            filterContraAccountNumberFromDescription(transaction);
            return transaction;
        }

        private void calculateBalanceAfter(CashTransaction transaction) {
            double newBalance = currentBalance + transaction.getAmount();
            currentBalance = (double) Math.round(newBalance * 100) / 100;
            transaction.setAccountBalance(currentBalance);
        }

        private void filterContraAccountNameFromDescription(CashTransaction transaction) {
            String description = transaction.getDescription();
            if (description.contains(" Buchungstext:")) {
                String name = description.substring(0, description.indexOf(" Buchungstext:"));
                if (name.indexOf("Kto/IBAN:") > 0) {
                    name = name.substring(0, name.indexOf("Kto/IBAN:"));
                }
                name = name.replace("Auftraggeber: ", "");
                name = name.replace("Empfänger: ", "");
                transaction.setContraAccountName(name);
            }
        }

        private void filterContraAccountNumberFromDescription(CashTransaction transaction) {
            String description = transaction.getDescription();
            int startOfAccountNumber = description.indexOf("Kto/IBAN: ") + 10;
            int endOfAccountNumber = description.indexOf(" BLZ/BIC: ");
            if (startOfAccountNumber == -1 + 10) {
                return;
            }
            if (endOfAccountNumber == -1) {
                endOfAccountNumber = description.indexOf(" Buchungstext:");
            }
            String number = description.substring(startOfAccountNumber, endOfAccountNumber);
            transaction.setContraAccountNumber(number);
        }
    }
}
