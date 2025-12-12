package bank2budget.adapter.parser;

import java.math.BigDecimal;
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

        return CSVFormat.DEFAULT.withDelimiter(parserConfig.getDelimiter()).withFirstRecordAsHeader();
    }

    @Override
    public List<CSVRecord> getTransactionRecordsFrom(List<CSVRecord> allRecords) {
        Collections.reverse(allRecords);
        return allRecords;
    }

    @Override
    public RawCashTransaction parseCashTransactionFromNEW(CSVRecord record) throws ParseException {
        RawCashTransaction transaction = new RawCashTransaction();
        transaction.accountNumber = (record.get("IBAN Auftragskonto"));
        transaction.contraAccountName = (record.get("Name Zahlungsbeteiligter"));
        transaction.contraAccountNumber = (record.get("IBAN Zahlungsbeteiligter"));
        transaction.amount = bigDecimalFromString(record.get("Betrag"));
        transaction.accountBalance = bigDecimalFromString(record.get("Saldo nach Buchung"));
        transaction.description = (record.get("Verwendungszweck"));
        transaction.date = parseDateFrom(record.get("Buchungstag"));
        return transaction;
    }
}
