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

	static void replace(File file, String regex, String replacement) throws IOException {
		write(file, read(file).replace(regex, replacement));
	}
	
	static boolean contains(File file, CharSequence match) throws IOException {
		return read(file).contains(match);
	}
	
	static String read(File file) throws IOException {
		try (FileInputStream in = new FileInputStream(file) ; ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
			int ch = -1;
			while ((ch = in.read()) != -1)
			    bos.write(ch);
			return new String(bos.toByteArray());
		}

	}

	static void write(File file, String content) throws IOException {
		try(BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
			writer.write(content);
		}
	}
	
}
