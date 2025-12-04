package bank2budget;

/**
 *
 * @author joostmeulenkamp
 */
public class Notes {
    
    // Refactoring notes - TransactionRecord and RawTransactionRow
    // IntegrityChecker - FileOrigin is null and can't be used by integritychecker anymore
    // Rules - can rules use transaction record?
    
    // Account.merge
    // inserting new set of incomplete transactions in the middle, leads to merge with incomplete set
    // [ old ]  [empty]  [ old ] 
    // [ old ]  [ new ]  [ old ] 
    
    // [2,2,1]  [0,0,0]  [2,2,1]
    // [2,2,1]  [2,2,1]  [2,2,1]
    
    
    //     [     ]      ---> overlap of three days
    //     [2,2,1]      ---> new set contains 5 transactions
    // [2,2,1] [2,2,1]  ---> existing contains 3 transactions
    //     [ ]          ---> new set should be preferred
    //         [ ]      ---> existing should be preferred 
    
    
    
    // Budget CLI mode
    
    // Budget UI mode
    // Open app
    // - load budget template
    // - load budget
    // View dashboard
    // - goal: inspect current status
    // - view planned vs actual per category
    // - inspect transactions of category
    // - shift money between goals/deficits/buffers 
    // - synonyms for shifting: allocate / adjust / correct / shift / rebalance 
    // - categorize transactions
    
    // LastOfDay
    // AccountReader takes directly from Excel, what if changed? however, what if accountnumber is changed.
    
    // BalanceCalculator
    // Takes accounts
    // Filter only last transaction of day
    // Calculate balance history per account
    // Identify oldest date of account
    // Persist oldest overall date
    // Get transaction of date 
    // Get balance of transaction
    // If oldest date, persist balance before first transaction of day as date before
    // Get next date
    // Get transaction of date
    // If none available take balance of previous date  
    // Calculate overall balance history 
    // Get balance of oldest date for all accounts
    // If none available then assume zero
    
    // IntegrityChecker
    // Takes accounts
    // iterates transactions from newest to oldest
    // get balance before transaction
    // get balance of next transactions
    // compare if balance is equal
    // if balance is not equal, throw an error message 
    // message contains tells which transaction broke integrity broke and from which file it stems
    
//      SELECT SUM(amount), category FROM transactions
//      GROUP BY category
    
    
    // REFACTORING NOTES

    // Decide where to move or remove commented out code (thought: account names should not be overwritten, maybe an alias)
    // TransactionReaderForXlsxDone.getAllCashTransactionsFrom()
    //                  TransactionParser.overwriteAccountNames(transaction);
    //                  TransactionParser.addMissingAccountNumbers(transaction);
    
    // Move rule application out of the parser completely
    // Move Transaction.isInternal logic outside of TransactionParser
    
    
}
