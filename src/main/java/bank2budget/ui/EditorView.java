package bank2budget.ui;

import bank2budget.App;
import bank2budget.adapters.db.BudgetDatabase;
import bank2budget.adapters.reader.TransactionReaderForCsv;
import bank2budget.core.Account;
import bank2budget.core.CashTransaction;
import bank2budget.core.MultiAccountBudget;
import bank2budget.core.RuleEngine;
import java.io.File;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Window;

/**
 *
 * @author joostmeulenkamp
 */
public class EditorView extends BorderPane {

    private AccountsView accountsView;
    private final App app;

    public EditorView(App app) {
        this.app = app;

        MenuBar menuBar = getMenuBar();
        this.setTop(menuBar);

        if (true) {
            this.accountsView = new AccountsView();
            this.setCenter(accountsView);

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

        importCsvs.setOnAction((e) -> onImportCsvs(menuBar.getScene().getWindow()));
        save.setOnAction(this::onSave);

        menuBar.getMenus().add(fileMenu);
        fileMenu.getItems().addAll(importCsvs, save, quit);

        return menuBar;
    }

    private void onImportCsvs(Window ownerWindow) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import CSV Files");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );

        List<File> files = fileChooser.showOpenMultipleDialog(ownerWindow);
        if (files == null) {
            return;
        }

        // move files to todo or archive
        // process files
        TransactionReaderForCsv reader = new TransactionReaderForCsv(files);
        List<CashTransaction> importedTransactions = reader.getAllTransactions();

        // apply rules  
        RuleEngine ruleEngine = app.getRuleEngine();
        ruleEngine.overwriteAccountNames(importedTransactions);
        ruleEngine.determineInternalTransactions(importedTransactions);
        ruleEngine.applyRules(importedTransactions);

        // add to accounts
        Account.addTransactionsToAccounts(importedTransactions);

        // check integrity 
        // reload table
        accountsView.reload();

        // show import finished
    }

    private void onSave(ActionEvent e) {
        // save transactions to xlsx and db 
        app.getTransactionWriterForXlsx().write(Account.getAccounts());
        BudgetDatabase database = app.getBudgetDatabase();
        for (Account account : Account.getAccounts()) {
            database.insertTransactions(account.getAllTransactionsAscending());
        }

        // process budget
        MultiAccountBudget budget = app.getBudgetReaderForXlsx().read();
        budget.setAccounts(Account.getAccounts());

        // save budget to xlsx and db
        app.getBudgetWriterForXlsx().write(budget);
        database.insertMonthlyBudgets(budget.getMonthlyBudgets().values());

        // show save finished
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
