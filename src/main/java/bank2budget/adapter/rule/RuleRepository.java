package bank2budget.adapter.rule;

import bank2budget.core.rule.RuleConfig;
import bank2budget.ports.RuleRepositoryPort;
import java.util.List;

/**
 *
 * @author joostmeulenkamp
 */
public class RuleRepository implements RuleRepositoryPort {
    
    private final RuleReader reader;
    private final RuleWriter writer;
    
    public RuleRepository(RuleReader reader, RuleWriter writer) {
        this.reader = reader;
        this.writer = writer;
    }
    
    public List<RuleConfig> load() {
        return reader.read();
    }
    
    public void save(List<RuleConfig> rules) {
        writer.write(rules);
    }
}
