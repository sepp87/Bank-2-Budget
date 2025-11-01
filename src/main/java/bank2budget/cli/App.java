package bank2budget.cli;

import bank2budget.adapters.db.BudgetDatabase;
import bank2budget.adapters.reader.BudgetReaderForXlsx;
import bank2budget.adapters.reader.TransactionReaderForCsvTodo;
import bank2budget.adapters.reader.TransactionReaderForXlsxDone;
import bank2budget.adapters.writer.BudgetWriterForXlsx;
import bank2budget.adapters.writer.TransactionWriterForCsv;
import bank2budget.adapters.writer.TransactionWriterForXlsx;

/**
 *
 * @author joostmeulenkamp
 */
public class App {

    private final AppPaths paths;
    private final TransactionReaderForCsvTodo transactionReaderForCsvTodo;
    private final TransactionReaderForXlsxDone transactionReaderForXlsxDone;
    private final TransactionWriterForCsv transactionWriterForCsv;
    private final TransactionWriterForXlsx transactionWriterForXlsx;
    private final BudgetReaderForXlsx budgetReaderForXlsx;
    private final BudgetWriterForXlsx budgetWriterForXlsx;
    private final BudgetDatabase budgetDatabase;

    public App(AppPaths paths) {
        this.paths = paths;
        this.transactionReaderForCsvTodo = new TransactionReaderForCsvTodo();
        this.transactionReaderForXlsxDone = new TransactionReaderForXlsxDone();
        this.transactionWriterForCsv = new TransactionWriterForCsv();
        this.transactionWriterForXlsx = new TransactionWriterForXlsx();
        this.budgetReaderForXlsx = new BudgetReaderForXlsx();
        this.budgetWriterForXlsx = new BudgetWriterForXlsx();
        this.budgetDatabase = new BudgetDatabase(paths.getDatabaseFile().toString());
    }

    public TransactionReaderForCsvTodo getTransactionReaderForCsvTodo() {
        return transactionReaderForCsvTodo;
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

}
