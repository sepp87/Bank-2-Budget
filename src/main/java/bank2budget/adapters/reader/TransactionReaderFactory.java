package bank2budget.adapters.reader;

import java.io.File;

/**
 *
 * @author joostmeulenkamp
 */
public class TransactionReaderFactory {

    public static TransactionReaderForCsv parse(File file) {
        return new TransactionReaderForCsv(file);
    }
}
