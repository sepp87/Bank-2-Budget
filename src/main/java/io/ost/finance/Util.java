package io.ost.finance;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import static io.ost.finance.TransactionManager.getTransactionManager;

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
        String path = any.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        return path.endsWith(".jar") ? path.substring(0, path.lastIndexOf('/') + 1) : fallbackPath;
    }

    public static boolean isMyAccountNumber(String accountNumber) {
        if (accountNumber == null) {
            return false;
        }
        return getTransactionManager().myAccounts.containsKey(accountNumber);
    }

    public static boolean isMyAccountName(String accountName) {
        if (accountName == null) {
            return false;
        }
        return getTransactionManager().myAccounts.containsValue(accountName);
    }

    public static String getMyAccountNumberFrom(String accountName) {
        for (Map.Entry<Object, Object> entry : getTransactionManager().myAccounts.entrySet()) {
            String myAccountName = (String) entry.getValue();
            if (myAccountName.equals(accountName)) {
                String myAccountNumber = (String) entry.getKey();
                return myAccountNumber;
            }
        }
        return null;
    }


}
