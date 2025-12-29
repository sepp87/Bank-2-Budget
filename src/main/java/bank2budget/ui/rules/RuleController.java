package bank2budget.ui.rules;

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

    public RuleController(RuleView rulesView) {
        this.view = rulesView;
    }
    
        public void load(List<EditableRuleConfig> rules) {
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


    public List<EditableRuleConfig> rules() {
        return List.copyOf(view.getRuleTable().getItems());
    }

    public void setOnFinished(EventHandler<ActionEvent> eh) {
        view.getFinishButton().setOnAction(eh);
    }

    public void setOnCanceled(EventHandler<ActionEvent> eh) {
        view.getCancelButton().setOnAction(eh);
    }
}
