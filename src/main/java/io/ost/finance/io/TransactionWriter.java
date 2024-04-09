package io.ost.finance.io;

import io.ost.finance.CashTransaction;
import io.ost.finance.Config;
import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author joost
 */
public abstract class TransactionWriter {

    public static final String[] HEADER = {
        "label",
        "amount",
        "transactionNumber",
        "date",
        "accountBalance",
        "accountInstitution",
        "accountNumber",
        "accountName",
        "contraAccountNumber",
        "contraAccountName",
        "internal",
        "transactionType",
        "description"
    };

    public static String[] getStringArrayFrom(CashTransaction transaction) {
        Object[] values = getObjectArrayFrom(transaction);
        String[] stringValues = new String[values.length];
        int i = 0;
        for (Object value : values) {
            stringValues[i] = valueToString(value);
            i++;
        }
        return stringValues;
    }

    public static Object[] getObjectArrayFrom(CashTransaction transaction) {
        Object[] values = new Object[HEADER.length];
        try {
            int i = 0;
            for (String column : HEADER) {
                Field propertyField = CashTransaction.class.getDeclaredField(column);
                propertyField.setAccessible(true);
                Object value = propertyField.get(transaction);
                values[i] = value;
                i++;
            }
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            Logger.getLogger(CashTransaction.class.getName()).log(Level.SEVERE, null, ex);
        }
        return values;
    }

    private static String valueToString(Object value) {
        if (value == null) {
            return null;
        } else if (value instanceof String) {
            return value.toString();
        } else if (value instanceof Number) {
            return (value + "").replace('.', Config.getDecimalSeperator());
        }
        return value.toString();
    }
}
