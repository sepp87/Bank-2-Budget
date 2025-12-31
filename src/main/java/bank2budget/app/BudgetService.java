package bank2budget.app;

import bank2budget.core.budget.Budget;
import bank2budget.core.budget.BudgetCalculator;
import bank2budget.core.budget.BudgetMonth;
import bank2budget.core.budget.BudgetTemplate;
import bank2budget.ports.BudgetRepositoryPort;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author joostmeulenkamp
 */
public class BudgetService {

    private final AccountService accountService;
    private final BudgetRepositoryPort<Budget> newRepo;
    private final Budget newBudget;
    private final BudgetCalculator calculator;
    private final BudgetTemplate template;

    public BudgetService(AccountService accountService, BudgetRepositoryPort<Budget> newRepo, BudgetCalculator calculator, BudgetTemplateService templateService) {

        this.accountService = accountService;
        this.newRepo = newRepo;
        this.newBudget = newRepo.load();
        this.calculator = calculator;
        this.template = templateService.getTemplate();

        List<BudgetMonth> created = calculator.create(template, newBudget, accountService.getAccounts());
        List<BudgetMonth> replaced = newBudget.replace(created);

        accountService.setOnAccountsUpdated(this::recalculate);
    }

    public void load() {
//        this.budget = repository.load();
//        budget.setAccounts(accountService.getAccounts());
    }

    public Budget getBudget() {
        if (this.newBudget == null) {
            load();
        }
        return newBudget;
    }

    public BudgetTemplate getBudgetTemplate() {
        return template;
    }

    public BudgetMonth month(LocalDate firstOfMonth) {
        return newBudget.month(firstOfMonth);
    }

    public List<BudgetMonth> months() {
        return newBudget.months();
    }

    public Set<LocalDate> monthKeys() {
        return newBudget.monthKeys();
    }

    public BudgetMonth nextMonth(LocalDate firstOfMonth) {
        return newBudget.nextMonth(firstOfMonth);
    }

    public void importFromTodoAndSave() {
        if (accountService.importFromTodoAndSave()) {
            load();
            save();
        }
    }

    public void recalculateAndSave() {
        recalculate();
        save();
    }

    private final List<Runnable> onBudgetRecalculatedListeners = new ArrayList<>();

    public void setOnBudgetRecalculated(Runnable r) {
        onBudgetRecalculatedListeners.add(r);
    }

    private void onBudgetRecalculated() {
        onBudgetRecalculatedListeners.forEach(Runnable::run);
    }

    public void recalculate() {
        List<BudgetMonth> created = calculator.create(template, newBudget, accountService.getAccounts());
        List<BudgetMonth> replaced = newBudget.replace(created);

        onBudgetRecalculated();
    }

    private void save() {
        newRepo.save(newBudget);
    }

    public void setBudgetedForCategory(LocalDate firstOfMonth, String category, BigDecimal budgeted) {
        var updated = calculator.updateBudgeted(newBudget, firstOfMonth, category, budgeted);
        newBudget.replace(updated);
    }

    public void setAdjustmentsForCategory(LocalDate firstOfMonth, String category, BigDecimal adjustments) {
        var updated = calculator.updateAdjustments(newBudget, firstOfMonth, category, adjustments);
        newBudget.replace(updated);
    }

}
