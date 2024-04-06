package io.ost.finance.parser;

import io.ost.finance.CashTransaction;
import io.ost.finance.ParserConfig;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

public class MuenchnerBankParser extends TransactionParser {

    private double currentBalance;
    protected String accountNumber;
    protected int startingBalanceRecordOffset = 2;
    protected int firstRecordIndex = 9;

    public MuenchnerBankParser(ParserConfig config) {
        super(config);
    }


    @Override
    public CSVFormat getCsvFormat() {
        String[] header = new String[]{
            "Buchungstag",
            "Valuta",
            "Auftraggeber/Zahlungsempfänger",
            "Empfänger/Zahlungspflichtiger",
            "Konto-Nr.",
            "IBAN",
            "BLZ",
            "BIC",
            "Vorgang/Verwendungszweck",
            "Kundenreferenz",
            "Währung",
            "Umsatz",
            "Soll/Haben"
        };
        return CSVFormat.DEFAULT.withDelimiter(';').withHeader(header);
    }

    @Override
    public List<CSVRecord> getTransactionRecordsFrom(List<CSVRecord> allRecords) { // and set current balance to calculateBalanceAfter(CashTransaction transaction)
        List<CSVRecord> transactionRecords = allRecords.subList(firstRecordIndex, allRecords.size() - startingBalanceRecordOffset);
        Collections.reverse(transactionRecords);
        CSVRecord startingBalanceRecord = allRecords.get(allRecords.size() - startingBalanceRecordOffset);
        currentBalance = getExplicitAmountFrom(startingBalanceRecord);
        accountNumber = getAccountNumberFrom(allRecords);
        return transactionRecords;
    }

    @Override
    public CashTransaction parseCashTransactionFrom(CSVRecord record) throws ParseException {
        CashTransaction transaction = new CashTransaction();
        transaction.setAccountNumber(accountNumber);
        transaction.setContraAccountName(record.get("Empfänger/Zahlungspflichtiger"));
        transaction.setDescription(record.get("Vorgang/Verwendungszweck"));
        transaction.setOriginalRecord(record.toMap().values());
        parseDateFrom(record.get("Buchungstag"), transaction);
        parseDescriptionFrom(record, transaction);
        parseAmountFrom(record, transaction);
        calculateBalanceAfter(transaction);
        filterContraAccountNumberFromDescription(transaction);
        return transaction;
    }

    protected void parseDescriptionFrom(CSVRecord record, CashTransaction transaction) {
        String description = record.get("Vorgang/Verwendungszweck");
        description = description.replaceAll("\\n|\\r", "")
                .replaceAll(" ?(CRED:) ?", " CRED: ")
                .replaceAll("(Summenbeleg)", "Summenbeleg ")
                .replaceAll("(GUTSCHRIFT)", "GUTSCHRIFT ")
                .replaceAll("(ABSCHLUSS) ?", "ABSCHLUSS ")
                .replaceAll("(GEBUEHRENJAHRESGEBÜHR)", "GEBUEHREN JAHRESGEBÜHR ")
                .replaceAll("(IHREGIROCARDKARTENNUMMER)", "IHRE GIROCARD KARTENNUMMER")
                .replaceAll(" ?(NEBENKOSTEN) ?", " NEBENKOSTEN ")
                .replaceAll(" ?(MIETE) ?", " MIETE ")
                .replaceAll(" ?(Datum:) ?", " Datum: ")
                .replaceAll("(Internet-Euro-Überweisung)", " Internet-Euro-Überweisung")
                .replaceAll(" ?(Lastschrift \\(Einzug\\))", " Lastschrift (Einzug)")
                .replaceAll("(IhrEinkauf)", "Ihr Einkauf")
                .replaceAll("(LOHN/GEHALT)", "LOHN/GEHALT ")
                .replaceAll("(Euro-Überweisung) ?", "Euro-Überweisung ")
                .replaceAll("(Kostenübernahme durch dieMünchner)", " Kostenübernahme durch die Münchner")
                .replaceAll("(Überweisungsauftrag)", "Überweisungsauftrag ")
                .replaceAll("(Überweisungsgutschr.)", "Überweisungsgutschr. ")
                .replaceAll("(Abschluss)", "Abschluss ")
                .replaceAll("(Basislastschrift)", "Basislastschrift ")
                .replaceAll("(Zinsen/Kontoführung)", " Zinsen/Kontoführung")
                .replaceAll("(Überweisungs-Gutschrift)", " Überweisungs-Gutschrift")
                .replaceAll("(  )", " ")
                .replaceAll("(CI:)", " CI: ")
                .replaceAll("(MANDAT:)", " MANDAT: ")
                .replaceAll(" ?(TAN:) ?", " TAN: ")
                .replaceAll(" ?(IBAN:) ?", " IBAN: ")
                .replaceAll(" ?(BIC:) ?", " BIC: ");
        transaction.setDescription(description);
    }

    /**
     * Parse amount with explicit transaction type { S:Debit, H:Credit }
     */
    protected void parseAmountFrom(CSVRecord record, CashTransaction transaction) {
        double amount = getExplicitAmountFrom(record);
        transaction.setAmount(amount);
    }

    protected double getExplicitAmountFrom(CSVRecord record) {
        double amount = getDoubleFrom(record.get("Umsatz"));
        double explicitAmount = isDebit(record) ? -amount : amount;
        return explicitAmount;
    }

    protected boolean isDebit(CSVRecord record) {
        return record.get("Soll/Haben").equals("S");
    }

    protected void calculateBalanceAfter(CashTransaction transaction) {
        double newBalance = currentBalance + transaction.getAmount();
        currentBalance = (double) Math.round(newBalance * 100) / 100;
        transaction.setAccountBalance(currentBalance);
    }

    private void filterContraAccountNumberFromDescription(CashTransaction transaction) {
        String description = transaction.getDescription();
        String descriptionWithoutSpacesAndNewLines = description.replaceAll(" |\\n", "");
        if (descriptionWithoutSpacesAndNewLines.contains("IBAN:")) {
            String iban = descriptionWithoutSpacesAndNewLines.replaceAll("^.*(IBAN:)|(BIC:).*$|(Datum:).*$", "");
            transaction.setContraAccountNumber(iban);
        }
    }

    protected String getAccountNumberFrom(List<CSVRecord> allRecords) {
        String blz = allRecords.get(2).get(1);
        String konto = allRecords.get(3).get(1);
        return getGermanIban(blz, konto);
    }

}
