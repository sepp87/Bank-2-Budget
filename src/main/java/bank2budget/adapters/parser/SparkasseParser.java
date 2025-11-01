package bank2budget.adapters.parser;

import bank2budget.core.CashTransaction;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

//TODO
public class SparkasseParser extends TransactionParser {

    private double currentBalance = 0;

    public SparkasseParser(ParserConfig config) {
        super(config);
    }

    @Override
    public CSVFormat getCsvFormat() {
        String[] header1 = new String[]{
            "Auftragskonto", "Buchungstag", "Valutadatum", "Buchungstext",
            "Verwendungszweck", "Glaeubiger ID", "Mandatsreferenz",
            "Kundenreferenz (End-to-End)", "Sammlerreferenz",
            "Lastschrift Ursprungsbetrag", "Auslagenersatz Ruecklastschrift",
            "Beguenstigter/Zahlungspflichtiger", "Kontonummer/IBAN",
            "BIC (SWIFT-Code)", "Betrag", "Waehrung", "Info"};

        String[] header2 = new String[]{
            "Auftragskonto", "Buchungstag", "Valutadatum", "Buchungstext",
            "Verwendungszweck", "Beguenstigter/Zahlungspflichtiger",
            "Kontonummer", "BLZ", "Betrag", "Waehrung", "Info"};

        return CSVFormat.DEFAULT.withDelimiter(config.getDelimiter()).withFirstRecordAsHeader();
    }

    @Override
    public List<CSVRecord> getTransactionRecordsFrom(List<CSVRecord> allRecords) {
        currentBalance = calculateBalanceDeltaFrom(allRecords);
        Collections.reverse(allRecords);
        return allRecords;
    }

    
    private double calculateBalanceDeltaFrom(List<CSVRecord> allRecords) {
        double delta = 0;
        for (CSVRecord record : allRecords) {
            if (isNotProcessedByBank(record)) {
                continue;
            }
            delta -= getDoubleFrom(record.get("Betrag"));
        }
        return delta;
    }
    
    private boolean isNotProcessedByBank(CSVRecord record) {
        return record.get("Info").equals("Umsatz vorgemerkt");
    }

    @Override
    public CashTransaction parseCashTransactionFrom(CSVRecord record) throws ParseException {
        CashTransaction transaction = new CashTransaction();
        if (isNotProcessedByBank(record)) {
            Logger.getLogger(SparkasseParser.class.getName()).log(Level.WARNING, "Record skipped since value date was left open, meaning the cash transaction was NOT yet processed by the bank.");
            return null;
        }
        transaction.setAccountNumber(record.get("Auftragskonto"));
        transaction.setContraAccountName(record.get("Beguenstigter/Zahlungspflichtiger"));
        transaction.setContraAccountNumber(getContraAccountNumberFrom(record));
        transaction.setAmount(getDoubleFrom(record.get("Betrag")));
        transaction.setDescription(record.get("Verwendungszweck"));
        transaction.setOriginalRecord(record.toMap().values());
        parseDateFrom(record.get("Buchungstag"), transaction);
        calculateBalanceAfter(transaction);
        return transaction;
    }

    private String getContraAccountNumberFrom(CSVRecord record) {
        String contraAccountNumber;
        if (record.getParser().getHeaderNames().contains("Kontonummer")) {
            contraAccountNumber = record.get("Kontonummer");
        } else {
            contraAccountNumber = record.get("Kontonummer/IBAN");
        }
        return contraAccountNumber;
    }

    private void calculateBalanceAfter(CashTransaction transaction) {
        double newBalance = currentBalance + transaction.getAmount();
        currentBalance = (double) Math.round(newBalance * 100) / 100;
        transaction.setAccountBalance(currentBalance);
    }
}
