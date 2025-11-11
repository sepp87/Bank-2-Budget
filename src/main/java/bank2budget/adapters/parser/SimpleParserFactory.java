package bank2budget.adapters.parser;

import bank2budget.core.CreditInstitution;
import bank2budget.core.Config;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ParserFactory determines the CreditInstitution the CSV file originates from
 * and creates the corresponding TransactionParser.
 *
 * @author joost
 */
public class SimpleParserFactory {


    public static TransactionParser createTransactionParser(File csvFile) throws Exception {

        String firstLine = openFileAndReadFirstLine(csvFile);
        TransactionParser parser = null;

        // Remove BOM if present
        if (!firstLine.isEmpty() && firstLine.charAt(0) == '\uFEFF') {
            firstLine = firstLine.substring(1);
        }

        if (firstLine.matches("^\"\\d{2}-\\d{2}-\\d{4}\";\"NL\\d{2}ASNB.*")) {
            parser = new AsnBankParser(new ParserConfig(CreditInstitution.ASN_BANK, csvFile, ';'));

        } else if (firstLine.matches("^\\d{2}-\\d{2}-\\d{4},NL\\d{2}ASNB.*")) {
            parser = new AsnBankParser(new ParserConfig(CreditInstitution.ASN_BANK, csvFile, ','));

        } else if (firstLine.startsWith(";")) {
            parser = new ComdirectParser(new ParserConfig(CreditInstitution.COMDIRECT, csvFile, ';'));

        } else if (firstLine.startsWith("\"Kontonummer:\";\"DE")) {
            parser = new DkbParser(new ParserConfig(CreditInstitution.DKB, csvFile, ';'));

        } else if (firstLine.startsWith("\"Girokonto\";\"DE")) {
            parser = new DkbParser2024(new ParserConfig(CreditInstitution.DKB, csvFile, ';', "UTF-8"));

        } else if (firstLine.startsWith("\"Tagesgeld\";\"DE")) {
            parser = new DkbParser2024(new ParserConfig(CreditInstitution.DKB, csvFile, ';', "UTF-8"));

        } else if (firstLine.startsWith("Girokonto;Valuta;BIC / BLZ;IBAN / Kontonummer;Buchungsinformationen;")) {
            parser = new FlatexParser(new ParserConfig(CreditInstitution.FLATEX, csvFile, ';'));

        } else if (firstLine.startsWith("\"GLS Bank\"")) {
            parser = new GlsParser2019(new ParserConfig(CreditInstitution.GLS, csvFile, ';'));

        } else if (firstLine.startsWith("Bezeichnung Auftragskonto;IBAN Auftragskonto;BIC Auftragskonto;Bankname Auftragskonto;Buchungstag;Valutadatum;Name Zahlungsbeteiligter;IBAN Zahlungsbeteiligter;BIC (SWIFT-Code) Zahlungsbeteiligter;Buchungstext;Verwendungszweck;Betrag;Waehrung;Saldo nach Buchung;Bemerkung;Kategorie;Steuerrelevant;Glaeubiger ID;Mandatsreferenz")) {
            parser = new GlsParser2022(new ParserConfig(CreditInstitution.GLS, csvFile, ';', "UTF-8"));

        } else if (firstLine.startsWith("\"GRENKE BANK AG\"")) {
            parser = new GrenkeBankParser(new ParserConfig(CreditInstitution.GRENKE_BANK, csvFile, ';'));

        } else if (firstLine.startsWith("Umsatzanzeige;Datei erstellt am:")) {
            parser = new IngDiBaParser(new ParserConfig(CreditInstitution.ING_DIBA, csvFile, ';'));

        } else if (firstLine.startsWith("\"Datum\";\"Naam / Omschrijving\";\"Rekening\";\"Tegenrekening\"")) {
            parser = new IngParser(new ParserConfig(CreditInstitution.ING, csvFile, ';'));

        } else if (firstLine.startsWith("\"Datum\",\"Naam / Omschrijving\",\"Rekening\",\"Tegenrekening\"")) {
            parser = new IngParser(new ParserConfig(CreditInstitution.ING, csvFile, ','));

        } else if (firstLine.startsWith("\"Münchner Bank eG\"")) {
            parser = new MuenchnerBankParser(new ParserConfig(CreditInstitution.MUENCHNER_BANK, csvFile, ';'));

        } else if (firstLine.startsWith("\"IBAN/BBAN\",\"Munt\",")) {
            parser = new RabobankParser(new ParserConfig(CreditInstitution.RABOBANK, csvFile, ','));

        } else if (firstLine.matches("^\"\\d{2}-\\d{2}-\\d{4}\";\"NL\\d{2}SNSB.*")) {
            parser = new SnsBankParser(new ParserConfig(CreditInstitution.SNS_BANK, csvFile, ';'));

        } else if (firstLine.matches("^\\d{2}-\\d{2}-\\d{4},NL\\d{2}SNSB.*")) {
            parser = new SnsBankParser(new ParserConfig(CreditInstitution.SNS_BANK, csvFile, ','));

        } else if (firstLine.startsWith("Auftragskonto;Buchungstag;Valutadatum;Buchungstext;Verwendungszweck;")) {
            parser = new SparkasseParser(new ParserConfig(CreditInstitution.SPARKASSE, csvFile, ';'));

        } else if (firstLine.startsWith("\"category\",\"amount\",\"transactionNumber\",\"date\",\"accountBalance\"")) {
            parser = new UnifiedCsvParser(new ParserConfig(CreditInstitution.UNKNOWN, csvFile, ','));
        }

        if (parser == null) {
            throw new Exception("ERROR: Unknown bank, please validate if " + csvFile.getName() + " is really a CSV file. Does the file exist? Create transaction parser terminated. First line: " + firstLine);
        }
        System.out.println("\nIdentified " + parser.getConfig().getCreditInstitution().toString() + " from file \"" + csvFile.getName() + "\"");

        return parser;
    }

    private static String openFileAndReadFirstLine(File csvFile) {
        String firstLine = "";
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
            firstLine = reader.readLine();
        } catch (IOException ex) {
            Logger.getLogger(SimpleParserFactory.class.getName()).log(Level.SEVERE, null, ex);
        }
        return firstLine;
    }
}
