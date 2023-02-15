package io.ost.finance.io;

import io.ost.finance.Account;
import io.ost.finance.App;
import io.ost.finance.CashTransaction;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author joost
 */
public class TransactionWriterForBudget {

    public static final String BUDGET_TRANSACTIONS = "transactions.xlsx";

    public void process() {
        File budgetDirectory = new File(App.getBudgetDirectory());
        if (budgetDirectory.exists() && budgetDirectory.isDirectory()) {

//            saveTransactions(budgetTransactions);
        } else {
            budgetDirectory.mkdir();
            process();
        }
    }

    public void write(Collection<Account> accounts) {
        File file = new File(App.getBudgetDirectory() + BUDGET_TRANSACTIONS);

        // Create a Workbook
        Workbook workbook = new XSSFWorkbook(); // new HSSFWorkbook() for generating `.xls` file

        try {
            for (Account account : accounts) {

                // Create a Sheet
                Sheet sheet = workbook.createSheet(account.getAccountNumber());

                // Create a Font for styling header cells
                Font headerFont = workbook.createFont();
                headerFont.setBold(true);

                // Create a CellStyle with the font
                CellStyle headerCellStyle = workbook.createCellStyle();
                headerCellStyle.setFont(headerFont);

                // Create a Row
                Row headerRow = sheet.createRow(0);

                // Create cells
                String[] columns = CashTransaction.getHeader();
                for (int i = 0; i < columns.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(columns[i]);
                    cell.setCellStyle(headerCellStyle);
                }

                // TODO Freeze Header Row
                // Create Cell Style for formatting Date
                CellStyle dateCellStyle = workbook.createCellStyle();
                /* CreationHelper helps us create instances of various things like DataFormat,
            Hyperlink, RichTextString etc, in a format (HSSF, XSSF) independent way */
                CreationHelper createHelper = workbook.getCreationHelper();
                dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));

                // Create Other rows and cells with cash transaction data
                int rowNum = 1;
                List<CashTransaction> reversedTransactions = new ArrayList<>(account.getAllTransactions());
                Collections.reverse(reversedTransactions);

                for (CashTransaction transaction : reversedTransactions) {
                    Row row = sheet.createRow(rowNum++);

                    int i = 0;
                    for (Object value : transaction.toObjectRecord()) {
                        if (value == null) {
                            row.createCell(i).setCellValue("");
                        } else if (Double.class.isAssignableFrom(value.getClass())) {
                            row.createCell(i).setCellValue((Double) value);
                        } else if (Integer.class.isAssignableFrom(value.getClass())) {
                            row.createCell(i).setCellValue((Integer) value);
                        } else {
                            row.createCell(i).setCellValue(value.toString());
                        }
                        i++;
                    }
                }

                // Resize all columns to fit the content size
                for (int i = 0; i < columns.length; i++) {
                    sheet.autoSizeColumn(i);
                }
            }

            // Write the output to a file
            FileOutputStream fileOut = new FileOutputStream(file);
            workbook.write(fileOut);
            fileOut.close();

            // Closing the workbook
            workbook.close();
        } catch (IOException ex) {
            Logger.getLogger(TransactionWriterForBudget.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
