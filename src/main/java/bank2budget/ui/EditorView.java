package bank2budget.ui;

import bank2budget.core.Account;
import bank2budget.core.CashTransaction;
import java.util.Iterator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;

/**
 *
 * @author joostmeulenkamp
 */
public class EditorView extends BorderPane {

    public EditorView() {

        MenuBar menuBar = getMenuBar();
        this.setTop(menuBar);

        if (true) {
            Iterator<Account> iterator = Account.getAccounts().iterator();
            iterator.next();
            ObservableList<CashTransaction> transactions = FXCollections.observableArrayList(iterator.next().getAllTransactionsAscending());
            TransactionsView transactionsView = new TransactionsView(transactions);
            this.setCenter(transactionsView);

        } else {
            BudgetView budgetView = new BudgetView();
            budgetView.addBudgetCategory("GROCERIES", 500., 100.);
            budgetView.addBudgetCategory("RESTAURANT", 500., 100.);
            budgetView.addBudgetCategory("VACATION", 500., 100.);
            this.setCenter(budgetView);
        }

    }

    private MenuBar getMenuBar() {
        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");
        MenuItem importCsvs = new MenuItem("Import CSV's");
        MenuItem save = new MenuItem("Save");
        MenuItem quit = new MenuItem("Quit");

        menuBar.getMenus().add(fileMenu);
        fileMenu.getItems().addAll(importCsvs, save, quit);

        return menuBar;
    }

    private void buildBudgetSettingsView() {
        // first of month - int (1-28)
        // category - budgeted (double)

        Label firstOfMonth = new Label("First of Month");
        Label categories = new Label("Categories");
        TextField category = new TextField("GROCERIES");
        TextField budgeted = new TextField("500");

    }
}
