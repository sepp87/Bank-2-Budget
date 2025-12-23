package bank2budget.adapter.parser;

import bank2budget.core.CreditInstitution;
import bank2budget.core.CashTransaction;
import bank2budget.core.CashTransactionBuilder;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * TransactionParser sets transactionNumber, lastOfDay and positionOfDay
 * automatically. No need to set these properties in subclass parsers.
 *
 * @author joostmeulenkamp
 */
public class RawCashTransaction {
    
    public int transactionNumber;
    public boolean lastOfDay;
    public int positionOfDay;
    
    public LocalDate date;
    public BigDecimal amount;
    public BigDecimal accountBalance;
    public String accountNumber;
    public String accountName;
    public CreditInstitution accountInstitution;
    public String contraAccountNumber;
    public String contraAccountName;
    public String description;
    
    public String category;
    public String notes;
    
    public CashTransaction toTransaction() {
        CashTransactionBuilder builder = new CashTransactionBuilder()
                .transactionNumber(transactionNumber)
                .lastOfDay(lastOfDay)
                .positionOfDay(positionOfDay)
                .date(date)
                .amount(amount)
                .accountBalance(accountBalance)
                .accountNumber(accountNumber)
                .accountName(accountName)
                .accountInstitution(accountInstitution)
                .contraAccountNumber(contraAccountNumber)
                .contraAccountName(contraAccountName)
                .description(description)
                .category(category)
                .notes(notes);
        return builder.build();
    }
}
