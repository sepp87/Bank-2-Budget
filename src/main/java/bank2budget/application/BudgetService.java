package bank2budget.application;

import bank2budget.core.budget.Budget;
import bank2budget.core.budget.BudgetCalculator;
import bank2budget.core.budget.BudgetMonth;
import bank2budget.core.budget.BudgetTemplate;
import bank2budget.ports.BudgetRepositoryPort;
import java.util.List;

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

    public BudgetService(AccountService accountService, BudgetRepositoryPort<Budget> newRepo, BudgetCalculator calculator, BudgetTemplate template) {

        this.accountService = accountService;
        this.newRepo = newRepo;
        this.newBudget = newRepo.load();
        this.calculator = calculator;
        this.template = template;

        List<BudgetMonth> created = calculator.create(template, newBudget, accountService.getAccounts());
        List<BudgetMonth> replaced = newBudget.replace(created);
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

    public void importFromTodoAndSave() {
        if (accountService.importFromTodoAndSave()) {
            load();
            save();
        }
    }

    public void recalculateAndSave() {
//        budget.setAccounts(accountService.getAccounts());
        save();
    }

    private void save() {
        newRepo.save(newBudget);
    }
}
