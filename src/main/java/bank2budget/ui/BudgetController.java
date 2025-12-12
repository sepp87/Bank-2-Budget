package bank2budget.ui;

import bank2budget.App;
import bank2budget.app.BudgetService;
import bank2budget.core.budget.Budget;
import bank2budget.core.budget.BudgetMonth;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import javafx.util.Subscription;

/**
 *
 * @author joostmeulenkamp
 */
public class BudgetController {

    private final BudgetView view;
    private final App app;

//    private final Budget budget;
    private final BudgetService budget;
    private Subscription selectMonthSubscription;

    public BudgetController(BudgetView budgetView, App app) {
        this.view = budgetView;
        this.app = app;
        this.budget = app.getBudgetService();

        view.populateMonthSelector(budget.monthKeys(), null);
        view.lastExport().setText(app.getAccountService().getLastExportDate().toString());

        this.selectMonthSubscription = newSelectMonthSubscription();

    }

    private Subscription newSelectMonthSubscription() {
        return view.monthSelector().getSelectionModel().selectedItemProperty().subscribe(this::clearAndSelectMonth);
    }

    public void reload() {
        selectMonthSubscription.unsubscribe();
        LocalDate key = view.monthSelector().getSelectionModel().getSelectedItem();
        view.monthSelector().getItems().clear();
        view.populateMonthSelector(budget.monthKeys(), key);;
        selectMonthSubscription = newSelectMonthSubscription();
    }

    private void clearAndSelectMonth(LocalDate key) {
        view.clearTables();
        selectMonth(key);
    }

    private void selectMonth(LocalDate key) {
        BudgetMonth month = null;
        if (key == null) {
            month = budget.months().getLast();
        } else {
            month = budget.month(key);
        }

        
        view.selectedMonth(month.financialMonth(), month.financialYear());
        view.variance().setText("€ " + month.variance().toPlainString());
        view.closing().setText("€ " + month.closing().toPlainString());

        loadBudgetedVsExpenses(month);
        loadSavingsBuffersAndDeficits(month);
    }

    private void loadBudgetedVsExpenses(BudgetMonth month) {
        for (var category : month.operatingCategories()) {
            String name = category.name();
            BigDecimal budgeted = category.budgeted();
            BigDecimal expenses = category.actual();
            view.addCategoryToBudgetedVsExpenses(name, budgeted, expenses);
        }
        var unappliedExpenses = month.unappliedExpenses();
        var unappliedIncome = month.unappliedIncome();
        view.addCategoryToBudgetedVsExpenses(unappliedExpenses.name(), BigDecimal.ZERO, unappliedExpenses.actual());
        view.addCategoryToBudgetedVsExpenses(unappliedIncome.name(), BigDecimal.ZERO, unappliedIncome.actual());
    }

    private void loadSavingsBuffersAndDeficits(BudgetMonth month) {
        for (var category : month.operatingCategories()) {
            String name = category.name();
            BigDecimal closing = category.closing();
            BigDecimal adjustments = category.adjustments();
            view.addCategoryToSavingsBuffersAndDeficits(name, closing, adjustments);
        }
    }

}
