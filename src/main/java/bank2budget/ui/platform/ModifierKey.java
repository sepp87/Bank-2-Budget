package bank2budget.ui.platform;

import javafx.scene.input.KeyEvent;

/**
 *
 * @author joostmeulenkamp
 */
public class ModifierKey {

    public static boolean isKeyDown(KeyEvent event) {
        switch (OperatingSystem.current()) {
            case WINDOWS:
                return event.isControlDown();
            case MACOS:
                return event.isMetaDown();
            case LINUX:
                return event.isMetaDown();
            default:
                return event.isControlDown();
        }
    }
}
