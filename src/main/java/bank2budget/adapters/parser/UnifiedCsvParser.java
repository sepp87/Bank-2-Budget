package bank2budget.adapters.parser;

import bank2budget.core.CashTransaction;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

public class UnifiedCsvParser extends TransactionParser {

    public UnifiedCsvParser(ParserConfig config) {
        super(config);
    }

    @Override
    public CSVFormat getCsvFormat() {
        String[] header = new String[]{
            "category", "amount", "transactionNumber", "date",
            "accountBalance", "accountInstitution", "accountNumber", "accountName",
            "contraAccountNumber", "contraAccountName", "internal", "transactionType", "description"
        };
        return CSVFormat.DEFAULT.withFirstRecordAsHeader();
    }

    @Override
    public List<CSVRecord> getTransactionRecordsFrom(List<CSVRecord> allRecords) {
        allRecords.get(0);
        return allRecords.subList(1, allRecords.size());
    }

    @Override
    public RawCashTransaction parseCashTransactionFromNEW(CSVRecord record) throws ParseException {
        RawCashTransaction transaction = new RawCashTransaction();
        transaction.accountNumber = (record.get("accountNumber"));
        transaction.contraAccountName = (record.get("contraAccountName"));
        transaction.contraAccountNumber = (record.get("contraAccountNumber"));
        transaction.amount =BigDecimal.valueOf(getDoubleFrom(record.get("amount")));
        transaction.accountBalance = BigDecimal.valueOf(getDoubleFrom(record.get("accountBalance")));
        transaction.transactionNumber = (Integer.parseInt(record.get("transactionNumber")));
        transaction.description = (record.get("description"));
        transaction.date = parseDateFrom(record.get("date"));
        return transaction;
    }

}
