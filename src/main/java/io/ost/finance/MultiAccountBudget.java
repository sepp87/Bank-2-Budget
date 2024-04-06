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
public class MultiAccountBudget {

    static int firstOfMonth = 25;
    static Map<String, Double> budgetedForCategory = new TreeMap<>();
    Map<String, MultiMonthlyBudget> monthlyBudgets;

    private Collection<Account> accounts;

    public MultiAccountBudget() {
        new BudgetSettingsReader().read();
        monthlyBudgets = new TreeMap<>();
    }

    public MultiAccountBudget(Collection<Account> accounts) {
        new BudgetSettingsReader().read();
        monthlyBudgets = new TreeMap<>();
        this.accounts = accounts;
        calculateAllMonthlyBudgets();
    }

    public void addMonthlyBudget(MultiMonthlyBudget month) {
        monthlyBudgets.putIfAbsent(month.getFirstOfMonth(), month);
    }

    public Map<String, MultiMonthlyBudget> getMonthlyBudgets() {
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
        LocalDate newest = getNewestTransactionDateFromAccounts();
        LocalDate oldest = getOldestTransactionDateFromAccounts();
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

    private LocalDate getNewestTransactionDateFromAccounts() {
        LocalDate newest = null;
        for (Account account : accounts) {
            LocalDate candidate = LocalDate.parse(account.getNewestTransactionDate());
            if (newest == null) {
                newest = candidate;
            } else if (candidate.isAfter(newest)) {
                newest = candidate;
            }
        }
        return newest;
    }

    private LocalDate getOldestTransactionDateFromAccounts() {
        LocalDate oldest = null;
        for (Account account : accounts) {
            LocalDate candidate = LocalDate.parse(account.getNewestTransactionDate());
            if (oldest == null) {
                oldest = candidate;
            } else if (candidate.isBefore(oldest)) {
                oldest = candidate;
            }
        }
        return oldest;
    }

    private void calculateMonthlyBudgetFor(LocalDate firstOfMonth) {
        List<CashTransaction> transactions = getTransactionsFromAccountsFor(firstOfMonth);
        // check for existing months before creating a new one
        if (monthlyBudgets.containsKey(firstOfMonth.toString())) {
            MultiMonthlyBudget month = monthlyBudgets.get(firstOfMonth.toString());
            month.addTransactions(transactions);
        } else {
            MultiMonthlyBudget month = new MultiMonthlyBudget(this, firstOfMonth.toString(), transactions);
            monthlyBudgets.put(firstOfMonth.toString(), month);
        }
    }

    private List<CashTransaction> getTransactionsFromAccountsFor(LocalDate firstOfMonth) {
        List<CashTransaction> transactions = new ArrayList<>();
        for (Account account : accounts) {
            LocalDate nextFirst = ChronoUnit.MONTHS.addTo(firstOfMonth, 1);
            transactions.addAll(account.getTransactions(firstOfMonth, nextFirst));
        }
        return transactions;
    }

}
