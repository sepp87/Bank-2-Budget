package bank2budget.ui;

import bank2budget.App;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author joostmeulenkamp
 */
public class UiApp extends Application {

    private static App app;

    public static void setApp(App app) {
        UiApp.app = app;
    }

    private static final double APP_WIDTH = 1200;
    private static final double APP_HEIGHT = 800;

    @Override
    public void start(Stage stage) throws Exception {
        Scene scene = new Scene(new EditorView(app.getRuleEngine()), APP_WIDTH, APP_HEIGHT);
        stage.setScene(scene);
        stage.show();
        stage.setTitle("Bank-2-Budget");
        stage.setFullScreen(false);
    }

}
