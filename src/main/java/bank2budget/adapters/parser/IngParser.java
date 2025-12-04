package bank2budget.adapters.parser;

import bank2budget.core.CashTransaction;
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

    private void parseAmountFrom(CSVRecord record, CashTransaction transaction) {
        String plusOrMinus = record.get("Af Bij").equals("Af") ? "-" : "+";
        String amountString = plusOrMinus + record.get("Bedrag (EUR)");
        double amount = getDoubleFrom(amountString);
        transaction.setAmount(amount);
    }

    private void calculateBalanceAfter(CashTransaction transaction) {
        currentBalance = currentBalance.add(BigDecimal.valueOf(transaction.getAmount()));
        transaction.setAccountBalance(currentBalance.doubleValue());
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
            transaction.accountBalance = BigDecimal.valueOf(getDoubleFrom(record.get("Saldo na mutatie")));
        } else {
            calculateBalanceAfterNEW(transaction);
        }
        transaction.description = (record.get("Mededelingen"));
        return transaction;
    }

    private BigDecimal parseAmountFrom(CSVRecord record) {
        String plusOrMinus = record.get("Af Bij").equals("Af") ? "-" : "+";
        String amountString = plusOrMinus + record.get("Bedrag (EUR)");
        return BigDecimal.valueOf(getDoubleFrom(amountString));
    }

    private void calculateBalanceAfterNEW(RawCashTransaction transaction) {
        currentBalance = currentBalance.add(transaction.amount);
        transaction.accountBalance = currentBalance;
    }

}
