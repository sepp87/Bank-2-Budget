package io.ost.finance.parser;

import io.ost.finance.CashTransaction;
import io.ost.finance.ParserConfig;
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
        return CSVFormat.DEFAULT.withDelimiter(config.getDelimiter()).withHeader(header);
    }

    @Override
    public List<CSVRecord> getTransactionRecordsFrom(List<CSVRecord> allRecords) {
        return allRecords;
    }

    // TODO Saldo na trn is actually Saldo voor trn
    @Override
    public CashTransaction parseCashTransactionFrom(CSVRecord record) throws ParseException {
        CashTransaction transaction = new CashTransaction();
        transaction.setAccountNumber(record.get("IBAN/BBAN"));
        transaction.setContraAccountName(record.get("Naam tegenpartij"));
        transaction.setContraAccountNumber(record.get("Tegenrekening IBAN/BBAN"));
        transaction.setAmount(Double.parseDouble(record.get("Bedrag")));
        double balance = Double.parseDouble(record.get("Saldo voor trn")) + transaction.getAmount();
        transaction.setAccountBalance(balance);
        transaction.setDescription(record.get("Omschrijving"));
        transaction.setOriginalRecord(record.toMap().values());
        parseDateFrom(record.get("Datum"), transaction);
        return transaction;
    }

}
