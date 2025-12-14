package bank2budget.ui;

import bank2budget.App;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 *
 * @author joostmeulenkamp
 */
public class EditorView extends BorderPane {

    private final MultiAccountView accountsView;
    private final BudgetView budgetView;
    private final VBox contentWrapper;

    public EditorView(App app) {

        MenuBar menuBar = createMenuBar();
        this.setTop(menuBar);

        this.contentWrapper = new VBox();
        contentWrapper.setMinWidth(800);
        contentWrapper.setMaxWidth(1740);
        contentWrapper.prefWidthProperty().bind(this.widthProperty().multiply(0.95));
//        contentWrapper.setStyle("-fx-background-color: green;");

        this.accountsView = new MultiAccountView();
        VBox.setVgrow(accountsView, Priority.ALWAYS);

        this.budgetView = new BudgetView();

        if (false) {
            contentWrapper.getChildren().add(accountsView);
        } else {
            contentWrapper.getChildren().add(budgetView);
        }

        HBox center = new HBox(contentWrapper);
        center.setMaxWidth(USE_PREF_SIZE);
        center.setAlignment(Pos.TOP_CENTER);
//        center.setStyle("-fx-background-color: green;");

        this.setCenter(center);
        this.getStyleClass().add("editor");

    }

    public MultiAccountView accountsView() {
        return accountsView;
    }

    public BudgetView budgetView() {
        return budgetView;
    }

    public void showAccountsView() {
        switchViewTo(accountsView);
    }

    public void showBudgetView() {
        switchViewTo(budgetView);
    }

    private void switchViewTo(Node view) {
        if (contentWrapper.getChildren().contains(view)) {
            return;
        }
        contentWrapper.getChildren().clear();
        contentWrapper.getChildren().add(view);
    }

    // Menu bar
    private MenuItem importCsvs;
    private MenuItem save;
    private MenuItem accounts;
    private MenuItem budget;

    private MenuBar createMenuBar() {

        // File menu
        Menu fileMenu = new Menu("File");
        this.importCsvs = new MenuItem("Import CSV's");
        this.save = new MenuItem("Save");
        MenuItem quit = new MenuItem("Quit");
        fileMenu.getItems().addAll(importCsvs, save, quit);

        // View menu
        Menu viewMenu = new Menu("View");
        this.accounts = new MenuItem("Transactions");
        this.budget = new MenuItem("Budget");
        viewMenu.getItems().addAll(accounts, budget);

        // Root menu bar
        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().addAll(fileMenu, viewMenu);

        return menuBar;
    }

    public MenuItem menuItemImportCsvs() {
        return importCsvs;
    }

    public MenuItem menuItemSave() {
        return save;
    }

    public MenuItem menuItemAccounts() {
        return accounts;
    }

    public MenuItem menuItemBudget() {
        return budget;
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
