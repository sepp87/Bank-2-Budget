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

    public final static String[] COLUMNS = new String[]{
        "Category",
        "Opening",
        "Actual",
        "Budgeted",
        "Adjustments",
        "Closing"
    };

    public BudgetWriter(Path budgetFile) {
        this.budgetFile = budgetFile;
    }

    public void write(Budget budget) {
        File file = budgetFile.toFile();

        // Create a Workbook
        workbook = new XSSFWorkbook(); // new HSSFWorkbook() for generating `.xls` file

        List<BudgetMonth> reversedMonthlyBudgets = new ArrayList<>(budget.months());
        Collections.reverse(reversedMonthlyBudgets);

        Font titleFont = getFont(18, true);
        Font headerFont = getFont(10, true);
        Font bodyFont = getFont(10, false);
        Font totalFont = getFont(14, true);

        LinkedHashMap<String, CellStyle> map = new LinkedHashMap<>();
        map.put("Category", normalStyle());
        map.put("Opening", openingStyle());
        map.put("Actual", actualStyle());
        map.put("Budgeted", budgetedStyle());
        map.put("Adjustments", adjustmentsStyle());
        map.put("Closing", closingStyle());

        CellStyle normalStyle = normalStyle();
        CellStyle openingStyle = openingStyle();
        CellStyle actualStyle = actualStyle();
        CellStyle budgetedStyle = budgetedStyle();
        CellStyle closingStyle = closingStyle();

        CellStyle titleNormalColumnStyle = styleWithFont(normalStyle, titleFont);
        CellStyle titleOpeningColumnStyle = styleWithFont(openingStyle, titleFont);
        CellStyle titleBudgetedColumnStyle = styleWithFont(budgetedStyle, titleFont);
        CellStyle titleClosingColumnStyle = styleWithFont(closingStyle, titleFont);

        CellStyle headerNormalColumnStyle = styleWithFont(normalStyle, headerFont);
        CellStyle headerOpeningColumnStyle = styleWithFont(openingStyle, headerFont);
        CellStyle headerBudgetedColumnStyle = styleWithFont(budgetedStyle, headerFont);
        CellStyle headerClosingColumnStyle = styleWithFont(closingStyle, headerFont);

        CellStyle bodyNormalColumnStyle = styleWithFont(normalStyle, bodyFont);
        CellStyle bodyOpeningColumnStyle = styleWithFont(openingStyle, bodyFont);
        CellStyle bodyBudgetedColumnStyle = styleWithFont(budgetedStyle, bodyFont);
        CellStyle bodyClosingColumnStyle = styleWithFont(closingStyle, bodyFont);

        CellStyle totalNormalColumnStyle = styleWithFont(normalStyle, totalFont);
        CellStyle totalOpeningColumnStyle = styleWithFont(openingStyle, totalFont);
        CellStyle totalBudgetedColumnStyle = styleWithFont(budgetedStyle, totalFont);
        CellStyle totalClosingColumnStyle = styleWithFont(closingStyle, totalFont);

        CellStyle[][] styles = {
            {
                titleNormalColumnStyle,
                titleOpeningColumnStyle,
                titleNormalColumnStyle,
                titleBudgetedColumnStyle,
                titleBudgetedColumnStyle,
                titleClosingColumnStyle
            },
            {
                headerNormalColumnStyle,
                headerOpeningColumnStyle,
                headerNormalColumnStyle,
                headerBudgetedColumnStyle,
                headerBudgetedColumnStyle,
                headerClosingColumnStyle
            },
            {
                bodyNormalColumnStyle,
                bodyOpeningColumnStyle,
                bodyNormalColumnStyle,
                bodyBudgetedColumnStyle,
                bodyBudgetedColumnStyle,
                bodyClosingColumnStyle
            },
            {
                totalNormalColumnStyle,
                totalOpeningColumnStyle,
                totalNormalColumnStyle,
                totalBudgetedColumnStyle,
                totalBudgetedColumnStyle,
                totalClosingColumnStyle
            }
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
//                 columns = HEADER;
                for (int i = 1; i < COLUMNS.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(COLUMNS[i]);
                }

                List<BudgetMonthCategory> categories = month.operatingCategories();

                int i = 2;
                for (BudgetMonthCategory category : categories) {

                    if (!category.isExpense()) {
                        continue;
                    }

                    Row row = sheet.createRow(i);

                    String name = category.name();
                    BigDecimal opening = category.opening();
                    BigDecimal actual = category.actual();
                    BigDecimal budgeted = category.budgeted();
                    BigDecimal adjustments = category.adjustments();

                    row.createCell(0).setCellValue(name);
                    row.createCell(1).setCellValue(toExcelNumber(opening));
                    row.createCell(2).setCellValue(toExcelNumber(actual));
                    row.createCell(3).setCellValue(toExcelNumber(budgeted));
                    row.createCell(4).setCellValue(toExcelNumber(adjustments));
                    int excelRowNum = i + 1;
                    row.createCell(5).setCellFormula("B" + excelRowNum + "+C" + excelRowNum + "+D" + excelRowNum + "+E" + excelRowNum);

                    i++;
                }

                BudgetMonthCategory unappliedExpenses = month.unappliedExpenses();
                Row unappliedExpensesRow = sheet.createRow(i++);
                unappliedExpensesRow.createCell(0).setCellValue(unappliedExpenses.name());
                unappliedExpensesRow.createCell(1).setCellValue(toExcelNumber(unappliedExpenses.opening()));
                unappliedExpensesRow.createCell(2).setCellValue(toExcelNumber(unappliedExpenses.actual()));
                unappliedExpensesRow.createCell(3).setCellValue(toExcelNumber(unappliedExpenses.budgeted()));
                unappliedExpensesRow.createCell(4).setCellValue(toExcelNumber(unappliedExpenses.adjustments()));
                unappliedExpensesRow.createCell(5).setCellValue(toExcelNumber(unappliedExpenses.closing()));

                sheet.createRow(i++); // empty row between expenses and income

                for (BudgetMonthCategory category : categories) {

                    if (!category.isIncome()) {
                        continue;
                    }

                    Row row = sheet.createRow(i);

                    String name = category.name();
                    BigDecimal opening = category.opening();
                    BigDecimal actual = category.actual();
                    BigDecimal budgeted = category.budgeted();
                    BigDecimal adjustments = category.adjustments();

                    row.createCell(0).setCellValue(name);
                    row.createCell(1).setCellValue(toExcelNumber(opening));
                    row.createCell(2).setCellValue(toExcelNumber(actual));
                    row.createCell(3).setCellValue(toExcelNumber(budgeted));
                    row.createCell(4).setCellValue(toExcelNumber(adjustments));
                    int excelRowNum = i + 1;
                    row.createCell(5).setCellFormula("B" + excelRowNum + "+C" + excelRowNum + "+D" + excelRowNum + "+E" + excelRowNum);

                    i++;
                }

                BudgetMonthCategory unappliedIncome = month.unappliedIncome();
                Row unappliedIncomeRow = sheet.createRow(i++);
                unappliedIncomeRow.createCell(0).setCellValue(unappliedIncome.name());
                unappliedIncomeRow.createCell(1).setCellValue(toExcelNumber(unappliedIncome.opening()));
                unappliedIncomeRow.createCell(2).setCellValue(toExcelNumber(unappliedIncome.actual()));
                unappliedIncomeRow.createCell(3).setCellValue(toExcelNumber(unappliedIncome.budgeted()));
                unappliedIncomeRow.createCell(4).setCellValue(toExcelNumber(unappliedIncome.adjustments()));
                unappliedIncomeRow.createCell(5).setCellValue(toExcelNumber(unappliedIncome.closing()));

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
//                int lastRowNum = sheet.getLastRowNum();
//                int rowCount = lastRowNum + 1;
//                int m = 0; // m decides if it is title, header, body or total
//                for (int j = 0; j < rowCount; j++) {
//                    Row row = sheet.getRow(j);
//                    if (m > 1) {
//                        m = (j != lastRowNum) ? 2 : 3;
//                    }
//                    for (int k = 0; k < 6; k++) {
//                        CellStyle style = styles[m][k]; // k decides which style/color is taken
//                        Cell cell = (row.getCell(k) != null) ? row.getCell(k) : row.createCell(k);
//                        cell.setCellStyle(style);
//                    }
//                    m++;
//                }

                int lastRowNum = sheet.getLastRowNum();
                int rowCount = lastRowNum + 1;
                int m = 0; // m decides if it is title, header, body or total
                for (int j = 0; j < rowCount; j++) {
                    Row row = sheet.getRow(j);
                    if (m > 1) {
                        m = (j != lastRowNum) ? 2 : 3;
                    }
                    for (int k = 0; k < 6; k++) {
                        CellStyle colStyle = map.get(COLUMNS[k]);
                        CellStyle style = styles[m][k]; // k decides which style/color is taken
                        Cell cell = (row.getCell(k) != null) ? row.getCell(k) : row.createCell(k);
                        cell.setCellStyle(style);
                    }
                    m++;
                }

                // Resize all columns to fit the content size
                for (int j = 0; j < COLUMNS.length; j++) {
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
