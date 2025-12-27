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
    
    // BACKLOG
    
    // Budgeting
    //      DONE Labels for tiles on dashboards
    //      DONE Adjustments - support correcting a budget at the end of month to negate variances
    //      DONE Adjustments - from and to XLSX
    //      DONE TableView instead of GridPanes
    //      DONE Balance Ratio - Pie Chart for balance per account 
    //      Budgeted - enable changing the budgeted value and saving it to settings
    //      Category transactions - inspect when clicking on category
    //      Category transactions - change in inspection mode and enable (virtual) save
    //      Support opening account balance
    //      DONE Change category rules to only label when null
    
    // Transactions
    //      DONE Simplified categorization rules
    //      Virtual save - when editing categories and recalculate budget accordingly
    //      DONE After editing category manual, regain focus on table view instead of tabs
    //      DONE After editing completed by pressing enter, select next cell
    //      DONE Start editing when user starts typing on keyboard

    // General
    //      DONE Save on another thread
    //      DONE Notifications (saving, integrity check, csvs loaded, ...)
    //      Undo / Redo
    
    // Rules
    //      Add dynamic rules e.g. to rename
    //      Apply rules again
    //      Reload rules
    
    // Budget Settings view
    //      improve UX Choice Box editing
    //      DONE Styling
    //      DONE opening/switching overlay
    //      saving
    //      First of month
    //      add row
    //      remove row
    
    // Rules View
    //      

    
    
    // Thoughts
    //      BudgetMonth: should Adjustments, Budgeted and Variance also be streamed from control categories? does it hurt? it makes method pattern consistent with others
    
    
}
