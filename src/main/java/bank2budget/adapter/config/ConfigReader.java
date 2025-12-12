package bank2budget.adapter.config;

import bank2budget.adapter.budget.BudgetTemplateReader;
import bank2budget.AppPaths;
import bank2budget.core.Config;
import bank2budget.core.budget.BudgetTemplate;
import bank2budget.core.rule.RuleConfig;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author joostmeulenkamp
 */
public class ConfigReader {

    private static final Logger LOGGER = Logger.getLogger(ConfigReader.class.getName());

    private final AppPaths paths;
    private final Config config;

    public ConfigReader(AppPaths paths) {
        this.paths = paths;
        Map<String, String> myAccounts = propertiesToMap(readProperties(paths.getMyAccountsFile()));
        Map<String, String> otherAccounts = propertiesToMap(readProperties(paths.getOtherAccountsFile()));
        List<RuleConfig> ruleConfigs = new RuleReaderNew(paths.getProcessingRulesFile().toFile()).read();
        BudgetTemplate budgetTemplate = new BudgetTemplateReader(paths.getBudgetTemplateFile()).read();
        this.config = new Config(myAccounts, otherAccounts, ruleConfigs, budgetTemplate);
    }

    public Config getConfig() {
        return config;
    }

    private Properties readProperties(Path path) {
        Properties properties = new Properties();
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            properties.load(reader);
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, "Could NOT read file, proceeding without {0}", path.getFileName());
        }
        return properties;
    }

    private Map<String, String> propertiesToMap(Properties p) {
        Map<String, String> result = new HashMap<>();
        for (Entry<Object, Object> e : p.entrySet()) {
            result.put(e.getKey().toString(), e.getValue().toString());
        }
        return result;
    }

}
