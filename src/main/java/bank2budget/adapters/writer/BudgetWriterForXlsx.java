package bank2budget.adapters.writer;

import bank2budget.adapters.writer.AccountWriter;
import bank2budget.Launcher;
import bank2budget.core.MonthlyBudget;
import bank2budget.core.MultiAccountBudget;
import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author joost
 */
public class BudgetWriterForXlsx {

    private final Path budgetFile;
    private XSSFWorkbook workbook;

    public BudgetWriterForXlsx(Path budgetFile) {
        this.budgetFile = budgetFile;
    }

    public void write(MultiAccountBudget budget) {
        File file = budgetFile.toFile();

        // Create a Workbook
        workbook = new XSSFWorkbook(); // new HSSFWorkbook() for generating `.xls` file

        List<MonthlyBudget> reversedMonthlyBudgets = new ArrayList<>(budget.getMonthlyBudgets().values());
        Collections.reverse(reversedMonthlyBudgets);

        Font titleFont = getFont(18, true);
        Font headerFont = getFont(10, true);
        Font bodyFont = getFont(10, false);
        Font totalFont = getFont(14, true);

        CellStyle budgetedStyle = getBudgetedColumnStyle();
        CellStyle normalStyle = getNormalColumnStyle();
        CellStyle lastMonthStyle = getLastMonthColumnStyle();
        CellStyle remainderStyle = getRemainderColumnStyle();

        CellStyle titleNormalColumnStyle = mergeCellStyleWithFont(normalStyle, titleFont);
        CellStyle titleBudgetedColumnStyle = mergeCellStyleWithFont(budgetedStyle, titleFont);
        CellStyle titleLastMonthColumnStyle = mergeCellStyleWithFont(lastMonthStyle, titleFont);
        CellStyle titleRemainderColumnStyle = mergeCellStyleWithFont(remainderStyle, titleFont);

        CellStyle headerNormalColumnStyle = mergeCellStyleWithFont(normalStyle, headerFont);
        CellStyle headerBudgetedColumnStyle = mergeCellStyleWithFont(budgetedStyle, headerFont);
        CellStyle headerLastMonthColumnStyle = mergeCellStyleWithFont(lastMonthStyle, headerFont);
        CellStyle headerRemainderColumnStyle = mergeCellStyleWithFont(remainderStyle, headerFont);

        CellStyle bodyNormalColumnStyle = mergeCellStyleWithFont(normalStyle, bodyFont);
        CellStyle bodyBudgetedColumnStyle = mergeCellStyleWithFont(budgetedStyle, bodyFont);
        CellStyle bodyLastMonthColumnStyle = mergeCellStyleWithFont(lastMonthStyle, bodyFont);
        CellStyle bodyRemainderColumnStyle = mergeCellStyleWithFont(remainderStyle, bodyFont);

        CellStyle totalNormalColumnStyle = mergeCellStyleWithFont(normalStyle, totalFont);
        CellStyle totalBudgetedColumnStyle = mergeCellStyleWithFont(budgetedStyle, totalFont);
        CellStyle totalLastMonthColumnStyle = mergeCellStyleWithFont(lastMonthStyle, totalFont);
        CellStyle totalRemainderColumnStyle = mergeCellStyleWithFont(remainderStyle, totalFont);

        CellStyle[][] styles = {
            {titleNormalColumnStyle, titleBudgetedColumnStyle, titleNormalColumnStyle, titleLastMonthColumnStyle, titleRemainderColumnStyle},
            {headerNormalColumnStyle, headerBudgetedColumnStyle, headerNormalColumnStyle, headerLastMonthColumnStyle, headerRemainderColumnStyle},
            {bodyNormalColumnStyle, bodyBudgetedColumnStyle, bodyNormalColumnStyle, bodyLastMonthColumnStyle, bodyRemainderColumnStyle},
            {totalNormalColumnStyle, totalBudgetedColumnStyle, totalNormalColumnStyle, totalLastMonthColumnStyle, totalRemainderColumnStyle},};

        try {
            for (MonthlyBudget month : reversedMonthlyBudgets) {

                // Create a sheet
                Sheet sheet = workbook.createSheet(month.getFirstOfMonth().toString());

                // Create a title row
                Row titleRow = sheet.createRow(0);
                titleRow.createCell(1).setCellValue(month.getFirstOfMonth().toString());
                titleRow.setHeight((short) 500);
//                titleRow.setHeight((short) -1);

                // Create a header row
                Row headerRow = sheet.createRow(1);
                String[] columns = MultiAccountBudget.getHeader();
                for (int i = 1; i < columns.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(columns[i]);

                }

                Map<String, Double> budgetedForCategory = month.getBudgetedForCategories();
                Map<String, Double> expensesForCategory = month.getExpensesForCategories();
                Map<String, Double> remainderForCategoryLastMonth = month.getRemainderForCategoriesLastMonth();

                int i = 2;
                for (Entry<String, Double> entry : budgetedForCategory.entrySet()) {
                    Row row = sheet.createRow(i);

                    String category = entry.getKey();
                    double budgeted = entry.getValue();
//                    if(category.equals("BUDGET")) {
//                        System.out.println("BudgetWriterForXlsx" + month.getFirstOfMonth());
//                    }
                    double expenses = expensesForCategory.get(category);
                    double remainderLastMonth = remainderForCategoryLastMonth.get(category);

                    if (budgeted < 0) {
                        continue;
                    }

                    row.createCell(0).setCellValue(category);
                    row.createCell(1).setCellValue(budgeted);
                    row.createCell(2).setCellValue(expenses);
                    row.createCell(3).setCellValue(remainderLastMonth);
                    int excelRowNum = i + 1;
                    row.createCell(4).setCellFormula("B" + excelRowNum + "+C" + excelRowNum + "+D" + excelRowNum);

                    i++;
                }

                Row unassignedExpensesRow = sheet.createRow(i++);
                unassignedExpensesRow.createCell(0).setCellValue("UNASSIGNED EXPENSES");
                unassignedExpensesRow.createCell(1).setCellValue(0);
                unassignedExpensesRow.createCell(2).setCellValue(month.getUnassignedExpenses());
                unassignedExpensesRow.createCell(3).setCellValue(month.getUnassignedExpensesRemainderLastMonth());
                unassignedExpensesRow.createCell(4).setCellValue(month.getUnassignedExpensesRemainder());

                sheet.createRow(i++); // empty row between expenses and income

                for (Entry<String, Double> entry : month.getBudgetedForCategories().entrySet()) {

                    String category = entry.getKey();
                    double budgeted = entry.getValue();
                    double expenses = expensesForCategory.get(category);
                    double remainderLastMonth = remainderForCategoryLastMonth.get(category);

                    if (budgeted >= 0) {
                        continue;
                    }

                    Row row = sheet.createRow(i);
                    row.createCell(0).setCellValue(category);
                    row.createCell(1).setCellValue(budgeted);
                    row.createCell(2).setCellValue(expenses);
                    row.createCell(3).setCellValue(remainderLastMonth);
                    int excelRowNum = i + 1;
                    row.createCell(4).setCellFormula("B" + excelRowNum + "+C" + excelRowNum + "+D" + excelRowNum);

                    i++;
                }

                Row unassignedIncomeRow = sheet.createRow(i++);
                unassignedIncomeRow.createCell(0).setCellValue("UNASSIGNED INCOME");
                unassignedIncomeRow.createCell(1).setCellValue(0);
                unassignedIncomeRow.createCell(2).setCellValue(month.getUnassignedIncome());
                unassignedIncomeRow.createCell(3).setCellValue(month.getUnassignedIncomeRemainderLastMonth());
                unassignedIncomeRow.createCell(4).setCellValue(month.getUnassignedIncomeRemainder());

                sheet.createRow(i++); // empty row between income and total

                Row totalRow = sheet.createRow(i++);

                String formula = "";
                int rowNum = 3;
                while (rowNum < i) {
                    formula += "B" + rowNum + "+";
                    rowNum++;
                }
                formula = formula.substring(0, formula.length() - 1);

                totalRow.createCell(0).setCellValue("TOTAL");
                totalRow.createCell(1).setCellFormula(formula);
                totalRow.createCell(4).setCellFormula(formula.replace("B", "E"));
                totalRow.setHeight((short) 400);

                // Style the table
                int lastRowNum = sheet.getLastRowNum();
                int rowCount = lastRowNum + 1;
                int m = 0;
                for (int j = 0; j < rowCount; j++) {
                    Row row = sheet.getRow(j);
                    if (m > 1) {
                        m = (j != lastRowNum) ? 2 : 3;
                    }
                    for (int k = 0; k < 5; k++) {
                        CellStyle style = styles[m][k];
                        Cell cell = (row.getCell(k) != null) ? row.getCell(k) : row.createCell(k);
                        cell.setCellStyle(style);
                    }
                    m++;
                }

                // Resize all columns to fit the content size
                for (int j = 0; j < columns.length; j++) {
                    sheet.autoSizeColumn(j);
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

    private CellStyle getNormalColumnStyle() {
        return getCellStyleWithBackground(null); // transparent
    }

    private CellStyle getBudgetedColumnStyle() {
        return getCellStyleWithBackground(new java.awt.Color(205, 220, 172)); // green
    }

    private CellStyle getLastMonthColumnStyle() {
        return getCellStyleWithBackground(new java.awt.Color(251, 202, 163)); // orange
    }

    private CellStyle getRemainderColumnStyle() {
        return getCellStyleWithBackground(new java.awt.Color(164, 213, 226)); // blue
    }

    private CellStyle getCellStyleWithBackground(Color color) {
        XSSFCellStyle style = workbook.createCellStyle();
        if (color != null) {
            style.setFillForegroundColor(new XSSFColor(color, new DefaultIndexedColorMap()));
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }
        return style;
    }

    private Font getFont(int size, boolean isBold) {
        Font font = workbook.createFont();
        font.setBold(isBold);
        font.setFontHeightInPoints((short) size);
        return font;
    }

    private CellStyle mergeCellStyleWithFont(CellStyle style, Font font) {
        XSSFCellStyle newStyle = workbook.createCellStyle();
        newStyle.cloneStyleFrom(style);
        newStyle.setFont(font);
        return newStyle;
    }

}
