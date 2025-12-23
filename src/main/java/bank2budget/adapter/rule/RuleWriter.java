package bank2budget.adapter.rule;

import bank2budget.core.rule.RuleConfig;
import java.nio.file.Path;
import java.util.List;

/**
 *
 * @author joostmeulenkamp
 */
public class RuleWriter {

    private final Path rulesFile;

    public RuleWriter(Path rulesFile) {
        this.rulesFile = rulesFile;
    }

    public void write(List<RuleConfig> rules) {
        
    }
    
}
