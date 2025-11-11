package bank2budget.adapters.reader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 *
 * @author joostmeulenkamp
 */
public class FileUtil {

    private static final Logger LOGGER = Logger.getLogger(FileUtil.class.getName());

    public static List<Path> filterDirectoryByExtension(String extension, Path dir) {

        try (Stream<Path> dirContent = Files.list(dir)) {
            return dirContent
                    .filter(path -> (Files.isRegularFile(path) && path.toString().toLowerCase().endsWith(extension.toLowerCase())))
                    .toList();

        } catch (IOException ex) {
            LOGGER.log(Level.INFO, "Error reading directory {0}: {1}", new Object[]{dir.getFileName(), ex.getMessage()});
        }
        return Collections.emptyList();
    }

    public static List<Path> filterFilesByExtension(String extension, Path... paths) {
        List<Path> result = new ArrayList<>();
        for (Path p : paths) {
            if (Files.isRegularFile(p) && p.toString().toLowerCase().endsWith(extension.toLowerCase())) {
                result.add(p);
            }
        }
        return result;
    }
}
