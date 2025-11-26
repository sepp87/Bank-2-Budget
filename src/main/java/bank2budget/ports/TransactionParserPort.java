package bank2budget.ports;

import java.util.List;

/**
 *
 * @author joostmeulenkamp
 */
public interface TransactionParserPort {
    
    List<String> importRows();
    
}
