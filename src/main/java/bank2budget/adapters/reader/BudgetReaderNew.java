package bank2budget.adapters.reader;

import bank2budget.core.budget.Budget;
import bank2budget.core.budget.BudgetMonth;
import bank2budget.core.budget.BudgetMonthCategory;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

/**
 *
 * @author joost
 */
public class BudgetReaderNew {

    private final Path budgetFile;
    private final Budget budget;

    public BudgetReaderNew(Path budgetFile) {
        // at this point the account is still unknown
        this.budgetFile = budgetFile;
        budget = new Budget();
    }

    public Budget read() {
        File file = budgetFile.toFile();
        readFrom(file);
        return budget;
    }

    private Budget readFrom(File file) {

        // goal is to retrieve the budget with all its months, categories and corresponding budgeted costs
        // expenses, remainder and last month's remainder are calculated from the transactions
        if (!file.exists()) {
            return null;
        }

        try {
            Workbook workbook = WorkbookFactory.create(file);

            // monthly budgets are sorted descending cycle
            int last = workbook.getNumberOfSheets() - 1;
            for (int i = last; i > -1; i--) {
                Sheet sheet = workbook.getSheetAt(i);
                BudgetMonth month = getMonthlyBudgetFrom(sheet);
                budget.addMonth(month);
            }

        } catch (IOException | EncryptedDocumentException ex) {
            Logger.getLogger(AccountReader.class.getName()).log(Level.SEVERE, null, ex);
        }

        return budget;
    }

    private BudgetMonth getMonthlyBudgetFrom(Sheet sheet) {
        // budget, firstofmonth, budgetedForCategories
        LocalDate firstOfMonth = LocalDate.parse(sheet.getSheetName());
        int rowCount = sheet.getLastRowNum() - 1; // remove last row and empty row before it
        List<BudgetMonthCategory> categories = new ArrayList<>();

//        for (int i = 1; i < rowCount; i++) {
        for (int i = 2; i < rowCount; i++) {

            Row row = sheet.getRow(i);
            String name = row.getCell(0).getStringCellValue();
//            BigDecimal budgeted = fromExcelNumber(row.getCell(1));
//            BigDecimal actual = fromExcelNumber(row.getCell(2));
//            BigDecimal opening = fromExcelNumber(row.getCell(3));
//            BigDecimal closing = fromExcelNumber(row.getCell(4));
//            BigDecimal adjustments = fromExcelNumber(row.getCell(5));
            BigDecimal budgeted = BigDecimal.valueOf(row.getCell(1).getNumericCellValue());
//            BigDecimal actual = BigDecimal.valueOf(row.getCell(2).getNumericCellValue());
//            BigDecimal opening = BigDecimal.valueOf(row.getCell(3).getNumericCellValue());
//            BigDecimal closing = BigDecimal.valueOf(row.getCell(4).getNumericCellValue());

            BigDecimal adjustments = BigDecimal.ZERO;

            if (budgeted.compareTo(BigDecimal.ZERO) != 0) {
                BudgetMonthCategory category = new BudgetMonthCategory(
                        firstOfMonth,
                        name,
                        budgeted,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        adjustments,
                        Collections.emptyList()
                );
                categories.add(category);
            }

            // unassigned should be the last row
        }
        BudgetMonth month = new BudgetMonth(firstOfMonth, categories);

        return month;
    }

    private static BigDecimal fromExcelNumber(Cell cell) {
        DataFormatter formatter = new DataFormatter();
        String text = formatter.formatCellValue(cell);
        return new BigDecimal(text);
    }

}
