package bank2budget.ui;

import bank2budget.App;
import bank2budget.adapters.reader.AccountImporter;
import bank2budget.adapters.repository.BudgetDatabase;
import bank2budget.adapters.reader.TransactionReaderForCsv;
import bank2budget.core.Account;
import bank2budget.core.MultiAccountBudget;
import java.io.File;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
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

//        
        VBox contentWrapper = new VBox();
//        VBox.setVgrow(contentWrapper, Priority.ALWAYS);
//        contentWrapper.maxHeight(Double.MAX_VALUE);
        contentWrapper.setMinWidth(800);
        contentWrapper.setMaxWidth(1200);
        contentWrapper.prefWidthProperty().bind(
                this.widthProperty().multiply(0.80)
        );
        contentWrapper.setStyle("-fx-background-color: green;");

        if (false) {
            this.accountsView = new AccountsView(app.getAccountService());
            VBox.setVgrow(accountsView, Priority.ALWAYS);
            contentWrapper.getChildren().add(accountsView);

        } else {
            BudgetView budgetView = new BudgetView(app);
            contentWrapper.getChildren().add(budgetView);
        }

        HBox center = new HBox(contentWrapper);
        center.setMaxWidth(USE_PREF_SIZE);
        center.setAlignment(Pos.TOP_CENTER);
//        center.setStyle("-fx-background-color: green;");

        this.setCenter(center);

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
        // apply rules  
        // add to accounts
        TransactionReaderForCsv reader = new TransactionReaderForCsv(files);
        List<Account> imported = new AccountImporter(reader).importAccounts();
        app.getAccountService().importAccounts(imported); // normalizes, applies rules and merges with existing accounts

        
        // check integrity 
        // reload table
        accountsView.reload();

        // show import finished
    }

    private void onSave(ActionEvent e) {
        // save transactions to xlsx and db 
        app.getAccountWriter().write(app.getAccountService().getAccounts());
        BudgetDatabase database = app.getBudgetDatabase();
        for (Account account : app.getAccountService().getAccounts()) {
            database.insertTransactions(account.getAllTransactionsAscending());
        }

        // process budget
        MultiAccountBudget budget = app.getBudgetReaderForXlsx().read();
        budget.setAccounts(app.getAccountService().getAccounts());

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
