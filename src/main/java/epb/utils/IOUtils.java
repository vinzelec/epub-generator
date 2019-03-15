package epb.utils;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Should probably be replaced by apache commons IO...
 * 
 * @author Vinze
 *
 */
public interface IOUtils {

	/**
	 * Open a file, replace a regex by some content and writes the files.
	 * 
	 * @param file the file to miodify
	 * @param regex the regex to search
	 * @param replacement the replacement for the searched pattern
	 * @throws IOException in case of problem reading or writing the file
	 */
	static void replace(File file, String regex, String replacement) throws IOException {
		write(file, read(file).replace(regex, replacement));
	}

	/**
	 * Search a pattern in a file
	 * 
	 * @param file the file to search in
	 * @param match the pattern to search for a match
	 * @return <code>true</code> if the pattern was found
	 * @throws IOException in case of problem reading the file
	 */
	static boolean contains(File file, CharSequence match) throws IOException {
		return read(file).contains(match);
	}
	
	/**
	 * @deprecated use commons-io instead
	 */
	static String read(File file) throws IOException {
		try (FileInputStream in = new FileInputStream(file) ; ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
			int ch = -1;
			while ((ch = in.read()) != -1)
			    bos.write(ch);
			return new String(bos.toByteArray());
		}

	}

	/**
	 * @deprecated use commons-io instead
	 */
	static void write(File file, String content) throws IOException {
		try(BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
			writer.write(content);
		}
	}
	
}
