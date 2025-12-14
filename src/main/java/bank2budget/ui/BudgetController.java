package bank2budget.ui;

import bank2budget.App;
import bank2budget.app.BudgetService;
import bank2budget.core.budget.BudgetMonth;
import java.time.LocalDate;
import javafx.util.Subscription;

/**
 *
 * @author joostmeulenkamp
 */
public class BudgetController {

    private final BudgetView view;
    private final App app;

    private final BudgetService budget;
    private final ProfitAndLossController profitAndLossController;
    private final BudgetedVsActualController budgetedVsActualController;

    private Subscription selectMonthSubscription;

    public BudgetController(BudgetView budgetView, App app) {
        this.view = budgetView;
        this.app = app;
        this.budget = app.getBudgetService();

        view.populateMonthSelector(budget.monthKeys(), null);
        view.lastExport().setText(app.getAccountService().getLastExportDate().toString());

        LocalDate latest =budget.months().getLast().firstOfMonth();
        
        this.profitAndLossController = new ProfitAndLossController(view.getProfitAndLossView(), app.getBudgetReportService(), app.getBudgetService());
        profitAndLossController.load(latest);

        this.budgetedVsActualController = new BudgetedVsActualController(view.getBudgetedVsActualView(), app.getBudgetReportService(), app.getBudgetService());
        budgetedVsActualController.load(latest);
        
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
        selectMonth(key);
        profitAndLossController.load(key);
        budgetedVsActualController.load(key);
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

    }



}
