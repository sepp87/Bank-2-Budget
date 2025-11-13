package bank2budget.ui;

import bank2budget.App;
import bank2budget.adapters.reader.TransactionReaderForXlsxDone;
import bank2budget.core.Account;
import javafx.application.Application;

/**
 *
 * @author joostmeulenkamp
 */
public class UiAppRunner {

    private final App app;

    public UiAppRunner(App app) {
        this.app = app;
    }

    public void run() {
        TransactionReaderForXlsxDone oldXlsxTransactions = app.getTransactionReaderForXlsxDone().read();
        Account.addTransactionsToAccounts(oldXlsxTransactions.getAsList(), true);

        UiApp.setApp(app);
        Application.launch(UiApp.class);
    }
}
