package io.ost.finance.parser;

import io.ost.finance.CashTransaction;
import io.ost.finance.ParserConfig;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

public class GlsParser2022 extends TransactionParser {

    public GlsParser2022(ParserConfig config) {
        super(config);
    }

    @Override
    public CSVFormat getCsvFormat() {
        String[] header = new String[]{
            "Bezeichnung Auftragskonto",
            "IBAN Auftragskonto",
            "BIC Auftragskonto",
            "Bankname Auftragskonto",
            "Buchungstag",
            "Valutadatum",
            "Name Zahlungsbeteiligter",
            "IBAN Zahlungsbeteiligter",
            "BIC (SWIFT-Code) Zahlungsbeteiligter",
            "Buchungstext",
            "Verwendungszweck",
            "Betrag",
            "Waehrung",
            "Saldo nach Buchung",
            "Bemerkung",
            "Kategorie",
            "Steuerrelevant",
            "Glaeubiger ID",
            "Mandatsreferenz"
        };
        
        return CSVFormat.DEFAULT.withDelimiter(config.getDelimiter()).withFirstRecordAsHeader();
    }

    @Override
    public List<CSVRecord> getTransactionRecordsFrom(List<CSVRecord> allRecords) {
        Collections.reverse(allRecords);
        return allRecords;
    }

    @Override
    public CashTransaction parseCashTransactionFrom(CSVRecord record) throws ParseException {
        CashTransaction transaction = new CashTransaction();
        transaction.setAccountNumber(record.get("IBAN Auftragskonto"));
        transaction.setContraAccountName(record.get("Name Zahlungsbeteiligter"));
        transaction.setContraAccountNumber(record.get("IBAN Zahlungsbeteiligter"));
        transaction.setAmount(getDoubleFrom(record.get("Betrag")));
        transaction.setAccountBalance(getDoubleFrom(record.get("Saldo nach Buchung")));
        transaction.setDescription(record.get("Verwendungszweck"));
        transaction.setOriginalRecord(record.toMap().values());
        parseDateFrom(record.get("Buchungstag"), transaction);
        generateTransactionNumber(transaction);
        return transaction;
    }
}
