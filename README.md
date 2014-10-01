epub-generator
==============

Ant script to generate an epub (epub2 and epub3) from metadata files and raw content (xhtml, images, css)


Preamble
--------
###Requirement
To use this script you need to install on your computer

* Java runtime environment (1.6 or more)
  * can be downloaded at [Oracle website](http://www.oracle.com/technetwork/java/index.html)
* Ant script environment (1.9 or more)
  * can be downloaded at [Ant page](http://ant.apache.org/)
  
Then in the epub-generator directory you must first run the command `ant -f compile.xml`

###Important note on ZIP
Because of how the zip task is implemented within ant (and java) and because of OPF standard,
the script only works using the zip command from the system (activated using option `-DuseSystemZip=true`)  
This command is default under unix system but needs to be installed for windows.  
You can find [A set of unix commands](http://sourceforge.net/projects/unxutils/files/latest/download) that contains zip implementation
(once deployed somewhere, the command directory has to be added to the user path)

###Nota Bene on XHTML
Libre Office and Open Office have xhtml export function but the result is not really clean (CSS is mixed),
you may prefer a tool like the writer2epub plugin that will create an epub (file that can be unzipped to extract each file)
with clean separation of xhtml and css.  
Unless you write all files directly into xhtml editor of course...


The example
-----------
The directory in contains a folder *example* that show what's explained next.  


HOW-TO use the script
---------------------
###The Directory structure
Here is the classic structure to work with (no directory is mandatory ; each can also contains .txt files if you want to add the licence of a file used) :

    my-book
       +- OEBPS
            +- fonts (put all fonts used in .otf format)
            +- images (put all images used in .jpg or .png formats)
            +- styles (put all styles used in .css format)
            +- text (put all texts used in .xhtml format)
    
###The table of content
Within the my-book folder, the file `index.csv` will be used to automatically generate the table of content :

* the toc.ncx file for epub2;
* the toc.xhtml using epub3 and to be included within the book.

####toc.part files
As the xhtml file for the table of content can be generated and included into the book, you can customize it by putting optionnal files in my-book :

* header-toc.part : add html code in the header (add css import for example);
* pre-toc.part : add html code before the `nav` (or `ul` if generating epub2) element (if absent a `h2` title will be generated using the entry in index.csv file, see below);
* post-toc.part : add html code after the `nav` (or `ul` if generating epub2) element.

####index.csv file detail
TODO

###The metadata files
Within the my-book folder, a set of properties files will be used to generate the metadata of the generated epub
TODO
    
    
