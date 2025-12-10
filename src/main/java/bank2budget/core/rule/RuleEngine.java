package bank2budget.core.rule;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 *
 * @author joostmeulenkamp
 */
public class RuleEngine<T> {

    private final List<Rule<T>> rules;
    private final List<Rule<T>> systemRules;
    private final List<Rule<T>> userRules;


    public RuleEngine(List<Rule<T>> systemRules, List<Rule<T>> userRules) {
        this.systemRules = systemRules;
        this.userRules = userRules;
        this.rules = Stream.concat(systemRules.stream(), userRules.stream()).toList();
    }

    public List<T> applySystemRules(List<T> transactions) {
        return applyRules(systemRules, transactions);
    }

    public List<T> applyRules(List<T> transactions) {
        return applyRules(rules, transactions);
    }

    private List<T> applyRules(List<Rule<T>> rules, List<T> transactions) {
        List<T> result = new ArrayList<>();
        for (T transaction : transactions) {
            T newTransaction = applyRules(rules, transaction);
            if (newTransaction != null) {
                result.add(newTransaction);
            }
        }
        return result;
    }

    private T applyRules(List<Rule<T>> rules, T transaction) {
        T result = null; // <--- return null if rules did not apply
        for (Rule<T> rule : rules) {
            T newTransaction = rule.apply(transaction);
            if (newTransaction != null) {
                transaction = newTransaction; // <--- update transaction incrementally with successive rules
                result = newTransaction; // <--- return latest transaction, if rules were applied
            }
        }
        return result;
    }

}
