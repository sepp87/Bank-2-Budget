package bank2budget.ui.rules;

import java.util.List;
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

    public void load(List<EditableRuleConfig> categories) {
        view.getRuleTable().getItems().setAll(categories);
        view.getRuleTable().sort();
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
