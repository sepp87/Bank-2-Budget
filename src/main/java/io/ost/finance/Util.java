package io.ost.finance;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.io.FileFilter;
import static io.ost.finance.App.get;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Util {

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

    public static Date getDateFrom(String dateString) {
        try {
            String dateStringFormat = getDateFormatFrom(dateString);
            SimpleDateFormat format = new SimpleDateFormat(dateStringFormat);
            return format.parse(dateString);
        } catch (ParseException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new Date();
    }

    public static String getDateIsoFormattedFrom(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
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

    public static long getDateUnixFormattedFrom(String isoDate) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            Date date = format.parse(isoDate);
            return date.getTime() / 1000;
        } catch (ParseException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0;
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
        }
    };

    public static double round(double value) {
        return (double) Math.round(value * 100) / 100;
    }


}
