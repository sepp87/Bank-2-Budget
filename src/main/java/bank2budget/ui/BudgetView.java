package bank2budget.ui;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.Collection;
import java.util.Comparator;
import java.util.TreeSet;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 *
 * @author joostmeulenkamp
 */
public class BudgetView extends AnchorPane {

    private final ComboBox<LocalDate> monthSelection;

    private final Label currentMonth;
    private final Label remainingIncome;
    private final Label currentBalance;
    private final Label lastExport;
    private final GridPane budgetedVsExpensesTable;
    private final GridPane savingsBuffersAndDeficitsTable;

    // Month Year                                               [ month ] [ save ]
    // Budgeted vs. Expenses                                    Savings, Buffers and Deficits
    // Header   category - budgeted - expenses - remainder      Header  category - amount
    // Row      category - budgeted - expenses - remainder      Row     category - amount
//    public BudgetView(App app) {
    public BudgetView() {

        // Build header
        this.currentMonth = new Label("January 2026");
        this.lastExport = new Label("2025-01-01");
        this.remainingIncome = new Label("€500,-");
        this.currentBalance = new Label("€1000,-");
        GridPane header = new GridPane(3, 3);
        ColumnConstraints width25Percent = new ColumnConstraints();
        width25Percent.setPercentWidth(25);
        header.getColumnConstraints().addAll(width25Percent, width25Percent, width25Percent, width25Percent);
        header.addRow(0,
                wrapKpiWithLabel(currentMonth, "Selected month"),
                wrapKpiWithLabel(lastExport, "Last export date"),
                wrapKpiWithLabel(remainingIncome, "Available budget"),
                wrapKpiWithLabel(currentBalance, "Closing balance")
        );

        currentMonth.getStyleClass().add("header");
        lastExport.getStyleClass().add("header");
        remainingIncome.getStyleClass().add("header");
        currentBalance.getStyleClass().add("header");

        // Build tables
        ColumnConstraints width37_5Percent = new ColumnConstraints();
        width37_5Percent.setPercentWidth(37.5);
        ColumnConstraints categoryConstraint = new ColumnConstraints();
        categoryConstraint.setPrefWidth(200);
        ColumnConstraints numberConstraint = new ColumnConstraints();
        numberConstraint.setPrefWidth(80);

        Label budgetedVsExpensesHeader = new Label("Budgeted vs. Expenses");
        budgetedVsExpensesHeader.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        this.budgetedVsExpensesTable = new GridPane(3, 0);
//        budgetedVsExpensesTable.setStyle("-fx-background-color: red;");

        budgetedVsExpensesTable.getColumnConstraints().addAll(categoryConstraint, numberConstraint, numberConstraint, numberConstraint);
        addHeaderToBudgetedVsExpenses();
        VBox budgetedVsExpenses = new VBox(budgetedVsExpensesHeader, budgetedVsExpensesTable);

        Label savingsBuffersAndDeficitsHeader = new Label("Savings, Buffers and Deficits");
        savingsBuffersAndDeficitsHeader.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        this.savingsBuffersAndDeficitsTable = new GridPane(3, 0);
//        savingsBuffersAndDeficitsTable.setStyle("-fx-background-color: blue;");

        savingsBuffersAndDeficitsTable.getColumnConstraints().addAll(categoryConstraint, numberConstraint);
        addHeaderToSavingsBuffersAndDeficits();
        VBox savingsBuffersAndDeficits = new VBox(savingsBuffersAndDeficitsHeader, savingsBuffersAndDeficitsTable);

        GridPane tables2 = new GridPane(3, 3);
        tables2.getColumnConstraints().addAll(width37_5Percent, width37_5Percent, width25Percent);
        tables2.addRow(0,
                wrapInStackPane(budgetedVsExpenses),
                wrapInStackPane(savingsBuffersAndDeficits)
        );

        // Build menu
        this.monthSelection = new ComboBox<>();
        Button save = new Button("Save");
        HBox menu = new HBox(3, monthSelection, save);
        AnchorPane.setRightAnchor(menu, 10.);
        AnchorPane.setBottomAnchor(menu, 10.);

        VBox budgetOverview = new VBox(10, header, tables2);
//        budgetOverview.setStyle("-fx-background-color: blue;");
        budgetOverview.setFillWidth(true);

//        this.setStyle("-fx-background-color: green;");
        AnchorPane.setLeftAnchor(budgetOverview, 0.0);
        AnchorPane.setRightAnchor(budgetOverview, 0.0);

        this.getChildren().addAll(budgetOverview, menu);

    }

