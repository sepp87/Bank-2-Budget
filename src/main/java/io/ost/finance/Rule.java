package io.ost.finance;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Rukes are loaded from processing-rules.json (see resources) and defined by
 * the user. Each Rule consist of a comparison and an outcome. Each statement
 * inside the comparison between the CashTransaction and Rule must yield true
 * for the outcome to be applied. When the outcome is applied, it sets its
 * values to the original CashTransaction.
 *
 * Each rule can overwrite the outcome done by the rules before it. Rules can
 * work compounding. Meaning a Rule's outcome can yield the commparison of its
 * consecutive Rules to be true, where it would not without it.
 *
 * INFO for Strings the comparison search if the property value contains the
 * comparison value.
 *
 * @author joost
 */
public class Rule {

    private CashTransaction compareObject;
    private CashTransaction outcomeObject;
    private final Set<String> compareStatements;
    private final Set<String> outcomeStatements;

    public Rule() {
        this.compareStatements = new HashSet<>();
        this.outcomeStatements = new HashSet<>();
    }

    public void addStatement(String property, StatementType type) {
        if (type == StatementType.COMPARE) {
            compareStatements.add(property);
        } else if (type == StatementType.OUTCOME) {
            outcomeStatements.add(property);
        }
    }

    public void process(CashTransaction cashTransaction) {
        if (isApplicableTo(cashTransaction)) {
            setOutcomeTo(cashTransaction);
        }
    }

    private boolean isApplicableTo(CashTransaction cashTransaction) {
        for (String property : compareStatements) {
            try {
                Field propertyField = CashTransaction.class.getField(property);
                Object propertyValue = propertyField.get(cashTransaction);
                Object compareValue = propertyField.get(compareObject);
                if (propertyValue == null) {
                    return compareValue == null;
                }
                if (propertyValue instanceof String && compareValue != null) {
                    String propertyString = (String) propertyValue;
                    String compareString = (String) compareValue;
                    return propertyString.toLowerCase().contains(compareString.toLowerCase());
                } else {
                    return propertyValue.equals(compareValue);
                }

            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
                Logger.getLogger(Rule.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
        }
        return true;
    }

    private void setOutcomeTo(CashTransaction cashTransaction) {
        for (String property : outcomeStatements) {
            try {
                Field propertyField = CashTransaction.class.getField(property);
                Object outcomeValue = propertyField.get(outcomeObject);
                propertyField.set(cashTransaction, outcomeValue);
            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
                Logger.getLogger(Rule.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void setCompareObject(CashTransaction compareObject) {
        this.compareObject = compareObject;
    }

    public void setOutcomeObject(CashTransaction outcomeObject) {
        this.outcomeObject = outcomeObject;
    }

    public enum StatementType {
        COMPARE,
        OUTCOME,
    }
}
