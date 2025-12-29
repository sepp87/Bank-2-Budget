package bank2budget.adapter.rule;

import bank2budget.core.Util;
import bank2budget.core.rule.RuleConfig;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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
        List<String> result = new ArrayList<>();
        List<RuleConfig> sorted = rules.stream().sorted(Comparator.comparing(RuleConfig::resultValue)).toList();
        var size = sorted.size();
        for (int i = 0; i < size; i++) {
            var rule = sorted.get(i);
            String category = Util.padWithTabs("category;", 2, false);
            String name = Util.padWithTabs(rule.resultValue() + ";", 3, false);
            String when = Util.padWithTabs("when;", 1, false);
            String field = Util.padWithTabs(rule.checkField() + ";", 3, false);
            String contains = Util.padWithTabs("contains;", 2, false);
            String value = rule.checkValue();
            String entry = category + name + when + field + contains + value;
            result.add(entry);
            
            int next = i + 1 < size ? i + 1 : i;
            boolean isNextSection = !rule.resultValue().equals(sorted.get(next).resultValue());
            if (isNextSection) {
                result.add("");
            }
        }
        
        try {
            Files.write(rulesFile, result, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            Logger.getLogger(RuleWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
