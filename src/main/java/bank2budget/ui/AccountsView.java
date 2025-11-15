package bank2budget.ui;

import bank2budget.core.Account;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import javafx.collections.FXCollections;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

/**
 *
 * @author joostmeulenkamp
 */
public class AccountsView extends TabPane {

    private final Map<String, TransactionsView> transactionsViews = new TreeMap<>();

    public AccountsView() {
        Collection<Account> accounts = Account.getAccounts();
        for (Account account : accounts) {
            addTransactionsView(account);
        }
    }

    private void addTransactionsView(Account account) {
        String accountNumber = account.getAccountNumber();
        TransactionsView transactionsView = new TransactionsView(FXCollections.observableArrayList(account.getAllTransactionsAscending()));
        transactionsViews.put(accountNumber, transactionsView);
        Tab tab = new Tab(account.getAccountNumber());
        tab.setContent(transactionsView);
        this.getTabs().add(tab);
    }

    public void reload() {
        Collection<Account> accounts = Account.getAccounts();
        for (Account account : accounts) {
            String accountNumber = account.getAccountNumber();
            if (transactionsViews.containsKey(accountNumber)) {
                TransactionsView transactionsView = transactionsViews.get(accountNumber);
                transactionsView.reload(account.getAllTransactionsAscending());
            } else {
                addTransactionsView(account);
            }
        }
    }
}
