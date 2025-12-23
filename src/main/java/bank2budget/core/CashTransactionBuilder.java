package bank2budget.core;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 *
 * @author joostmeulenkamp
 */
public class CashTransactionBuilder {

    private int transactionNumber;
    private LocalDate date;
    private int positionOfDay;
    private boolean lastOfDay;
    private BigDecimal amount;
    private BigDecimal accountBalance;
    private String accountNumber;
    private String accountName;
    private CreditInstitution accountInstitution;
    private String contraAccountNumber;
    private String contraAccountName;
    private boolean internal;
    private String description;
    private String category;
    private String notes;

    public CashTransactionBuilder transactionNumber(int value) {
        this.transactionNumber = value;
        return this;
    }

    public CashTransactionBuilder date(LocalDate value) {
        this.date = value;
        return this;
    }

    public CashTransactionBuilder positionOfDay(int value) {
        this.positionOfDay = value;
        return this;
    }

    public CashTransactionBuilder lastOfDay(boolean value) {
        this.lastOfDay = value;
        return this;
    }

    public CashTransactionBuilder amount(BigDecimal value) {
        this.amount = value;
        return this;
    }

    public CashTransactionBuilder accountBalance(BigDecimal value) {
        this.accountBalance = value;
        return this;
    }

    public CashTransactionBuilder accountNumber(String value) {
        this.accountNumber = (value == null || value.isBlank()) ? null : value;
        return this;
    }

    public CashTransactionBuilder accountName(String value) {
        this.accountName = (value == null || value.isBlank()) ? null : value;
        return this;
    }

    public CashTransactionBuilder accountInstitution(CreditInstitution value) {
        this.accountInstitution = value;
        return this;
    }

    public CashTransactionBuilder contraAccountNumber(String value) {
        this.contraAccountNumber = (value == null || value.isBlank()) ? null : value;
        return this;
    }

    public CashTransactionBuilder contraAccountName(String value) {
        this.contraAccountName = (value == null || value.isBlank()) ? null : value;
        return this;
    }

    public CashTransactionBuilder internal(boolean value) {
        this.internal = value;
        return this;
    }

    public CashTransactionBuilder description(String value) {
        this.description = (value == null || value.isBlank()) ? null : value;
        return this;
    }

    public CashTransactionBuilder category(String value) {
        this.category = (value == null || value.isBlank()) ? null : value;
        return this;
    }

    public CashTransactionBuilder notes(String value) {
        this.notes = (value == null || value.isBlank()) ? null : value;
        return this;
    }

    public CashTransaction build() {

        if (amount == null) {
            amount = BigDecimal.ZERO;
        }

        if (accountBalance == null) {
            accountBalance = BigDecimal.ZERO;
        }

        CashTransaction.TransactionType transactionType = amount.compareTo(BigDecimal.ZERO) > 0
                ? CashTransaction.TransactionType.CREDIT : CashTransaction.TransactionType.DEBIT;

        return new CashTransaction(
                transactionNumber,
                date,
                positionOfDay,
                lastOfDay,
                amount,
                accountBalance,
                accountNumber,
                accountName,
                accountInstitution,
                contraAccountNumber,
                contraAccountName,
                internal,
                transactionType,
                description,
                category,
                notes
        );
    }
}
