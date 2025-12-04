package bank2budget.adapters.parser;

import bank2budget.core.CashTransaction;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

public class MuenchnerBankParser extends TransactionParser {

    private BigDecimal currentBalance;
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

    protected String parseDescriptionFrom(CSVRecord record) {
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
        return description;
    }

    /**
     * Parse amount with explicit transaction type { S:Debit, H:Credit }
     */
    protected BigDecimal parseAmountFrom(CSVRecord record) {
        BigDecimal amount = getExplicitAmountFrom(record);
        return amount;
    }

    protected BigDecimal getExplicitAmountFrom(CSVRecord record) {
        BigDecimal amount = BigDecimal.valueOf(getDoubleFrom(record.get("Umsatz")));
        BigDecimal explicitAmount = isDebit(record) ? amount.negate() : amount;
        return explicitAmount;
    }

    protected boolean isDebit(CSVRecord record) {
        return record.get("Soll/Haben").equals("S");
    }

    protected void calculateBalanceAfter(CashTransaction transaction) {
        currentBalance = currentBalance.add(BigDecimal.valueOf(transaction.getAmount()));
        transaction.setAccountBalance(currentBalance.doubleValue());
    }

    private String filterContraAccountNumberFromDescription(String description) {
        String descriptionWithoutSpacesAndNewLines = description.replaceAll(" |\\n", "");
        if (descriptionWithoutSpacesAndNewLines.contains("IBAN:")) {
            String iban = descriptionWithoutSpacesAndNewLines.replaceAll("^.*(IBAN:)|(BIC:).*$|(Datum:).*$", "");
            return iban;
        }
        return null;
    }

    protected String getAccountNumberFrom(List<CSVRecord> allRecords) {
        String blz = allRecords.get(2).get(1);
        String konto = allRecords.get(3).get(1);
        return getGermanIban(blz, konto);
    }

    @Override
    public RawCashTransaction parseCashTransactionFromNEW(CSVRecord record) throws ParseException {
        RawCashTransaction transaction = new RawCashTransaction();
        transaction.accountNumber = (accountNumber);
        transaction.contraAccountName = (record.get("Empfänger/Zahlungspflichtiger"));
        transaction.description = (record.get("Vorgang/Verwendungszweck"));
        transaction.date = parseDateFrom(record.get("Buchungstag"));
        transaction.description = parseDescriptionFrom(record);
        transaction.amount = parseAmountFrom(record);
        calculateBalanceAfterNEW(transaction);
        transaction.contraAccountNumber = filterContraAccountNumberFromDescription(record.get("Vorgang/Verwendungszweck"));
        return transaction;
    }

    protected void calculateBalanceAfterNEW(RawCashTransaction transaction) {
        currentBalance = currentBalance.add(transaction.amount);
        transaction.accountBalance = currentBalance;
    }

}
