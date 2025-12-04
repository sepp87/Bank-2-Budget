package bank2budget.application;

import bank2budget.core.MultiAccountBudget;
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

    private final BudgetRepositoryPort<MultiAccountBudget> repository;
    private final AccountService accountService;
    private MultiAccountBudget budget;
    private final BudgetRepositoryPort<Budget> newRepo;
    private final Budget newBudget;
    private final BudgetCalculator calculator;
    private final BudgetTemplate template;

    public BudgetService(BudgetRepositoryPort<MultiAccountBudget> repository, AccountService accountService, BudgetRepositoryPort<Budget> newRepo, BudgetCalculator calculator, BudgetTemplate template) {
        this.repository = repository;
        this.accountService = accountService;
        this.newRepo = newRepo;
        this.newBudget = newRepo.load();
        this.calculator = calculator;
        this.template = template;

        List<BudgetMonth> updated = calculator.createUpdated(template, newBudget, accountService.getAccounts());
        List<BudgetMonth> replaced = newBudget.replace(updated);
        
    }

    public void load() {
        this.budget = repository.load();
        budget.setAccounts(accountService.getAccounts());
    }

    public MultiAccountBudget getBudget() {
        if (this.budget == null) {
            load();
        }
        return budget;

    }

    public void importFromTodoAndSave() {
        if (accountService.importFromTodoAndSave()) {
            load();
            save();
        }
    }

    public void recalculateAndSave() {
        budget.setAccounts(accountService.getAccounts());
        save();
    }

    private void save() {
        repository.save(budget);
        newRepo.save(newBudget);
    }
}
