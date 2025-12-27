package bank2budget.ui.rules;

import bank2budget.core.rule.RuleConfig;

/**
 *
 * @author joostmeulenkamp
 */
public class EditableRuleConfig {

    private RuleConfig domain;

    public EditableRuleConfig(RuleConfig ruleConfig) {
        this.domain = ruleConfig;
    }

    public String resultValue() {
        return domain.resultValue();
    }

    public String when() {
        return "WHEN";
    }

    public String checkField() {
        return domain.checkField();
    }

    public String operator() {
        return "CONTAINS";
    }

    public String checkValue() {
        return domain.checkValue();
    }

    public void setResultValue(String category) {
        domain = domain.withResultValue(category);
    }

    public void setCheckField(String field) {
        domain = domain.withCheckField(field);
    }

    public void setCheckValue(String value) {
        domain = domain.withCheckValue(value);
    }

}
