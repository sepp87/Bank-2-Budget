package bank2budget.core;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 *
 * @author joostmeulenkamp
 */
public record CashTransaction(
        /**
         * The transaction number is a unique number bound to this transaction.
         * Every time this cash transaction is parsed this number should remain
         * the same. Then together with account number it can be used as primary
         * key.
         */
        int transactionNumber,
        LocalDate date,
        int positionOfDay,
        boolean lastOfDay,
        BigDecimal amount,
        BigDecimal accountBalance,
        String accountNumber,
        String accountName,
        CreditInstitution accountInstitution,
        String contraAccountNumber,
        String contraAccountName,
        boolean internal,
        TransactionType transactionType,
        String description,
        String category,
        String notes) {

    public enum TransactionType {
        DEBIT,
        CREDIT
    }

    public BigDecimal accountBalanceBefore() {
        return accountBalance.subtract(amount);
    }

    public CashTransaction withLastOfDay(boolean newLastOfDay) {
        return new CashTransaction(
                transactionNumber,
                date,
                positionOfDay,
                newLastOfDay,
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

    public CashTransaction withAccountName(String newAccountName) {
        return new CashTransaction(
                transactionNumber,
                date,
                positionOfDay,
                lastOfDay,
                amount,
                accountBalance,
                accountNumber,
                newAccountName,
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

    public CashTransaction withContraAccountName(String newContraAccountName) {
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
                newContraAccountName,
                internal,
                transactionType,
                description,
                category,
                notes
        );
    }

    public CashTransaction withInternal(boolean newInternal) {
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
                newInternal,
                transactionType,
                description,
                category,
                notes
        );
    }

    public CashTransaction withCategory(String newCategory) {
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
                newCategory,
                notes
        );
    }

    public CashTransaction withNotes(String newNotes) {
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
                newNotes
        );
    }

}