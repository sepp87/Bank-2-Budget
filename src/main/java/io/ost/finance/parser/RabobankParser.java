package io.ost.finance.parser;

import io.ost.finance.CashTransaction;
import io.ost.finance.ParserConfig;
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
    public CashTransaction parseCashTransactionFrom(CSVRecord record) throws ParseException {
        CashTransaction transaction = new CashTransaction();
        transaction.setAccountNumber(record.get("IBAN/BBAN"));
        transaction.setContraAccountName(record.get("Naam tegenpartij"));
        transaction.setContraAccountNumber(record.get("Tegenrekening IBAN/BBAN"));
        transaction.setAmount(getDoubleFrom(record.get("Bedrag")));
        transaction.setAccountBalance(getDoubleFrom(record.get("Saldo na trn")));
        transaction.setDescription(record.get("Omschrijving-1"));
        transaction.setOriginalRecord(record.toMap().values());
        parseDateFrom(record.get("Datum"), transaction);
        return transaction;
    }

}
