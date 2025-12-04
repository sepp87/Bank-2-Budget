package bank2budget.core.rule;

import bank2budget.core.CashTransaction;
import java.util.List;
import java.util.Map;

/**
 *
 * @author joostmeulenkamp
 */
public class RuleEngine<T> {

    private final List<Rule<T>> rules;
    private final Map<String, String> myAccounts;
    private final Map<String, String> otherAccounts;

    public RuleEngine(List<Rule<T>> rules, Map<String, String> myAccounts, Map<String, String> otherAccounts) {
        this.rules = rules;
        this.myAccounts = myAccounts;
        this.otherAccounts = otherAccounts;
    }

    public void applyRules(List<T> transactions) {
        for (T transaction : transactions) {
            applyRules(transaction);
        }
    }

    public void applyRules(T transaction) {
        for (Rule<T> rule : rules) {
            rule.apply(transaction);
        }
    }

    // overwrite account names
    public void overwriteAccountNames(List<CashTransaction> transactions) {
        for (CashTransaction t : transactions) {

            String accountNumber = t.getAccountNumber();
            String accountName = getAccountNameFrom(accountNumber);
            if (accountName != null) {
                t.setAccountName(accountName);
            }

            String contraAccountNumber = t.getContraAccountNumber();
            String contraAccountName = getAccountNameFrom(contraAccountNumber);
            if (contraAccountName != null) {
                t.setContraAccountName(contraAccountName);
            }
        }
    }

    private String getAccountNameFrom(String accountNumber) {
        if (accountNumber == null) {
            return null;
        }
        String result = myAccounts.get(accountNumber);
        if (result == null) {
            result = otherAccounts.get(accountNumber);
        }
        return result;
    }

    // add missing account numbers
    public void addMissingAccountNumbers(List<CashTransaction> transactions) {
        for (CashTransaction t : transactions) {
            if (t.getAccountNumber() == null) {
                t.setAccountNumber(getAccountNumberFrom(t.getAccountName()));
            }
            if (t.getContraAccountNumber() == null) {
                t.setContraAccountNumber(getAccountNumberFrom(t.getContraAccountName()));
            }
        }
    }

    private String getAccountNumberFrom(String accountName) {
        if (accountName == null) {
            return null;
        }

        String result = getKeyFromValue(accountName, myAccounts);
        if (result == null) {
            result = getKeyFromValue(accountName, otherAccounts);
        }
        return result;
    }

    private static String getKeyFromValue(String value, Map<String, String> properties) {
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            String candidate = entry.getValue();
            if (candidate.equals(value)) {
                String key = entry.getKey();
                return key;
            }
        }
        return null;
    }

    // determine internal transactions
    public void determineInternalTransactions(List<CashTransaction> transactions) {
        for (CashTransaction t : transactions) {
            boolean internal = myAccounts.containsKey(t.getAccountNumber())
                    && myAccounts.containsKey(t.getContraAccountNumber());
            t.setInternal(internal);
        }
    }
}
