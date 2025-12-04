package bank2budget.adapters.parser;

import bank2budget.core.CashTransaction;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

public class IngDiBaParser extends TransactionParser {

    protected String accountNumber;

    public IngDiBaParser(ParserConfig config) {
        super(config);
    }

    @Override
    public CSVFormat getCsvFormat() {
        String[] header = new String[]{"DATE", "Valuta", "CONTRA_ACCOUNT_NAME", "Buchungstext", "DESCRIPTION", "ACCOUNT_BALANCE", "Saldowährung", "AMOUNT", "Betragwährung"};
        return CSVFormat.DEFAULT.withDelimiter(';').withHeader(header);
    }

    @Override
    public List<CSVRecord> getTransactionRecordsFrom(List<CSVRecord> allRecords) {
        List<CSVRecord> transactionsRecords = allRecords.subList(11, allRecords.size());
        Collections.reverse(transactionsRecords);
        accountNumber = getAccountNumberFrom(allRecords);
        return transactionsRecords;
    }

    protected String getAccountNumberFrom(List<CSVRecord> allRecords) {
        CSVRecord accountNumberRecord = allRecords.get(2);
        return accountNumberRecord.get(1).replace(" ", "");
    }

    private void parseContraAccountNumberFrom(CSVRecord record, CashTransaction transaction) {
        // TODO filter CONTRA_ACCOUNT_NUMBER from record
    }

    @Override
    public RawCashTransaction parseCashTransactionFromNEW(CSVRecord record) throws ParseException {
        RawCashTransaction transaction = new RawCashTransaction();
        transaction.accountNumber = (accountNumber);
        transaction.contraAccountName = (record.get("CONTRA_ACCOUNT_NAME"));
        transaction.amount = BigDecimal.valueOf(getDoubleFrom(record.get("AMOUNT")));
        transaction.accountBalance = BigDecimal.valueOf(getDoubleFrom(record.get("ACCOUNT_BALANCE")));
        transaction.description = (record.get("DESCRIPTION"));
        transaction.date = parseDateFrom(record.get("DATE"));
        return transaction;
    }
}
