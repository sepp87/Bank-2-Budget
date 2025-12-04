package bank2budget.core;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 *
 * @author joostmeulenkamp
 */
public record Transaction(
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
}

//    private String category;
//
//
