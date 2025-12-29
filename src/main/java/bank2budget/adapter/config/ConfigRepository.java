package bank2budget.adapter.config;

import bank2budget.core.Config;
import bank2budget.ports.ConfigRepositoryPort;

/**
 *
 * @author joostmeulenkamp
 */
public class ConfigRepository implements ConfigRepositoryPort {

    private final ConfigReader reader;
    private final ConfigWriter writer;

    public ConfigRepository(ConfigReader reader, ConfigWriter writer) {
        this.reader = reader;
        this.writer = writer;
    }

    @Override
    public Config load() {
        return reader.read();
    }

    @Override
    public void save(Config config) {
        writer.write(config);
    }

}
