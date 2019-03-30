package epb.tasks;

import epb.utils.IOUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

/**
 * Replace all tokens in a file from the value of a property file.
 */
public class ReplaceTask extends Task {

    private String file;
    private String properties;

    public void setFile(String file) {
        this.file = file;
    }

    public void setProperties(String properties) {
        this.properties = properties;
    }

    @Override
    public void execute() {
        log("replacing patterns in "+file+" using "+properties);
        File fileRef = new File(file);
        Properties propertiesRef = new Properties();
        try {
            propertiesRef.load(new FileReader(new File(properties)));
            MutableString content = new MutableString(IOUtils.read(fileRef));
            propertiesRef.forEach((k,v) -> content.value = content.value.replace(k.toString(),v.toString()));
            IOUtils.write(fileRef, content.value);
        } catch (IOException e) {
            throw new BuildException("failed to replace all values from property file", e);
        }

    }

    /**
     * mutable reference to a string so it can be changed in a foreach lambda
     */
    private static class MutableString {

        String value;

        MutableString(String content) {
            this.value = content;
        }

        @Override
        public String toString() {
            return value;
        }
    }
}
