package bank2budget;

/**
 *
 * @author joostmeulenkamp
 */
public class Notes {
    
    
    // LastOfDay
    // AccountReader takes directly from Excel, what if changed? however, what if accountnumber is changed.

    // REFACTORING NOTES

    // Decide whether to continue overwriting (contra) account names
    //      OR - introduce an alias into the data model
    //      OR - make it a UI only thing
    
    // BACKLOG PRIO
    //      Account Merge is not immutable
    //      Support opening account balance
    //      Thoroughly test UX of main use case
    //      Provide year-in-review dashboard
    //      Toggle filter in PnL view
    //      Scroll pane for dashboard
    
    // Config
    //      Define start of financial year > user needs to declare a month
    
    // Budgeting
    //      Budgeted - enable saving changed budgeted value to settings   
    //      

    // Transactions
    //      Virtual save - when editing categories and recalculate budget accordingly
    //      DONE MultiAccountView to modal

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
    
}
