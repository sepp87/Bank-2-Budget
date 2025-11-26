package bank2budget.ui;

import bank2budget.App;
import javafx.application.Application;

/**
 *
 * @author joostmeulenkamp
 */
public class UiAppRunner {

    private final App app;

    public UiAppRunner(App app) {
        this.app = app;
    }

    public void run() {
        UiApp.setApp(app);
        Application.launch(UiApp.class);
    }
}
