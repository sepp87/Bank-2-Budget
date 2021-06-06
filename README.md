# Description
In short Bank-2-Budget processes your digital bank statements, so you can use it to run your budget.

Use processing rules to automatically process your statements even further. Change or add values to cash transactions based on your preconditions e.g. if description contains the word "restaurant" then appoint the label "dining out".

Unify the format of your different bank account statements. Almost each bank structures its data differently, how annoying is that. 

# How To
Download your digital bank statement (.CSV files) and place it into the "todo" folder. Run the app by double clicking "Bank-2-Budget.jar". Look into the "done" folder, you'll find your processed files here. Open it up in Excel, group by label, create sub-totals and track your expenses in a budget.

# Configuration
* my-accounts.txt - keep tracks of your accounts. Transactions between your personal accounts get marked as internal.
* other-accounts.txt - keep track of other accounts. These account numbers and names are used to fill in blanks, since some banks omit one of both.
* processing-rules.txt - rules to process your bank statements even further. Use "Rule-Builder.html" to get yourself going.

# Supported Banks
* ASN Bank
* Comdirect Bank
* DKB
* Flatex
* GLS Bank
* Grenke Bank
* ING-DiBa
* ING
* Münchner Bank
* Rabobank
* SNS Bank
* Sparkasse

# Possible To Do's
* Support date/number ranges for rules
* Support new bank formats
* Export to Excel file format
* Create and update budgets