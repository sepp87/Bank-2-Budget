package bank2budget.application;

import bank2budget.adapters.reader.TransactionReaderForCsv;
import bank2budget.adapters.writer.TransactionWriterForCsv;
import bank2budget.core.CashTransaction;
import bank2budget.core.RuleEngine;
import java.util.List;

/**
 *
 * @author joostmeulenkamp
 */
public class CsvCleanupService {

    private final TransactionReaderForCsv csvReader;
    private final TransactionWriterForCsv csvWriter;
    private final RuleEngine ruleEngine;

    public CsvCleanupService(TransactionReaderForCsv csvReader, TransactionWriterForCsv csvWriter, RuleEngine ruleEngine) {
        this.csvReader = csvReader;
        this.csvWriter = csvWriter;
        this.ruleEngine = ruleEngine;
    }
    
    public void cleanTodoDirectory() {
       List<CashTransaction> transactions = csvReader.getAllTransactions();
       
       ruleEngine.overwriteAccountNames(transactions);
       ruleEngine.determineInternalTransactions(transactions);
       ruleEngine.applyRules(transactions);
       
       csvWriter.write(csvReader.getPerFile());
    }
    
    

}
