package bank2budget.core.rule;

/**
 *
 * @author joostmeulenkamp
 */
public record RuleConfig(
        String checkField,
        String checkValue,
        String resultField,
        String resultValue) {
}
