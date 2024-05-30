package io.ost.finance;

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
public class MultiAccountBudget extends SingleAccountBudget {

//    static int firstOfMonth = 25;
//    static Map<String, Double> budgetedForCategory = new TreeMap<>();
//    Map<String, MonthlyBudget> monthlyBudgets;
//    private Collection<Account> accounts;
    
    private final Map<String, Account> accounts;

    public MultiAccountBudget() {
        new BudgetSettingsReader().read();
        this.monthlyBudgets = new TreeMap<>();
        this.accounts = new TreeMap<>();
    }

    public MultiAccountBudget(Collection<Account> accounts) {
        new BudgetSettingsReader().read();
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

    @Override
    public void addMonthlyBudget(MonthlyBudget month) {
        monthlyBudgets.putIfAbsent(month.getFirstOfMonth(), month);
    }

    @Override
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
        for (LocalDate first : allFirst) {
            calculateMonthlyBudgetFor(first);
        }
    }

    private List<LocalDate> calculateAllFirstOfMonth() {
        LocalDate newest = getBoundaryTransactionDate(true);
        LocalDate oldest = getBoundaryTransactionDate(false);
        
        // if the first of month comes after oldest transaction date, the month of year should be one earlier
        LocalDate first = (firstOfMonth > oldest.getDayOfMonth()) ? oldest.minusMonths(1).withDayOfMonth(firstOfMonth): oldest.withDayOfMonth(firstOfMonth);
        List<LocalDate> allFirst = new ArrayList<>();
        while (first.isBefore(newest) || first.equals(newest)) { 
            allFirst.add(first);
            first = ChronoUnit.MONTHS.addTo(first, 1);
        }
        return allFirst;
    }
    
    private LocalDate getBoundaryTransactionDate(boolean newest) {
        List<LocalDate> dates = new ArrayList<>();
        for (Account account : accounts.values()) {
            String date = newest ? account.getNewestTransactionDate() : account.getOldestTransactionDate();
            dates.add(LocalDate.parse(date));
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

}
