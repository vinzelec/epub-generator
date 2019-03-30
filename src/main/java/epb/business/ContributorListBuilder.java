package epb.business;

import epb.utils.FileUtils;
import org.apache.tools.ant.BuildException;

import java.io.File;
import java.util.Arrays;
import java.util.Properties;

/**
 * Builds the contributor list for OPF file from the property file
 */
public class ContributorListBuilder {

    private File contributorsFile;
    private int targetVersion;

    private int indexCounter = 0;

    public ContributorListBuilder(File contributorsFile, int targetVersion) {
        this.contributorsFile = contributorsFile;
        this.targetVersion = targetVersion;
    }

    public String build() {
        final StringBuilder sb = new StringBuilder();
        if(contributorsFile.exists()) {
            final Properties content = FileUtils.readFileProperties(contributorsFile);
            if (content.isEmpty()) {
                throw new BuildException("failed to read contributors file");
            }
            content.keySet().forEach(
                    key -> addContributors(sb, content, ((String) key).trim())
            );

        }
        return sb.toString();
    }

    private void addContributors(StringBuilder sb, Properties content, String role) {
        String[] contribs = content.getProperty(role).split(",");
        Arrays.stream(contribs).forEach(
                contrib -> addContributor(role, sb, contrib.trim())
        );
    }

    private void addContributor(String role, StringBuilder sb, String contributor) {
        String id = role + indexCounter++;
        sb.append("<dc:contributor  id=\"").append(id).append("\"");
        if (targetVersion == 2) {
            sb.append(" opf:role=\"").append(role).append("\"");
        }
        sb.append(">");
        sb.append(contributor).append("</dc:contributor>\n");
        if (targetVersion == 3) {
            sb.append("<meta refines=\"#").append(id);
            sb.append("\" property=\"role\" scheme=\"marc:relators\">");
            sb.append(role).append("</meta>\n");
        }
    }

}
