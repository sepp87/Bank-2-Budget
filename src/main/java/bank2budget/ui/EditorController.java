package bank2budget.ui;

import bank2budget.App;
import java.io.File;
import java.util.List;
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

    public EditorController(EditorView editorView, App app) {
        this.view = editorView;
        this.app = app;

        this.accountsController = new MultiAccountController(view.accountsView(), app.getAccountService());
        this.budgetController = new BudgetController(view.budgetView(), app);

        // File menu - event handlers
        view.menuItemImportCsvs().setOnAction((e) -> onImportCsvs(view.getScene().getWindow()));
        view.menuItemSave().setOnAction(this::onSave);

        // View menu - event handlers
        view.menuItemAccounts().setOnAction((e) -> view.showAccountsView());
        view.menuItemBudget().setOnAction((e) -> view.showBudgetView());

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
        app.getAccountService().importFromFiles(files); // normalizes, applies rules and merges with existing accounts

        // check integrity 
        // reload table
        accountsController.reload();

        // show import finished
    }

    private void onSave(ActionEvent e) {
        // save transactions to xlsx and db 
        accountsController.saveToDomain();
        
        app.getAccountService().save();
        app.getAnalyticsExportService().exportAccounts(app.getAccountService().getAccounts());

        // recalculate and save budget
        app.getBudgetService().recalculateAndSave();

        // save to db
        app.getAnalyticsExportService().exportBudget(app.getBudgetService().getBudget());

        // show save finished
    }
}
