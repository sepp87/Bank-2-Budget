package bank2budget.adapter.parser;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

public class DkbParser extends TransactionParser {

    private BigDecimal currentBalance;
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

    private BigDecimal getStartingBalanceFrom(List<CSVRecord> allRecords) {
        List<CSVRecord> transactionRecords = allRecords.subList(5, allRecords.size());
        CSVRecord closingBalanceRecord = allRecords.get(3);
        BigDecimal balance = getBalanceFrom(closingBalanceRecord);
        for (CSVRecord record : transactionRecords) {
            balance = balance.subtract(bigDecimalFromString(record.get("Betrag (EUR)")));
        }
        return balance;
    }

    private BigDecimal getBalanceFrom(CSVRecord closingBalanceRecord) {
        String numberString = closingBalanceRecord.get(1).replace(" EUR", "");
        return bigDecimalFromString(numberString);
    }

    private String getAccountNumberFrom(CSVRecord record) {
        return record.get(1).split(" ")[0];
    }

    @Override
    public RawCashTransaction parseCashTransactionFromNEW(CSVRecord record) throws ParseException {
        RawCashTransaction transaction = new RawCashTransaction();
        transaction.accountNumber = (accountNumber);
        transaction.contraAccountName = (record.get("Auftraggeber / Begünstigter"));
        transaction.contraAccountNumber = (record.get("Kontonummer"));
        transaction.amount = bigDecimalFromString(record.get("Betrag (EUR)"));
        transaction.description = (record.get("Verwendungszweck"));
        transaction.date = parseDateFrom(record.get("Buchungstag"));
        calculateBalanceAfterNEW(transaction);
        return transaction;
    }

    private void calculateBalanceAfterNEW(RawCashTransaction transaction) {
        currentBalance = currentBalance.add(transaction.amount);
        transaction.accountBalance = currentBalance;
    }

}
