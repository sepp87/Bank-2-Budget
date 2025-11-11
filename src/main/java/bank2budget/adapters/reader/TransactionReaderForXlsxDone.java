package bank2budget.adapters.reader;

import bank2budget.core.CashTransaction;
import bank2budget.core.CreditInstitution;
import bank2budget.adapters.parser.TransactionParser;
import java.io.File;
import java.io.IOException;
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
public class TransactionReaderForXlsxDone {


    private final List<CashTransaction> budgetTransactions;
    private final Map<String, List< CashTransaction>> budgetTransactionsPerSheet;
    private final Path transactionsFile;

    public TransactionReaderForXlsxDone(Path transactionsFile) {
        this.transactionsFile = transactionsFile;
        budgetTransactions = new ArrayList<>();
        budgetTransactionsPerSheet = new TreeMap<>();
    }

    static List<String> header;

    public Map<String, List< CashTransaction>> getPerSheet() {
        return budgetTransactionsPerSheet;
    }

    public List<CashTransaction> getAsList() {
        return budgetTransactions;
    }

    public TransactionReaderForXlsxDone read() {
        readFrom(transactionsFile.toFile());
        return this;
    }

    private Map<String, List< CashTransaction>> readFrom(File file) {

        if (!file.exists()) {
            return Collections.emptyMap();
        }

        try {
            Workbook workbook = WorkbookFactory.create(file);

            Map<String, List<CashTransaction>> accountMap = new TreeMap<>();
            int sheetCount = workbook.getNumberOfSheets();

            for (int i = 0; i < sheetCount; i++) {
                Sheet sheet = workbook.getSheetAt(i);
                String accountNumber = sheet.getSheetName();
                List<CashTransaction> transactions = getAllCashTransactionsFrom(sheet);
                accountMap.put(accountNumber, transactions);
                budgetTransactions.addAll(transactions);
            }

            return accountMap;
        } catch (IOException | EncryptedDocumentException ex) {
            Logger.getLogger(TransactionReaderForXlsxDone.class.getName()).log(Level.SEVERE, null, ex);
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

    private static List<CashTransaction> getAllCashTransactionsFrom(Sheet sheet) {
        List<CashTransaction> transactions = new ArrayList<>();
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

            CashTransaction transaction = getCashTransactionFrom(row);
//            ruleEngine.overwriteAccountNames(transaction);
//            ruleEngine.addMissingAccountNumbers(transaction);
            transactions.add(transaction);
        }
        TransactionParser.deriveLastOfDay(transactions);
        return transactions;
    }

    private static boolean rowIsEmpty(Row row) {
        // date column is never empty, test if there is a value
        int dateColumn = header.indexOf("date");
        return row.getCell(dateColumn) == null || row.getCell(dateColumn).getStringCellValue().equals("");
    }

    private static CashTransaction getCashTransactionFrom(Row row) {
        CashTransaction transaction = new CashTransaction();

        int i = 0;
        for (String column : header) {
            Cell cell = row.getCell(i);
            if (cell != null) {
                switch (column) {
                    case "category": // String
                        transaction.setCategory(cell.getStringCellValue());
                        break;
                    case "amount": // Numeric
                        transaction.setAmount(cell.getNumericCellValue());
                        break;
                    case "transactionNumber": // Numeric
                        int transactionNumber = (int) cell.getNumericCellValue();
                        transaction.setTransactionNumber(transactionNumber);
                        int positionOfDay = Integer.parseInt(String.valueOf(transactionNumber).substring(6)); // YYMMDDXXX
                        transaction.setPositionOfDay(positionOfDay);
                        break;
                    case "date": // String
                        transaction.setDate(LocalDate.parse(cell.getStringCellValue()));
                        break;
                    case "accountBalance": // Numeric
                        transaction.setAccountBalance(cell.getNumericCellValue());
                        break;
                    case "accountInstitution": // String to Enum
                        transaction.setAccountInstitution(CreditInstitution.valueOf(cell.getStringCellValue()));
                        break;
                    case "accountNumber": // String
                        transaction.setAccountNumber(cell.getStringCellValue());
                        break;
                    case "accountName": // String
                        transaction.setAccountName(cell.getStringCellValue());
                        break;
                    case "contraAccountNumber": // String
                        transaction.setContraAccountNumber(cell.getStringCellValue());
                        break;
                    case "contraAccountName": // String
                        transaction.setContraAccountName(cell.getStringCellValue());
                        break;
                    case "description": // String
                        transaction.setDescription(cell.getStringCellValue());
                        break;
                }
            }
            i++;
        }
        return transaction;
    }

}
