package io.ost.finance;

import java.io.File;

/**
 *
 * @author joost
 */
public class ParserConfig {

    private final CreditInstitution creditInstitution;
    private final File file;
    private final char delimiter;


    public ParserConfig(CreditInstitution creditInstitution, File file, char delimiter) {
        this.file = file;
        this.creditInstitution = creditInstitution;
        this.delimiter = delimiter;
    }

    public CreditInstitution getCreditInstitution() {
        return creditInstitution;
    }

    public File getFile() {
        return file;
    }

    public char getDelimiter() {
        return delimiter;
    }

}
