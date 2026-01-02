
package bank2budget.app;

import bank2budget.core.Config;
import bank2budget.ports.ConfigRepositoryPort;
import java.util.List;

/**
 *
 * @author joostmeulenkamp
 */
public class ConfigService {
    
    private final ConfigRepositoryPort repository;
    private Config config;
    
    public ConfigService ( ConfigRepositoryPort repository) {
        this.repository = repository;
        load();
    }
    
    private void load () {
        config = repository.load();
    }
    
    public List<String> excludePnlCategories() {
        return config.excludePnlCategories();
    }
}
