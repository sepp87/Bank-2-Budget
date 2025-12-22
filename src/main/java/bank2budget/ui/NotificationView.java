package bank2budget.ui;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 *
 * @author joostmeulenkamp
 */
public class NotificationView extends VBox {

    public NotificationView() {

        getStyleClass().add("notification-view");
//        this.setAlignment(Pos.BOTTOM_CENTER);

        if (false) {
            VBox n1 = new VBox(new Label("Import successful!"));
            VBox n2 = new VBox(new Label("Import aborted! Balance history interrupted."));

            n1.getStyleClass().add("notification");
            n2.getStyleClass().add("notification");
            n2.getStyleClass().add("error");

            getChildren().add(n1);
            getChildren().add(n2);
        }

    }

    public void showError(String message) {
        showNotification(message, true);
    }

    public void showNotification(String message) {
        showNotification(message, false);
    }

    private void showNotification(String message, boolean isError) {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> showNotification(message, isError));
            return;
        }

        Label label = new Label(message);
        VBox notificationBubble = new VBox();
        notificationBubble.getStyleClass().add("notification");
        if (isError) {
            notificationBubble.getStyleClass().add("error");
        }
        notificationBubble.getChildren().add(label);
        getChildren().add(notificationBubble);

        PauseTransition timer = new PauseTransition(Duration.seconds(2));
        timer.setOnFinished(e -> getChildren().remove(notificationBubble));
        timer.play();
    }
}
