package bank2budget.core;

import bank2budget.core.Account;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author joost
 */
public class MultiAccountBudget {

    static int firstOfMonth = 1;
    static Map<String, Double> budgetedForCategory = new TreeMap<>();
    Map<String, MonthlyBudget> monthlyBudgets;

    private final Map<String, Account> accounts;

    public MultiAccountBudget() {
        this.monthlyBudgets = new TreeMap<>();
        this.accounts = new TreeMap<>();
    }

    public MultiAccountBudget(Collection<Account> accounts) {
        this.monthlyBudgets = new TreeMap<>();
        this.accounts = new TreeMap<>();
        setAccounts(accounts);
    }

    public final void setAccounts(Collection<Account> accounts) {
        for (Account account : accounts) {
            this.accounts.putIfAbsent(account.getAccountNumber(), account);
        }
        calculateAllMonthlyBudgets();
    }

    public void addMonthlyBudget(MonthlyBudget month) {
        monthlyBudgets.putIfAbsent(month.getFirstOfMonth(), month);
    }

    public Map<String, MonthlyBudget> getMonthlyBudgets() {
        return monthlyBudgets;
    }

    public static String[] getHeader() {
        return new String[]{
            "Category",
            "Budgeted",
            "Expenses",
            "Last Month",
            "Remainder"
        };
    }

    private void calculateAllMonthlyBudgets() {
        List<LocalDate> allFirst = calculateAllFirstOfMonth();
        // TODO 

        for (LocalDate first : allFirst) {
            calculateMonthlyBudgetFor(first);
        }
    }

    /**
     *
     * @return a list of all first of month dates in ascending order
     */
    private List<LocalDate> calculateAllFirstOfMonth() {
        LocalDate newest = getBoundaryTransactionDate(true);
        LocalDate oldest = getBoundaryTransactionDate(false);

        // if the first of month comes after oldest transaction date, the month of year should be one earlier
        LocalDate first = (firstOfMonth > oldest.getDayOfMonth()) ? oldest.minusMonths(1).withDayOfMonth(firstOfMonth) : oldest.withDayOfMonth(firstOfMonth);
//        System.out.println(oldest + "\t" + first);
        List<LocalDate> allFirst = new ArrayList<>();
        while (first.isBefore(newest) || first.equals(newest)) {
            allFirst.add(first);
            first = ChronoUnit.MONTHS.addTo(first, 1);
        }
//                System.out.println(allFirst);

        return allFirst;
    }

    /**
     *
     * @param newest set to true, to get the newest first of date
     * @return the oldest/newest first of month based on the transactions
     * contained across all accounts.
     */
    private LocalDate getBoundaryTransactionDate(boolean newest) {
        List<LocalDate> dates = new ArrayList<>();
        for (Account account : accounts.values()) {
            LocalDate date = newest ? account.getNewestTransactionDate() : account.getOldestTransactionDate();
            dates.add(date);
        }
        return Util.findBoundaryDate(dates, newest);
    }

    private void calculateMonthlyBudgetFor(LocalDate firstOfMonth) {
        List<CashTransaction> transactions = getTransactionsFromAccountsFor(firstOfMonth);
        // check for existing months before creating a new one
        if (monthlyBudgets.containsKey(firstOfMonth.toString())) {
            MonthlyBudget month = monthlyBudgets.get(firstOfMonth.toString());
            month.addTransactions(transactions);
        } else {
            MonthlyBudget month = new MonthlyBudget(this, firstOfMonth.toString(), transactions);
            monthlyBudgets.put(firstOfMonth.toString(), month);
        }
    }

    private List<CashTransaction> getTransactionsFromAccountsFor(LocalDate firstOfMonth) {
        List<CashTransaction> transactions = new ArrayList<>();
        LocalDate lastOfMonth = firstOfMonth.plusMonths(1).minusDays(1);
        for (Account account : accounts.values()) {
            transactions.addAll(account.getTransactions(firstOfMonth, lastOfMonth));
        }
        return transactions;
    }

    public static void setFirstOfMonth(int i) {
        firstOfMonth = i;
    }

    public static int getFirstOfMonth() {
        return firstOfMonth;
    }

    public static void setBudgetTemplate(Map<String, Double> budgetedForCategory) {
        MultiAccountBudget.budgetedForCategory.putAll(budgetedForCategory);
    }
}
