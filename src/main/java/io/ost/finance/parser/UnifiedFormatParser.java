package io.ost.finance.parser;

import io.ost.finance.CashTransaction;
import io.ost.finance.ParserConfig;
import java.text.ParseException;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

public class UnifiedFormatParser extends TransactionParser {

    public UnifiedFormatParser(ParserConfig config) {
        super(config);
    }

    @Override
    public CSVFormat getCsvFormat() {
        String[] header = new String[]{
            "label", "amount", "transactionNumber", "date", "accountBalance",
            "accountNumber", "accountName", "contraAccountNumber",
            "contraAccountName", "internal", "transactionType", "description"
        };
        return CSVFormat.DEFAULT.withFirstRecordAsHeader();
    }

    @Override
    public List<CSVRecord> getTransactionRecordsFrom(List<CSVRecord> allRecords) {
        return allRecords.subList(1, allRecords.size());
    }

    @Override
    public CashTransaction parseCashTransactionFrom(CSVRecord record) throws ParseException {
        CashTransaction transaction = new CashTransaction();
        transaction.setAccountNumber(record.get("accountNumber"));
        transaction.setContraAccountName(record.get("contraAccountName"));
        transaction.setContraAccountNumber(record.get("contraAccountNumber"));
        transaction.setAmount(getDoubleFrom(record.get("amount")));
        transaction.setAccountBalance(getDoubleFrom(record.get("accountBalance")));
        transaction.setTransactionNumber(Integer.parseInt(record.get("transactionNumber")));
        transaction.setDescription(record.get("description"));
        transaction.setOriginalRecord(record.toMap().values());
        parseDateFrom(record.get("date"), transaction);
        return transaction;
    }

}
