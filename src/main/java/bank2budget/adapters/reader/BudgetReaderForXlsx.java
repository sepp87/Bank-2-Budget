package bank2budget.adapters.reader;

import bank2budget.Launcher;
import bank2budget.core.MonthlyBudget;
import bank2budget.core.MultiAccountBudget;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

/**
 *
 * @author joost
 */
public class BudgetReaderForXlsx {

    private final Path budgetFile;
    private final MultiAccountBudget budget;

    public BudgetReaderForXlsx(Path budgetFile) {
        // at this point the account is still unknown
        this.budgetFile = budgetFile;
        budget = new MultiAccountBudget();
    }

    public MultiAccountBudget read() {
        File file = budgetFile.toFile();
        readFrom(file);
        return budget;
    }

    private MultiAccountBudget readFrom(File file) {

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
                MonthlyBudget month = getMonthlyBudgetFrom(sheet);
                budget.addMonthlyBudget(month);
            }

        } catch (IOException | EncryptedDocumentException ex) {
            Logger.getLogger(TransactionReaderForXlsxDone.class.getName()).log(Level.SEVERE, null, ex);
        }

        return budget;
    }

    private MonthlyBudget getMonthlyBudgetFrom(Sheet sheet) {
        // budget, firstofmonth, budgetedForCategories
        String firstOfMonth = sheet.getSheetName();
        int rowCount = sheet.getLastRowNum() - 1; // remove last row and empty row before it
        Map<String, Double> budgetedForCategory = new TreeMap<>();
        Map<String, Double> expensesForCategory = new TreeMap<>();
        Map<String, Double> remainderForCategory = new TreeMap<>();
        Map<String, Double> remainderLastMonthForCategory = new TreeMap<>();

//        for (int i = 1; i < rowCount; i++) {
        for (int i = 2; i < rowCount; i++) {

            Row row = sheet.getRow(i);
            String category = row.getCell(0).getStringCellValue();
            double budgeted = row.getCell(1).getNumericCellValue();
            double expenses = row.getCell(2).getNumericCellValue();
            double remainder = row.getCell(4).getNumericCellValue();
            double remainderLastMonth = row.getCell(3).getNumericCellValue();

            if (budgeted != 0.) {
                budgetedForCategory.put(category, budgeted);
            }

            // unassigned should be the last row
        }
        MonthlyBudget month = new MonthlyBudget(budget, firstOfMonth, budgetedForCategory);

        return month;
    }

}
