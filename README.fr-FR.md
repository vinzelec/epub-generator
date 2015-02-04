epub-generator
==============

version courante : 0.1

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
Vous pouvez trouver [un jeu de commandes unix](http://sourceforge.net/projects/unxutils/files/latest/download) 
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
Dans le répertoire my-book, le fichier `index.csv` va être utilisé pour créer le sommaire :

* le fichier toc.ncx utilisé par epub2;
* le fichier toc.xhtml utilisé epub3 et utilisable dans l'ebook.

####fichiers toc.part
Comme le fichier xhtml du sommaire peut être inclus dans le livre, vous pouvez le customiser en ajouter des fichiers dans my-book :

* header-toc.part : ajoute du code html dans le header (par exemple pour ajouter un import CSS) ;
* pre-toc.part : ajoute du code html avant l'élément `nav` (ou `ul` si on génère du epub2) 
(si absent, un titre `h2` est ajouté utilisant l'entrée du fichier index.csv, voir ci-dessous) ;
* post-toc.part : ajoute du code html après l'élément `nav` (ou `ul` si on génère du epub2).

####détail du fichier index.csv
ce fichier est utilisé pour générer le sommaire et l'ordre de lecture des pages du livre.  
Chaque ligne est une entrée pour un fichier
Chaque ligne suit le format suivant : `(playOrder;)fichier;profondeur;type*;(titre)`  

* playOrder est optionnel (en cas d'absence, le fichier est considéré déjà trié),
* Le chemin du fichier est relatif au répertoire my-book,
* La profondeur débute à l'index 0 (premier niveau) et est utilisée pour créer la hiérarchie (chapitre, sous-chapitre...),
* Le type est un mot-clef issu du [vocabulaire Epub](http://idpf.org/epub/vocab/structure/) qui sera inclus dans les fichiers pour epub3 (on suppose que les fichiers xhtml ne contiennent pas de métadonnées epub3)
* Le titre est le nom à afficher pour l'entrée dans le sommaire ; s'il est absent, l'entrée sera inclus dans l'ordre de lecture mais n'apparaîtra pas au sommaire généré.

Exemple :

    OEBPS/text/00_-_1_cover.xhtml;0;cover; Couverture  
    OEBPS/text/00_-_2_title.xhtml;0;titlepage; Page de titre  
    OEBPS/text/00_-_3_avantpropos.xhtml;0;preface; Avant-Propos  
    OEBPS/text/00_-_4_pubLAFA.xhtml;0;; Pub éhontée pour l'éditeur d'origine  
    OEBPS/text/01_-_Je_meurs_comme_j_ai_vecu.xhtml;0;titlepage; Je meurs comme j’ai vécu  
    OEBPS/text/02_-_credits.xhtml;0;backmatter; Crédits  

###The metadata files
Dans le répertoire my-book, deux fichiers properties sont utilisés pour compléter le fichier epub généré.  

####metadata.properties
Le premier (et seul obligatoire) est `metadata.properties` (je ne sais pas trop pourquoi mais pour les lettre accentuées, ça bug à moins que le fichier soit en ISO-8859-1 alors que le reste est en UTF-8). Voici les propriétés à renseigner dans ce fichier :

* epb.author = L'auteur du livre
* epb.filename = Le nom du fichier à générer (le script va générer un fichier out/filename-epub2.epub ou out/filename-epub3.epub en fonction des paramètres)
* epb.publisher = Le nom du publieur (éditeur puisqu'il n'y a pas de scission de ces deux notions dans l'édition française)
* epb.title = Le titre du livre
* epb.lang = La langue dans laquelle le livre est écrit
* epb.tags = une liste de mots-clefs séparés par des virgules (pour l'indexation du fichier)
* epb.coverFile = Le chemin du fichier à utiliser comme couverture (en général OEBPS/text/cover-file.xhtml)

####contributors.properties
Le second fichier (optionnel) est `contributors.properties` qui sert à ajouter des contributeurs au livre.
Les clefs des propriétés sont des [MARC relators](http://www.loc.gov/marc/relators/relaterm.html) et les valeurs associés une liste (séparés par des virgules) des personnes ayant rempli ce rôle.  
Par exemple:

    ill = premier illustrateur, un autre illustrateur  
    crr = le correcteur  
    edt = l'éditeur  


####Le fichier optionnel replace.properties
Au cours de la création de l'ebook, une tâche de nettoyage est appliquée à chaque fichier XHTML (principalement pour nettoyer l'export xhtml qui crée du code comme &lt;b>Cette&lt;/b>&lt;b> &lt;/b>&lt;b>phrase&lt;/b>&lt;b>.&lt;/b>)  
(par conséquent des classes ne doivent pas être placés sur les tags `i|em|b|strong|s|strike|u` pour éviter que cette tâche n'en change le comportement)
  
En plus du processus de nettoyage automatique, un mécanisme spécial pour faire du remplacement spécifique est implémenté. Il s'utilise en ajoutant un fichier `replace.properties` où la clef est un pattern matching (respectant [la tâche Ant replace](https://ant.apache.org/manual/Tasks/replace.html))
et la valeur est la chaîne de remplacement.  
Exemple (ajout de classe pour les dialogues et les séparateurs) :

    <p>— : <p class\="dialogue">—    
    <p\u0020class\="center">*</p> : <p class\="separator">*</p>
    
    
    
Exécuter le script
------------------

###utilisation par défaut du script
Une fois le projet installé, le script se lance (en ligne de commande dans le répertoire epub-generator) en appelant le programme `ant`.  
Un paramètre est obligatoire : `-Dbase` qui indique le chemin du répertoire `my-book`.  
Par exemple : `ant -Dbase=in/my-book`  

Il y a ensuite différents paramètres optionnels :

* `-Dtarget` indique la version d'epub à générer. Les valeurs possibles sont `2` et `3` (`3` par défaut)  
* `-DuseSystemZip` indique d'utiliser la commande `zip` du système au lieu de la tâche ant. Les valeurs possibles sont `true` et `false` (`false` par défaut mais à cause du bug – issue #2 – il faut utiliser `true`)  
* `-Doutfile` permets de sélectionner un fichier de sortie différent de celui définit dans `metadata.properties`. Par exemple `-Doutfile=another.epub` (le nom doit contenir l'extension .epub)  
* `-Doverwrite` permets d'indiquer un chemin vers un répertoire à utiliser pour surcharger des fichiers du répertoire par défaut. Ainsi on peut créer une version alternative du livre sans en modifier les sources.

###autres tâches disponibles
La liste des tâches disponibles peut être obtenue avec la commande `ant -p`. Voici la liste des tâches ansi que leurs descriptions et leur mode d'utilisation.  
Une tâche s'appelle en ajoutant son nom comme premier argument de la commande `ant` (avant les paramètres en `-D`) : par example `ant all`.

####construction partielle de l'epub
Le script se déroule en plusieurs étapes, il est possible d'arrêter le processus en cours. Bien sûr certains paramètres n'ont pas d'impact sur certaines target (`overwrite` est utilisé à partir de `skeleton`, mais `useSystemZip` ne l'est pas avant `pack`)

> skeleton: crée un squelette d'ebook (structure des dossiers, avec les fichiers incluant ceux générés automatiquement)  
> cleanFiles: nettoie le contenu des fichiers xhtml  
> pack: réalise le pacquettage des fichiers de l'epub (zip le squelette dans un fichier epub)  
> all: comportement par défaut : crée le fichier epub puis vérifie sa validité  

Exemple: `ant skeleton -Dbase=in/example -Dtarget=2` crée seulement un dossier avec les fichiers contenus dans un epub 2.  


####autres tâches
Ci-dessous se trouvent des tâches qui n'appartiennent pas au comportement par défaut du script mais s'avèrent utiles.
`clean` et `onlyEpubCheck` sont les seuls ne nécessitant pas le paramètre `-Dbase`.

> clean: vide le répertoire 'out' (supprime tous les fichiers générés)  
> unifyNames: renomme correctement (pas d'espace ou de lettre accentuée) les fichiers xhtml d'entrée dans le sous-répertoire '/OEBPS/text' de -Dbase  
> makeIndex: fait la liste des fichiers dans 'OEBPS/text' par ordre alphabétique pour aider à la création du fichier index.csv  
> onlyEpubCheck: vérifie la validité d'un fichier epub soumis par le paramètre -Depb.filename  
> loop: boucle sur plusieurs alternative d'overwrite et appelle récursivement la tâche principale,
-Doverwrite doit indiquer le chemin d'un répertoire ou chaque sous-répertoire respecte la structure d'un livre
(tous les fichiers en sortie porteront le nom du sous-répertoire correspondant)

Exemple: `ant loop -Dbase=in/example -Doverwrite=ext/folder-of-all-overwrite` va créer plusieurs alternatives du livre à partir d'un répertoire de plusieurs variantes.
