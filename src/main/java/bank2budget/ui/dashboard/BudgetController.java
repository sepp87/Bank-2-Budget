package bank2budget.ui.dashboard;

import bank2budget.App;
import bank2budget.app.BudgetService;
import bank2budget.core.budget.BudgetMonth;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.TreeMap;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.util.Subscription;

/**
 *
 * @author joostmeulenkamp
 */
public class BudgetController {

    private final BudgetView view;
    private final App app;

    private final BudgetService budgetService;
    private final ProfitAndLossController profitAndLossController;
    private final BudgetedVsActualController budgetedVsActualController;
    private final AccountBalanceView accountBalanceView;

    private Subscription selectMonthSubscription;

    public BudgetController(BudgetView budgetView, App app) {
        this.view = budgetView;
        this.app = app;
        this.budgetService = app.getBudgetService();

        view.populateMonthSelector(budgetService.monthKeys(), null);
        view.lastExport().setText(app.getAccountService().getLastExportDate().toString());

        this.profitAndLossController = new ProfitAndLossController(view.getProfitAndLossView(), app.getBudgetReportService(), app.getBudgetService());
        
        this.budgetedVsActualController = new BudgetedVsActualController(view.getBudgetedVsActualView(), app.getBudgetReportService(), app.getBudgetService());
        budgetedVsActualController.setOnEdited(e -> profitAndLossController.reload());

        this.accountBalanceView = view.getAccountBalanceView();

        this.selectMonthSubscription = newSelectMonthSubscription();

    }

    public BudgetMonth getSelectedMonth() {
        LocalDate selected = view.monthSelector().getSelectionModel().getSelectedItem();
        return app.getBudgetService().month(selected);
    }

    private Subscription newSelectMonthSubscription() {
        return view.monthSelector().getSelectionModel().selectedItemProperty().subscribe(this::clearAndSelectMonth);
    }

    public void reload() {
        selectMonthSubscription.unsubscribe();
        LocalDate key = view.monthSelector().getSelectionModel().getSelectedItem();
        view.monthSelector().getItems().clear();
        view.populateMonthSelector(budgetService.monthKeys(), key);;
        selectMonthSubscription = newSelectMonthSubscription();
    }

    private void clearAndSelectMonth(LocalDate key) {
        selectMonth(key);
        setAccountBalance(key);
        profitAndLossController.load(key);
        budgetedVsActualController.load(key);

    }

    private void setAccountBalance(LocalDate key) {
        Map<String, BigDecimal> balanceData = new TreeMap<>();
        if (budgetService.nextMonth(key) == null) {
            app.getAccountService().getAccounts().forEach(e -> balanceData.put(e.getAccountNumber(), e.getCurrentBalance()));
        } else {
            budgetService.nextMonth(key);
            app.getAccountService().getAccounts().forEach(e -> balanceData.put(e.getAccountNumber(), e.getOpeningBalanceOn(key)));
        }
        accountBalanceView.setData(balanceData);
    }

    private void selectMonth(LocalDate key) {
        BudgetMonth month = budgetService.month(key);
        view.selectedMonth(month.financialMonth(), month.financialYear());
        view.variance().setText("€ " + month.variance().toPlainString());
        view.closing().setText("€ " + month.closing().toPlainString());
    }

    public void setOnReviewTransactions(EventHandler<ActionEvent> eh) {
        view.getReviewTransactionsButton().setOnAction(eh);
    }

}
