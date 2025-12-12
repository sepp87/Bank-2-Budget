package bank2budget.adapter.parser;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

public class SnsBankParser extends TransactionParser {

    public SnsBankParser(ParserConfig config) {
        super(config);
    }

    @Override
    public CSVFormat getCsvFormat() {
        String[] header = new String[]{
            "Datum", "IBAN/BBAN", "Tegenrekening IBAN/BBAN", "Naam tegenpartij",
            "Unknown value 1", "Unknown value 2", "Unknown value 3", "Munt", "Saldo voor trn", "Munt",
            "Bedrag", "Transactiedatum", "Valutadatum", "Unknown number 1",
            "Soort bij-/afschrijving", "Unknown number 2", "Unknown value 4",
            "Omschrijving", "Unknown number 3",};
        return CSVFormat.DEFAULT.withDelimiter(parserConfig.getDelimiter()).withHeader(header);
    }

    @Override
    public List<CSVRecord> getTransactionRecordsFrom(List<CSVRecord> allRecords) {
        return allRecords;
    }

    // TODO Saldo na trn is actually Saldo voor trn
    @Override
    public RawCashTransaction parseCashTransactionFromNEW(CSVRecord record) throws ParseException {
        RawCashTransaction transaction = new RawCashTransaction();
        transaction.accountNumber = (record.get("IBAN/BBAN"));
        transaction.contraAccountName = (record.get("Naam tegenpartij"));
        transaction.contraAccountNumber = (record.get("Tegenrekening IBAN/BBAN"));
        transaction.amount = BigDecimal.valueOf(Double.parseDouble(record.get("Bedrag")));
        BigDecimal balance = BigDecimal.valueOf(Double.parseDouble(record.get("Saldo voor trn"))).add(transaction.amount);
        transaction.accountBalance = (balance);
        transaction.description = (record.get("Omschrijving"));
        transaction.date = parseDateFrom(record.get("Datum"));
        return transaction;
    }

}
