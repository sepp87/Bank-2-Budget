package io.ost.finance;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author joost
 */
public class SingleAccountBudget {

    static int firstOfMonth = 25;
    static Map<String, Double> budgetedForCategory = new TreeMap<>();
    Map<String, MonthlyBudget> monthlyBudgets;

    private Account account;

    public SingleAccountBudget() {
        new BudgetSettingsReader().read();
        monthlyBudgets = new TreeMap<>();
    }

    public SingleAccountBudget(Account account) {
        new BudgetSettingsReader().read();
        monthlyBudgets = new TreeMap<>();
        this.account = account;
        calculateAllMonthlyBudgets();
    }

    // you can only add an account once
    public boolean addAccount(Account account) {
        if (this.account == null) {
            this.account = account;
            calculateAllMonthlyBudgets();
            return true;
        }
        return false;
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
        for (LocalDate first : allFirst) {
            calculateMonthlyBudgetFor(first);
        }
    }

    private List<LocalDate> calculateAllFirstOfMonth() {
        LocalDate newest = LocalDate.parse(account.getNewestTransactionDate());
        LocalDate oldest = LocalDate.parse(account.getOldestTransactionDate());
        // if the first of month comes after oldest transaction date, the month of year should be one earlier
        int month = (firstOfMonth > oldest.getDayOfMonth()) ? oldest.getMonthValue() - 1 : oldest.getMonthValue();
        LocalDate first = LocalDate.of(oldest.getYear(), month, firstOfMonth);
        List<LocalDate> allFirst = new ArrayList<>();
        while (first.isBefore(newest)) {
            allFirst.add(first);
            first = ChronoUnit.MONTHS.addTo(first, 1);
        }
        return allFirst;
    }

    private void calculateMonthlyBudgetFor(LocalDate firstOfMonth) {
        List<CashTransaction> transactions = getTransactionsFromAccountFor(firstOfMonth);
        // check for existing months before creating a new one
        if (monthlyBudgets.containsKey(firstOfMonth.toString())) {
            MonthlyBudget month = monthlyBudgets.get(firstOfMonth.toString());
            month.addTransactions(transactions);
        } else {
            MonthlyBudget month = new MonthlyBudget(this, firstOfMonth.toString(), transactions);
            monthlyBudgets.put(firstOfMonth.toString(), month);
        }
    }

    private List<CashTransaction> getTransactionsFromAccountFor(LocalDate firstOfMonth) {
        LocalDate nextFirst = ChronoUnit.MONTHS.addTo(firstOfMonth, 1);
        return account.getTransactions(firstOfMonth, nextFirst);
    }

}