    public Label lastExport() {
        return lastExport;
    }

    public Label selectedMonth() {
        return currentMonth;
    }

    public void selectedMonth(int month, int year) {
        String monthYear = capitalize(Month.of(month).toString()) + " " + year;
        currentMonth.setText(monthYear);
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        return s.substring(0, 1).toUpperCase()
                + s.substring(1).toLowerCase();
    }

    public Label variance() {
        return remainingIncome;
    }

    public Label closing() {
        return currentBalance;
    }

    public ComboBox<LocalDate> monthSelector() {
        return monthSelection;
    }

    public void clearTables() {
        budgetedVsExpensesTable.getChildren().clear();
        addHeaderToBudgetedVsExpenses();
        savingsBuffersAndDeficitsTable.getChildren().clear();
        addHeaderToSavingsBuffersAndDeficits();
    }

    public void populateMonthSelector(Collection<LocalDate> months, LocalDate selected) {
        TreeSet<LocalDate> monthsDescending = new TreeSet<>(Comparator.reverseOrder());
        months.forEach(monthsDescending::add);

        for (LocalDate month : monthsDescending) {
            monthSelection.getItems().add(month);
        }
        if (selected == null || !months.contains(selected)) {
            monthSelection.getSelectionModel().selectFirst();
        } else {
            monthSelection.getSelectionModel().select(selected);
        }
    }

    private void addHeaderToBudgetedVsExpenses() {
        Label category = new Label("Category");
        Label budgeted = new Label("Budgeted");
        Label actual = new Label("Actual");
        Label variance = new Label("Remainder");
        budgetedVsExpensesTable.addRow(0, category, budgeted, actual, variance);
    }

    private void addHeaderToSavingsBuffersAndDeficits() {
        Label category = new Label("Category");
        Label amount = new Label("Amount");
        Label adjustments = new Label("Adjustments");
        savingsBuffersAndDeficitsTable.addRow(0, category, amount, adjustments);
    }

    public void addCategoryToBudgetedVsExpenses(String category, BigDecimal budgeted, BigDecimal actual) {
        int index = budgetedVsExpensesTable.getRowCount();
        BigDecimal variance = budgeted.add(actual);
        budgetedVsExpensesTable.addRow(
                index,
                new Label(category),
                new TextField(budgeted.toPlainString()),
                newNumberLabel(actual),
                newNumberLabel(variance)
        );
    }

    public void addCategoryToSavingsBuffersAndDeficits(String category, BigDecimal closing, BigDecimal adjustments) {
        // if amount equals zero, then there is no savings, buffer or deficit
        if (closing.compareTo(BigDecimal.ZERO) == 0) {
            return;
        }
        int index = savingsBuffersAndDeficitsTable.getRowCount();
        savingsBuffersAndDeficitsTable.addRow(index,
                new Label(category),
                newNumberLabel(closing),
                new TextField(adjustments.toPlainString())
        );
    }

    private StackPane wrapKpiWithLabel(Node node, String title) {
        VBox box = new VBox();
        Label label = new Label(title);
        box.setAlignment(Pos.CENTER);
        box.getChildren().addAll(node, label);
        return wrapInStackPane(box);
    }

    private StackPane wrapInStackPane(Node node) {
        StackPane pane = new StackPane(node);
        pane.getStyleClass().add("tile");
        return pane;
    }

    private Label newNumberLabel(BigDecimal value) {
        String twoDecimals = String.format("%.2f", value);
        Label label = new Label(twoDecimals);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setAlignment(Pos.CENTER_RIGHT);
        GridPane.setHgrow(label, Priority.ALWAYS);
        return label;
    }

}
