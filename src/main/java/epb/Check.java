package epb;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import com.adobe.epubcheck.api.EpubCheck;
import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.util.FeatureEnum;

/**
 * Ant task to wrap {@link EpubCheck}
 * 
 * @author Vinze
 *
 */
public class Check extends Task {

	String file;
	
	public void setFile(String file) {
		this.file = file;
	}

	@Override
	public void execute() throws BuildException {
		Report report = new Report() {
			int warningCount = 0, errorCount = 0;
			@Override
			public int getWarningCount() {
				return warningCount;
			}
			@Override
			public int getErrorCount() {
				return errorCount;
			}
			@Override
			public int getExceptionCount() {
				return 0;
			}
			@Override
			public void info(String resource, FeatureEnum feature, String message) {
				// too verbose...
//				if(null != resource) log("INFO on [" + resource + "] " +" [FEATURE="+feature+"] :"+ message);
//				else  log("INFO [FEATURE="+feature+"] : " + message);
			}
			@Override
			public void hint(String resource, int line, int column, String message) {
				if(null != resource) log("HINT on [" + resource + "] at line " + line + ", column " + column + " : " + message);
				else  log("HINT : " + message);
			}
			@Override
			public void warning(String resource, int line, int column, String message) {
				warningCount++;
				if(null != resource) log("WARNING on [" + resource + "] at line " + line + ", column " + column + " : " + message);
				else  log("WARNING : " + message);
			}
			@Override
			public void error(String resource, int line, int column, String message) {
				errorCount++;
				if(null != resource) log("ERROR on [" + resource + "] at line " + line + ", column " + column + " : " + message);
				else  log("ERROR : " + message);
			}
			@Override
			public void exception(String resource, Exception e) {
				if(null != resource) throw new BuildException("exception while inspecting resource ["+resource+"]", e);
				throw new BuildException(e);
			}
		};
		EpubCheck check = new EpubCheck(new File(file), report);
		check.validate();
		if(0 < report.getErrorCount()) throw new BuildException(report.getErrorCount()+" error(s) and "+ report.getWarningCount() +" warning(s) reported by epubcheck");
		log(file+" is valid with " + report.getWarningCount() + " warning(s)");
	}
	
}
