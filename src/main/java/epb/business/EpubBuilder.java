package epb.business;

import epb.utils.CsvUtils;
import epb.utils.FileUtils;
import epb.utils.IOUtils;
import org.apache.tools.ant.BuildException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import static epb.utils.FileUtils.*;

/**
 * Build the epub file
 */
public class EpubBuilder {

    private static final Logger LOGGER = Logger.getLogger(EpubBuilder.class.getName());

    private static final String OEBPS = "OEBPS/";
    private static final String CONTENT_OPF = "/OEBPS/content.opf";
    private static final String TOC_NCX = "/OEBPS/toc.ncx";
    private static final String TOC_XHTML = "/OEBPS/toc.xhtml";

    private String base;
    private String coverImage;
    private String tocTitle;
    private String lang;
    private File baseDir;
    private File destDir;
    private int targetVersion;

    public EpubBuilder(String base, String coverImage, String tocTitle, String lang, String destDir, int targetVersion) {
        this.base = base;
        this.baseDir = new File(base);
        this.coverImage = coverImage;
        this.tocTitle = tocTitle;
        this.lang = lang;
        this.destDir = new File(destDir);
        this.targetVersion = targetVersion;
    }

    public void buildEpubFile() {
        LOGGER.info("creating manifest in " + destDir.getAbsolutePath());
        final String manifest = generateManifest();
        try {
            IOUtils.replace(new File(destDir, CONTENT_OPF), "${manifest}", manifest);
        } catch (IOException e) {
            throw new BuildException(e);
        }
        LOGGER.info("reading CSV file");
        List<String[]> csv = readCSV();
        if (csv.isEmpty()) {
            throw new BuildException("Index file can't be empty");
        }
        final String firstElem = csv.get(0)[0];
        int startIndex = 0;
        // legacy mode : in first version in index.csv a line could start with it's playOrder
        // now by default if lines don't start with a number it is considered sorted (playOrder not mandatory)
        try {
            Integer.parseInt(firstElem);
            LOGGER.info("sorting CSV content");
            CsvUtils.sortCSVData(csv);
            startIndex = 1;
        } catch (NumberFormatException e) {
            LOGGER.info("Index is already sorted");
        }
        final StringBuilder spineBuilder = new StringBuilder();
        final StringBuilder tocBuilder = new StringBuilder();
        final StringBuilder navmapBuilder = new StringBuilder();
        LOGGER.info("creating spine, toc and navmap");
        buildSpineTocAndNavmap(csv, spineBuilder, tocBuilder, navmapBuilder, startIndex);
        try {
            IOUtils.replace(new File(destDir, CONTENT_OPF), "${spine}", spineBuilder.toString());
            if (2 == targetVersion) IOUtils.replace(new File(destDir, TOC_NCX), "${navmap}", navmapBuilder.toString());
            IOUtils.write(new File(destDir, TOC_XHTML), tocBuilder.toString());
        } catch (IOException e) {
            throw new BuildException(e);
        }
        File contributorsFile = new File(baseDir, "contributors.properties");
        String contributors = new ContributorListBuilder(contributorsFile, targetVersion).build();
        try {
            IOUtils.replace(new File(destDir, CONTENT_OPF), "${contributors}", contributors);
        } catch (IOException e) {
            throw new BuildException(e);
        }
        addMetadataInXHTMLFiles(csv, startIndex);
    }

    private String getId(String href) {
        final String idref = href.contains("/") ? href.substring(href.lastIndexOf('/') + 1) : href;
        String id;
        if (coverImage.equals(OEBPS + href)) {
            id = "cover";
        } else if (href.equalsIgnoreCase("toc")) {
            id = "toc";
        } else {
            id = encodeId(idref);
        }
        return id;
    }

    private String encodeId(String id) {
        return (id.matches("[A-Za-z].*")) ? id : "a" + id;
    }


    private void addMetadataInXHTMLFiles(List<String[]> csv, int csvStartIndex) {
        csv.forEach(line -> addMetadataInXHTMLFile(line, csvStartIndex));
    }

    private void addMetadataInXHTMLFile(String[] line, int csvStartIndex) {
        final String href = line[csvStartIndex];
        final String type = line[csvStartIndex + 2];
        if (!"".equals(type) && !href.equalsIgnoreCase("toc")) {
            final File file = new File(destDir, href);
            try {
                // lang
                addLang(file);
                // epub:type if epub 3
                if (3 == targetVersion) {
                    addEpubType(file, type);
                }
            } catch (IOException e) {
                throw new BuildException(e);
            }
        }
    }

