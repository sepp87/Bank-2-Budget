package bank2budget.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.io.FileFilter;
import static bank2budget.cli.Launcher.get;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author joost
 */
public class Util {

    public static boolean compareMoney(double a, double b) {
        return compareDoubles(a, b, 2);
    }

    public static boolean compareDoubles(double a, double b, int precision) {
        return (a - b) < Math.pow(0.1, precision);
    }

    public static String readFileAsString(File file) throws IOException {
        String allLines = "";
        try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                allLines += line;
            }
        }
        return allLines;
    }

    /**
     *
     * @param any
     * @param fallbackPath
     * @return the app root directory if any object is inside a .jar file
     */
    public static String getAppRootDirectory(Object any, String fallbackPath) {
        try {
            URI uri = any.getClass().getProtectionDomain().getCodeSource().getLocation().toURI();
            String path = new File(uri).getAbsolutePath();
            String targetSeperatorCharClasses = "target" + File.separatorChar + "classes";
            if (path.endsWith(targetSeperatorCharClasses)) {
                fallbackPath = path.substring(0, path.length() - targetSeperatorCharClasses.length()) + fallbackPath + File.separatorChar;
            }
            return path.endsWith(".jar") ? path.substring(0, path.lastIndexOf(File.separatorChar) + 1) : fallbackPath;
        } catch (URISyntaxException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
        }
        return fallbackPath;
    }

    public static boolean isMyAccountNumber(String accountNumber) {
        if (accountNumber == null) {
            return false;
        }
        return get().myAccounts.containsKey(accountNumber);
    }

    public static boolean isMyAccountName(String accountName) {
        if (accountName == null) {
            return false;
        }
        return get().myAccounts.containsValue(accountName);
    }

    public static String getMyAccountNumberFrom(String accountName) {
        for (Map.Entry<Object, Object> entry : get().myAccounts.entrySet()) {
            String myAccountName = (String) entry.getValue();
            if (myAccountName.equals(accountName)) {
                String myAccountNumber = (String) entry.getKey();
                return myAccountNumber;
            }
        }
        return null;
    }

    public static File[] getFilesByExtensionFrom(File directory, String extension) {
        return directory.listFiles(getFileExtensionFilter(extension));
    }

    // https://howtodoinjava.com/java/io/java-filefilter-example/
    private static FileFilter getFileExtensionFilter(String extension) {
        FileFilter filter = new FileFilter() {
            //Override accept method
            public boolean accept(File file) {
                //if the file extension is .csv return true, else false
                return file.getName().toLowerCase().endsWith(extension);
            }
        };
        return filter;
    }

    public static String getDateIsoFormattedFrom(String dateString) {
        try {
            String dateStringFormat = getDateFormatFrom(dateString);
            SimpleDateFormat format = new SimpleDateFormat(dateStringFormat);
            Date date = format.parse(dateString);
            return new SimpleDateFormat("yyyy-MM-dd").format(date);
        } catch (ParseException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "1970-01-01";
    }

    public static boolean isDateIsoFormatted(String dateString) {
        return dateString.matches("^\\d{4}-\\d{1,2}-\\d{1,2}$");
    }

    public static String getDateFormatFrom(String dateString) throws ParseException {
        for (String regex : DATE_FORMAT_REGEX.keySet()) {
            if (dateString.matches(regex)) {
                return DATE_FORMAT_REGEX.get(regex);
            }
        }

        throw new ParseException("Date parsing NOT supported for value \"" + dateString + "\".", 0);
    }

    // https://balusc.omnifaces.org/2007/09/dateutil.html
    // SRC https://stackoverflow.com/questions/11310065/how-to-detect-the-given-date-format-using-java
    static final Map<String, String> DATE_FORMAT_REGEX = new HashMap<String, String>() {
        {
            put("^\\d{8}$", "yyyyMMdd");
            put("^\\d{1,2}\\.\\d{1,2}\\.\\d{4}$", "dd.MM.yyyy");
            put("^\\d{4}-\\d{1,2}-\\d{1,2}$", "yyyy-MM-dd");
            put("^\\d{2}-\\d{2}-\\d{4}$", "dd-MM-yyyy");
            put("^\\d{2}\\.\\d{2}\\.\\d{2}$", "dd.MM.yy");
        }
    };

    public static double round(double value) {
        return (double) Math.round(value * 100) / 100;
    }

    public static LocalDate[] findOverlap(List<LocalDate> list1, List<LocalDate> list2) {
        LocalDate oldest1 = findBoundaryDate(list1, false);
        LocalDate oldest2 = findBoundaryDate(list2, false);
        LocalDate newest1 = findBoundaryDate(list1, true);
        LocalDate newest2 = findBoundaryDate(list2, true);

        LocalDate oldestOverlap = oldest1.isAfter(oldest2) ? oldest1 : oldest2;
        LocalDate newestOverlap = newest1.isBefore(newest2) ? newest1 : newest2;

        // Find overlap
        if (newestOverlap.compareTo(oldestOverlap) >= 0) {
            // There is an overlap
            LocalDate[] result = {oldestOverlap, newestOverlap};
            return result;
        }
        // There is no overlap
        return null;
    }

    /**
     *
     * @param list unsorted list of dates
     * @param newest if set to false the method looks for the oldest date in the
     * list
     * @return
     */
    public static LocalDate findBoundaryDate(List<LocalDate> list, boolean newest) {
        LocalDate result = list.getFirst();
        for (LocalDate date : list) {
            if (newest) {
                result = date.isAfter(result) ? date : result;
            } else {
                result = date.isBefore(result) ? date : result;
            }
        }
        return result;
    }

    public static String padWithTabs(Object value, int tabs) {
        // When value is an array, then convert to string
        if (value != null && value.getClass().getComponentType() != null) {
            value = Arrays.toString((Object[]) value);
        }
        // When value is null, set result to "null" otherwise convert value to string
        String result = value == null ? "null" : value.toString();

        // When result is just as long or longer than available space, then shorten the result
        if (result.length() >= tabs * 8) {
            result = result.substring(0, (tabs - 1) * 8 + 7);
        }
        int lengthToPad = tabs * 8 - result.length();
        int padding = (int) Math.floor(lengthToPad / 8);
        padding = lengthToPad % 8 == 0 ? padding - 1 : padding;
        int i = 0;
        while (i <= padding) {
            result = result + "\t";
            i++;
        }
        return result;
    }

}
