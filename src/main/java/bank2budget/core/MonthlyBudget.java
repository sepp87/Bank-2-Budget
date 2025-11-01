package bank2budget.core;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @author joost
 */
public class MonthlyBudget {

    private final MultiAccountBudget budget;
    private final String firstOfMonth;
    private final List<CashTransaction> transactions;

    // split into unassigned expenses and income
    private double unassignedExpenses = 0;
    private double unassignedExpensesRemainder = 0;
    private double unassignedExpensesRemainderLastMonth = 0;

    // split into unassigned expenses and income
    private double unassignedIncome = 0;
    private double unassignedIncomeRemainder = 0;
    private double unassignedIncomeRemainderLastMonth = 0;

    final Map<String, Double> budgetedForCategories = new TreeMap<>();
    final Map<String, Double> expensesForCategories = new TreeMap<>();
    final Map<String, Double> remainderForCategories = new TreeMap<>();
    final Map<String, Double> remainderForCategoriesLastMonth = new TreeMap<>();

    public MonthlyBudget(MultiAccountBudget budget, String firstOfMonth, Map<String, Double> budgetedForCategories) {
        this.budget = budget;
        this.firstOfMonth = firstOfMonth;
        this.transactions = new ArrayList<>();
        this.budgetedForCategories.putAll(budgetedForCategories);

//        this.remainderForCategoriesLastMonth.putAll(getRemainderLastMonth());
//        alignCategories(); // should come before calculateRemainder, otherwise null pointers eveywhere
//        calculateRemainder();
    }

    public MonthlyBudget(MultiAccountBudget budget, String firstOfMonth, List<CashTransaction> transactions) {
        this.budget = budget;
        this.firstOfMonth = firstOfMonth;
        this.transactions = transactions;
        this.remainderForCategoriesLastMonth.putAll(getRemainderLastMonth());
        calculateExpenses(); // can yield new categories, so before alignCategories
        alignBudgetedForCategories(); // should come before alignCategories, otherwise the budgeted costs won't show up
        alignCategories(); // should come before calculateRemainder, otherwise null pointers everywhere
        calculateRemainder();
    }

    public int getFinancialYear() {
        LocalDate date = LocalDate.parse(firstOfMonth);
        int day = date.getDayOfMonth();
        int month = date.getMonthValue();
        int year = date.getYear();
        if (month == 12 && day > 16) {
            return year + 1;
        }
        return year;
    }

    public int getFinancialMonth() {
        LocalDate date = LocalDate.parse(firstOfMonth);
        if (date.getDayOfMonth() > 16) {
            return date.plusMonths(1).getMonthValue();
        }
        return date.getMonthValue();
    }

    public void addTransactions(List<CashTransaction> transactions) {
        this.transactions.addAll(transactions);
        this.remainderForCategoriesLastMonth.putAll(getRemainderLastMonth());
        calculateExpenses();
        alignCategories();
        calculateRemainder();
    }

    List<CashTransaction> getTransactions() {
        return transactions;
    }

    public Map<String, Double> getBudgetedForCategories() {
        return budgetedForCategories;
    }

    public Map<String, Double> getExpensesForCategories() {
        return expensesForCategories;
    }

    public Map<String, Double> getRemainderForCategories() {
        return remainderForCategories;
    }

    public Map<String, Double> getRemainderForCategoriesLastMonth() {
        return remainderForCategoriesLastMonth;
    }

    public double getUnassignedExpenses() {
        return unassignedExpenses;
    }

    public double getUnassignedExpensesRemainder() {
        return unassignedExpensesRemainder;
    }

    public double getUnassignedExpensesRemainderLastMonth() {
        return unassignedExpensesRemainderLastMonth;
    }

    public double getUnassignedIncome() {
        return unassignedIncome;
    }

    public double getUnassignedIncomeRemainder() {
        return unassignedIncomeRemainder;
    }

    public double getUnassignedIncomeRemainderLastMonth() {
        return unassignedIncomeRemainderLastMonth;
    }

    public String getFirstOfMonth() {
        return firstOfMonth;
    }

    public double getBudgetedTotal() {
        double result = 0.;
        for (double value : budgetedForCategories.values()) {
            result += value;
        }
        return Util.round(result);
    }

    public double getRemainderTotal() {
        double result = 0.;
        for (double value : remainderForCategories.values()) {
            result += value;
        }
        return Util.round(result + unassignedExpensesRemainder + unassignedIncomeRemainder);
    }

