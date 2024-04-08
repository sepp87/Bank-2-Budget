package io.ost.finance.io;

import io.ost.finance.App;
import io.ost.finance.CashTransaction;
import io.ost.finance.Util;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
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
public class TransactionReaderForBudget {

    public static final String BUDGET_TRANSACTIONS = "transactions.xlsx";

    private final List<CashTransaction> budgetTransactions;
    private final Map<String, List< CashTransaction>> budgetTransactionsPerSheet;

    public TransactionReaderForBudget() {
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

    public TransactionReaderForBudget read() {
        File file = new File(App.getBudgetDirectory() + BUDGET_TRANSACTIONS);
        readFrom(file);
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
            Logger.getLogger(TransactionReaderForBudget.class.getName()).log(Level.SEVERE, null, ex);
        }

        return Collections.EMPTY_MAP;
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
        int columnCount = header.size();

        for (int i = last; i > 0; i--) {
            Row row = sheet.getRow(i);
            if (row == null) {
                continue;
            }
            if (rowIsEmpty(row)) {
                continue;
            }
            CashTransaction transaction = getCashTransactionFrom(row);
            transactions.add(transaction);

        }
        return transactions;
    }

    private static boolean rowIsEmpty(Row row) {
        // date column is never empty, test if there is a value
        return row.getCell(3) == null;
    }

    private static CashTransaction getCashTransactionFrom(Row row) {
        CashTransaction transaction = new CashTransaction();
        int i = 0;
        for (String column : header) {
            Cell cell = row.getCell(i);
            if (cell != null) {
                setCellToTransactionByColumn(cell, transaction, column);
            }
            i++;
        }
        return transaction;
    }

    @SuppressWarnings("unchecked")
    private static boolean setCellToTransactionByColumn(Cell cell, CashTransaction transaction, String column) {
        try {
            Field field = CashTransaction.class.getField(column);
            Type type = field.getType();
            Object value = null;

            switch (cell.getCellType()) {

                case STRING:
                    value = cell.getStringCellValue();
                    if (type.equals(Boolean.class)) {
                        value = Boolean.valueOf((String) value);
                    } else if (field.getType().isEnum()) {
                        value = Enum.valueOf((Class<Enum>) field.getType(), (String) value);  // if not surpressed, causes warning "Some input files use unverified or unsafe processes."                      
                    } else if (value.equals("")) {
                        value = null;
                    } else if (type.equals(Double.class)) {
                        value = Double.parseDouble((String) value);
                    }
                    break;

                case NUMERIC:
                    value = cell.getNumericCellValue();
                    if (type.equals(int.class)) {
                        value = (int) ((double) value);
                    }

                    break;
            }

            if (value != null) {
                field.set(transaction, value);
            }

            return true;
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            Logger.getLogger(TransactionReaderForBudget.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

}
