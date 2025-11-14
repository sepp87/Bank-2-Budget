package bank2budget;

import bank2budget.adapters.db.BudgetDatabase;
import bank2budget.adapters.parser.SimpleParserFactory;
import bank2budget.adapters.reader.BudgetReaderForXlsx;
import bank2budget.adapters.reader.ConfigReader;
import bank2budget.adapters.reader.TransactionReaderForCsv;
import bank2budget.adapters.reader.TransactionReaderForXlsxDone;
import bank2budget.adapters.writer.BudgetWriterForXlsx;
import bank2budget.adapters.writer.TransactionWriterForCsv;
import bank2budget.adapters.writer.TransactionWriterForXlsx;
import bank2budget.core.Config;
import bank2budget.core.RuleEngine;

/**
 *
 * @author joostmeulenkamp
 */
public class App {

    private final AppPaths paths;
    private final TransactionReaderForXlsxDone transactionReaderForXlsxDone;
    private final TransactionWriterForCsv transactionWriterForCsv;
    private final TransactionWriterForXlsx transactionWriterForXlsx;
    private final BudgetReaderForXlsx budgetReaderForXlsx;
    private final BudgetWriterForXlsx budgetWriterForXlsx;
    private final BudgetDatabase budgetDatabase;
    private final RuleEngine ruleEngine;

    public App(AppPaths paths, char decimalSeparatorChar) {
        this.paths = paths;

        Config config = new ConfigReader(paths).getConfig();

        this.transactionReaderForXlsxDone = new TransactionReaderForXlsxDone(paths.getTransactionsFile());
        this.transactionWriterForCsv = new TransactionWriterForCsv(paths.getDoneDirectory(), decimalSeparatorChar);
        this.transactionWriterForXlsx = new TransactionWriterForXlsx(paths.getTransactionsFile());
        this.budgetReaderForXlsx = new BudgetReaderForXlsx(paths.getBudgetFile());
        this.budgetWriterForXlsx = new BudgetWriterForXlsx(paths.getBudgetFile());
        this.budgetDatabase = new BudgetDatabase(paths.getDatabaseFile().toString());
        this.ruleEngine = new RuleEngine(config.rules(), config.myAccounts(), config.otherAccounts());
    }

    public TransactionReaderForXlsxDone getTransactionReaderForXlsxDone() {
        return transactionReaderForXlsxDone;
    }

    public TransactionWriterForCsv getTransactionWriterForCsv() {
        return transactionWriterForCsv;
    }

    public TransactionWriterForXlsx getTransactionWriterForXlsx() {
        return transactionWriterForXlsx;
    }

    public BudgetReaderForXlsx getBudgetReaderForXlsx() {
        return budgetReaderForXlsx;
    }

    public BudgetWriterForXlsx getBudgetWriterForXlsx() {
        return budgetWriterForXlsx;
    }

    public BudgetDatabase getBudgetDatabase() {
        return budgetDatabase;
    }

    public RuleEngine getRuleEngine() {
        return ruleEngine;
    }
    
}
