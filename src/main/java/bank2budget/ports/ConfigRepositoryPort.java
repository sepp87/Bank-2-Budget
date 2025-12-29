
package bank2budget.ports;

import bank2budget.core.Config;

/**
 *
 * @author joostmeulenkamp
 */
public interface ConfigRepositoryPort {
    
    Config load();
    
    void save(Config config);
    
}
