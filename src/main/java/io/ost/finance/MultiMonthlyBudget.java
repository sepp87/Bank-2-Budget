package io.ost.finance;

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
public class MultiMonthlyBudget {

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

    public MultiMonthlyBudget(MultiAccountBudget budget, String firstOfMonth, Map<String, Double> budgetedForCategories) {
        this.budget = budget;
        this.firstOfMonth = firstOfMonth;
        this.transactions = new ArrayList<>();
        this.budgetedForCategories.putAll(budgetedForCategories);

//        this.remainderForCategoriesLastMonth.putAll(getRemainderLastMonth());
//        alignCategories(); // should come before calculateRemainder, otherwise null pointers eveywhere
//        calculateRemainder();
    }

    public MultiMonthlyBudget(MultiAccountBudget budget, String firstOfMonth, List<CashTransaction> transactions) {
        this.budget = budget;
        this.firstOfMonth = firstOfMonth;
        this.transactions = transactions;
        this.remainderForCategoriesLastMonth.putAll(getRemainderLastMonth());
        calculateExpenses(); // can yield new categories, so before alignCategories
        alignBudgetedForCategories(); // should come before alignCategories, otherwise the budgeted costs won't show up
        alignCategories(); // should come before calculateRemainder, otherwise null pointers everywhere
        calculateRemainder();
    }

    public void addTransactions(List<CashTransaction> transactions) {
        this.transactions.addAll(transactions);
        this.remainderForCategoriesLastMonth.putAll(getRemainderLastMonth());
        calculateExpenses();
        alignCategories();
        calculateRemainder();
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
        for (Entry<String, Double> entry : SingleAccountBudget.budgetedForCategory.entrySet()) {
            budgetedForCategories.putIfAbsent(entry.getKey(), entry.getValue());
        }
    }

    private void alignCategories() {
        Set<String> categories = new HashSet<>();
        categories.addAll(SingleAccountBudget.budgetedForCategory.keySet());
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

    private Map<String, Double> getRemainderLastMonth() {
        MultiMonthlyBudget previous = getPreviousMonthlyBudget();
        if (previous == null) {
            return Collections.EMPTY_MAP;
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

    private MultiMonthlyBudget getPreviousMonthlyBudget() {
        MultiMonthlyBudget result = null;
        LocalDate first = LocalDate.parse(firstOfMonth);
        LocalDate previousFirst = ChronoUnit.MONTHS.addTo(first, -1);
        if (budget.monthlyBudgets.containsKey(previousFirst.toString())) {
            result = budget.monthlyBudgets.get(previousFirst.toString());
        }
        return result;
    }

    private void calculateExpenses() {
        for (CashTransaction transaction : transactions) {
            double expense = transaction.amount;
            if (transaction.label != null) {
                addExpenseToCategory(expense, transaction.label);
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
            expense = expensesForCategories.get(category) + expense;
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
