package bank2budget.adapters.writer;

import java.io.File;

/**
 *
 * @author joostmeulenkamp
 */
public class TransactionWriterFactory {

    public static TransactionWriterForCsv create(File target, char decimalSeparator) {
        return new TransactionWriterForCsv(target, decimalSeparator);
    }
}
