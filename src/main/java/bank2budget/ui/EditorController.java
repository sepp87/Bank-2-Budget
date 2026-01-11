package bank2budget.ui;

import bank2budget.App;
import bank2budget.ui.budgettemplate.BudgetTemplateController;
import bank2budget.ui.dashboard.DashboardController;
import bank2budget.ui.rules.RuleController;
import bank2budget.ui.transaction.EditableCashTransaction;
import bank2budget.ui.transaction.AccountReviewController;
import bank2budget.ui.transaction.TransactionReviewController;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javafx.application.Platform;
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

    private final AccountReviewController accountReviewController;
    private final DashboardController budgetController;
    private final BudgetTemplateController budgetTemplateController;
    private final RuleController ruleController;
    private final TransactionReviewController transactionReviewController;

    public EditorController(EditorView editorView, App app) throws IOException {
        this.view = editorView;
        this.app = app;

        this.accountReviewController = new AccountReviewController(view.accountReviewView(), app.getAccountService());
        this.budgetController = new DashboardController(view.budgetView(), app);

        this.budgetTemplateController = new BudgetTemplateController(view.budgetTemplateView(), app.getTemplateService());
        this.ruleController = new RuleController(view.rulesView(), app.getRuleService());
        this.transactionReviewController = new TransactionReviewController(view.transactionReviewView(), app.getAccountService());

        // File menu - event handlers
        view.menuItemImportCsvs().setOnAction((e) -> onImportCsvs(view.getScene().getWindow()));
        view.menuItemSave().setOnAction(this::onSave);

        // View menu - event handlers
        view.menuItemAccounts().setOnAction((e) -> view.showAccountsView());
        view.menuItemBudgetTemplate().setOnAction((e) -> {
            budgetTemplateController.reload();
            view.showBudgetTemplateView();
        });
        view.menuItemRules().setOnAction((e) -> {
            ruleController.reload();
            view.showRulesView();
        });

        // Budget - event handler
        budgetController.setOnReviewTransactions((e) -> startTransactionReview());
        app.getBudgetService().setOnBudgetRecalculated(
                () -> Platform.runLater(budgetController::reload)
        );

        // Modal menu - event handlers
        accountReviewController.setOnCanceled((e) -> closeOverlayModal());
        accountReviewController.setOnFinished((e) -> finishAccountReview());
        budgetTemplateController.setOnCanceled((e) -> closeOverlayModal());
        budgetTemplateController.setOnFinished((e) -> finishBudgetTemplateEdit());
        ruleController.setOnCanceled((e) -> closeOverlayModal());
        ruleController.setOnFinished((e) -> finishRuleEdit());
        transactionReviewController.setOnFinished((e) -> finishTransactionReview());
        transactionReviewController.setOnCanceled((e) -> closeOverlayModal());

//        view.menuItemRules().fire();
    }

    private void startTransactionReview() {
        ObservableList<EditableCashTransaction> transactions = FXCollections.observableList(budgetController.getSelectedMonth().transactions().stream().map(EditableCashTransaction::new).toList());
        transactionReviewController.load(transactions);
        view.showTransactionReview();
    }

    private void finishTransactionReview() {
        transactionReviewController.commitChanges();
        closeOverlayModal();
    }

    private void finishAccountReview() {
        accountReviewController.commitChanges();
        closeOverlayModal();
    }

    private void finishBudgetTemplateEdit() {
        budgetTemplateController.commitChanges();
        closeOverlayModal();
    }

    private void finishRuleEdit() {
        ruleController.commitChanges();
        closeOverlayModal();
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
//        multiAccountController.reload();
        // show import finished
        if (isSuccess) {
            view.notificationView().showNotification("Import succesful!");
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
                // save transactions to xlsx and db 
//                multiAccountController.commitChanges();

                app.getAccountService().save();
                app.getAnalyticsExportService().exportAccounts(app.getAccountService().getAccounts());

                // recalculate and save budget
                app.getBudgetService().recalculateAndSave();

                // save to db
                app.getAnalyticsExportService().exportBudget(app.getBudgetService().getBudget());

                // save settings
                app.getRuleService().save();
                app.getTemplateService().save();

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
