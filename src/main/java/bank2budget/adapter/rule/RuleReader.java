package bank2budget.adapter.rule;

import bank2budget.core.rule.RuleConfig;
import bank2budget.ports.RuleReaderPort;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author joost
 */
public class RuleReader implements RuleReaderPort {

    private final Path rulesFile;

    public RuleReader(Path rulesFile) {
        this.rulesFile = rulesFile;
    }

    public List<RuleConfig> read() {
        try {
            var lines = Files.readAllLines(rulesFile);
            return rulesFromLines(lines);
        } catch (IOException ex) {
            Logger.getLogger(RuleReader.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Collections.emptyList();
    }

    private List<RuleConfig> rulesFromLines(List<String> lines) {
        List<RuleConfig> result = new ArrayList<>();
        for (String line : lines) {
            try {
                String lower = line.strip().toLowerCase();
                String resultField = "category";
                if (lower.startsWith(resultField)) {
                    String[] parts = line.split(";");
                    String resultValue = parts[1].strip();
                    String checkField = parts[3].strip();
                    String checkValue = parts[5].strip();
                    var rule = new RuleConfig(checkField, checkValue, resultField, resultValue);
                    result.add(rule);
                }
            } catch (Exception e) {
                Logger.getLogger(RuleReader.class.getName()).log(Level.INFO, "Could NOT read rule, proceeding without {0}", line);
            }
        }
        return result;
    }

}
