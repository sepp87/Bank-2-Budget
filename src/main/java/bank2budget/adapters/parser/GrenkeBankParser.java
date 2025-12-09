package bank2budget.adapters.parser;

import java.text.ParseException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

public class GrenkeBankParser extends MuenchnerBankParser {

    public GrenkeBankParser(ParserConfig config) {
        super(config);
        startingBalanceRecordOffset = 3;
        firstRecordIndex = 10;
    }

    @Override
    public CSVFormat getCsvFormat() {
        String[] header = new String[]{
            "Buchungstag", "Valuta", "Textschlüssel", "Primanota",
            "Zahlungsempfänger", "ZahlungsempfängerKto",
            "ZahlungsempfängerIBAN", "ZahlungsempfängerBLZ",
            "ZahlungsempfängerBIC", "Vorgang/Verwendungszweck",
            "Kundenreferenz", "Währung", "Umsatz", "Soll/Haben"
        };
        return CSVFormat.DEFAULT.withDelimiter(';').withHeader(header);
    }

    @Override
    public RawCashTransaction parseCashTransactionFromNEW(CSVRecord record) throws ParseException {
        RawCashTransaction transaction = new RawCashTransaction();
        transaction.accountNumber = (accountNumber);
        transaction.contraAccountName = (record.get("Zahlungsempfänger"));
        transaction.contraAccountNumber = (record.get("ZahlungsempfängerIBAN"));
        transaction.description = (record.get("Vorgang/Verwendungszweck"));
        transaction.date  = parseDateFrom(record.get("Buchungstag"));
        transaction.description = parseDescriptionFrom(record);
        transaction.amount = parseAmountFrom(record);
        calculateBalanceAfterNEW(transaction);
        return transaction;
    }

}
