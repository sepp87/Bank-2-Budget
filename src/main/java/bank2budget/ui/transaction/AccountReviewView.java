package bank2budget.ui.transaction;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 *
 * @author joostmeulenkamp
 */
public class AccountReviewView extends VBox {

    private final TabPane accountTabPane;
    private final Button cancelButton;
    private final Button finishButton;

    public AccountReviewView() {
        this.finishButton = new Button("Finish");
        this.cancelButton = new Button("Cancel");
        HBox controls = new HBox(finishButton, cancelButton);
        controls.setAlignment(Pos.CENTER_RIGHT);

        this.accountTabPane = new TabPane();
        accountTabPane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
        VBox.setVgrow(accountTabPane, Priority.ALWAYS);
        VBox.setVgrow(this, Priority.ALWAYS);

        this.getChildren().addAll(controls, accountTabPane);
        this.getStyleClass().add("overlay-modal");

    }

    public void addTab(String name, Node content, boolean isActive) {
        Tab tab = new Tab(name);
        tab.setContent(content);
        accountTabPane.getTabs().add(tab);
        if (isActive) {
            accountTabPane.getSelectionModel().select(tab);
        }
    }

    public Button getFinishButton() {
        return finishButton;
    }

    public Button getCancelButton() {
        return cancelButton;
    }

}
