package bank2budget.core;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 *
 * @author joostmeulenkamp
 */
public class Config {

    private static final Logger LOGGER = Logger.getLogger(Config.class.getName());

    private final Map<String, String> myAccounts;
    private final Map<String, String> otherAccounts;
    private final List<String> excludePnlCategories;

    public Config(Map<String, String> myAccounts, Map<String, String> otherAccounts, List<String> excludePnlCategories) {
        this.myAccounts = myAccounts;
        this.otherAccounts = otherAccounts;
        this.excludePnlCategories = excludePnlCategories;
    }

    public Map<String, String> myAccounts() {
        return Map.copyOf(myAccounts);
    }

    public Map<String, String> otherAccounts() {
        return Map.copyOf(otherAccounts);
    }

    public List<String> excludePnlCategories() {
        return List.copyOf(excludePnlCategories);
    }
    
}
