package bank2budget.ui.transaction;

import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

/**
 *
 * @author joostmeulenkamp
 */
public class MultiAccountView extends TabPane {

    public MultiAccountView() {
        this.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
    }

    public void addTab(String name, Node content) {
        Tab tab = new Tab(name);
        tab.setContent(content);
        this.getTabs().add(tab);
    }

}
