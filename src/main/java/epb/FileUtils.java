package epb;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * functions to filter files in a directory.
 */
abstract class FileUtils {

    static BiFunction<File, FilenameFilter, String[]> nullSafeFilter = (file, filter) ->
            Optional.of(file).map(f -> f.list(filter)).orElse(new String[]{});

    static FilenameFilter filterXHTMLfiles = (dir, name) -> name.endsWith(".xhtml");

    static FilenameFilter filterImagefiles = (dir, name) -> name.endsWith(".jpg") || name.endsWith(".png");

    static FilenameFilter filterTxtfiles = (dir, name) -> name.endsWith(".txt");

    static FilenameFilter filterCssfiles = (dir, name) -> name.endsWith(".css");

    static FilenameFilter filterFontfiles = (dir, name) -> name.endsWith(".otf");


}
