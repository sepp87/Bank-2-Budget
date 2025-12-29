package bank2budget.ui;

import bank2budget.App;
import bank2budget.AppPaths;
import bank2budget.adapter.rule.RuleReader;
import bank2budget.core.CashTransaction;
import bank2budget.core.CashTransactionDomainLogic;
import bank2budget.ui.budgettemplate.BudgetTemplateController;
import bank2budget.ui.budgettemplate.EditableBudgetTemplateCategory;
import bank2budget.ui.dashboard.BudgetController;
import bank2budget.ui.rules.EditableRuleConfig;
import bank2budget.ui.rules.RuleController;
import bank2budget.ui.transaction.EditableCashTransaction;
import bank2budget.ui.transaction.MultiAccountController;
import bank2budget.ui.transaction.TransactionReviewController;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.stage.FileChooser;
import javafx.stage.Window;

/**
 *
 * @author joostmeulenkamp
 */
public class EditorController {

    private final EditorView view;
    private final App app;

    private final MultiAccountController accountsController;
    private final BudgetController budgetController;
    private final BudgetTemplateController budgetTemplateController;
    private final RuleController rulesController;
    private final TransactionReviewController transactionReviewController;

    public EditorController(EditorView editorView, App app) throws IOException {
        this.view = editorView;
        this.app = app;

        this.accountsController = new MultiAccountController(view.accountsView(), app.getAccountService());
        this.budgetController = new BudgetController(view.budgetView(), app);

        this.budgetTemplateController = new BudgetTemplateController(view.budgetTemplateView());
        this.rulesController = new RuleController(view.rulesView());
        this.transactionReviewController = new TransactionReviewController(view.transactionReviewView());

        // File menu - event handlers
        view.menuItemImportCsvs().setOnAction((e) -> onImportCsvs(view.getScene().getWindow()));
        view.menuItemSave().setOnAction(this::onSave);

        // View menu - event handlers
        view.menuItemAccounts().setOnAction((e) -> view.showAccountsView());
        view.menuItemBudget().setOnAction((e) -> view.showBudgetView());
        view.menuItemBudgetTemplate().setOnAction((e) -> {
            var categories = app.getBudgetService().getBudgetTemplate().operatingCategories().values().stream().map(EditableBudgetTemplateCategory::new).toList();
            budgetTemplateController.setFirstOfMonth(app.getBudgetService().getBudgetTemplate().firstOfMonth());
            budgetTemplateController.load(categories);
            view.showBudgetTemplateView();
        });
        AppPaths paths = new AppPaths();
        view.menuItemRules().setOnAction((e) -> {
            var rules = new RuleReader(paths.getCategorizationRulesFile()).read().stream().map(EditableRuleConfig::new).toList();
            rulesController.load(rules);
            view.showRulesView();
        });

        // Budget - event handler
        budgetController.setOnReviewTransactions((e) -> startTransactionReview());

        // Modal menu - event handlers
        budgetTemplateController.setOnCanceled((e) -> closeOverlayModal());
        budgetTemplateController.setOnFinished((e) -> closeOverlayModal());
        rulesController.setOnCanceled((e) -> closeOverlayModal());
        rulesController.setOnFinished((e) -> closeOverlayModal());
        transactionReviewController.setOnFinished((e) -> finishTransactionReview());
        transactionReviewController.setOnCanceled((e) -> closeOverlayModal());

        view.menuItemRules().fire();
    }

    private void startTransactionReview() {
        ObservableList<EditableCashTransaction> transactions = FXCollections.observableList(budgetController.getSelectedMonth().transactions().stream().map(EditableCashTransaction::new).toList());
        transactionReviewController.load(transactions);
        view.showTransactionReview();
    }

    private void finishTransactionReview() {
        List<CashTransaction> transactions = transactionReviewController.transactions().stream().map(EditableCashTransaction::toDomain).toList();
        var grouped = CashTransactionDomainLogic.groupByAccountNumber(transactions);
        for (var ac : app.getAccountService().getAccounts()) {
            if (grouped.containsKey(ac.getAccountNumber())) {
                ac.replace(grouped.get(ac.getAccountNumber()));
            }
        }
        app.getBudgetService().recalculate();
        budgetController.reload();
        view.hideOverlay();

    }

    private void closeOverlayModal() {
        view.hideOverlay();

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
        // check integrity 
        // add to accounts
        boolean isSuccess = app.getAccountService().importFromFiles(files); // normalizes, applies rules and merges with existing accounts

        // reload table
        accountsController.reload();

        // show import finished
        if (isSuccess) {
            view.notificationView().showNotification("Import succesful!");
            app.getBudgetService().recalculate();
            budgetController.reload();
        } else {
            view.notificationView().showError("Import aborted! Balance history interrupted.");
        }
    }

    private void onSave(ActionEvent e) {

//        if(true) {
//            return;
//        }
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
//                // save transactions to xlsx and db 
//                accountsController.saveToDomain();
//
//                app.getAccountService().save();
//                app.getAnalyticsExportService().exportAccounts(app.getAccountService().getAccounts());
//
//                // recalculate and save budget
//                app.getBudgetService().recalculateAndSave();
//
//                // save to db
//                app.getAnalyticsExportService().exportBudget(app.getBudgetService().getBudget());

                // show save finished
                view.notificationView().showNotification("Save successful!");
                return null;
            }
        };

        // Run the task in a separate thread
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();

    }
}
