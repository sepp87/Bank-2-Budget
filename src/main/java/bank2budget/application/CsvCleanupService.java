package bank2budget.application;

import bank2budget.AppPaths;
import bank2budget.adapters.reader.FileUtil;
import bank2budget.adapters.reader.TransactionReaderFactory;
import bank2budget.adapters.writer.TransactionWriterFactory;
import bank2budget.core.CashTransaction;
import bank2budget.core.rule.RuleEngine;
import java.nio.file.Path;
import java.util.List;

/**
 *
 * @author joostmeulenkamp
 */
public class CsvCleanupService {

    private final AppPaths paths;
    private final RuleEngine ruleEngine;
    private final char decimalSeparator;

    public CsvCleanupService(AppPaths paths, RuleEngine ruleEngine, char decimalSeparator) {
        this.paths = paths;
        this.ruleEngine = ruleEngine;
        this.decimalSeparator = decimalSeparator;
    }

    public void cleanTodoDirectory() {
        Path todo = paths.getTodoDirectory();
        List<Path> csvFiles = FileUtil.filterDirectoryByExtension(".csv", todo);

        Path done = paths.getDoneDirectory();

        for (Path csv : csvFiles) {
            String source = csv.getFileName().toString();
            String base = source.substring(0, source.length() - 4);
            Path target = done.resolve(base + " cleaned.csv");

            List<CashTransaction> transactions = TransactionReaderFactory.parse(csv.toFile()).getTransactions();
            
            ruleEngine.overwriteAccountNames(transactions);
            ruleEngine.determineInternalTransactions(transactions);
            ruleEngine.applyRules(transactions);
            
            TransactionWriterFactory.create(target.toFile(), decimalSeparator).write(transactions);
        }
    }

}
