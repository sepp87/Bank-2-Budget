package io.ost.finance.io;

import io.ost.finance.App;
import io.ost.finance.MonthlyBudget;
import io.ost.finance.SingleAccountBudget;
import java.io.File;
import java.io.IOException;
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
public class BudgetReader {

    public static final String BUDGET_MASTER = "budget.xlsx";

    private final SingleAccountBudget budget;

    public BudgetReader() {
        // at this point the account is still unknown
        budget = new SingleAccountBudget();
    }

    public SingleAccountBudget read() {
        File file = new File(App.getBudgetDirectory() + BUDGET_MASTER);
        readFrom(file);
        return budget;
    }

    private SingleAccountBudget readFrom(File file) {

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
            Logger.getLogger(TransactionReaderForBudget.class.getName()).log(Level.SEVERE, null, ex);
        }

        return budget;
    }

    private MonthlyBudget getMonthlyBudgetFrom(Sheet sheet) {
        // budget, firstofmonth, budgetedForCategories
        String firstOfMonth = sheet.getSheetName();
        int rowCount = sheet.getLastRowNum() - 1; // remove last row and empty row before it
        Map<String, Double> budgetedForCategory = new TreeMap<>();
//        for (int i = 1; i < rowCount; i++) {
        for (int i = 2; i < rowCount; i++) {

            Row row = sheet.getRow(i);
            String category = row.getCell(0).getStringCellValue();
            double budgeted = row.getCell(1).getNumericCellValue();

            if (budgeted != 0.) {
                budgetedForCategory.put(category, budgeted);
            }

            // unassigned should be the last row
        }
        MonthlyBudget month = new MonthlyBudget(budget, firstOfMonth, budgetedForCategory);

        return month;
    }

}
