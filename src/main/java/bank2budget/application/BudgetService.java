package bank2budget.application;

import bank2budget.core.MultiAccountBudget;
import bank2budget.ports.BudgetRepositoryPort;

/**
 *
 * @author joostmeulenkamp
 */
public class BudgetService {

    private final BudgetRepositoryPort repository;
    private final AccountService accountService;
    private final MultiAccountBudget budget;

    public BudgetService(BudgetRepositoryPort repository, AccountService accountService) {
        this.repository = repository;
        this.accountService = accountService;
        this.budget = repository.load();
        budget.setAccounts(accountService.getAccounts());
    }

    public MultiAccountBudget getBudget() {
        return budget;
    }

    public void importFromTodoAndSave() {
        if (accountService.importFromTodoAndSave()) {
            recalculateAndSave();
        }
    }

    public void recalculateAndSave() {
        budget.setAccounts(accountService.getAccounts());
        save();
    }

    private void save() {
        repository.save(budget);
    }
}
