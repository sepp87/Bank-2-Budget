package bank2budget;

/**
 *
 * @author joostmeulenkamp
 */
public class Notes {
    
    
    
    
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
    // IntegrityChecker - FileOrigin is null and can't be used by integritychecker anymore

        
    // REFACTORING NOTES

    // Decide whether to continue overwriting (contra) account names
    //      OR - introduce an alias into the data model
    //      OR - make it a UI only thing
    
    // BACKLOG PRIO
    //      Account Merge is not immutable
    //      Support opening account balance
    //      Thoroughly test UX of main use case
    //      Provide year-in-review dashboard
    
    // Config
    //      Define start of financial year > user needs to declare a month
    
    // Budgeting
    //      Budgeted - enable saving changed budgeted value to settings    

    // Transactions
    //      Virtual save - when editing categories and recalculate budget accordingly

    // General
    //      Undo / Redo
    //      Resolve account aliases for account numbers e.g. "123" is "Savings Account"
    //      Rename categories (Template, Budget Months, Transactions, Categorization Rules)
    //      TransactionReview get suggestions from all transactions not only loaded
    
    // Rules
    //      Add dynamic rules e.g. to rename
    //      Apply rules again
    //      Reload rules
    
    // Budget Settings view
    //      On missing settings follow wizard
    // 

    // WARNING
    // First of Month used as hardcoded value in AnalyticsExportService
    // Template not reloaded dynamically in BudgetService for recalculate
    // MultiAccountView to modal
    
    // Thoughts
    //      BudgetMonth: should Adjustments, Budgeted and Variance also be streamed from control categories? does it hurt? it makes method pattern consistent with others
    
    
}