    private void alignBudgetedForCategories() {
        for (Entry<String, Double> entry : MultiAccountBudget.budgetedForCategory.entrySet()) {
            budgetedForCategories.putIfAbsent(entry.getKey(), entry.getValue());
        }
    }

    private void alignCategories() {
        Set<String> categories = new HashSet<>();
        categories.addAll(MultiAccountBudget.budgetedForCategory.keySet());
        categories.addAll(budgetedForCategories.keySet());
        categories.addAll(expensesForCategories.keySet());
        categories.addAll(remainderForCategories.keySet());
        categories.addAll(remainderForCategoriesLastMonth.keySet());
        addMissingCategoriesToMap(categories, budgetedForCategories);
        addMissingCategoriesToMap(categories, expensesForCategories);
        addMissingCategoriesToMap(categories, remainderForCategories);
        addMissingCategoriesToMap(categories, remainderForCategoriesLastMonth);
    }

    private void addMissingCategoriesToMap(Set<String> categories, Map<String, Double> map) {
        for (String category : categories) {
            if (!map.containsKey(category)) {
                map.put(category, 0.);
            }
        }
    }

    /**
     *
     * @return the remainder of last month's budget. If there was no budget
     * (meaning this was the first), the initial balance is added as unassigned
     * expense or income.
     */
    private Map<String, Double> getRemainderLastMonth() {
        MonthlyBudget previous = getPreviousMonthlyBudget();
        if (previous == null) {
            setInitialBalanceAsUnassignedItem();
            return Collections.emptyMap();
        }
        Map<String, Double> result = new TreeMap<>();
        for (Entry<String, Double> previousRemainderForCategory : previous.remainderForCategories.entrySet()) {
            if (previousRemainderForCategory.getValue() == 0.) {
                continue;
            }
            result.put(previousRemainderForCategory.getKey(), previousRemainderForCategory.getValue());
        }
        this.unassignedExpensesRemainderLastMonth = previous.unassignedExpensesRemainder;
        this.unassignedIncomeRemainderLastMonth = previous.unassignedIncomeRemainder;
        return result;
    }

    private MonthlyBudget getPreviousMonthlyBudget() {
        MonthlyBudget result = null;
        LocalDate first = LocalDate.parse(firstOfMonth);
        LocalDate previousFirst = ChronoUnit.MONTHS.addTo(first, -1);
        if (budget.monthlyBudgets.containsKey(previousFirst.toString())) {
            result = budget.monthlyBudgets.get(previousFirst.toString());
        }
        return result;
    }

    private void setInitialBalanceAsUnassignedItem() {
        // get initial balance from first transaction
        double initialBalance = transactions.getFirst().getAccountBalanceBefore();
        if (initialBalance > 0) {
            unassignedIncomeRemainderLastMonth = initialBalance;
        } else {
            unassignedExpensesRemainderLastMonth = initialBalance;
        }
    }

    private void calculateExpenses() {
        for (CashTransaction transaction : transactions) {
            double expense = transaction.getAmount();
            if (transaction.getLabel() != null) {
                addExpenseToCategory(expense, transaction.getLabel());
            } else {
                if (expense < 0) {
                    unassignedExpenses += expense;
                } else {
                    unassignedIncome += expense;
                }
            }
        }
    }

    // expense is the negated transaction amount
    private void addExpenseToCategory(double expense, String category) {
        if (expensesForCategories.containsKey(category)) {
            expense = Util.round(expensesForCategories.get(category) + expense);
            expensesForCategories.put(category, expense);
        } else {
            expensesForCategories.put(category, expense);
        }
    }

    private void calculateRemainder() {
        // TODO BUG categories can also come from budgeted and last month's remainder, meaning need for alignment before here
        for (String category : expensesForCategories.keySet()) {

            double remainderLastMonth = 0;
            double budgeted = 0;
            double expenses = expensesForCategories.get(category);

            if (remainderForCategoriesLastMonth.containsKey(category)) {
                remainderLastMonth = remainderForCategoriesLastMonth.get(category);
                remainderForCategoriesLastMonth.put(category, remainderLastMonth);
            }
            if (budgetedForCategories.containsKey(category)) {
                budgeted = budgetedForCategories.get(category);
            }

            double remainder = Util.round(remainderLastMonth + budgeted + expenses);
            remainderForCategories.put(category, remainder);
        }

        this.unassignedExpensesRemainder = Util.round(unassignedExpensesRemainderLastMonth + unassignedExpenses);
        this.unassignedIncomeRemainder = Util.round(unassignedIncomeRemainderLastMonth + unassignedIncome);
    }
}
