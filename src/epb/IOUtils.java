package epb;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

public abstract class IOUtils {

	public static void replace(File file, String regex, String replacement) throws IOException {
		write(file, read(file).replace(regex, replacement));
	}
	
	public static boolean contains(File file, CharSequence match) throws IOException {
		return read(file).contains(match);
	}
	
	public static String read(File file) throws IOException {
		FileInputStream in = null;
		ByteArrayOutputStream bos = null;
		try {
			in = new FileInputStream(file);
			bos = new ByteArrayOutputStream();
			int ch = -1;
			while ((ch = in.read()) != -1)
			    bos.write(ch);
			String result = new String(bos.toByteArray());
			return result;
		} catch (IOException e) {
			throw e;
		} finally {
			if(null != in) in.close();
			if(null != bos) bos.close();
		}

	}

	public static void write(File file, String content) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		writer.write(content);
        writer.close();
	}
	
}
