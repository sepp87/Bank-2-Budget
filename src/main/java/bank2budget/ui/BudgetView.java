package bank2budget.ui;

import bank2budget.App;
import bank2budget.core.Account;
import bank2budget.core.MonthlyBudget;
import bank2budget.core.MultiAccountBudget;
import bank2budget.core.Util;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Subscription;

/**
 *
 * @author joostmeulenkamp
 */
public class BudgetView extends AnchorPane {

    private final ComboBox<LocalDate> monthSelection;

    private final App app;
    private final MultiAccountBudget budget;
    private final Label currentMonth;
    private final Label remainingIncome;
    private final Label currentBalance;
    private final Label lastExport;
    private final GridPane budgetedVsExpensesTable;
    private final GridPane savingsBuffersAndDeficitsTable;
    private Subscription selectMonthSubscription;

    // Month Year                                               [ month ] [ save ]
    // Budgeted vs. Expenses                                    Savings, Buffers and Deficits
    // Header   category - budgeted - expenses - remainder      Header  category - amount
    // Row      category - budgeted - expenses - remainder      Row     category - amount
    public BudgetView(App app) {
        this.app = app;
        this.budget = app.getBudgetReaderForXlsx().read();
        budget.setAccounts(Account.getAccounts());

        // Build header
        this.currentMonth = new Label("January 2026");

        String lastExportDate = Account.getLastExportDate() == null ? "" : Account.getLastExportDate().toString();
        this.lastExport = new Label(lastExportDate);
        this.remainingIncome = new Label("€500,-");
        this.currentBalance = new Label("€1000,-");
        GridPane header = new GridPane(3, 3);
        ColumnConstraints width25Percent = new ColumnConstraints();
        width25Percent.setPercentWidth(25);
        header.getColumnConstraints().addAll(width25Percent, width25Percent, width25Percent, width25Percent);
        header.addRow(0,
                wrapInStackPane(currentMonth),
                wrapInStackPane(lastExport),
                wrapInStackPane(remainingIncome),
                wrapInStackPane(currentBalance)
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
        populateMonthSelection(null);
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
        this.selectMonthSubscription = newSelectMonthSubscription();

    }

    private Subscription newSelectMonthSubscription() {
        return monthSelection.getSelectionModel().selectedItemProperty().subscribe(this::clearAndSelectMonth);
    }

    public void reload() {
        selectMonthSubscription.unsubscribe();
        budget.setAccounts(Account.getAccounts());
        monthSelection.getItems().clear();
        LocalDate key = monthSelection.getSelectionModel().getSelectedItem();
        populateMonthSelection(key);
        selectMonthSubscription = newSelectMonthSubscription();
    }

    private void clearAndSelectMonth(LocalDate key) {
        clearTables();
        selectMonth(key);
    }

    private void clearTables() {
        budgetedVsExpensesTable.getChildren().clear();
        addHeaderToBudgetedVsExpenses();
        savingsBuffersAndDeficitsTable.getChildren().clear();
        addHeaderToSavingsBuffersAndDeficits();
    }

    private void populateMonthSelection(LocalDate key) {
        List<LocalDate> monthsDesc = new ArrayList<>(budget.getMonthlyBudgets().keySet()).reversed();
        for (LocalDate month : monthsDesc) {
            monthSelection.getItems().add(month);
        }
        if (key == null) {
            monthSelection.getSelectionModel().selectFirst();
        } else {
            monthSelection.getSelectionModel().select(key);
        }
    }

    private void selectMonth(LocalDate key) {
        MonthlyBudget month = null;
        if (key == null) {
            month = budget.getMonthlyBudgets().lastEntry().getValue();
        } else {
            month = budget.getMonthlyBudgets().get(key);
        }

        currentMonth.setText(Month.of(month.getFinancialMonth()).toString() + " " + month.getFinancialYear());
//        remainingIncome.setText(key);
        currentBalance.setText(Account.getTotalBalanceOn(month.getLastOfMonth()) + "");

        loadBudgetedVsExpenses(month);
        loadSavingsBuffersAndDeficits(month);
    }

    private void loadBudgetedVsExpenses(MonthlyBudget month) {
        for (Entry<String, Double> entry : month.getBudgetedForCategories().entrySet()) {
            String category = entry.getKey();
            Double budgeted = entry.getValue();
            Double expenses = month.getExpensesForCategories().get(category);
            addCategoryToBudgetedVsExpenses(category, budgeted, expenses);
        }
        addCategoryToBudgetedVsExpenses("UNASSIGNED EXPENSES", 0., month.getUnassignedExpenses());
        addCategoryToBudgetedVsExpenses("UNASSIGNED INCOME", 0., month.getUnassignedIncome());
    }

    private void loadSavingsBuffersAndDeficits(MonthlyBudget month) {
        for (Entry<String, Double> entry : month.getBudgetedForCategories().entrySet()) {
            String category = entry.getKey();
            Double amount = month.getRemainderForCategories().get(category);
            addCategoryToSavingsBuffersAndDeficits(category, amount);
        }
    }

    private void addHeaderToBudgetedVsExpenses() {
        Label category = new Label("Category");
        Label budgeted = new Label("Budgeted");
        Label expenses = new Label("Expenses");
        Label remainder = new Label("Remainder");
        budgetedVsExpensesTable.addRow(0, category, budgeted, expenses, remainder);
    }

    private void addHeaderToSavingsBuffersAndDeficits() {
        Label category = new Label("Category");
        Label amount = new Label("Amount");
        savingsBuffersAndDeficitsTable.addRow(0, category, amount);
    }

    public void addCategoryToBudgetedVsExpenses(String category, Double budgeted, Double expenses) {
        int index = budgetedVsExpensesTable.getRowCount();
        Double remainder = Math.floor((budgeted + expenses) * 100) / 100;
        budgetedVsExpensesTable.addRow(
                index,
                new Label(category),
                new TextField(budgeted.toString()),
                new Label(expenses.toString()),
                new Label(remainder.toString())
        );
    }

    public void addCategoryToSavingsBuffersAndDeficits(String category, Double amount) {
        // if amount equals zero, then there is no savings, buffer or deficit
        if (Util.compareMoney(amount, 0.)) {
            return;
        }
        int index = savingsBuffersAndDeficitsTable.getRowCount();
        savingsBuffersAndDeficitsTable.addRow(
                index,
                new Label(category),
                new Label(amount.toString()),
                new Button("Rebalance")
        );
    }

    private StackPane wrapInStackPane(Node node) {
        StackPane pane = new StackPane(node);
        pane.getStyleClass().add("tile");
        return pane;
    }

}
