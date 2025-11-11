package bank2budget.core;

import java.util.Collection;
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
    private Collection<Rule> rules;
    private Map<String, Double> budgetTemplate;
    private int firstOfMonth = 1;

    public Config(Map<String, String> myAccounts, Map<String, String> otherAccounts, Collection<Rule> rules, Map<String, Double> budgetTemplate, int firstOfMonth) {
        this.myAccounts = myAccounts;
        this.otherAccounts = otherAccounts;
        this.rules = rules;
        this.budgetTemplate = budgetTemplate;
        this.firstOfMonth = validateFirstOfMonth(firstOfMonth);

    }

    private int validateFirstOfMonth(int i) {
        if (i > 0 && i < 29) {
            return i;
        } else {
            LOGGER.log(Level.WARNING, "Specified first of month ({0}) does NOT fall within range 1 > 28, using fallback: 1", i);
        }
        return 1;
    }

    public Map<String, String> myAccounts() {
        return myAccounts;
    }

    public Map<String, String> otherAccounts() {
        return otherAccounts;
    }

    public Collection<Rule> rules() {
        return rules;
    }

    public Map<String, Double> budgetTemplate() {
        return budgetTemplate;
    }

    public int firstOfMonth() {
        return firstOfMonth;
    }
}
