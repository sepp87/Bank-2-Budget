package bank2budget.ui;

import bank2budget.App;
import bank2budget.core.Account;
import bank2budget.core.MonthlyBudget;
import bank2budget.core.MultiAccountBudget;
import bank2budget.core.Util;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Subscription;

/**
 *
 * @author joostmeulenkamp
 */
public class BudgetView extends AnchorPane {

    private final ComboBox<String> monthSelection;

    private final App app;
    private final MultiAccountBudget budget;
    private final Label currentMonth;
    private final Label currentYear;
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
        this.currentMonth = new Label("January");
        this.currentYear = new Label("2026");
        this.lastExport = new Label("2026-01-01");
        this.remainingIncome = new Label("€500,-");
        this.currentBalance = new Label("€1000,-");
        HBox header = new HBox(3, currentMonth, currentYear, lastExport, remainingIncome, currentBalance);
        
        currentMonth.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        currentYear.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        lastExport.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        remainingIncome.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        currentBalance.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        // Build tables
        ColumnConstraints categoryConstraint = new ColumnConstraints();
        categoryConstraint.setPrefWidth(200);
        ColumnConstraints numberConstraint = new ColumnConstraints();
        numberConstraint.setPrefWidth(80);

        Label budgetedVsExpensesHeader = new Label("Budgeted vs. Expenses");
        budgetedVsExpensesHeader.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        this.budgetedVsExpensesTable = new GridPane(3, 0);
        budgetedVsExpensesTable.getColumnConstraints().addAll(categoryConstraint, numberConstraint, numberConstraint, numberConstraint);
        addHeaderToBudgetedVsExpenses();
        VBox budgetedVsExpenses = new VBox(budgetedVsExpensesHeader, budgetedVsExpensesTable);

        Label savingsBuffersAndDeficitsHeader = new Label("Savings, Buffers and Deficits");
        savingsBuffersAndDeficitsHeader.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        this.savingsBuffersAndDeficitsTable = new GridPane(3, 0);
        savingsBuffersAndDeficitsTable.getColumnConstraints().addAll(categoryConstraint, numberConstraint);
        addHeaderToSavingsBuffersAndDeficits();
        VBox savingsBuffersAndDeficits = new VBox(savingsBuffersAndDeficitsHeader, savingsBuffersAndDeficitsTable);

        HBox tables = new HBox(50, budgetedVsExpenses, savingsBuffersAndDeficits);

        // Build menu
        this.monthSelection = new ComboBox<>();
        populateMonthSelection(null);
        Button save = new Button("Save");
        HBox menu = new HBox(3, monthSelection, save);
        AnchorPane.setRightAnchor(menu, 10.);
        AnchorPane.setTopAnchor(menu, 10.);

        VBox budgetOverview = new VBox(10, header, tables);
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
        String key = monthSelection.getSelectionModel().getSelectedItem();
        populateMonthSelection(key);
        selectMonthSubscription = newSelectMonthSubscription();
    }

    private void clearAndSelectMonth(String key) {
        clearTables();
        selectMonth(key);
    }

    private void clearTables() {
        budgetedVsExpensesTable.getChildren().clear();
        addHeaderToBudgetedVsExpenses();
        savingsBuffersAndDeficitsTable.getChildren().clear();
        addHeaderToSavingsBuffersAndDeficits();
    }

    private void populateMonthSelection(String key) {
        List<String> monthsDesc = new ArrayList<>(budget.getMonthlyBudgets().keySet()).reversed();
        for (String month : monthsDesc) {
            monthSelection.getItems().add(month);
        }
        if (key == null) {
            monthSelection.getSelectionModel().selectFirst();
        } else {
            monthSelection.getSelectionModel().select(key);
        }
    }

    private void selectMonth(String key) {
        MonthlyBudget month = null;
        if (key == null) {
            month = budget.getMonthlyBudgets().lastEntry().getValue();
        } else {
            month = budget.getMonthlyBudgets().get(key);
        }

        currentMonth.setText(Month.of(month.getFinancialMonth()).toString());
        currentYear.setText(month.getFinancialYear() + "");

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

}
