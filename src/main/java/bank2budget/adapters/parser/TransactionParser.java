package bank2budget.adapters.parser;

import bank2budget.Launcher;
import bank2budget.core.Transaction;
import bank2budget.core.Util;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * The abstract class TransactionParser parses the CashTranasactions inside the
 * CSV file. Furthermore it offers several general purpose functions for its
 * subclasses to help with parsing dates, numbers and IBANs
 *
 * Before parsing CashTransactions together with the CSV format it needs to
 * determines which records are actual CashTransactions. Depending on the CSV
 * format also some other key attributes like account name, number and balance
 * need to be determined. These things are specified by subclasses of
 * TransactionParser.
 *
 * TODO this class contains a few general purpose functions, some might me
 * eligible for the separate Utility class
 *
 * @author joost
 */
public abstract class TransactionParser {

    private static final Logger LOGGER = Logger.getLogger(TransactionParser.class.getName());

    protected final ParserConfig parserConfig;

    public TransactionParser(ParserConfig config) {
        this.parserConfig = config;
    }

    public List<Transaction> parse() {
        //ANSI files are  read correctly, but now UTF-8 files are not
        try (CSVParser parser = CSVParser.parse(new InputStreamReader(new FileInputStream(parserConfig.getFile()), parserConfig.getCharset()), getCsvFormat())) { // To read ANSI encoded characters like 'ü' correctly in macOS

            List<Transaction> transactions = parseRecordsWith(parser);
            if (Launcher.LOG_TRANSACTIONS) {
                for (var t : transactions) {
                    System.out.println(t.toString());
                }
            }

            return transactions;
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return Collections.emptyList();
    }

    /**
     *
     * @return the CSVFormat with its title row and delimiter (value separator)
     */
    protected abstract CSVFormat getCsvFormat();

    protected List<Transaction> parseRecordsWith(CSVParser parser) throws IOException {
        List<RawCashTransaction> rawTransactions = new ArrayList<>();
        List<CSVRecord> records = parser.getRecords();
        List<CSVRecord> transactionRecords = getTransactionRecordsFrom(records);
        for (var record : transactionRecords) {
            try {
                RawCashTransaction raw = parseCashTransactionFromNEW(record);
                if (raw == null) {
                    continue;
                }
                setAccountInstitutionAndFileOrigin(raw);
                rawTransactions.add(raw);
            } catch (ParseException ex) {
                LOGGER.log(Level.WARNING, "Record skipped due to inccorrect values. {0}", ex.getMessage());
            }
        }
        generateTransactionNumberAndDeriveLastOfDay(rawTransactions);
        var transactions = rawTransactions.stream().map(RawCashTransaction::toTransaction).toList();
        return transactions;
    }

    /**
     *
     * @param records
     * @return a sublist which contains only the transaction records. All
     * records without its title row, header- and footer data.
     *
     * Transaction records must be sorted date ascending to ensure the
     * transaction number uniqueness requirement. For more explanation see
     * method generateTransactionNumber()
     */
    protected abstract List<CSVRecord> getTransactionRecordsFrom(List<CSVRecord> records);

    /**
     * Parse all data from the CSV record to a CashTransaction
     *
     * @param record a single CSV transaction record
     * @return the newly created CashTransaction
     * @throws ParseException
     */
    protected abstract RawCashTransaction parseCashTransactionFromNEW(CSVRecord record) throws ParseException;

    protected void setAccountInstitutionAndFileOrigin(RawCashTransaction transaction) {
        transaction.accountInstitution = (parserConfig.getCreditInstitution());
//        transaction.setFileOrigin(parserConfig.getFile());
    }

    /**
     * Transaction number is based on transaction date and in conjunction its
     * corresponding sequential number. Meaning the 2nd transaction on January
     * 18th 2021 will always yield the number 210118002.
     *
     * @param transactions list sorted by date ascending
     */
    public static void generateTransactionNumberAndDeriveLastOfDay(List<RawCashTransaction> transactions) {
        generateTransactionNumberAndDeriveLastOfDay(transactions, false);
    }

    /**
     * Transaction number is based on transaction date and in conjunction its
     * corresponding sequential number. Meaning the 2nd transaction on January
     * 18th 2021 will always yield the number 210118002.
     *
     * @param transactions list sorted by
     * @param deriveLastOfDayOnly should be set to false if transactionNumber
     * and positionOfDay are not yet generated
     */
    private static void generateTransactionNumberAndDeriveLastOfDay(List<RawCashTransaction> transactions, boolean deriveLastOfDayOnly) {
        Map<String, RawCashTransaction> lastTransactionOfDateMap = new TreeMap<>();

        for (RawCashTransaction transaction : transactions) {
            int uuMMdd = Integer.parseInt(transaction.date.format(DateTimeFormatter.ofPattern("uuMMdd")));
            String key = transaction.accountNumber + transaction.accountName + uuMMdd;
            int positionOfDay = 1;
            if (lastTransactionOfDateMap.containsKey(key)) {
                RawCashTransaction lastTransaction = lastTransactionOfDateMap.get(key);
                if (deriveLastOfDayOnly) {
                    // in case the list is not sorted by date e.g. the transactions spreadsheet was resorted
                    if (transaction.positionOfDay > lastTransaction.positionOfDay) {
                        lastTransactionOfDateMap.put(key, transaction);
                    }
                } else {
                    positionOfDay = lastTransaction.positionOfDay + 1;
                    lastTransactionOfDateMap.put(key, transaction);
                }

            } else {
                lastTransactionOfDateMap.put(key, transaction);
            }

            if (!deriveLastOfDayOnly) {
                int number = uuMMdd * 1000 + positionOfDay;
                transaction.transactionNumber = number;
                transaction.positionOfDay = positionOfDay;
            }

        }
        for (RawCashTransaction transaction : lastTransactionOfDateMap.values()) {
            transaction.lastOfDay = true;
        }
    }

    /**
     * transactions with the highest position of day are get flagged as last of
     * day
     *
     * @param transactions unsorted list of transactions
     */
    public static void deriveLastOfDay(List<RawCashTransaction> transactions) {
        generateTransactionNumberAndDeriveLastOfDay(transactions, true);
    }

//    protected double getDoubleFrom(String numberString) {
//        String noEur = numberString.replace("EUR", "");
//        String noPlus = noEur.replace("+", "");
//        String noPlusAndSpace = noPlus.replace(" ", "");
//        String noPlusSpaceAndDot = noPlusAndSpace.replace(".", "");
//        String cleanNumberString = noPlusSpaceAndDot.replace(",", ".");
//        return Double.parseDouble(cleanNumberString);
//    }
    protected BigDecimal bigDecimalFromString(String numberString) {
        String noEur = numberString.replace("EUR", "");
        String noPlus = noEur.replace("+", "");
        String noPlusAndSpace = noPlus.replace(" ", "");
        String noPlusSpaceAndDot = noPlusAndSpace.replace(".", "");
        String cleanNumberString = noPlusSpaceAndDot.replace(",", ".");
        return new BigDecimal(cleanNumberString);
    }

    protected static LocalDate parseDateFrom(String date) throws ParseException {
        String isoDate = Util.getDateIsoFormattedFrom(date);
        return LocalDate.parse(isoDate);
    }

    // https://www.sparkonto.org/manuelles-berechnen-der-iban-pruefziffer-sepa/
    protected String getGermanIban(String bankleitzahl, String kontonummer) {
        kontonummer = String.format("%010d", new BigInteger(kontonummer));
        BigInteger numericIban = new BigInteger(bankleitzahl + kontonummer + "131400");
        BigInteger remainder = numericIban.divideAndRemainder(new BigInteger("97"))[1];
        int checksum = 98 - Integer.parseInt(remainder.toString());
        return "DE" + checksum + bankleitzahl + kontonummer;
    }

    public ParserConfig getConfig() {
        return parserConfig;
    }

}
