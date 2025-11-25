package bank2budget.core;

import java.util.List;

/**
 *
 * @author joostmeulenkamp
 */
public interface TransactionParserPort {
    
    List<String> importRows();
    
}
