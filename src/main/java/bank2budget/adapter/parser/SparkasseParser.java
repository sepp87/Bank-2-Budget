package bank2budget.adapter.parser;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

//TODO
public class SparkasseParser extends TransactionParser {

    private BigDecimal currentBalance = BigDecimal.ZERO;

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

        return CSVFormat.DEFAULT.withDelimiter(parserConfig.getDelimiter()).withFirstRecordAsHeader();
    }

    @Override
    public List<CSVRecord> getTransactionRecordsFrom(List<CSVRecord> allRecords) {
        currentBalance = calculateBalanceDeltaFrom(allRecords);
        Collections.reverse(allRecords);
        return allRecords;
    }

    private BigDecimal calculateBalanceDeltaFrom(List<CSVRecord> allRecords) {
        BigDecimal delta = BigDecimal.ONE;
        for (CSVRecord record : allRecords) {
            if (isNotProcessedByBank(record)) {
                continue;
            }
            BigDecimal amount = bigDecimalFromString(record.get("Betrag"));
            delta = delta.min(amount);
        }
        return delta;
    }

    private boolean isNotProcessedByBank(CSVRecord record) {
        return record.get("Info").equals("Umsatz vorgemerkt");
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

    @Override
    public RawCashTransaction parseCashTransactionFromNEW(CSVRecord record) throws ParseException {
        RawCashTransaction transaction = new RawCashTransaction();
        if (isNotProcessedByBank(record)) {
            Logger.getLogger(SparkasseParser.class.getName()).log(Level.WARNING, "Record skipped since value date was left open, meaning the cash transaction was NOT yet processed by the bank.");
            return null;
        }
        transaction.contraAccountNumber = (record.get("Auftragskonto"));
        transaction.contraAccountName = (record.get("Beguenstigter/Zahlungspflichtiger"));
        transaction.contraAccountNumber = (getContraAccountNumberFrom(record));
        transaction.amount = bigDecimalFromString(record.get("Betrag"));
        transaction.description = (record.get("Verwendungszweck"));
        transaction.date = parseDateFrom(record.get("Buchungstag"));
        calculateBalanceAfterNEW(transaction);
        return transaction;
    }

    private void calculateBalanceAfterNEW(RawCashTransaction transaction) {
        currentBalance = currentBalance.add(transaction.amount);
        transaction.accountBalance = currentBalance;
    }

}
