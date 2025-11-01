package bank2budget.adapters.reader;

import bank2budget.cli.AppPaths;
import bank2budget.core.Config;

/**
 *
 * @author joostmeulenkamp
 */
public class ConfigReader {

    private final AppPaths paths;
    
    public ConfigReader(AppPaths paths) {
        this.paths = paths;
    }
    
    public Config read() {
        return new Config();
    }
}
