package bank2budget.ui;

import bank2budget.App;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import javafx.application.Application;
import javafx.application.Platform;
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
        EditorView editorView = new EditorView(app);
        EditorController editorController = new EditorController(editorView, app);
        
        Scene scene = new Scene(editorView, APP_WIDTH, APP_HEIGHT);
        setStylesheetToScene(scene);
        stage.setScene(scene);
        stage.show();
        stage.setTitle("Bank-2-Budget");
        stage.setFullScreen(false);
    }

    public static void setStylesheetToScene(Scene scene) {
        // Load the CSS from classpath using ClassLoader
        String stylesheetPath = "css/light.css";
        URL resourceUrl = UiApp.class.getClassLoader().getResource(stylesheetPath);

        if (resourceUrl != null) {
            scene.getStylesheets().clear();
            scene.getStylesheets().add(resourceUrl.toExternalForm());
            System.out.println("CSS Loaded: " + resourceUrl.toExternalForm());
        } else {
            System.err.println("Stylesheet not found: " + stylesheetPath);
            return;
        }

        // Enable file watching only if running in IDE (not in JAR)
        Path filePath = Paths.get("src/main/resources/", stylesheetPath); // Path in IDE
        if (Files.exists(filePath)) {
            watchForCssChanges(scene, filePath);
        }
    }

    private static void watchForCssChanges(Scene scene, Path path) {
        new Thread(() -> {
            try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
                path.getParent().register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
                while (true) {
                    WatchKey key = watchService.take();
                    for (WatchEvent<?> event : key.pollEvents()) {
                        if (event.context().toString().equals(path.getFileName().toString())) {
                            Platform.runLater(() -> {
                                scene.getStylesheets().clear();
                                scene.getStylesheets().add(path.toUri().toString());
                                System.out.println("CSS Reloaded!");
                            });
                        }
                    }
                    key.reset();
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

}
