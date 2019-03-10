package epb.tasks;

import epb.business.EpubBuilder;
import org.apache.tools.ant.Task;

/**
 * Ant task wrapper to execute the {@link EpubBuilder}
 */
public class BuildEpubTask extends Task {

    private String base;
    private String coverImage;
    private String tocTitle;
    private String lang;
    private String destDir;
    private int targetVersion;

    public void setBase(String base) {
        this.base = base;
    }

    public void setDir(String dir) {
        destDir = dir;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public void setTocTitle(String tocTitle) {
        this.tocTitle = tocTitle;
    }

    public void setTarget(int target) {
        this.targetVersion = target;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    @Override
    public void execute() {
        EpubBuilder builder = new EpubBuilder(base, coverImage, tocTitle, lang, destDir, targetVersion);
        builder.buildEpubFile();
    }

}
