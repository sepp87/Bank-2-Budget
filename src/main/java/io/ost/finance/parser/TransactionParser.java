package io.ost.finance.parser;

import io.ost.finance.CashTransaction;
import io.ost.finance.ParserConfig;
import io.ost.finance.Rule;
import io.ost.finance.Util;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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
import static io.ost.finance.App.get;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * The abstract class TransactionParser parses the CashTranasactions inside the
 * CSV file and post processes them according to Rules defined in the config
 * (post-processing.json). Furthermore it offers several general purpose
 * functions for its subclasses to help with parsing dates, numbers and IBANs
 *
 * Before parsing CashTransactions together with the CSV format it needs to
 * determines which records are actual CashTransactions. Depending on the CSV
 * format also some other key attributes like account name, number and balance
 * need to be determined. These things are specified by subclasses of
 * TransactionParser.
 *
 * TODO this class contains a few general purpose functions, some might me
 * eligible for the seperate Utility class
 *
 * @author joost
 */
public abstract class TransactionParser {

    protected final ParserConfig config;

    public TransactionParser(ParserConfig config) {
        this.config = config;
    }

    public List<CashTransaction> parse() {
        //ANSI files are  read correctly, but now UTF-8 files are not
        try (CSVParser parser = CSVParser.parse(new InputStreamReader(new FileInputStream(config.getFile()), config.getCharset()), getCsvFormat())) { // To read ANSI encoded characters like 'ü' correctly in macOS

//        try (CSVParser parser = CSVParser.parse(new InputStreamReader(new FileInputStream(config.getFile()), "Cp1252"), getCsvFormat())) { // To read ANSI encoded characters like 'ü' correctly in macOS
            List<CashTransaction> transactions = parseRecordsWith(parser);
            for (CashTransaction t : transactions) {
                System.out.println(t.toString());
            }
            return transactions;
        } catch (IOException ex) {
            Logger.getLogger(TransactionParser.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Collections.emptyList();
    }

    /**
     *
     * @return the CSVFormat with its title row and delimiter (value separator)
     */
    protected abstract CSVFormat getCsvFormat();

    protected List<CashTransaction> parseRecordsWith(CSVParser parser) throws IOException {
        List<CashTransaction> transactions = new ArrayList<>();
        List<CSVRecord> records = parser.getRecords();
        List<CSVRecord> transactionRecords = getTransactionRecordsFrom(records);
        for (var record : transactionRecords) {
            try {
                CashTransaction transaction = parseCashTransactionFrom(record);
                if (transaction == null) {
                    continue;
                }
                postProcess(transaction);
                transactions.add(transaction);
            } catch (ParseException ex) {
                Logger.getLogger(TransactionParser.class.getName()).log(Level.WARNING, "Record skipped due to inccorrect values. {0}", ex.getMessage());
            }
        }
        generateTransactionNumberAndDeriveLastOfDay(transactions);
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
    protected abstract CashTransaction parseCashTransactionFrom(CSVRecord record) throws ParseException;

    public void postProcess(CashTransaction transaction) {
        transaction.setAccountInstitution(config.getCreditInstitution());
        for (Rule rule : get().rules) {
            rule.process(transaction);
        }

        // TODO Unsure if this method is needed, only Comdirect uses it
        // it sets (and overwrites) my account number based on the account name, which makes sense when an account number is not given in the bank statement (which happens in case of Comdirect)
        // BUT should it overwrite the account number
        deriveAccountNumberFrom(transaction);

        // TODO Unsure if this method is needed, only INGDIBA might use it
        // it sets (and overwrite) contra account number (only of transactions to my own bank accounts) based on the contra account name, which makes sense when the contra account number is not given in the bank statement (which happens in case of Comdirect)
        // BUT should it overwrite the contra account number
        deriveContraAccountNumberFrom(transaction);
    }

    private void deriveAccountNumberFrom(CashTransaction transaction) {
        if (Util.isMyAccountName(transaction.getAccountName())) {
            if (!Util.isMyAccountNumber(transaction.getAccountNumber())) {
                transaction.setAccountNumber(Util.getMyAccountNumberFrom(transaction.getAccountName()));
            }
        }
    }

    private void deriveContraAccountNumberFrom(CashTransaction transaction) {
        if (Util.isMyAccountName(transaction.getContraAccountName())) {
            if (!Util.isMyAccountNumber(transaction.getContraAccountNumber())) {
                transaction.setContraAccountNumber(Util.getMyAccountNumberFrom(transaction.getContraAccountName()));
            }
        }
    }

    /**
     * Transaction number is based on transaction date and in conjunction its
     * corresponding sequential number. Meaning the 2nd transaction on January
     * 18th 2021 will always yield the number 210118002.
     *
     * @param transactions list sorted by date ascending
     */
    public static void generateTransactionNumberAndDeriveLastOfDay(List<CashTransaction> transactions) {
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
    private static void generateTransactionNumberAndDeriveLastOfDay(List<CashTransaction> transactions, boolean deriveLastOfDayOnly) {
        Map<String, CashTransaction> lastTransactionOfDateMap = new TreeMap<>();

        for (CashTransaction transaction : transactions) {
            int uuMMdd = Integer.parseInt(LocalDate.parse(transaction.getDate()).format(DateTimeFormatter.ofPattern("uuMMdd")));
            String key = transaction.getAccountNumber() + transaction.getAccountName() + uuMMdd;
            int positionOfDay = 1;
            if (lastTransactionOfDateMap.containsKey(key)) {
                CashTransaction lastTransaction = lastTransactionOfDateMap.get(key);
                if (deriveLastOfDayOnly) {
                    // in case the list is not sorted by date e.g. the transactions spreadsheet was resorted
                    if (transaction.getPositionOfDay() > lastTransaction.getPositionOfDay()) {
                        lastTransactionOfDateMap.put(key, transaction);
                    }
                } else {
                    positionOfDay = lastTransaction.getPositionOfDay() + 1;
                    lastTransactionOfDateMap.put(key, transaction);
                }

            } else {
                lastTransactionOfDateMap.put(key, transaction);
            }

            if (!deriveLastOfDayOnly) {
                int number = uuMMdd * 1000 + positionOfDay;
                transaction.setTransactionNumber(number);
                transaction.setPositionOfDay(positionOfDay);
            }

        }
        for (CashTransaction transaction : lastTransactionOfDateMap.values()) {
            transaction.setLastOfDay(true);
        }
    }

    /**
     * transactions with the highest position of day are get flagged as last of
     * day
     *
     * @param transactions unsorted list of transactions
     */
    public static void deriveLastOfDay(List<CashTransaction> transactions) {
        generateTransactionNumberAndDeriveLastOfDay(transactions, true);
    }

    protected double getDoubleFrom(String numberString) {
        String noEur = numberString.replace("EUR", "");
        String noPlus = noEur.replace("+", "");
        String noPlusAndSpace = noPlus.replace(" ", "");
        String noPlusSpaceAndDot = noPlusAndSpace.replace(".", "");
        String cleanNumberString = noPlusSpaceAndDot.replace(",", ".");
        return Double.parseDouble(cleanNumberString);
    }

    protected void parseDateFrom(String date, CashTransaction transaction) throws ParseException {
        String isoDate = Util.getDateIsoFormattedFrom(date);
        transaction.setDate(isoDate);
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
        return config;
    }

}
