package bank2budget.adapters.reader;

import bank2budget.core.CashTransaction;
import bank2budget.core.CreditInstitution;
import bank2budget.core.Account;
import bank2budget.core.Transaction;
import bank2budget.core.TransactionBuilder;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.*;

/**
 *
 * @author joost
 */
public class AccountReader {

    private final Map<String, Account> accountsIndex;

    public AccountReader(Path transactionsFile) {
        accountsIndex = readFrom(transactionsFile.toFile());

    }

    static List<String> header;

    public Map<String, Account> getAccountsIndex() {
        return accountsIndex;
    }

    private Map<String, Account> readFrom(File file) {

        if (!file.exists()) {
            return Collections.emptyMap();
        }

        try {
            Workbook workbook = WorkbookFactory.create(file);

            Map<String, Account> accountMap = new TreeMap<>();
            int sheetCount = workbook.getNumberOfSheets();

            for (int i = 0; i < sheetCount; i++) {
                Sheet sheet = workbook.getSheetAt(i);
                String accountNumber = sheet.getSheetName();
                List<Transaction> transactions = transactionsFromSheet(sheet);
                accountMap.put(accountNumber, new Account(accountNumber, transactions, null));
            }

            return accountMap;
        } catch (IOException | EncryptedDocumentException ex) {
            Logger.getLogger(AccountReader.class.getName()).log(Level.SEVERE, null, ex);
        }

        return Collections.emptyMap();
    }

    private static List<String> getHeaderFrom(Sheet sheet) {
        Row row = sheet.getRow(0);
        int columnCount = row.getLastCellNum();
        List<String> list = new ArrayList<>();
        for (int i = 0; i < columnCount; i++) {
            String column = row.getCell(i).getStringCellValue();
            list.add(column);
        }
        return list;
    }

    private static List<Transaction> transactionsFromSheet(Sheet sheet) {
        List<Transaction> transactions = new ArrayList<>();
        int last = sheet.getLastRowNum();
        header = getHeaderFrom(sheet);

        for (int i = last; i > 0; i--) {
            Row row = sheet.getRow(i);
            if (row == null) {
                continue;
            }
            if (rowIsEmpty(row)) {
                continue;
            }

            Transaction transaction = transactionFromRow(row);
            transactions.add(transaction);
        }
        return transactions;
    }

    private static boolean rowIsEmpty(Row row) {
        // date column is never empty, test if there is a value
        int dateColumn = header.indexOf("date");
        return row.getCell(dateColumn) == null || row.getCell(dateColumn).getStringCellValue().equals("");
    }

    private static Transaction transactionFromRow(Row row) {
        TransactionBuilder builder = new TransactionBuilder();

        int i = 0;
        for (String column : header) {
            Cell cell = row.getCell(i);
            if (cell != null) {
                switch (column) {
                    case "category": // String
                        builder.category((getStringValue(cell)));
                        break;
                    case "amount": // Numeric
                        builder.amount(BigDecimal.valueOf(cell.getNumericCellValue()));
                        break;
                    case "transactionNumber": // Numeric
                        int transactionNumber = (int) cell.getNumericCellValue();
                        builder.transactionNumber(transactionNumber);
                        int positionOfDay = Integer.parseInt(String.valueOf(transactionNumber).substring(6)); // YYMMDDXXX
                        builder.positionOfDay(positionOfDay);
                        break;
                    case "lastOfDay": // Boolean
                        boolean lastOfDay = cell.getBooleanCellValue();
                        builder.lastOfDay(lastOfDay);
                        break;
                    case "date": // String
                        builder.date(LocalDate.parse(getStringValue(cell)));
                        break;
                    case "accountBalance": // Numeric
                        builder.accountBalance(BigDecimal.valueOf(cell.getNumericCellValue()));
                        break;
                    case "accountInstitution": // String to Enum
                        builder.accountInstitution(CreditInstitution.valueOf(getStringValue(cell)));
                        break;
                    case "accountNumber": // String
                        builder.accountNumber(getStringValue(cell));
                        break;
                    case "accountName": // String
                        builder.accountName(getStringValue(cell));
                        break;
                    case "contraAccountNumber": // String
                        builder.contraAccountNumber(getStringValue(cell));
                        break;
                    case "contraAccountName": // String
                        builder.contraAccountName(getStringValue(cell));
                        break;
                    case "description": // String
                        builder.description(getStringValue(cell));
                        break;
                    case "notes": // String
                        builder.notes(getStringValue(cell));
                        break;
                }
            }
            i++;
        }
        return builder.build();
    }

    private static String getStringValue(Cell cell) {
        String value = cell.getStringCellValue();
        return value.isBlank() ? null : value;
    }

}
