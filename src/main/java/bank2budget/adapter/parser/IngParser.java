package bank2budget.adapter.parser;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

public class IngParser extends TransactionParser {

    private BigDecimal currentBalance = BigDecimal.ZERO;

    public IngParser(ParserConfig config) {
        super(config);
    }

    @Override
    public CSVFormat getCsvFormat() {
        String[] header = new String[]{
            "Datum", "Naam / Omschrijving", "Rekening", "Tegenrekening", "Code", "Af Bij",
            "Bedrag (EUR)", "Mutatiesoort", "Mededelingen", "Saldo na mutatie", "Tag"
        };
        return CSVFormat.DEFAULT.withDelimiter(parserConfig.getDelimiter()).withFirstRecordAsHeader();
    }

    @Override
    public List<CSVRecord> getTransactionRecordsFrom(List<CSVRecord> allRecords) {
        Collections.reverse(allRecords);
        return allRecords.subList(1, allRecords.size());
    }

    @Override
    public RawCashTransaction parseCashTransactionFromNEW(CSVRecord record) throws ParseException {
        RawCashTransaction transaction = new RawCashTransaction();
        transaction.amount = parseAmountFrom(record);
        transaction.date = parseDateFrom(record.get("Datum"));
        transaction.accountNumber = (record.get("Rekening"));
        transaction.contraAccountName = (record.get("Naam / Omschrijving"));
        transaction.contraAccountNumber = (record.get("Tegenrekening"));
        if (parserConfig.getDelimiter() == ';') {
            transaction.accountBalance = bigDecimalFromString(record.get("Saldo na mutatie"));
        } else {
            calculateBalanceAfterNEW(transaction);
        }
        transaction.description = (record.get("Mededelingen"));
        return transaction;
    }

    private BigDecimal parseAmountFrom(CSVRecord record) {
        String plusOrMinus = record.get("Af Bij").equals("Af") ? "-" : "+";
        String amountString = plusOrMinus + record.get("Bedrag (EUR)");
        return bigDecimalFromString(amountString);
    }

    private void calculateBalanceAfterNEW(RawCashTransaction transaction) {
        currentBalance = currentBalance.add(transaction.amount);
        transaction.accountBalance = currentBalance;
    }

}
