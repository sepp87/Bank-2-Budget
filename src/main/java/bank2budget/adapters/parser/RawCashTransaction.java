package bank2budget.adapters.parser;

import bank2budget.core.CashTransaction;
import bank2budget.core.CreditInstitution;
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
    
    public CashTransaction toCashTransaction() {
        CashTransaction transaction = new CashTransaction();
        transaction.setTransactionNumber(transactionNumber);
        transaction.setLastOfDay(lastOfDay);
        transaction.setPositionOfDay(positionOfDay);
        transaction.setDate(date);
        transaction.setAmount(amount.doubleValue());
        transaction.setAccountBalance(accountBalance.doubleValue());
        transaction.setAccountNumber(accountNumber);
        transaction.setAccountName(accountName);
        transaction.setAccountInstitution(accountInstitution);
        transaction.setContraAccountNumber(contraAccountNumber);
        transaction.setContraAccountName(contraAccountName);
        transaction.setCategory(category);
        transaction.setNotes(notes);
        return transaction;
    }
}
