package bank2budget.adapters.parser;

import bank2budget.core.CashTransaction;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

public class RabobankParser extends TransactionParser {

    public RabobankParser(ParserConfig config) {
        super(config);
    }

    @Override
    public CSVFormat getCsvFormat() {
        String[] header = new String[]{
            "IBAN/BBAN", "Munt", "BIC", "Volgnr", "Datum", "Rentedatum",
            "Bedrag", "Saldo na trn", "Tegenrekening IBAN/BBAN", "Naam tegenpartij",
            "Naam uiteindelijke partij", "Naam initiërende partij", "BIC tegenpartij",
            "Code", "Batch ID", "Transactiereferentie", "Machtigingskenmerk",
            "Incassant ID", "Betalingskenmerk", "Omschrijving-1", "Omschrijving-2",
            "Omschrijving-3", "Reden retour", "Oorspr bedrag", "Oorspr munt", "Koers"
        };
        return CSVFormat.DEFAULT.withFirstRecordAsHeader();
    }

    @Override
    public List<CSVRecord> getTransactionRecordsFrom(List<CSVRecord> allRecords) {
        return allRecords.subList(1, allRecords.size());
    }

    @Override
    public RawCashTransaction parseCashTransactionFromNEW(CSVRecord record) throws ParseException {
        RawCashTransaction transaction = new RawCashTransaction();
        transaction.accountNumber = (record.get("IBAN/BBAN"));
        transaction.contraAccountName = (record.get("Naam tegenpartij"));
        transaction.contraAccountNumber = (record.get("Tegenrekening IBAN/BBAN"));
        transaction.amount = BigDecimal.valueOf(getDoubleFrom(record.get("Bedrag")));
        transaction.accountBalance = BigDecimal.valueOf(getDoubleFrom(record.get("Saldo na trn")));
        transaction.description = (record.get("Omschrijving-1"));
        transaction.date = parseDateFrom(record.get("Datum"));
        return transaction;
    }

}
