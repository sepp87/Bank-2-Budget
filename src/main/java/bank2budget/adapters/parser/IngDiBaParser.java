package bank2budget.adapters.parser;

import bank2budget.core.CashTransaction;
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

    @Override
    public CashTransaction parseCashTransactionFrom(CSVRecord record) throws ParseException {
        CashTransaction transaction = new CashTransaction();
        transaction.setAccountNumber(accountNumber);
        transaction.setContraAccountName(record.get("CONTRA_ACCOUNT_NAME"));
        transaction.setAmount(getDoubleFrom(record.get("AMOUNT")));
        transaction.setAccountBalance(getDoubleFrom(record.get("ACCOUNT_BALANCE")));
        transaction.setDescription(record.get("DESCRIPTION"));
        transaction.setOriginalRecord(record.toMap().values());
        parseDateFrom(record.get("DATE"), transaction);
        parseContraAccountNumberFrom(record, transaction);
        return transaction;
    }

    private void parseContraAccountNumberFrom(CSVRecord record, CashTransaction transaction) {
        // TODO filter CONTRA_ACCOUNT_NUMBER from record
    }

    protected String getAccountNumberFrom(List<CSVRecord> allRecords) {
        CSVRecord accountNumberRecord = allRecords.get(2);
        return accountNumberRecord.get(1).replace(" ", "");
    }
}
