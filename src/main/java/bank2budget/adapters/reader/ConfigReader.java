package bank2budget.adapters.reader;

import bank2budget.AppPaths;
import bank2budget.Launcher;
import bank2budget.core.Config;
import bank2budget.core.Rule;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
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
        Collection<Rule> rules = new RuleReaderForJson(paths.getProcessingRulesFile().toFile()).read();
        BudgetSettingsReader budgetSettingsReader = new BudgetSettingsReader(paths.getBudgetSettingsFile().toFile());
        budgetSettingsReader.read();
        int firstOfMonth = budgetSettingsReader.getFirstOfMonth();
        Map<String, Double> budgetTemplate = budgetSettingsReader.getBudgetTemplate();
        this.config = new Config(myAccounts, otherAccounts, rules, budgetTemplate, firstOfMonth);
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
