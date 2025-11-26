package bank2budget.adapters.reader;

import bank2budget.core.Account;
import bank2budget.ports.AccountImporterPort;
import bank2budget.core.CashTransaction;
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

    private final TransactionReaderForCsv csvReader;

    public AccountImporter(TransactionReaderForCsv csvReader) {
        this.csvReader = csvReader;
    }

    @Override
    public List<Account> importAccounts() {
        List<Account> result = new ArrayList<>();
        Map<String, List<CashTransaction>> transactionsPerFile = csvReader.getPerFile();
        for (List<CashTransaction> transactions : transactionsPerFile.values()) {
            List<Account> accounts = groupTransactionsByAccount(transactions);
            result.addAll(accounts);
        }
        return result;
    }

    private List<Account> groupTransactionsByAccount(List<CashTransaction> transactions) {
        Map<String, List<CashTransaction>> temporary = new TreeMap<>();
        for (CashTransaction transaction : transactions) {
            String accountNumber = transaction.getAccountNumber();
            temporary.computeIfAbsent(accountNumber, k -> new ArrayList<>()).add(transaction);
        }
        
        List<Account> result = new ArrayList<>();
        for(Entry<String, List<CashTransaction>> entry : temporary.entrySet()) {
            Account account = new Account(entry.getKey(), entry.getValue());
            result.add(account);
        }
        return result;
    }

}
