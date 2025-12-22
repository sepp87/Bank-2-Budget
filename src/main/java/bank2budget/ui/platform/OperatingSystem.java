package bank2budget.ui.platform;

/**
 *
 * @author joostmeulenkamp
 */
public enum OperatingSystem {
    WINDOWS,
    MACOS,
    LINUX,
    SOLARIS,
    OTHER_OS;

    public static OperatingSystem current() {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")) {
            return OperatingSystem.WINDOWS;
        } else if (osName.contains("mac")) {
            return OperatingSystem.MACOS;
        } else if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
            return OperatingSystem.LINUX;
        } else if (osName.contains("sunos")) {
            return OperatingSystem.SOLARIS;
        } else {
            return OperatingSystem.OTHER_OS;
        }
    }
}
