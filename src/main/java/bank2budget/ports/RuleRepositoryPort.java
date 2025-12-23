package bank2budget.ports;

import bank2budget.core.rule.RuleConfig;
import java.util.List;

/**
 *
 * @author joostmeulenkamp
 */
public interface RuleRepositoryPort {

    List<RuleConfig> load();

    void save(List<RuleConfig> rules);

}
