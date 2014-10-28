epub-generator
==============

Script Ant pour générer des fichiers epub (epub2 et epub3) à partir de métadonnées et de fichiers bruts (xhtml, images, css)


Avant-propos
------------
###Prérequis
Pour l'utilisation du script les programmes suivants doivent être installé sur votre ordinateur

* Java runtime environment (1.6 ou plus)
  * téléchargeable sur [le site d'Oracle](http://www.oracle.com/technetwork/java/index.html)
* Ant script environment (1.9 ou plus)
  * téléchargeable sur [la page Ant](http://ant.apache.org/)
  
Puis dans le répertoire d'epub-generator il faut lancer la commande `ant -f compile.xml`

###Note importante à propos de ZIP
À cause de la manière dont la tâche 'zip' est implémentée dans ant (et java) and avec les requis du standard OPF,
le script ne fonctionne qu'avec la commande zip du système (activée avec l'option `-DuseSystemZip=true`).
Cette commande existe par défaut dans les système unix mais doit être installée pour windows.
Vous pouvez trouver  [un jeu de commandes unix](http://sourceforge.net/projects/unxutils/files/latest/download) 
contenant une version de zip (une fois dézippé n'importe où, la commande doit être ajoutée au path utilisateur)

###Nota Bene sur XHTML
Libre Office et Open Office proposent une fonction d'export xhtml mais le résultat n'est pas très propre (CSS inséré directement dans le fichier),
préférez un outil comme le plugin writer2epub qui créé déjà un epub (le fichier peut être dézippé pour extraire chaque fichier)
avec une séparation claire des fichiers xhtml et css.
À moins de n'éditer directement tous les fichiers au format xhtml bien sûr...


L'exemple
-----------
Le répertoire "in" contient un répertoire *example* montrant la structure expliqué plus bas.  


HOW-TO utilisation du script
----------------------------
###La structure du répertoire
Voici la structure classique avec laquelle travailler 
(aucun répertoire n'est obligatoire ; chacun peut contenir en plus des fichiers  .txt par exemple pour ajouter des licenses) :

    my-book
       +- OEBPS
            +- fonts (y mettre les fichiers de polices au format .otf)
            +- images (y mettre les fichiers des images utilisées aux formats .jpg ou .png)
            +- styles (y mettre les fichiers des styles utilisées au format .css)
            +- text (y mettre les fichiers des textes au format .xhtml)
    
###Le sommaire (table of content)
Within the my-book folder, the file `index.csv` will be used to automatically generate the table of content :

* the toc.ncx file for epub2;
* the toc.xhtml using epub3 and to be included within the book.

####toc.part files
As the xhtml file for the table of content can be generated and included into the book, you can customize it by putting optionnal files in my-book :

* header-toc.part : add html code in the header (add css import for example);
* pre-toc.part : add html code before the `nav` (or `ul` if generating epub2) element (if absent a `h2` title will be generated using the entry in index.csv file, see below);
* post-toc.part : add html code after the `nav` (or `ul` if generating epub2) element.

####index.csv file detail
This file will be used to generate the table of content and order the pages of the ebook.  
Each line represent an entry for a file.  
Each line has the following format : `(playOrder;)file;depth;type*;(title)`  
* playOrder is optional (if not present, the file is supposed sorted).
* The file is relative to the my-book directory.
* The depth start at index 0 (first-level) and use to create hierarchy (chapter, sub-chapter...).
* The type is keyword from [Epub vocabulary](http://idpf.org/epub/vocab/structure/) that will be included into file for epub 3 format (it is assume xhtml don't already embed epub3 metadata)
* The title is the display name of the entry within the table of content, if omitted the entry will be played in the book but won't be indexed in the table of content.

From the example:

    OEBPS/text/00_-_1_cover.xhtml;0;cover; Couverture  
    OEBPS/text/00_-_2_title.xhtml;0;titlepage; Page de titre  
    OEBPS/text/00_-_3_avantpropos.xhtml;0;preface; Avant-Propos  
    OEBPS/text/00_-_4_pubLAFA.xhtml;0;; Pub éhontée pour l'éditeur d'origine  
    OEBPS/text/01_-_Je_meurs_comme_j_ai_vecu.xhtml;0;titlepage; Je meurs comme j’ai vécu  
    OEBPS/text/02_-_credits.xhtml;0;backmatter; Crédits  

###The metadata files
Within the my-book folder, 2 properties files will be used to generate the metadata of the generated epub.  

####metadata.properties
The first and only mandatory is called `metadata.properties` (don't know why for french ebooks it is required to be in ISO-8859-1 while everything else is UTF-8 if there is any accented letter). The properties to set are the following:

* epb.author = The author of the book
* epb.filename = The file name to generate (the script will generate a out/filename-epub2.epub or out/filename-epub3.epub depending on parameters)
* epb.publisher = The publisher of the book
* epb.title = The title of the book
* epb.lang = The... lang of the book
* epb.tags = list of comma-separated tags (keywords for ebook indexing)
* epb.coverFile = The path to the file used as cover (usually OEBPS/text/cover-file.xhtml)

####contributors.properties
The second (optional) metadata file is called `contributors.properties` and is used to add contributors to the book.  
The keys are [MARC relators](http://www.loc.gov/marc/relators/relaterm.html) and the values a comma-separated list of contributors.  
For example:

    ill = a person that did an illustration, another illustrator  
    crr = the person that did the correction  
    edt = the editor  


####The optional replace.properties file
During the ebook creation, a task of cleaning is performed on every XHTML file (mostly to clean the xhtml export that creates code like &lt;b>This&lt;/b>&lt;b> &lt;/b>&lt;b>sentence&lt;/b>&lt;b>.&lt;/b>)  
(as a consequence classes should not be placed on tags `i|em|b|strong|s|strike|u` to avoid the cleaning to change the code behavior)
  
In addition to automatic cleaning process, a special mechanism to perform specific replacement is implemented. It is used by adding an optional `replace.properties` file
where the key is the matching pattern (according to [Ant replace task](https://ant.apache.org/manual/Tasks/replace.html)) and the value the replacement.  
Example:

    <p>— : <p class\="dialogue">—    
    <p class\="center">*</p> : <p class\="separator">*</p>
    
    
    
Run the script
--------------

###default script use
Once the project is set up, the script can be run (from command line in epub-generator folder) by calling the program `ant`.  
One parameter is mandatory : `-Dbase` that indicate the path to the `my-book` folder.  
For example : `ant -Dbase=in/my-book`  

Then there is several optional parameter :

* `-Dtarget` indicates which version of epub is to be generated. The authorized values are `2` and `3` (default value is `3`)  
* `-DuseSystemZip` indicates to use the system `zip` command instead of ant task. The authorized values are `true` and `false` (default value is `false` but due to a bug – issue #2 – this has to be set to true)  
* `-Doutfile` allows to select an output filename that is different to the one defined in `metadata.properties`. For example `-Doutfile=another.epub` (the name must contain the .epub extension)  
* `-Doverwrite` allows to indicate the path to a folder to use to overwrite some files from default base folder. It allows to create an alternative version of the book without having to modify the source.

###other tasks available
You can list all available target by typing the command `ant -p`. Here are the list and a description on how to use it.  
Any task can be called by addind the name as first argument of the `ant` program (before all the `-D` parameters): `ant all` for example.

####partial epub construction
This script performs several steps, it is possible to stop the process in the middle. Of course some parameters have no impact on some target (`overwrite` is used since `skeleton`, but `useSystemZip` won't be used until `pack`)

> skeleton: generates an ebook skeleton (folder structure, with files included generated ones)  
> cleanFiles: cleans the xhtml content (performs the cleaning of all xhtml files)  
> pack: packages the epub file (zips the skeleton into an epub file)  
> all: default behavior: creates the epub file then checks it's validity  

Example: `ant skeleton -Dbase=in/example -Dtarget=2` to create only a folder with files for an epub2 format.  


####other tasks
Following are tasks that don't belong to default behavior of the script but revealed to be useful. `clean` and `onlyEpubCheck` are the only targets that don't require the parameter `-Dbase`.

> clean: cleans the 'out' directory (removes all generated files)  
> unifyNames: renames correctly (no space or accented letter) input xhtml files within -Dbase '/OEBPS/text' folder  
> makeIndex: list files in 'OEBPS/text' in alphabetic order to help create an index.csv file  
> onlyEpubCheck: checks validity for an epub file indicated with -Depb.filename  
> loop: loops over several overwrite alternative and recursively call the main task, -Doverwrite must be the path to a folder where each folder as a book structure
(all output file names will be the ones of the overwriting folder)

Example: `ant loop -Dbase=in/example -Doverwrite=ext/folder-of-all-overwrite` to create several alternatives of the book from a folder of overwrite.
