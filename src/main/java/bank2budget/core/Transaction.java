package bank2budget.core;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 *
 * @author joostmeulenkamp
 */
public record Transaction(
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

    public Transaction withLastOfDay(boolean newLastOfDay) {
        return new Transaction(
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

    public Transaction withAccountName(String newAccountName) {
        return new Transaction(
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

    public Transaction withContraAccountName(String newContraAccountName) {
        return new Transaction(
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

    public Transaction withInternal(boolean newInternal) {
        return new Transaction(
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

    public Transaction withCategory(String newCategory) {
        return new Transaction(
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

    public Transaction withNotes(String newNotes) {
        return new Transaction(
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