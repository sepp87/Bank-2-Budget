package io.ost.finance;

import java.io.File;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

/**
 *
 * @author joost
 */
@Command(name = "Bank-2-Budget", mixinStandardHelpOptions = true, version = "Bank-2-Budget 1.0",
        description = "Cleans up your bank statements and optionally adds them to your budget")
public class Config implements Runnable {

    private static Config config;

    private Config() {
    }

    public static Config get() {
        if (config == null) {
            config = new Config();
        }
        return config;
    }
//
    @Spec
    CommandSpec spec; // Inject CommandSpec to manually print help

    @Override
    public void run() {
        spec.commandLine().usage(System.out);
        System.out.println();
    }

//    @Option(names = {"-h", "--help"}, usageHelp = true, description = "Print help")
//    private boolean help;

    @Option(names = {"-c", "--clear-todo"}, description = "Clear the todo folder after processing it.")
    private boolean clearTodo = false;

    @Option(names = {"-d", "--decimal-separator"}, description = "Decimal separator options: DOT or COMMA. If not specified the system's locale default is used.")
    private final DecimalSeparator decimalSepator = DecimalSeparator.LOCALE;

    @Option(names = {"-m", "--mode"}, defaultValue = "CSV", description = "Processing mode options: CSV, XLSX and BUDGET. The app is run in CSV mode by default, which writes all bank statements to a comma separated files. XLSX mode writes all bank statements to one Excel file, with each sheet representing a single account. Budget mode summarizes all transactions into categories defined by which you labeled it.")
    private final Mode mode = Mode.CSV;

    @Option(names = {"-s", "--sqlite"}, description = "Use SQLite database")
    private boolean sqlite;

    @Parameters
    private static String[] paths = {};
    private static ArrayList<File> csvFiles;

    public static boolean isClearTodo() {
        return Config.get().clearTodo;
    }
    
    public static boolean hasSqlite() {
        return Config.get().sqlite;
    }

    public static char getDecimalSeperator() {
        switch (Config.get().decimalSepator) {
            case DOT:
                return '.';
            case COMMA:
                return ',';
            default:
                return new DecimalFormatSymbols(Locale.getDefault()).getDecimalSeparator();
        }
    }

    public static Mode getMode() {
        return Config.get().mode;
    }

    public static ArrayList<File> getCsvFiles() {
        if (csvFiles == null) {
            csvFiles = getCsvFilesFromPaths();
        }
        return csvFiles;
    }

    private static ArrayList<File> getCsvFilesFromPaths() {
        ArrayList<File> result = new ArrayList<>();
        for (String path : Config.get().paths) {
            File file = new File(path);
            if (file.exists() && file.getName().toLowerCase().endsWith(".csv")) {
                result.add(file);

            } else {
                Logger.getLogger(Config.class.getName()).log(Level.INFO, "{0} does not exist or is not a CSV file. File will be skipped.", file.getPath());

            }
        }
        return result;
    }

    public enum DecimalSeparator {
        DOT, COMMA, LOCALE
    }

    public enum Mode {
        CSV, XLSX, BUDGET
    }

}
