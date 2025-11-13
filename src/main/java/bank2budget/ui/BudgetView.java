package bank2budget.ui;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 *
 * @author joostmeulenkamp
 */
public class BudgetView extends AnchorPane {

    private GridPane budgetTable;

    public BudgetView() {
        Button save = new Button("Save");
        AnchorPane.setRightAnchor(save, 10.);
        AnchorPane.setTopAnchor(save, 10.);

        // Month Year
        // First of month
        // Header category - budgeted - expenses - last month - remainder
        // Row category - budgeted - expenses - last month - remainder
        Label month = new Label("February");
        Label year = new Label("2026");
        Label firstOfMonth = new Label("2026-01-26");
        HBox header = new HBox(10, month, year);
        HBox subheader = new HBox(firstOfMonth);
        this.budgetTable = getBudgetTable();

        VBox budgetOverview = new VBox(10, header, subheader, budgetTable);
        this.getChildren().addAll(budgetOverview, save);
    }

    private GridPane getBudgetTable() {
        GridPane table = new GridPane();
        Label category = new Label("Category");
        Label budgeted = new Label("Budgeted");
        Label expenses = new Label("Expenses");
        Label remainder = new Label("Remainder");
        table.addRow(0, category, budgeted, expenses, remainder);
        return table;
    }

    public void addBudgetCategory(String category, Double budgeted, Double expenses) {
        int index = budgetTable.getRowCount();
        budgetTable.addRow(
                index,
                new Label(category),
                new Label(budgeted.toString()),
                new Label(expenses.toString()),
                new Label(budgeted + expenses + "")
        );
    }

}
