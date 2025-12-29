package bank2budget.ui.rules;

import bank2budget.core.rule.RuleConfig;

/**
 *
 * @author joostmeulenkamp
 */
public class EditableRuleConfig {

    private RuleConfig initialValue;
    private RuleConfig domain;

    public EditableRuleConfig(RuleConfig ruleConfig) {
        this.initialValue = ruleConfig;
        this.domain = ruleConfig;
    }

    // GETTERS
    public RuleConfig toDomain() {
        return domain;
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

    // SETTERS
    public void setResultValue(String category) {
        domain = domain.withResultValue(category);
    }

    public void setCheckField(String field) {
        domain = domain.withCheckField(field);
    }

    public void setCheckValue(String value) {
        domain = domain.withCheckValue(value);
    }
    
    public boolean isEdited() {
        return initialValue != domain;
    }

}
