package bank2budget.ui.rules;

import bank2budget.app.RuleService;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

/**
 *
 * @author joostmeulenkamp
 */
public class RuleController {

    private final RuleView view;
    private final RuleService service;

    public RuleController(RuleView rulesView, RuleService ruleService) {
        this.view = rulesView;
        this.service = ruleService;
    }

    public void reload() {
        var rules = service.getRules().stream().map(EditableRuleConfig::new).toList();
        load(rules);
    }

    private void load(List<EditableRuleConfig> rules) {
        loadCategorySuggestions(rules);
        view.getRuleTable().getItems().setAll(rules);
        view.getRuleTable().sort();
    }

    private void loadCategorySuggestions(List<EditableRuleConfig> rules) {
        Set<String> availableCategories = new HashSet<>();
        for (EditableRuleConfig r : rules) {
            availableCategories.add(r.resultValue());
        }
        view.setCategorySuggestions(availableCategories);
    }

    public void setOnFinished(EventHandler<ActionEvent> eh) {
        view.getFinishButton().setOnAction(eh);
    }

    public void setOnCanceled(EventHandler<ActionEvent> eh) {
        view.getCancelButton().setOnAction(eh);
    }

    public void commitChanges() {
        var rules = view.getRuleTable().getItems().stream().map(EditableRuleConfig::toDomain).toList();
        var existing = service.getRules();

        if (rules.size() != existing.size() || !rules.containsAll(existing)) {
            service.setRules(rules);
        }
    }
}