    private void addLang(File file) throws IOException {
        if (!IOUtils.contains(file, "xml:lang")) {
            IOUtils.replace(file, "<html", "<html xml:lang=\"" + lang + "\"");
            // because iBooks (and inDD export) sometimes lang is searched on body...
            IOUtils.replace(file, "<body", "<body xml:lang=\"" + lang + "\"");
        }
    }

    private void addEpubType(File file, String type) throws IOException {
        if (!IOUtils.contains(file, "epub:type")) {
            IOUtils.replace(file, "<html", "<html xmlns:epub=\"http://www.idpf.org/2007/ops\"");
            IOUtils.replace(file, "<body", "<body epub:type=\"" + type + "\"");
        }
    }


    private void buildSpineTocAndNavmap(List<String[]> csvData, StringBuilder spineBuilder, StringBuilder tocBuilder, StringBuilder navmapBuilder, int csvStartIndex) {
        openToc(tocBuilder);
        int currentDepth = 0;
        int count = 0;
        LOGGER.info("reading the index");
        for (String[] line : csvData) {
            if (line.length - csvStartIndex < 4) {
                throw new BuildException("CSV data invalid : " + Arrays.toString(line));
            }
            String href = line[csvStartIndex].trim();
            int depth = Integer.parseInt(line[csvStartIndex + 1]);
            // type not used here
            StringBuilder titleBuilder = new StringBuilder();
            titleBuilder.append(line[csvStartIndex + 3].trim());
            // if more than 5 rebuild the last string
            for (int i = csvStartIndex + 4; i < line.length; i++) {
                titleBuilder.append(";").append(line[i]);
            }
            String title = titleBuilder.toString();
            // the spine
            String id = getId(href);
            spineBuilder.append("<itemref idref=\"").append(id).append("\" />\n");
            if ("".equals(title)) continue; // no title : only in spine not toc or nav
            // the toc & the navMap
            if (count > 0 && depth == currentDepth) {
                tocBuilder.append("</li>\n");
                navmapBuilder.append("</navPoint>\n");
            } else if (depth > currentDepth) {
                do {
                    tocBuilder.append("<ol>\n");
                } while (++currentDepth < depth);
            } else if (depth < currentDepth) {
                do {
                    tocBuilder.append("</li>\n</ol>\n");
                    navmapBuilder.append("</navPoint>\n");
                } while (--currentDepth > depth);
                tocBuilder.append("</li>\n");
                navmapBuilder.append("</navPoint>\n");
            }
            if (href.startsWith(OEBPS)) href = href.substring(6);
            else if (href.startsWith("/"+OEBPS)) href = href.substring(7);
            else if (id.equals("toc")) href = "toc.xhtml";
            tocBuilder.append("<li><a href=\"").append(href).append("\">");
            tocBuilder.append(title);
            tocBuilder.append("</a>\n");
            navmapBuilder.append("<navPoint id=\"navpoint").append(count++).append("\" playOrder=\"");
            navmapBuilder.append(count).append("\"");
            if (currentDepth < 5) navmapBuilder.append(" class=\"h").append(currentDepth + 1).append("\"");
            navmapBuilder.append(">");
            navmapBuilder.append("\n<navLabel>\n<text>");
            navmapBuilder.append(title).append("</text>\n</navLabel>\n");
            if (href.equalsIgnoreCase("toc")) href = "toc.xhtml";
            navmapBuilder.append("<content src=\"").append(href).append("\" />\n");
        }
        for (int i = currentDepth; i >= 0; i--) {
            tocBuilder.append("</li></ol>");
            navmapBuilder.append("</navPoint>\n");
        }
        closeToc(tocBuilder);
    }

