package epb.utils;

import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;
import java.util.function.BiFunction;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * functions to filter files in a directory.
 */
public abstract class FileUtils {

    private static final Logger LOGGER = Logger.getLogger(FileUtils.class.getName());

    private FileUtils() {
    }

    public static final BiFunction<File, FilenameFilter, String[]> nullSafeFilter = (file, filter) ->
            Optional.of(file).map(f -> f.list(filter)).orElse(new String[]{});

    public static final FilenameFilter filterXHTMLfiles = (dir, name) -> name.endsWith(".xhtml");

    public static final FilenameFilter filterImagefiles = (dir, name) -> name.endsWith(".jpg") || name.endsWith(".png");

    public static final FilenameFilter filterTxtfiles = (dir, name) -> name.endsWith(".txt");

    public static final FilenameFilter filterCssfiles = (dir, name) -> name.endsWith(".css");

    public static final FilenameFilter filterFontfiles = (dir, name) -> name.endsWith(".otf");

    /**
     * Reads a list of properties files and return as one merged properties.<br />
     * Any file that cannot be read is ignored
     *
     * @param files
     * @return
     */
    public static Properties readFileProperties(File... files) {
        Properties properties = new Properties();
        Arrays.stream(files).forEach(
                f -> {
                    try {
                        properties.load(new FileReader(f));
                    } catch (IOException e) {
                        LOGGER.log(Level.WARNING, "failed to read file "+f, e);
                    }
                }
        );
        return properties;
    }

}
