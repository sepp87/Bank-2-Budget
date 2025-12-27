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

    public RuleConfig withCheckField(String updated) {
        return new RuleConfig(updated, checkValue, resultField, resultValue);
    }

    public RuleConfig withCheckValue(String updated) {
        return new RuleConfig(checkField, updated, resultField, resultValue);
    }

    public RuleConfig withResultValue(String updated) {
        return new RuleConfig(checkField, checkValue, resultField, updated);
    }

}
