epub-generator
==============

version courante : 0.1

Script Ant pour g�n�rer des fichiers epub (epub2 et epub3) � partir de m�tadonn�es et de fichiers bruts (xhtml, images, css)


Avant-propos
------------
###Pr�requis
Pour l'utilisation du script les programmes suivants doivent �tre install� sur votre ordinateur

* Java runtime environment (1.6 ou plus)
  * t�l�chargeable sur [le site d'Oracle](http://www.oracle.com/technetwork/java/index.html)
* Ant script environment (1.9 ou plus)
  * t�l�chargeable sur [la page Ant](http://ant.apache.org/)
  
Puis dans le r�pertoire d'epub-generator il faut lancer la commande `ant -f compile.xml`

###Note importante � propos de ZIP
� cause de la mani�re dont la t�che 'zip' est impl�ment�e dans ant (et java) and avec les requis du standard OPF,
le script ne fonctionne qu'avec la commande zip du syst�me (activ�e avec l'option `-DuseSystemZip=true`).
Cette commande existe par d�faut dans les syst�me unix mais doit �tre install�e pour windows.
Vous pouvez trouver [un jeu de commandes unix](http://sourceforge.net/projects/unxutils/files/latest/download) 
contenant une version de zip (une fois d�zipp� n'importe o�, la commande doit �tre ajout�e au path utilisateur)

###Nota Bene sur XHTML
Libre Office et Open Office proposent une fonction d'export xhtml mais le r�sultat n'est pas tr�s propre (CSS ins�r� directement dans le fichier),
pr�f�rez un outil comme le plugin writer2epub qui cr�� d�j� un epub (le fichier peut �tre d�zipp� pour extraire chaque fichier)
avec une s�paration claire des fichiers xhtml et css.
� moins de n'�diter directement tous les fichiers au format xhtml bien s�r...


L'exemple
-----------
Le r�pertoire "in" contient un r�pertoire *example* montrant la structure expliqu� plus bas.  


HOW-TO utilisation du script
----------------------------
###La structure du r�pertoire
Voici la structure classique avec laquelle travailler 
(aucun r�pertoire n'est obligatoire ; chacun peut contenir en plus des fichiers  .txt par exemple pour ajouter des licenses) :

    my-book
       +- OEBPS
            +- fonts (y mettre les fichiers de polices au format .otf)
            +- images (y mettre les fichiers des images utilis�es aux formats .jpg ou .png)
            +- styles (y mettre les fichiers des styles utilis�es au format .css)
            +- text (y mettre les fichiers des textes au format .xhtml)
    
###Le sommaire (table of content)
Dans le r�pertoire my-book, le fichier `index.csv` va �tre utilis� pour cr�er le sommaire :

* le fichier toc.ncx utilis� par epub2;
* le fichier toc.xhtml utilis� epub3 et utilisable dans l'ebook.

####fichiers toc.part
Comme le fichier xhtml du sommaire peut �tre inclus dans le livre, vous pouvez le customiser en ajouter des fichiers dans my-book :

* header-toc.part : ajoute du code html dans le header (par exemple pour ajouter un import CSS) ;
* pre-toc.part : ajoute du code html avant l'�l�ment `nav` (ou `ul` si on g�n�re du epub2) 
(si absent, un titre `h2` est ajout� utilisant l'entr�e du fichier index.csv, voir ci-dessous) ;
* post-toc.part : ajoute du code html apr�s l'�l�ment `nav` (ou `ul` si on g�n�re du epub2).

####d�tail du fichier index.csv
ce fichier est utilis� pour g�n�rer le sommaire et l'ordre de lecture des pages du livre.  
Chaque ligne est une entr�e pour un fichier
Chaque ligne suit le format suivant : `(playOrder;)fichier;profondeur;type*;(titre)`  

* playOrder est optionnel (en cas d'absence, le fichier est consid�r� d�j� tri�),
* Le chemin du fichier est relatif au r�pertoire my-book,
* La profondeur d�bute � l'index 0 (premier niveau) et est utilis�e pour cr�er la hi�rarchie (chapitre, sous-chapitre...),
* Le type est un mot-clef issu du [vocabulaire Epub](http://idpf.org/epub/vocab/structure/) qui sera inclus dans les fichiers pour epub3 (on suppose que les fichiers xhtml ne contiennent pas de m�tadonn�es epub3)
* Le titre est le nom � afficher pour l'entr�e dans le sommaire ; s'il est absent, l'entr�e sera inclus dans l'ordre de lecture mais n'appara�tra pas au sommaire g�n�r�.

Exemple :

    OEBPS/text/00_-_1_cover.xhtml;0;cover; Couverture  
    OEBPS/text/00_-_2_title.xhtml;0;titlepage; Page de titre  
    OEBPS/text/00_-_3_avantpropos.xhtml;0;preface; Avant-Propos  
    OEBPS/text/00_-_4_pubLAFA.xhtml;0;; Pub �hont�e pour l'�diteur d'origine  
    OEBPS/text/01_-_Je_meurs_comme_j_ai_vecu.xhtml;0;titlepage; Je meurs comme j�ai v�cu  
    OEBPS/text/02_-_credits.xhtml;0;backmatter; Cr�dits  

###The metadata files
Dans le r�pertoire my-book, deux fichiers properties sont utilis�s pour compl�ter le fichier epub g�n�r�.  

####metadata.properties
Le premier (et seul obligatoire) est `metadata.properties` (je ne sais pas trop pourquoi mais pour les lettre accentu�es, �a bug � moins que le fichier soit en ISO-8859-1 alors que le reste est en UTF-8). Voici les propri�t�s � renseigner dans ce fichier :

* epb.author = L'auteur du livre
* epb.filename = Le nom du fichier � g�n�rer (le script va g�n�rer un fichier out/filename-epub2.epub ou out/filename-epub3.epub en fonction des param�tres)
* epb.publisher = Le nom du publieur (�diteur puisqu'il n'y a pas de scission de ces deux notions dans l'�dition fran�aise)
* epb.title = Le titre du livre
* epb.lang = La langue dans laquelle le livre est �crit
* epb.tags = une liste de mots-clefs s�par�s par des virgules (pour l'indexation du fichier)
* epb.coverFile = Le chemin du fichier � utiliser comme couverture (en g�n�ral OEBPS/text/cover-file.xhtml)

####contributors.properties
Le second fichier (optionnel) est `contributors.properties` qui sert � ajouter des contributeurs au livre.
Les clefs des propri�t�s sont des [MARC relators](http://www.loc.gov/marc/relators/relaterm.html) et les valeurs associ�s une liste (s�par�s par des virgules) des personnes ayant rempli ce r�le.  
Par exemple:

    ill = premier illustrateur, un autre illustrateur  
    crr = le correcteur  
    edt = l'�diteur  


####Le fichier optionnel replace.properties
Au cours de la cr�ation de l'ebook, une t�che de nettoyage est appliqu�e � chaque fichier XHTML (principalement pour nettoyer l'export xhtml qui cr�e du code comme &lt;b>Cette&lt;/b>&lt;b> &lt;/b>&lt;b>phrase&lt;/b>&lt;b>.&lt;/b>)  
(par cons�quent des classes ne doivent pas �tre plac�s sur les tags `i|em|b|strong|s|strike|u` pour �viter que cette t�che n'en change le comportement)
  
En plus du processus de nettoyage automatique, un m�canisme sp�cial pour faire du remplacement sp�cifique est impl�ment�. Il s'utilise en ajoutant un fichier `replace.properties` o� la clef est un pattern matching (respectant [la t�che Ant replace](https://ant.apache.org/manual/Tasks/replace.html))
et la valeur est la cha�ne de remplacement.  
Exemple (ajout de classe pour les dialogues et les s�parateurs) :

    <p>� : <p class\="dialogue">�    
    <p\u0020class\="center">*</p> : <p class\="separator">*</p>
    
    
    
Ex�cuter le script
------------------

###utilisation par d�faut du script
Une fois le projet install�, le script se lance (en ligne de commande dans le r�pertoire epub-generator) en appelant le programme `ant`.  
Un param�tre est obligatoire : `-Dbase` qui indique le chemin du r�pertoire `my-book`.  
Par exemple : `ant -Dbase=in/my-book`  

Il y a ensuite diff�rents param�tres optionnels :

* `-Dtarget` indique la version d'epub � g�n�rer. Les valeurs possibles sont `2` et `3` (`3` par d�faut)  
* `-DuseSystemZip` indique d'utiliser la commande `zip` du syst�me au lieu de la t�che ant. Les valeurs possibles sont `true` et `false` (`false` par d�faut mais � cause du bug � issue #2 � il faut utiliser `true`)  
* `-Doutfile` permets de s�lectionner un fichier de sortie diff�rent de celui d�finit dans `metadata.properties`. Par exemple `-Doutfile=another.epub` (le nom doit contenir l'extension .epub)  
* `-Doverwrite` permets d'indiquer un chemin vers un r�pertoire � utiliser pour surcharger des fichiers du r�pertoire par d�faut. Ainsi on peut cr�er une version alternative du livre sans en modifier les sources.

###autres t�ches disponibles
La liste des t�ches disponibles peut �tre obtenue avec la commande `ant -p`. Voici la liste des t�ches ansi que leurs descriptions et leur mode d'utilisation.  
Une t�che s'appelle en ajoutant son nom comme premier argument de la commande `ant` (avant les param�tres en `-D`) : par example `ant all`.

####construction partielle de l'epub
Le script se d�roule en plusieurs �tapes, il est possible d'arr�ter le processus en cours. Bien s�r certains param�tres n'ont pas d'impact sur certaines target (`overwrite` est utilis� � partir de `skeleton`, mais `useSystemZip` ne l'est pas avant `pack`)

> skeleton: cr�e un squelette d'ebook (structure des dossiers, avec les fichiers incluant ceux g�n�r�s automatiquement)  
> cleanFiles: nettoie le contenu des fichiers xhtml  
> pack: r�alise le pacquettage des fichiers de l'epub (zip le squelette dans un fichier epub)  
> all: comportement par d�faut : cr�e le fichier epub puis v�rifie sa validit�  

Exemple: `ant skeleton -Dbase=in/example -Dtarget=2` cr�e seulement un dossier avec les fichiers contenus dans un epub 2.  


####autres t�ches
Ci-dessous se trouvent des t�ches qui n'appartiennent pas au comportement par d�faut du script mais s'av�rent utiles.
`clean` et `onlyEpubCheck` sont les seuls ne n�cessitant pas le param�tre `-Dbase`.

> clean: vide le r�pertoire 'out' (supprime tous les fichiers g�n�r�s)  
> unifyNames: renomme correctement (pas d'espace ou de lettre accentu�e) les fichiers xhtml d'entr�e dans le sous-r�pertoire '/OEBPS/text' de -Dbase  
> makeIndex: fait la liste des fichiers dans 'OEBPS/text' par ordre alphab�tique pour aider � la cr�ation du fichier index.csv  
> onlyEpubCheck: v�rifie la validit� d'un fichier epub soumis par le param�tre -Depb.filename  
> loop: boucle sur plusieurs alternative d'overwrite et appelle r�cursivement la t�che principale,
-Doverwrite doit indiquer le chemin d'un r�pertoire ou chaque sous-r�pertoire respecte la structure d'un livre
(tous les fichiers en sortie porteront le nom du sous-r�pertoire correspondant)

Exemple: `ant loop -Dbase=in/example -Doverwrite=ext/folder-of-all-overwrite` va cr�er plusieurs alternatives du livre � partir d'un r�pertoire de plusieurs variantes.
