package bank2budget.ui;

import bank2budget.core.CashTransaction;
import bank2budget.core.CashTransaction.TransactionType;
import bank2budget.core.CreditInstitution;
import bank2budget.core.Util;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * CashTransaction is parsed from one single Record in a CSV file. It checks
 * account names and numbers against the config to derive account names and
 * numbers. It also determines - by the same means - if the transaction is
 * internal, meaning moving cash between personal accounts e.g. from a savings
 * account to a running.
 *
 *
 * @author joost
 */
public class EditableCashTransaction {

    private CashTransaction domain;

    public EditableCashTransaction(CashTransaction transaction) {
        this.domain = transaction;
    }

    public EditableCashTransaction(EditableCashTransaction cashTransaction) {
        this.domain = cashTransaction.toDomain();
    }

    // SETTERS
    public void setCategory(String category) {
        domain = domain.withCategory(category);
    }

    public void setNotes(String notes) {
        domain = domain.withNotes(notes);
    }

    // GETTERS
    public CashTransaction toDomain() {
        return domain;
    }

    public String accountNumber() {
        return domain.accountNumber();
    }

    public String accountName() {
        return domain.accountName();
    }

    public BigDecimal accountBalance() {
        return domain.accountBalance();
    }

    public BigDecimal accountBalanceBefore() {
        return domain.accountBalanceBefore();
    }

    public CreditInstitution accountInstitution() {
        return domain.accountInstitution();
    }

    public String contraAccountNumber() {
        return domain.contraAccountNumber();
    }

    public String contraAccountName() {
        return domain.contraAccountName();
    }

    public BigDecimal amount() {
        return domain.amount();
    }

    public LocalDate date() {
        return domain.date();
    }

    public boolean internal() {
        return domain.internal();
    }

    public String category() {
        return domain.category();
    }

    public int transactionNumber() {
        return domain.transactionNumber();
    }

    public String description() {
        return domain.description();
    }

    public TransactionType transactionType() {
        return domain.transactionType();
    }

    public boolean lastOfDay() {
        return domain.lastOfDay();
    }

    public int positionOfDay() {
        return domain.positionOfDay();
    }

    public String notes() {
        return domain.notes();
    }

    @Override
    public String toString() {
        String result = domain.transactionNumber() + "\t" + domain.lastOfDay() + "\t" + domain.date() + "\t" + Util.padWithTabs("€" + domain.amount(), 2) + Util.padWithTabs(domain.accountName(), 4) + Util.padWithTabs(domain.contraAccountName(), 4) + Util.padWithTabs(domain.category(), 4);
        return result;
    }

}
