package bank2budget.app;

import bank2budget.core.CashTransaction;
import bank2budget.core.rule.RuleConfig;
import bank2budget.core.rule.RuleEngine;
import bank2budget.core.rule.RuleFactory;
import bank2budget.ports.RuleRepositoryPort;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author joostmeulenkamp
 */
public class RuleService {

    private final RuleRepositoryPort repository;
    private final RuleEngine<CashTransaction> engine;
    private final List<RuleConfig> rules;
    private final Map<String, String> myAccounts;

    public RuleService(RuleRepositoryPort repository, RuleEngine<CashTransaction> engine, Map<String, String> myAccounts) {
        this.repository = repository;
        this.engine = engine;
        this.rules = new ArrayList<>(repository.load());
        this.myAccounts = myAccounts;
        setRules();
    }

    private void setRules() {
        var systemRules = RuleFactory.createSystemRules(myAccounts);
        var userRules = rules.stream().map(RuleFactory::create).toList();
        engine.setSystemRules(systemRules);
        engine.setUserRules(userRules);
    }

    public List<RuleConfig> getRules() {
        return List.copyOf(rules);
    }

    public List<CashTransaction> applyRules(List<CashTransaction> transactions) {
        return engine.applyRules(transactions);
    }

    public void save() {
        repository.save(rules);
    }

}
