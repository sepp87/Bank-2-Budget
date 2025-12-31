package bank2budget.ui.transaction;

import bank2budget.app.AccountService;
import bank2budget.core.Account;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import javafx.collections.FXCollections;

/**
 *
 * @author joostmeulenkamp
 */
public class MultiAccountController {

    private final MultiAccountView view;
    private final AccountService accountService;
    private final Map<String, TransactionTableController> transactionTableControllers = new TreeMap<>();

    public MultiAccountController(MultiAccountView accountsView, AccountService accountService) {
        this.view = accountsView;
        this.accountService = accountService;

        accountService.setOnAccountsUpdated(this::reload);
        
        load();
    }

    private void load() {
        Collection<Account> accounts = accountService.getAccounts();
        for (Account account : accounts) {
            addAccountView(account);
        }
    }

    public void commitChanges() {
        for (var controller : transactionTableControllers.values()) {
            accountService.updateAccounts(controller.transactions().stream().map(EditableCashTransaction::toDomain).toList());
        }
    }

    private void addAccountView(Account account) {
        String accountNumber = account.getAccountNumber();
        var transactions = FXCollections.observableArrayList(account.transactionsAscending().stream().map(EditableCashTransaction::new).toList());
        TransactionTableView transactionTableView = new TransactionTableView();
        TransactionTableController transactionTableController = new TransactionTableController(transactionTableView);
        transactionTableController.load(transactions);
        transactionTableControllers.put(accountNumber, transactionTableController);
        view.addTab(accountNumber, transactionTableView);
    }

    private void reload() {
        Collection<Account> accounts = accountService.getAccounts();
        for (Account account : accounts) {
            String accountNumber = account.getAccountNumber();
            if (transactionTableControllers.containsKey(accountNumber)) {
                TransactionTableController transactionTableController = transactionTableControllers.get(accountNumber);
                var transactions = FXCollections.observableArrayList(account.transactionsAscending().stream().map(EditableCashTransaction::new).toList());
                transactionTableController.load(transactions);
            } else {
                addAccountView(account);
            }
        }
    }

}
