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
    private final String charset;

    public ParserConfig(CreditInstitution creditInstitution, File file, char delimiter) {
        this(creditInstitution, file, delimiter, "Cp1252");
    }

    public ParserConfig(CreditInstitution creditInstitution, File file, char delimiter, String charset) {
        this.file = file;
        this.creditInstitution = creditInstitution;
        this.delimiter = delimiter;
        this.charset = charset;
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
    
    public String getCharset(){
        return charset;
    }

}
