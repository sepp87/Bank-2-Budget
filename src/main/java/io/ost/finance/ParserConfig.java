package io.ost.finance;

import java.io.File;

/**
 *
 * @author joost
 */
public class ParserConfig {

    private final CreditInstitution creditInstitution;
    private final File csvFile;
    private final char delimiter;


    public ParserConfig(CreditInstitution creditInstitution, File csvFile, char delimiter) {
        this.csvFile = csvFile;
        this.creditInstitution = creditInstitution;
        this.delimiter = delimiter;
    }

    public CreditInstitution getCreditInstitution() {
        return creditInstitution;
    }

    public File getCsvFile() {
        return csvFile;
    }

    public char getDelimiter() {
        return delimiter;
    }

}
