package bank2budget.adapters.writer;

import bank2budget.core.Transaction;
import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author joost
 */
public abstract class TransactionWriter {

    public static final String[] HEADER = {
        "category",
        "amount",
        "transactionNumber",
        "positionOfDay",
        "lastOfDay",
        "date",
        "accountBalance",
        "accountInstitution",
        "accountNumber",
        "accountName",
        "contraAccountNumber",
        "contraAccountName",
        "internal",
        "transactionType",
        "description",
        "notes"
    };

    public static Object[] getObjectArrayFrom(Transaction transaction) {
        Object[] values = new Object[HEADER.length];
        try {
            int i = 0;
            for (String column : HEADER) {
                Field propertyField = Transaction.class.getDeclaredField(column);
                propertyField.setAccessible(true);
                Object value = propertyField.get(transaction);
                values[i] = value;
                i++;
            }
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            Logger.getLogger(TransactionWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return values;
    }


}
