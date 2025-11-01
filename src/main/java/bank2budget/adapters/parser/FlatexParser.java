package bank2budget.adapters.parser;

import bank2budget.core.CashTransaction;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

public class FlatexParser extends TransactionParser {

    private double currentBalance = 0;
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

    @Override
    public CashTransaction parseCashTransactionFrom(CSVRecord record) throws ParseException {
        CashTransaction transaction = new CashTransaction();
        transaction.setAccountNumber(accountNumber);
        transaction.setAccountName(record.get("Konto"));
        transaction.setContraAccountNumber(record.get("IBAN / Kontonummer"));
        transaction.setAmount(getDoubleFrom(record.get("Betrag")));
        transaction.setDescription(record.get("Buchungsinformationen"));
        transaction.setOriginalRecord(record.toMap().values());
        parseDateFrom(record.get("Buchtag"), transaction);
        calculateBalanceAfter(transaction);
        return transaction;
    }

    private void calculateBalanceAfter(CashTransaction transaction) {
        double newBalance = currentBalance + transaction.getAmount();
        currentBalance = (double) Math.round(newBalance * 100) / 100;
        transaction.setAccountBalance(currentBalance);
    }

    protected String getAccountNumberFrom(List<CSVRecord> allRecords) {
        String blz = "10130800";
        String konto = allRecords.get(1).get(8);
        return getGermanIban(blz, konto);
    }

}
