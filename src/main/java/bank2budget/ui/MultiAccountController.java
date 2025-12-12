package bank2budget.ui;

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
    private final Map<String, AccountController> accountControllers = new TreeMap<>();

    public MultiAccountController(MultiAccountView accountsView, AccountService accountService) {
        this.view = accountsView;
        this.accountService = accountService;

        load();
    }

    private void load() {
        Collection<Account> accounts = accountService.getAccounts();
        for (Account account : accounts) {
            addAccountView(account);
        }
    }
    
    public void saveToDomain() {
        for(var account : accountService.getAccounts()) {
            var number = account.getAccountNumber();
            var transactions = accountControllers.get(number).transactions().stream().map(EditableCashTransaction::toDomain).toList();
            account.replace(transactions);
        }
    }

    private void addAccountView(Account account) {
        String accountNumber = account.getAccountNumber();
        var transactions = FXCollections.observableArrayList(account.transactionsAscending().stream().map(EditableCashTransaction::new).toList());
        AccountView accountView = new AccountView();
        AccountController accountController = new AccountController(accountView, transactions);
        accountControllers.put(accountNumber, accountController);
        view.addTab(accountNumber, accountView);
    }
    
        public void reload() {
        Collection<Account> accounts = accountService.getAccounts();
        for (Account account : accounts) {
            String accountNumber = account.getAccountNumber();
            if (accountControllers.containsKey(accountNumber)) {
                AccountController accountController = accountControllers.get(accountNumber);
                var transactions = FXCollections.observableArrayList(account.transactionsAscending().stream().map(EditableCashTransaction::new).toList());
                accountController.reload(transactions);
            } else {
                addAccountView(account);
            }
        }
    }

}
