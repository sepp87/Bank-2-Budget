package bank2budget.core;

import bank2budget.core.budget.BudgetTemplate;
import bank2budget.core.rule.RuleConfig;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author joostmeulenkamp
 */
public class Config {

    private static final Logger LOGGER = Logger.getLogger(Config.class.getName());

    private Map<String, String> myAccounts;
    private Map<String, String> otherAccounts;

    public Config(Map<String, String> myAccounts, Map<String, String> otherAccounts) {
        this.myAccounts = myAccounts;
        this.otherAccounts = otherAccounts;
    }

    public Map<String, String> myAccounts() {
        return myAccounts;
    }

    public Map<String, String> otherAccounts() {
        return otherAccounts;
    }

}
