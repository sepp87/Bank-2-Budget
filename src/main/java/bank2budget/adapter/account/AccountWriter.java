package bank2budget.adapter.account;

import bank2budget.core.Account;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author joost
 */
public class AccountWriter extends TransactionWriter {

    private final Path transactionsFile;

    public AccountWriter(Path transactionsFile) {
        this.transactionsFile = transactionsFile;
    }

    // TODO when there is no category yet, add the category to the transaction
    public void write(Collection<Account> accounts) {
        File file = transactionsFile.toFile();

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
                for (int i = 0; i < HEADER.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(HEADER[i]);
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
                var reversed = new ArrayList<>(account.transactionsAscending());
                Collections.reverse(reversed);

                for (var transaction : reversed) {
                    Row row = sheet.createRow(rowNum++);

                    int i = 0;
                    for (Object value : getObjectArrayFrom(transaction)) {
                        if (value == null) {
                            row.createCell(i).setCellValue("");
                        } else if (Double.class.isAssignableFrom(value.getClass())) {
                            row.createCell(i).setCellValue((Double) value);
                        } else if (BigDecimal.class.isAssignableFrom(value.getClass())) {
                            row.createCell(i).setCellValue(((BigDecimal) value).doubleValue());
                        } else if (Integer.class.isAssignableFrom(value.getClass())) {
                            row.createCell(i).setCellValue((Integer) value);
                        } else if (Boolean.class.isAssignableFrom(value.getClass())) {
                            row.createCell(i).setCellValue((Boolean) value);
                        } else {
                            row.createCell(i).setCellValue(value.toString());
                        }
                        i++;
                    }
                }

                // Resize all columns to fit the content size
                for (int i = 0; i < HEADER.length; i++) {
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
            Logger.getLogger(AccountWriter.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
