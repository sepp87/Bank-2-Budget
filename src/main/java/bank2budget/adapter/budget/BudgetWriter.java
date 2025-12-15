package bank2budget.adapter.budget;

import bank2budget.adapter.account.AccountWriter;
import bank2budget.core.budget.Budget;
import bank2budget.core.budget.BudgetMonth;
import bank2budget.core.budget.BudgetMonthCategory;
import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
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
public class BudgetWriter {

    private final Path budgetFile;
    private XSSFWorkbook workbook;

    public BudgetWriter(Path budgetFile) {
        this.budgetFile = budgetFile;
    }

    public void write(Budget budget) {
        File file = budgetFile.toFile();

        // Create a Workbook
        workbook = new XSSFWorkbook(); // new HSSFWorkbook() for generating `.xls` file

        List<BudgetMonth> reversedMonthlyBudgets = new ArrayList<>(budget.months());
        Collections.reverse(reversedMonthlyBudgets);

        LinkedHashMap<String, CellStyle> styleMap = new LinkedHashMap<>();
        styleMap.put("Category", normalStyle());
        styleMap.put("Opening", openingStyle());
        styleMap.put("Actual", actualStyle());
        styleMap.put("Budgeted", budgetedStyle());
        styleMap.put("Adjustments", budgetedStyle());
        styleMap.put("Closing", closingStyle());

        List<String> columns = styleMap.keySet().stream().toList();

        Font[] fonts = new Font[]{
            getFont(18, true), // title
            getFont(10, true), // header
            getFont(10, false), // body
            getFont(14, true) // total
        };

        try {
            for (BudgetMonth month : reversedMonthlyBudgets) {

                // Create a sheet
                Sheet sheet = workbook.createSheet(month.firstOfMonth().toString());

                // Create a title row
                Row titleRow = sheet.createRow(0);
                titleRow.createCell(1).setCellValue(month.firstOfMonth().toString());
                titleRow.setHeight((short) 500);
//                titleRow.setHeight((short) -1);

                // Create a header row
                Row headerRow = sheet.createRow(1);
                for (int i = 1; i < columns.size(); i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(columns.get(i));
                }

                List<BudgetMonthCategory> categories = month.operatingCategories();

                int i = 2;
                for (BudgetMonthCategory category : categories) {

                    if (!category.isExpense()) {
                        continue;
                    }

                    Row row = sheet.createRow(i);
                    categoryToRow(category, row);
                    i++;
                }

                BudgetMonthCategory unappliedExpenses = month.unappliedExpenses();
                Row unappliedExpensesRow = sheet.createRow(i++);
                categoryToRow(unappliedExpenses, unappliedExpensesRow);

                sheet.createRow(i++); // empty row between expenses and income

                for (BudgetMonthCategory category : categories) {

                    if (!category.isIncome()) {
                        continue;
                    }

                    Row row = sheet.createRow(i);
                    categoryToRow(category, row);
                    
                    i++;
                }

                BudgetMonthCategory unappliedIncome = month.unappliedIncome();
                Row unappliedIncomeRow = sheet.createRow(i++);
                categoryToRow(unappliedIncome, unappliedIncomeRow);

                sheet.createRow(i++); // empty row between income and total

                Row totalRow = sheet.createRow(i++);

                int start = 3;
                int end = i - 2;
                String sum = "SUM(D" + start + ":D" + end + ")";

                totalRow.createCell(0).setCellValue("TOTAL");
                totalRow.createCell(3).setCellFormula(sum); // total of budgeted (should be zero)
                totalRow.createCell(4).setCellFormula(sum.replace("D", "E")); // total of adjustments (should be zero)
                totalRow.createCell(5).setCellFormula(sum.replace("D", "F")); // total of closing
                totalRow.setHeight((short) 400);

                // Style the table
                Map<String, CellStyle> mergedStyles = new TreeMap<>(); // prevent font+style duplication
                int lastRowNum = sheet.getLastRowNum();
                int rowCount = lastRowNum + 1;
                int f = 0; // f decides if it is title, header, body or total
                for (int j = 0; j < rowCount; j++) {
                    Row row = sheet.getRow(j);
                    if (f > 1) {
                        f = (j != lastRowNum) ? 2 : 3;
                    }
                    Font font = fonts[f];
                    for (int s = 0; s < 6; s++) {
                        String mergedIndex = f + "" + s;
                        CellStyle style = styleMap.get(columns.get(s)); // s decides which style/color is taken
                        CellStyle merged = mergedStyles.computeIfAbsent(mergedIndex, e -> styleWithFont(style, font));
                        Cell cell = (row.getCell(s) != null) ? row.getCell(s) : row.createCell(s);
                        cell.setCellStyle(merged);
                    }
                    f++;
                }

                // Resize all columns to fit the content size
                for (int j = 0; j < columns.size(); j++) {
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

    private void categoryToRow(BudgetMonthCategory category, Row row) {
        row.createCell(0).setCellValue(category.name());
        row.createCell(1).setCellValue(toExcelNumber(category.opening()));
        row.createCell(2).setCellValue(toExcelNumber(category.actual()));
        row.createCell(3).setCellValue(toExcelNumber(category.budgeted()));
        row.createCell(4).setCellValue(toExcelNumber(category.adjustments()));
        int excelRowNum = row.getRowNum() + 1;
        String sum = "SUM(B" + excelRowNum + ":E" + excelRowNum + ")";
        row.createCell(5).setCellFormula(sum);
    }

    private static double toExcelNumber(BigDecimal value) {
        return new BigDecimal(value.toPlainString()).doubleValue();
    }

    private CellStyle normalStyle() {
        return getCellStyleWithBackground(null); // transparent
    }

    private CellStyle openingStyle() {
        return getCellStyleWithBackground(null); // transparent
    }

    private CellStyle actualStyle() {
        return getCellStyleWithBackground(null); // transparent
//        return getCellStyleWithBackground(new java.awt.Color(251, 202, 163)); // orange
    }

    private CellStyle budgetedStyle() {
        return getCellStyleWithBackground(new java.awt.Color(205, 220, 172)); // green
    }

    private CellStyle adjustmentsStyle() {
        return getCellStyleWithBackground(new java.awt.Color(164, 213, 226)); // blue
    }

    private CellStyle closingStyle() {
        return getCellStyleWithBackground(null); // transparent
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

    private CellStyle styleWithFont(CellStyle style, Font font) {
        XSSFCellStyle newStyle = workbook.createCellStyle();
        newStyle.cloneStyleFrom(style);
        newStyle.setFont(font);
        return newStyle;
    }

}
