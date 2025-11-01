package bank2budget.adapters.parser;

import bank2budget.core.CashTransaction;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

public class IngParser extends TransactionParser {

    private double currentBalance = 0;

    public IngParser(ParserConfig config) {
        super(config);
    }

    @Override
    public CSVFormat getCsvFormat() {
        String[] header = new String[]{
            "Datum", "Naam / Omschrijving", "Rekening", "Tegenrekening", "Code", "Af Bij",
            "Bedrag (EUR)", "Mutatiesoort", "Mededelingen", "Saldo na mutatie", "Tag"
        };
        return CSVFormat.DEFAULT.withDelimiter(config.getDelimiter()).withFirstRecordAsHeader();
    }

    @Override
    public List<CSVRecord> getTransactionRecordsFrom(List<CSVRecord> allRecords) {
        Collections.reverse(allRecords);
        return allRecords.subList(1, allRecords.size());
    }

    @Override
    public CashTransaction parseCashTransactionFrom(CSVRecord record) throws ParseException {
        CashTransaction transaction = new CashTransaction();
        parseAmountFrom(record, transaction);
        parseDateFrom(record.get("Datum"), transaction);
        transaction.setAccountNumber(record.get("Rekening"));
        transaction.setContraAccountName(record.get("Naam / Omschrijving"));
        transaction.setContraAccountNumber(record.get("Tegenrekening"));
        if (config.getDelimiter() == ';') {
            transaction.setAccountBalance(getDoubleFrom(record.get("Saldo na mutatie")));
            transaction.setLabel(record.get("Tag"));
        } else {
            calculateBalanceAfter(transaction);
        }
        transaction.setDescription(record.get("Mededelingen"));
        transaction.setOriginalRecord(record.toMap().values());
        return transaction;
    }

    private void parseAmountFrom(CSVRecord record, CashTransaction transaction) {
        String plusOrMinus = record.get("Af Bij").equals("Af") ? "-" : "+";
        String amountString = plusOrMinus + record.get("Bedrag (EUR)");
        double amount = getDoubleFrom(amountString);
        transaction.setAmount(amount);
    }

    private void calculateBalanceAfter(CashTransaction transaction) {
        double newBalance = currentBalance + transaction.getAmount();
        currentBalance = (double) Math.round(newBalance * 100) / 100;
        transaction.setAccountBalance(currentBalance);
    }
}
