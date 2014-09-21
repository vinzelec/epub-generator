package epb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

public class Epub extends Task {

	private String base, coverImage, tocTitle, lang;
	private File baseDir, destDir;
	private int target;
	
	public void setBase(String base) {
		this.base = base;
		this.baseDir = new File(base);
	}

	public void setDir(String dir) {
		destDir = new File(dir);
	}

	public void setCoverImage(String coverImage) {
		this.coverImage = coverImage;
	}

	public void setTocTitle(String tocTitle) {
		this.tocTitle = tocTitle;
	}

	public void setTarget(int target) {
		this.target = target;
	}
	
	private String getId(String href) {
		String idref = href.contains("/") ? href.substring(href.lastIndexOf('/')+1) : href;
		return coverImage.equals("OEBPS/"+href) ? "cover" 
				: href.equalsIgnoreCase("toc") ? "toc" : encodeId(idref);
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	private String encodeId(String id) {
		return (id.matches("[A-Za-z].*")) ? id : "a"+id;
	}

	@Override
	public void execute() throws BuildException {
		log("creating manifest");
		String manifest = generateManifest();
		try {
			IOUtils.replace(new File(destDir, "/OEBPS/content.opf"), "${manifest}", manifest);
		} catch (IOException e) {
			throw new BuildException(e);
		}
		log("reading CSV file");
		List<String[]> csv = readCSV();
		if(csv.size() == 0) {
			throw new BuildException("Index file can't be empty");
		}
		String firstElem = csv.get(0)[0];
		int startIndex = 0;
		try {
			Integer.parseInt(firstElem);
			log("sorting CSV content");
			sortCSVData(csv);
			startIndex = 1;
		} catch (NumberFormatException e) {
			log("Index is already sorted");
		}
		StringBuilder spineBuilder = new StringBuilder(), tocBuilder = new StringBuilder(), navmapBuilder = new StringBuilder();
		log("creating spine, toc and navmap");
		buildSpineTocAndNavmap(csv, spineBuilder, tocBuilder, navmapBuilder, startIndex);
		try {
			IOUtils.replace(new File(destDir, "/OEBPS/content.opf"), "${spine}", spineBuilder.toString());
			if(2 == target) IOUtils.replace(new File(destDir, "/OEBPS/toc.ncx"), "${navmap}", navmapBuilder.toString());
			IOUtils.write(new File(destDir, "/OEBPS/toc.xhtml"), tocBuilder.toString());
		} catch (IOException e) {
			throw new BuildException(e);
		}
		File contributorsFile = new File(new File(base), "contributors.properties");
		String contributors = "";
		if(contributorsFile.exists()) {
			contributors = createContributorsList(contributorsFile);
		}
		try {
			IOUtils.replace(new File(destDir, "/OEBPS/content.opf"), "${contributors}", contributors);
		} catch (IOException e) {
			throw new BuildException(e);
		}
		addMetadataInXHTMLFiles(csv, startIndex);
	}
	
	private void addMetadataInXHTMLFiles(List<String[]> csv, int csvStartIndex) {
		for (String[] line : csv) {
			String href = line[csvStartIndex], type = line[csvStartIndex+2];
			if(!"".equals(type)) {
				if(href.equalsIgnoreCase("toc")) continue; // already taken care of
				File file = new File(destDir, href);
				try {
					// lang
					if(!IOUtils.contains(file, "xml:lang")){
						IOUtils.replace(file, "<html", "<html xml:lang=\""+lang+"\"");
						// because iBooks (and inDD export) sometimes lang is searched on body...
						IOUtils.replace(file, "<body", "<body xml:lang=\""+lang+"\"");
					}
					// epub:type if epub 3
					if(3 == target) {
						if(IOUtils.contains(file, "epub:type")) continue;
						IOUtils.replace(file, "<html", "<html xmlns:epub=\"http://www.idpf.org/2007/ops\"");
						IOUtils.replace(file, "<body", "<body epub:type=\""+type+"\"");
					} 
				} catch (IOException e) {
					throw new BuildException(e);
				}
			}
		}
	}

	private void buildSpineTocAndNavmap(List<String[]> csvData, StringBuilder spineBuilder, StringBuilder tocBuilder, StringBuilder navmapBuilder, int csvStartIndex) {
		openToc(tocBuilder);
		int currentDepth = 0;
		int count = 0;
		log("reading the index");
		for(String[] line : csvData) {
			if(line.length-csvStartIndex < 4) {
				throw new BuildException("CSV data invalid : " + Arrays.toString(line));
			}
			String href = line[csvStartIndex].trim();
			int depth = Integer.parseInt(line[csvStartIndex+1]);
			// type not used here
			String title = line[csvStartIndex+3].trim();
			// if more than 5 rebuild the last string
			for(int i = csvStartIndex+4 ; i < line.length ; i++) title += ";"+line[i];
			// the spine
			String id = getId(href);
			spineBuilder.append("<itemref idref=\"").append(id).append("\" />\n");
			if("".equals(title)) continue; // no title : only in spine not toc or nav
			// the toc & the navMap
			if(count > 0 && depth == currentDepth) {
				tocBuilder.append("</li>\n");
				navmapBuilder.append("</navPoint>\n");
			}
			else if(depth > currentDepth) {
				do {
					tocBuilder.append("<ol>\n");
				} while (++currentDepth < depth);
			}
			else if(depth < currentDepth) {
				do {
					tocBuilder.append("</li>\n</ol>\n");
					navmapBuilder.append("</navPoint>\n");
				} while (--currentDepth > depth);
				tocBuilder.append("</li>\n");
				navmapBuilder.append("</navPoint>\n");
			}
			if(href.startsWith("OEBPS/")) href = href.substring(6);
			else if(href.startsWith("/OEBPS/")) href = href.substring(7);
			else if (id.equals("toc")) href = "toc.xhtml";
			tocBuilder.append("<li><a href=\"").append(href).append("\">");
			tocBuilder.append(title);
			tocBuilder.append("</a>\n");
			navmapBuilder.append("<navPoint id=\"navpoint").append(count++).append("\" playOrder=\"");
			navmapBuilder.append(count).append("\"");
			if(currentDepth < 5) navmapBuilder.append(" class=\"h").append(currentDepth+1).append("\"");
			navmapBuilder.append(">");
			navmapBuilder.append("\n<navLabel>\n<text>");
			navmapBuilder.append(title).append("</text>\n</navLabel>\n");
			if(href.equalsIgnoreCase("toc")) href = "toc.xhtml";
			navmapBuilder.append("<content src=\"").append(href).append("\" />\n");
		}
		for(int i = currentDepth ; i >= 0 ; i--){
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
		if(headerToc.exists()) {
			try {
				log("including content of "+headerToc);
				tocBuilder.append(IOUtils.read(headerToc));
			} catch (IOException e) {
				log("failed to include header-toc.part content", e, Project.MSG_WARN);
			}
		}		
		tocBuilder.append("</head>\n");
		tocBuilder.append("<body>\n");
		// is there a pre-toc.part file
		File preToc = new File(baseDir, "pre-toc.part");
		if(preToc.exists()) {
			try {
				log("including content of "+preToc);
				tocBuilder.append(IOUtils.read(preToc));
			} catch (IOException e) {
				log("failed to include pre-toc.part content", e, Project.MSG_WARN);
			}
		} else {
			if(tocTitle != null) tocBuilder.append("<h2>").append(tocTitle).append("</h2>\n");
		}
		if(3 == target) tocBuilder.append("<nav epub:type=\"toc\">\n");
		tocBuilder.append("<ol>\n");
	}

	// create xhtml file footer
	private void closeToc(StringBuilder tocBuilder) {
		// tocBuilder.append("</ol>\n");
		if (3 == target)
			tocBuilder.append("</nav>\n");
		// is there a post-toc.part file
		File postToc = new File(baseDir, "post-toc.part");
		if (postToc.exists()) {
			try {
				log("including content of "+postToc);
				tocBuilder.append(IOUtils.read(postToc));
			} catch (IOException e) {
				log("failed to include post-toc.part content", e,
						Project.MSG_WARN);
			}
		}
		tocBuilder.append("</body>\n</html>");
	}
	
	private String generateManifest() {
		StringBuilder sb = new StringBuilder();
		File textDir = new File(baseDir, "/OEBPS/text");
		File imgDir = new File(baseDir, "/OEBPS/images");
		File styleDir = new File(baseDir, "/OEBPS/styles");
		if (textDir.exists()) {
			String[] texts = textDir.list(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(".xhtml");
				}
			});
			for (int i = 0; i < texts.length; i++) {
				log("creating manifest item for " + texts[i]);
				String href = "text/" + texts[i];
				String id = getId(texts[i]);
				sb.append("<item id=\"").append(id).append("\" href=\"")
						.append(href);
				sb.append("\" media-type=\"application/xhtml+xml\" />").append(
						"\n");
			}
		}
		if(imgDir.exists()) {
			String[] images = imgDir.list(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(".jpg") || name.endsWith(".png");
				}
			});
			for (int i = 0; i < images.length; i++) {
				log("creating manifest item for " + images[i]);
				String href = "images/" + images[i];
				String id = coverImage.equals("OEBPS/" + href) ? "cover"
						: getId(images[i]);
				sb.append("<item id=\"").append(id).append("\" href=\"")
						.append(href).append("\"");
				if (coverImage.equals("OEBPS/" + href) && target == 3)
					sb.append(" properties=\"cover-image\"");
				if(href.endsWith("jpg"))
					sb.append(" media-type=\"image/jpeg\" />");
				else if(href.endsWith("png"))
					sb.append(" media-type=\"image/png\" />");
				sb.append("\n");
			}
		}
		if (styleDir.exists()) {
			String[] styles = styleDir.list(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(".css");
				}
			});
			for (int i = 0; i < styles.length; i++) {
				log("creating manifest item for " + styles[i]);
				String href = "styles/" + styles[i];
				String id = styles[i];
				sb.append("<item id=\"").append(id).append("\" href=\"")
						.append(href);
				sb.append("\" media-type=\"text/css\" />").append("\n");
			}
		}
		return sb.toString();
	}
	
	private static void sortCSVData(List<String[]> data) {
		Collections.sort(data, new Comparator<String[]>() {
			@Override
			public int compare(String[] o1, String[] o2) {
				assert o1.length > 1 && o2.length > 1 : "CSV data invalid";
				Integer playorder1 = Integer.parseInt(o1[0]), playorder2  = Integer.parseInt(o2[0]);
				return playorder1.compareTo(playorder2);
			}
		});
	}
	
	// read the CSV file without using third part library
	private List<String[]> readCSV() throws BuildException {
		BufferedReader in = null;
		try {
			List<String[]> list = new ArrayList<String[]>();
			in = new BufferedReader(new FileReader(new File(new File(base), "index.csv")));
		    String line = "";
		    while((line = in.readLine()) != null) {
		    	if(line.trim().length() == 0) {
		    		continue; // ignoring empty line
		    	}
		    	if(line.endsWith(";")) {
		    		line = line+" ";// adding an extra space so split works correctly
		    	}
		    	list.add(line.split(";"));
		    }
			return list;
		} catch (IOException e) {
			throw new BuildException(e);
		} finally {
			if(in != null){
				try {
					in.close();
				} catch (IOException e) {
					// ignore failing to close stream
				}
			}
		}
	}
	
	private String createContributorsList(File contributorsFile) throws BuildException {
		StringBuilder sb = new StringBuilder();
		Properties content = new Properties();
		try {
			content.load(new FileReader(contributorsFile));
		} catch (IOException e) {
			throw new BuildException(e);
		}
		for(Object key : content.keySet()) {
			String role = ((String) key).trim();
			String[] contribs = ((String) content.getProperty(role)).split(",");
			for (int i = 0; i < contribs.length; i++) {
				sb.append("<dc:contributor  id=\"").append(role).append(i).append("\"");
				if(target == 2) sb.append(" opf:role=\"").append(role).append("\"");
				sb.append(">");
				sb.append(contribs[i].trim()).append("</dc:contributor>\n");
				if(target == 3) {
					sb.append("<meta refines=\"#").append(role).append(i);
					sb.append("\" property=\"role\" scheme=\"marc:relators\">");
					sb.append(role).append("</meta>\n");
				}
			}
		}
		return sb.toString();
	}
}
