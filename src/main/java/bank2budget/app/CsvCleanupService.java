package bank2budget.app;

import bank2budget.AppPaths;
import bank2budget.adapter.config.FileUtil;
import bank2budget.adapter.account.TransactionReaderFactory;
import bank2budget.adapter.account.TransactionWriterFactory;
import bank2budget.core.CashTransaction;
import bank2budget.core.rule.RuleEngine;
import java.nio.file.Path;
import java.util.List;
import java.util.TreeMap;

/**
 *
 * @author joostmeulenkamp
 */
public class CsvCleanupService {

    private final AppPaths paths;
    private final RuleEngine<CashTransaction> ruleEngine;
    private final char decimalSeparator;

    public CsvCleanupService(AppPaths paths, RuleEngine<CashTransaction> ruleEngine, char decimalSeparator) {
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

            var transactions = TransactionReaderFactory.parse(csv.toFile()).getTransactions();

            TreeMap<Integer, CashTransaction> index = new TreeMap<>();
            transactions.forEach(e -> index.put(e.transactionNumber(), e)); // put all transactions into a map to allow updates

            var updated = ruleEngine.applyRules(transactions);
            updated.forEach(e -> index.put(e.transactionNumber(), e)); // update transactions with new versions

            TransactionWriterFactory.create(target.toFile(), decimalSeparator).write(index.values());
        }
    }

}
