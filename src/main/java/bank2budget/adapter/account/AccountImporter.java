package bank2budget.adapter.account;

import bank2budget.AppPaths;
import bank2budget.adapter.config.FileUtil;
import bank2budget.core.Account;
import bank2budget.ports.AccountImporterPort;
import bank2budget.core.CashTransaction;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 *
 * @author joostmeulenkamp
 */
public class AccountImporter implements AccountImporterPort {

    private final AppPaths paths;

    public AccountImporter(AppPaths paths) {
        this.paths = paths;
    }

    @Override
    public List<Account> importFromTodo() {
        Path todo = paths.getTodoDirectory();
        List<File> csvFiles = FileUtil.filterDirectoryByExtension(".csv", todo).stream().map(Path::toFile).toList();
        return importFromFiles(csvFiles);
    }

    @Override
    public List<Account> importFromFiles(List<File> files) {
        List<Account> result = new ArrayList<>();
//        System.out.println();
        for (File file : files) {
            var transactions = TransactionReaderFactory.parse(file).getTransactions();
            List<Account> accounts = groupTransactionsByAccount(transactions);
            result.addAll(accounts);
        }
        return result;
    }

    private List<Account> groupTransactionsByAccount(List<CashTransaction> transactions) {
        Map<String, List<CashTransaction>> transactionsByAccounts = new TreeMap<>();
        for (var transaction : transactions) {
            String accountNumber = transaction.accountNumber();
            transactionsByAccounts.computeIfAbsent(accountNumber, k -> new ArrayList<>()).add(transaction);
        }

        List<Account> result = new ArrayList<>();
        for (Entry<String, List<CashTransaction>> entry : transactionsByAccounts.entrySet()) {
            Account account = new Account(entry.getKey(), entry.getValue());
            result.add(account);
        }
        return result;
    }

}
