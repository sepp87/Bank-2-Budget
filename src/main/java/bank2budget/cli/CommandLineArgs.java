package bank2budget.cli;

import java.nio.file.Path;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 *
 * @author joostmeulenkamp
 */
@CommandLine.Command(
        name = "Bank-2-Budget",
        mixinStandardHelpOptions = true,
        version = "Bank-2-Budget 1.0",
        description = "Cleans up your bank statements and optionally adds them to your budget")
public class CommandLineArgs implements Runnable {

    @Option(
            names = {"-c", "--clear-todo"},
            description = "Clear the todo folder after processing."
    )
    private boolean clearTodo;

    @Option(
            names = {"-d", "--decimal-separator"},
            description = "Decimal separator options: DOT, COMMA, or LOCALE (default)."
    )
    private DecimalSeparator decimalSeparator = DecimalSeparator.LOCALE;

    @Option(
            names = {"-m", "--mode"},
            defaultValue = "CSV",
            description = "Processing mode: CSV, XLSX, or BUDGET. Default: CSV."
    )
    private Mode mode;

    @Option(
            names = {"-s", "--sqlite"},
            description = "Use SQLite database."
    )
    private boolean sqlite;

    @Parameters(
            paramLabel = "PATHS",
            description = "Paths to input CSV files or directories.",
            arity = "0..*"
    )
    private List<Path> paths = new ArrayList<>();

    @Override
    public void run() {
        CommandLine.usage(this, System.out);
    }

    // ---- Getters ----
    public boolean shouldClearTodo() {
        return clearTodo;
    }

    public boolean useSqlite() {
        return sqlite;
    }

    public char getDecimalSeparatorChar() {
        return switch (decimalSeparator) {
            case DOT ->
                '.';
            case COMMA ->
                ',';
            default ->
                new DecimalFormatSymbols(Locale.getDefault()).getDecimalSeparator();
        };
    }

    public Mode getMode() {
        return mode;
    }

    public List<Path> getPaths() {
        return List.copyOf(paths);
    }

    // ---- Enums ----
    public enum DecimalSeparator {
        DOT, COMMA, LOCALE
    }

    public enum Mode {
        CSV, XLSX, BUDGET
    }
}
