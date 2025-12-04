package bank2budget.core;

import bank2budget.core.budget.BudgetTemplate;
import bank2budget.core.rule.RuleConfig;
import java.util.Collection;
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
    private List<RuleConfig> ruleConfigs;
    private Map<String, Double> budgetCategories;
    private int firstOfMonth = 1;
    private BudgetTemplate budgetTemplate;

    public Config(Map<String, String> myAccounts, Map<String, String> otherAccounts, List<RuleConfig> ruleConfigs, Map<String, Double> budgetCategories, int firstOfMonth, BudgetTemplate budgetTemplate) {
        this.myAccounts = myAccounts;
        this.otherAccounts = otherAccounts;
        this.ruleConfigs = ruleConfigs;
        this.budgetCategories = budgetCategories;
        this.firstOfMonth = validateFirstOfMonth(firstOfMonth);
        this.budgetTemplate = budgetTemplate;
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

    public List<RuleConfig> ruleConfigs() {
        return ruleConfigs;
    }

    public Map<String, Double> budgetCategories() {
        return budgetCategories;
    }

    public int firstOfMonth() {
        return firstOfMonth;
    }
    
    public BudgetTemplate budgetTemplate() {
        return budgetTemplate;
    }
}
