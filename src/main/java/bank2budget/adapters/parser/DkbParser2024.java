package bank2budget.adapters.parser;

import bank2budget.core.CashTransaction;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

public class DkbParser2024 extends TransactionParser {

    private double currentBalance;
    protected String accountNumber;

    public DkbParser2024(ParserConfig config) {
        super(config);
    }

    @Override
    public CSVFormat getCsvFormat() {
        String[] header = new String[]{
            "Buchungsdatum",
            "Wertstellung",
            "Status",
            "Zahlungspflichtige*r",
            "Zahlungsempfänger*in",
            "Verwendungszweck",
            "Umsatztyp",
            "IBAN",
            "Betrag (€)",
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
        CSVRecord closingBalanceRecord = allRecords.get(2);
        double balance = getBalanceFrom(closingBalanceRecord);
        for (CSVRecord record : transactionRecords) {
            balance = balance - getDoubleFrom(record.get("Betrag (€)"));
        }
        return balance;
    }

    private double getBalanceFrom(CSVRecord closingBalanceRecord) {
        String numberString = closingBalanceRecord.get(1).replace(" €", "");
        return getDoubleFrom(numberString);
    }

    private String getAccountNumberFrom(CSVRecord record) {
        return record.get(1);
    }

    @Override
    public CashTransaction parseCashTransactionFrom(CSVRecord record) throws ParseException {
        CashTransaction transaction = new CashTransaction();
        transaction.setAccountNumber(accountNumber);
        transaction.setAmount(getDoubleFrom(record.get("Betrag (€)")));
        if (transaction.getTransactionType() == CashTransaction.TransactionType.DEBIT) {
            transaction.setContraAccountName(record.get("Zahlungsempfänger*in"));
        } else {
            transaction.setContraAccountName(record.get("Zahlungspflichtige*r"));
        }
        transaction.setContraAccountNumber(record.get("IBAN"));
        transaction.setDescription(record.get("Verwendungszweck"));
        transaction.setOriginalRecord(record.toMap().values());
        parseDateFrom(record.get("Buchungsdatum"), transaction);
        calculateBalanceAfter(transaction);
        return transaction;
    }

    private void calculateBalanceAfter(CashTransaction transaction) {
        double newBalance = currentBalance + transaction.getAmount();
        currentBalance = (double) Math.round(newBalance * 100) / 100;
        transaction.setAccountBalance(currentBalance);
    }

}
