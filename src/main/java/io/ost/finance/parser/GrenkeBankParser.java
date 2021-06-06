package io.ost.finance.parser;

import io.ost.finance.CashTransaction;
import io.ost.finance.ParserConfig;
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
    public CashTransaction parseCashTransactionFrom(CSVRecord record) throws ParseException {
        CashTransaction transaction = new CashTransaction();
        transaction.setAccountNumber(accountNumber);
        transaction.setContraAccountName(record.get("Zahlungsempfänger"));
        transaction.setContraAccountNumber(record.get("ZahlungsempfängerIBAN"));
        transaction.setDescription(record.get("Vorgang/Verwendungszweck"));
        transaction.setOriginalRecord(record.toMap().values());
        parseDateFrom(record.get("Buchungstag"), transaction);
        parseDescriptionFrom(record, transaction);
        parseAmountFrom(record, transaction);
        calculateBalanceAfter(transaction);
        generateTransactionNumber(transaction);
        return transaction;
    }

}
