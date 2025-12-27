package bank2budget.ui;

import bank2budget.App;
import bank2budget.ui.budgettemplate.BudgetTemplateView;
import bank2budget.ui.dashboard.BudgetView;
import bank2budget.ui.rules.RuleView;
import bank2budget.ui.transaction.MultiAccountView;
import bank2budget.ui.transaction.TransactionReviewView;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 *
 * @author joostmeulenkamp
 */
public class EditorView extends BorderPane {

    private final MultiAccountView accountsView;
    private final BudgetView budgetView;
    private final VBox contentWrapper;
    private final VBox overlayWrapper;
    private final NotificationView notificationView;
    private final BudgetTemplateView budgetTemplateView;
    private final RuleView rulesView;
    private final TransactionReviewView transactionReviewView;
    private final StackPane overlayLayer;

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

        if (true) {
            contentWrapper.getChildren().add(accountsView);
        } else {
            contentWrapper.getChildren().add(budgetView);
        }

//        HBox center = new HBox(contentWrapper);
//        center.setMaxWidth(USE_PREF_SIZE);
//        center.setAlignment(Pos.TOP_CENTER);
//        center.getStyleClass().add("center");
        StackPane contentLayer = new StackPane();
        contentLayer.setMaxWidth(USE_PREF_SIZE); // to negate automatic stretching to full-width of panes by BorderPane
        contentLayer.setAlignment(Pos.TOP_CENTER);
        contentLayer.getStyleClass().add("center");
        contentLayer.getChildren().add(contentWrapper);

        this.budgetTemplateView = new BudgetTemplateView();
        budgetTemplateView.setMinWidth(800);
        budgetTemplateView.setMaxWidth(1740);
        budgetTemplateView.prefWidthProperty().bind(this.widthProperty().multiply(0.95));
        this.rulesView = new RuleView();
        rulesView.setMinWidth(800);
        rulesView.setMaxWidth(1740);
        rulesView.prefWidthProperty().bind(this.widthProperty().multiply(0.95));
        this.transactionReviewView = new TransactionReviewView();
        transactionReviewView.setMinWidth(800);
        transactionReviewView.setMaxWidth(1740);
        transactionReviewView.prefWidthProperty().bind(this.widthProperty().multiply(0.95));
        this.overlayWrapper = new VBox();
        overlayWrapper.setMaxWidth(USE_PREF_SIZE); // to negate automatic stretching to full-width of panes by BorderPane
        overlayWrapper.setAlignment(Pos.TOP_CENTER);
        overlayWrapper.getChildren().add(rulesView);
        this.overlayLayer = new StackPane();
        overlayLayer.getStyleClass().add("overlay");
        overlayLayer.setVisible(false);
        overlayLayer.getChildren().add(overlayWrapper);

        AnchorPane notificationLayer = new AnchorPane();
        notificationLayer.getStyleClass().add("anchor");
        notificationView = new NotificationView();
        notificationLayer.getChildren().add(notificationView);
        notificationLayer.setMouseTransparent(true);
        AnchorPane.setBottomAnchor(notificationView, 0.);
        AnchorPane.setRightAnchor(notificationView, 0.);

        StackPane viewport = new StackPane();
        this.getStyleClass().add("viewport");
        viewport.getChildren().addAll(contentLayer, overlayLayer, notificationLayer);

        notificationLayer.prefWidthProperty().bind(viewport.widthProperty());
        notificationLayer.prefHeightProperty().bind(viewport.heightProperty());

        this.setCenter(viewport);
        this.getStyleClass().add("editor");

        viewport.setMinHeight(0); // ensure the viewport shrinks to the window size
        contentLayer.setMinHeight(0); // ensure the content layer also shrinks to the window size

    }

    public MultiAccountView accountsView() {
        return accountsView;
    }

    public BudgetView budgetView() {
        return budgetView;
    }

    public RuleView rulesView() {
        return rulesView;
    }

    public BudgetTemplateView budgetTemplateView() {
        return budgetTemplateView;
    }

    public TransactionReviewView transactionReviewView() {
        return transactionReviewView;
    }

    public NotificationView notificationView() {
        return notificationView;
    }

    public void showAccountsView() {
        switchContentTo(accountsView);
    }

    public void showBudgetView() {
        switchContentTo(budgetView);
    }

    public void showBudgetTemplateView() {
        switchOverlayTo(budgetTemplateView);
        overlayLayer.setVisible(true);
    }

    public void showRulesView() {
        switchOverlayTo(rulesView);
        overlayLayer.setVisible(true);
    }

    public void showTransactionReview() {
        switchOverlayTo(transactionReviewView);
        overlayLayer.setVisible(true);
    }

    public void hideOverlay() {
        overlayLayer.setVisible(false);
    }

    private void switchContentTo(Node view) {
        if (contentWrapper.getChildren().contains(view)) {
            return;
        }
        contentWrapper.getChildren().setAll(view);
    }

    private void switchOverlayTo(Node view) {
        if (overlayWrapper.getChildren().contains(view)) {
            return;
        }
        overlayWrapper.getChildren().setAll(view);
    }

    // Menu bar
    private MenuItem importCsvs;
    private MenuItem save;
    private MenuItem accounts;
    private MenuItem budget;
    private MenuItem budgetTemplate;
    private MenuItem rules;

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
        this.budgetTemplate = new MenuItem("Budget Template");
        this.rules = new MenuItem("Categorization Rules");

        viewMenu.getItems().addAll(accounts, budget, budgetTemplate, rules);

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
    
    public MenuItem menuItemBudgetTemplate() {
        return budgetTemplate;
    }
    
    public MenuItem menuItemRules() {
        return rules;
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
