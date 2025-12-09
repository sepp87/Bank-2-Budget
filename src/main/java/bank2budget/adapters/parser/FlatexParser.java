package bank2budget.adapters.parser;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

public class FlatexParser extends TransactionParser {

    private BigDecimal currentBalance = BigDecimal.ZERO;
    private String accountNumber;

    public FlatexParser(ParserConfig config) {
        super(config);
    }

    @Override
    public CSVFormat getCsvFormat() {
        String[] header = new String[]{
            "Buchtag",
            "Valuta",
            "BIC / BLZ",
            "IBAN / Kontonummer",
            "Buchungsinformationen",
            "TA-Nr.",
            "Betrag",
            "Währung",
            "Auftraggeberkonto",
            "Konto"
        };
        return CSVFormat.DEFAULT.withDelimiter(';').withHeader(header);
    }

    @Override
    public List<CSVRecord> getTransactionRecordsFrom(List<CSVRecord> allRecords) {
        List<CSVRecord> transactionRecords = allRecords.subList(1, allRecords.size());
        transactionRecords = sortByDateAscending(transactionRecords);
        accountNumber = getAccountNumberFrom(allRecords);
        return transactionRecords;
    }

    private List<CSVRecord> sortByDateAscending(List<CSVRecord> transactionRecords) {
        if (transactionRecords.size() > 1) {
            BigInteger firstTransactionNumber = new BigInteger(transactionRecords.get(0).get("TA-Nr."));
            BigInteger secondTransactionNumber = new BigInteger(transactionRecords.get(1).get("TA-Nr."));
            if (firstTransactionNumber.compareTo(secondTransactionNumber) == 1) {
                Collections.reverse(transactionRecords);
            }
        }
        return transactionRecords;
    }

    protected String getAccountNumberFrom(List<CSVRecord> allRecords) {
        String blz = "10130800";
        String konto = allRecords.get(1).get(8);
        return getGermanIban(blz, konto);
    }

    @Override
    public RawCashTransaction parseCashTransactionFromNEW(CSVRecord record) throws ParseException {
        RawCashTransaction transaction = new RawCashTransaction();
        transaction.contraAccountNumber = (accountNumber);
        transaction.accountName = (record.get("Konto"));
        transaction.contraAccountNumber = (record.get("IBAN / Kontonummer"));
        transaction.amount = bigDecimalFromString(record.get("Betrag"));
        transaction.description = (record.get("Buchungsinformationen"));
        transaction.date = parseDateFrom(record.get("Buchtag"));
        calculateBalanceAfterNEW(transaction);
        return transaction;
    }

    private void calculateBalanceAfterNEW(RawCashTransaction transaction) {
        currentBalance = currentBalance.add(transaction.amount);
        transaction.accountBalance = currentBalance;
    }
}
