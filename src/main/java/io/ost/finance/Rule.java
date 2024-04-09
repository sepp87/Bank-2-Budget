package io.ost.finance;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
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
    private final Map<String, String> compareOperators;
    private final Set<String> compareStatements;
    private final Set<String> outcomeStatements;

    public Rule() {
        this.compareOperators = new TreeMap<>();
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

    public void addOperator(String property, String operator) {
        compareOperators.put(property, operator);
    }

    public void process(CashTransaction cashTransaction) {
//        System.out.println();
        if (isApplicableTo(cashTransaction)) {
            setOutcomeTo(cashTransaction);
        }
    }

    private boolean isApplicableTo(CashTransaction cashTransaction) {
        for (String property : compareStatements) {
            String operator = compareOperators.containsKey(property) ? compareOperators.get(property) : "==";
            boolean result = true;
            try {
                Field propertyField = CashTransaction.class.getDeclaredField(property);
                propertyField.setAccessible(true);
                Type type = propertyField.getAnnotatedType().getType();

                Object propertyValue = propertyField.get(cashTransaction);
                Object compareValue = propertyField.get(compareObject);

                if (operator.equals("==")) {
                    result = isEqual(propertyValue, compareValue);
                } else if (operator.equals("!=")) {
                    result = !isEqual(propertyValue, compareValue);
                } else if (operator.equals(">")) {
                    result = isLarger(propertyValue, compareValue, type);
                } else if (operator.equals("<")) {
                    result = isLarger(compareValue, propertyValue, type);
                } else if (operator.equals("<=")) {
                    result = !isLarger(propertyValue, compareValue, type);
                } else if (operator.equals(">=")) {
                    result = !isLarger(compareValue, propertyValue, type);
                }

//                System.out.println(result + "\t" + property);

                if (result == false) {
                    return false;
                }

            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
                Logger.getLogger(Rule.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
        }
//        System.out.println(Arrays.toString(cashTransaction.toRecord()));
        return true;
    }

    private boolean isEqual(Object propertyValue, Object compareValue) {
        boolean result = true;
        if (propertyValue == null) {
            result = compareValue == null;

        } else if (propertyValue instanceof String && compareValue != null) {
            String propertyString = (String) propertyValue;
            String compareString = (String) compareValue;
            result = propertyString.toLowerCase().contains(compareString.toLowerCase());

        } else {
            result = propertyValue.equals(compareValue);
        }
        return result;
    }

    // testing is smaller, can be done by switching the variables when calling the method "isLarger"
    // 1    <   2 true              2 > 1     
    // 2    <   2 false            2 > 2
    // 3    <   2 false            2 > 3
    //
    // testing is smaller or the same, can be done by negating the outcome of the method "isLarger"
    // testing is bigger or the same, can be done by switching the variables and negating the outcome
    // 1    >   2 false        true     2   >=  1       1   <=  2
    // 2    >   2 false        true     2   >=  2       2   <=  2
    // 3    >   2 true          false   2   >=  3       3   <=  2
    private boolean isLarger(Object propertyValue, Object compareValue, Type type) {
        if (propertyValue == null || compareValue == null) {
            return false;
        }

        if (type.equals(Double.class) || type.equals(double.class)) {
            return (double) propertyValue > (double) compareValue;

        } else if (type.equals(Integer.class) || type.equals(int.class)) {
            return (int) propertyValue > (int) compareValue;

        } else if (type.equals(String.class) && Util.isDateIsoFormatted((String) propertyValue)) {
            LocalDate propertyDate = LocalDate.parse((String) propertyValue);
            LocalDate compareDate = LocalDate.parse((String) compareValue);
            return propertyDate.isAfter(compareDate);
        }
        return false;
    }

    private void setOutcomeTo(CashTransaction cashTransaction) {
        for (String property : outcomeStatements) {
            try {
                Field propertyField = CashTransaction.class.getDeclaredField(property);
                propertyField.setAccessible(true);
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