    // create xhtml file header
    private void openToc(StringBuilder tocBuilder) {
        tocBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        tocBuilder.append("<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:epub=\"http://www.idpf.org/2007/ops\">\n");
        tocBuilder.append("<head>\n");
        String title = tocTitle == null ? "" : tocTitle;
        tocBuilder.append("<title>").append(title).append("</title>\n");
        // is there a header-toc.part file
        File headerToc = new File(baseDir, "header-toc.part");
        if (headerToc.exists()) {
            try {
                LOGGER.info("including content of " + headerToc);
                tocBuilder.append(IOUtils.read(headerToc));
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "failed to include header-toc.part content", e);
            }
        }
        tocBuilder.append("</head>\n");
        tocBuilder.append("<body>\n");
        // is there a pre-toc.part file
        File preToc = new File(baseDir, "pre-toc.part");
        if (preToc.exists()) {
            try {
                LOGGER.info("including content of " + preToc);
                tocBuilder.append(IOUtils.read(preToc));
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "failed to include pre-toc.part content", e);
            }
        } else {
            if (tocTitle != null) tocBuilder.append("<h2>").append(tocTitle).append("</h2>\n");
        }
        if (3 == targetVersion) tocBuilder.append("<nav epub:type=\"toc\">\n");
        tocBuilder.append("<ol>\n");
    }

    // create xhtml file footer
    private void closeToc(StringBuilder tocBuilder) {
        if (3 == targetVersion)
            tocBuilder.append("</nav>\n");
        // is there a post-toc.part file
        File postToc = new File(baseDir, "post-toc.part");
        if (postToc.exists()) {
            try {
                LOGGER.info("including content of " + postToc);
                tocBuilder.append(IOUtils.read(postToc));
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "failed to include post-toc.part content", e);
            }
        }
        tocBuilder.append("</body>\n</html>");
    }

    private String generateManifest() {
        StringBuilder sb = new StringBuilder();
        final File textDir = new File(baseDir, "/OEBPS/text");
        if (textDir.exists()) {
            String[] texts = nullSafeFilter.apply(textDir, filterXHTMLfiles);
            for (String text : texts) {
                LOGGER.info("creating manifest item for " + text);
                String href = "text/" + text;
                String id = getId(text);
                sb.append("<item id=\"").append(id).append("\" href=\"")
                        .append(href);
                sb.append("\" media-type=\"application/xhtml+xml\" />").append(
                        "\n");
            }
        }
        final File imgDir = new File(baseDir, "/OEBPS/images");
        if (imgDir.exists()) {
            String[] images = nullSafeFilter.apply(imgDir, filterImagefiles);
            for (String image : images) {
                LOGGER.info("creating manifest item for " + image);
                String href = "images/" + image;
                String id = coverImage.equals("OEBPS/" + href) ? "cover"
                        : getId(image);
                sb.append("<item id=\"").append(id).append("\" href=\"")
                        .append(href).append("\"");
                if (coverImage.equals("OEBPS/" + href) && targetVersion == 3) {
                    sb.append(" properties=\"cover-image\"");
                }
                if (href.endsWith("jpg")) {
                    sb.append(" media-type=\"image/jpeg\" />");
                }
                else if (href.endsWith("png")) {
                    sb.append(" media-type=\"image/png\" />");
                }
                sb.append("\n");
            }
            // if license or readme included
            String[] texts = nullSafeFilter.apply(imgDir, filterTxtfiles);
            for (String text : texts) {
                LOGGER.info("creating manifest item for " + text);
                String href = "images/" + text;
                sb.append("<item id=\"").append(text).append("\" href=\"")
                        .append(href);
                sb.append("\" media-type=\"text/plain\" />").append("\n");
            }
        }
        final File styleDir = new File(baseDir, "/OEBPS/styles");
        if (styleDir.exists()) {
            String[] styles = nullSafeFilter.apply(styleDir, filterCssfiles);
            for (String style : styles) {
                LOGGER.info("creating manifest item for " + style);
                String href = "styles/" + style;
                sb.append("<item id=\"").append(style).append("\" href=\"")
                        .append(href);
                sb.append("\" media-type=\"text/css\" />").append("\n");
            }
            // if license or readme included
            String[] texts = nullSafeFilter.apply(styleDir, filterTxtfiles);
            for (String text : texts) {
                LOGGER.info("creating manifest item for " + text);
                String href = "styles/" + text;
                sb.append("<item id=\"").append(text).append("\" href=\"")
                        .append(href);
                sb.append("\" media-type=\"text/plain\" />").append("\n");
            }
        }
        final File fontsDir = new File(baseDir, "/OEBPS/fonts");
        if (fontsDir.exists()) {
            String[] fonts = nullSafeFilter.apply(fontsDir, filterFontfiles);
            for (String font : fonts) {
                LOGGER.info("creating manifest item for " + font);
                String href = "fonts/" + font;
                sb.append("<item id=\"").append(font).append("\" href=\"")
                        .append(href);
                sb.append("\" media-type=\"font/opentype\" />").append("\n");
            }
            // if license or readme included
            String[] texts = nullSafeFilter.apply(fontsDir, filterTxtfiles);
            for (String text : texts) {
                LOGGER.info("creating manifest item for " + text);
                String href = "fonts/" + text;
                sb.append("<item id=\"").append(text).append("\" href=\"")
                        .append(href);
                sb.append("\" media-type=\"text/plain\" />").append("\n");
            }
        }
        return sb.toString();
    }

    // read the CSV file without using third part library
    private List<String[]> readCSV() {
        try{
            return CsvUtils.readCSV(new File(new File(base), "index.csv"));
        } catch (IOException e) {
            throw new BuildException(e);
        }
    }

}
