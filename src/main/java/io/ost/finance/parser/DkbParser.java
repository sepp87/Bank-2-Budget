package io.ost.finance.parser;

import io.ost.finance.CashTransaction;
import io.ost.finance.ParserConfig;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

public class DkbParser extends TransactionParser {

    private double currentBalance;
    protected String accountNumber;

    public DkbParser(ParserConfig config) {
        super(config);
    }


    @Override
    public CSVFormat getCsvFormat() {
        String[] header = new String[]{
            "Buchungstag",
            "Wertstellung",
            "Buchungstext",
            "Auftraggeber / Begünstigter",
            "Verwendungszweck",
            "Kontonummer",
            "BLZ",
            "Betrag (EUR)",
            "Gläubiger-ID",
            "Mandatsreferenz",
            "Kundenreferenz"
        };
        return CSVFormat.DEFAULT.withDelimiter(';').withHeader(header);
    }

    @Override
    public List<CSVRecord> getTransactionRecordsFrom(List<CSVRecord> allRecords) { // and set current balance to calculateBalanceAfter(CashTransaction transaction)
        List<CSVRecord> transactionRecords = allRecords.subList(5, allRecords.size());
        Collections.reverse(transactionRecords);
        currentBalance = getStartingBalanceFrom(allRecords);
        CSVRecord accountNumberRecord = allRecords.get(0);
        accountNumber = getAccountNumberFrom(accountNumberRecord);
        return transactionRecords;
    }

    private double getStartingBalanceFrom(List<CSVRecord> allRecords) {
        List<CSVRecord> transactionRecords = allRecords.subList(5, allRecords.size());
        CSVRecord closingBalanceRecord = allRecords.get(3);
        double balance = getBalanceFrom(closingBalanceRecord);
        for (CSVRecord record : transactionRecords) {
            balance = balance - getDoubleFrom(record.get("Betrag (EUR)"));
        }
        return balance;
    }

    private double getBalanceFrom(CSVRecord closingBalanceRecord) {
        String numberString = closingBalanceRecord.get(1).replace(" EUR", "");
        return getDoubleFrom(numberString);
    }

    private String getAccountNumberFrom(CSVRecord record) {
        return record.get(1).split(" ")[0];
    }

    @Override
    public CashTransaction parseCashTransactionFrom(CSVRecord record) throws ParseException {
        CashTransaction transaction = new CashTransaction();
        transaction.setAccountNumber(accountNumber);
        transaction.setContraAccountName(record.get("Auftraggeber / Begünstigter"));
        transaction.setContraAccountNumber(record.get("Kontonummer"));
        transaction.setAmount(getDoubleFrom(record.get("Betrag (EUR)")));
        transaction.setDescription(record.get("Verwendungszweck"));
        transaction.setOriginalRecord(record.toMap().values());
        parseDateFrom(record.get("Buchungstag"), transaction);
        calculateBalanceAfter(transaction);
        return transaction;
    }

    private void calculateBalanceAfter(CashTransaction transaction) {
        double newBalance = currentBalance + transaction.getAmount();
        currentBalance = (double) Math.round(newBalance * 100) / 100;
        transaction.setAccountBalance(currentBalance);
    }

}
