Le but de cet ensemble de script est de cr�er un epub "propre" en partant d'un ensemble de fichiers (xhtml, css, images + metadonn�es).

Il n�cessite que soit install� :
- un environnement Java (jre 1.6 min)
- le programme ant (1.9) (t�l�chargeable : http://ant.apache.org/)

Avant d'utiliser le script il est n�cessaire de l'initialiser en tapant la ligne de commande suivante :
> ant -f compile.xml


� cause de la structure zip du format opf que la t�che "zip" de ant ne permet pas de respecter,
 il faut utiliser le script avec l'option -DuseSystemZip=true pour utiliser la commande zip du syst�me...
sous linux, zip est par d�faut, sous windows il faut t�l�charger les �x�cutables unix (http://sourceforge.net/projects/unxutils/files/latest/download)
et les ajouter au PATH pour le m�me r�sultat.

NB : j'ai test� une demi-douzaine de solutions avec la t�che zip de ant sans succ�s mais si vous avez une solution je suis preneur.

NB2 : pour les utilisateurs de Libre Office ou Open Office, le plugin writer2epub fait un export xhtml clean, il suffit ensuite de d�zipper
le fichier pour r�cup�rer lesdits fichiers.

---

Etape 1 : Cr�er la structure
voir le r�pertoire example : 
 - un r�pertoire OEBPS avec tous les fichiers de m�tadonn�es et des sous-r�pertoires contenant les textes, les images et les styles.
 - des fichiers de m�tadonn�es qui permettront de g�n�rer l'epub

Quelques notes sur la structure source :
- les noms de fichiers doivent �tre unique car utilis�s comme id (on peut avoir toto.xhtml et toto.jpg dans deux r�pertoires ou un m�me
mais pas deux fichiers toto.xhtml dans deux r�pertoire diff�rents).

---

Etape 2 :
Cr�er un fichier metadata.properties (de pr�f�rence en ISO pour les accents mais je sais pas trop pourquoi... � jongler entre windows et linux j'y pige queud')
Le fichier metadata.properties contient la d�finition des donn�es suivantes 
epb.filename = nom du fichier qui sera g�n�r� (sauf extension .epub qui sera ajout�e automatiquement)
epb.author = nom de l'auteur
epb.publisher = nom de l'�diteur
epb.title = Titre du livre
epb.lang = la langue du bouquin ("fr" pour le fran�ais)
epb.tags = liste de tags, s�par� par des virgules, pour les r�f�rencements des m�tadonn�es
epb.isbn = 978-2-00000-000-0
epb.coverFile = chemin/vers/le/fichier/de/couverture.jpg (typiquement OEBPS/images/cover.jpg)

(optionnel)
Cr�er le fichier des contributeurs (pour ajouter aux m�tadonn�es une liste de contributeur en epub 3)
contributors.properties contient les ensembles de paires (role, liste de personnes).
Les r�les sont des "relators" MARC (faites vous plaisir avec la liste : http://www.loc.gov/marc/relators/relaterm.html).
Par exemple :
aut = auteur1, auteur2, auteur3
ill = illustrateur1, illustrateur2 
crr = correcteur
cov = cover designer

---

Etape 3 :
Cr�er le fichier index.csv (en UTF-8... va comprendre) recensant l'encha�nement des fichiers xhtml sous le format suivant :
(une ligne :)fichier;profondeur;type*;titre
par exemple :
1;OEBPS/text/titlePage.xhtml;0;titlepage;Page de titre
2;toc;0;toc;Sommaire
3;OEBPS/text/content0001.xhtml;0;part;Partie 1
4;OEBPS/text/content0002.xhtml;1;chapter;Chapitre 1
5;OEBPS/text/content0003.xhtml;1;chapter;Chapitre 2
6;OEBPS/text/content0004.xhtml;0;part;Partie 2
7;OEBPS/text/content0005.xhtml;1;chapter;Chapitre 3

NB : Un �l�ment n'ayant pas de titre sera r�f�renc� dans le manifest et le spine mais sera invisible du sommaire dans toc.xhtml.

vous pouvez �galement remplacer le fichier par "toc" (ou "TOC") si vous voulez faire apparaitre � cet endroit la table des mati�res
automatiquement cr��e � partir de ce fichier (le type est alors superflu).
Concernant la g�n�ration vous pouvez 
- ajouter du contenu dans le header du fichier toc.xhtml (par exemple pour inclure un css personnalis�) en cr�ant un fichier header-toc.part
- ajouter du contenu avant le contenu g�n�r� du fichier toc.xhtml (par exemple pour personnaliser le titre) en cr�ant un fichier pre-toc.part
(si le fichier n'est pas pr�sent c'est le titre d�fini dans index.csv pour la ligne toc qui sera ins�r� comme titre dans la page)
- ajouter du contenu apr�s le contenu g�n�r� du fichier toc.xhtml (par exemple pour ajouter un NB) en cr�ant un fichier post-toc.part


* Pour la valeur de type accept�e : voir http://idpf.org/epub/vocab/structure/ 
(nb : le type ne sert � rien si la cible est epub 2)

(optionnel)
un fichier replace.properties (UTF-8)
la partie "cleanFiles" va remplacer automatiquement les motifs (surtout utilis� pour customiser les classes css d'�l�ments)... par exemple :
<p>� : <p class\="dialogue">�
<p>� : <p class\="dialogue">�
<p class\="center">*</p> : <p class\="separateur">*</p>

---

Etape 4, derni�re �tape :
Il faut lancer le script avec le param�tre "base" indiquant le chemin vers le r�pertoire contenant la structure.
Un param�tre optionnel "target" acceptant pour valeur 2 ou 3 permet de sp�cifier la version de l'epub g�n�r� (par d�faut 3).
Par exemple :
ant -Dbase=in/example -Dtarget=2
il est possible de n'appeler ensuite que certaines t�ches interm�diaires (voir le script ant, ou appeler ant -p)
S'il n'y a pas de probl�me, un fichier livre.epub a bien �t� cr�� dans le r�pertoire out.
L'encha�nement des t�ches est :
- skeleton (cr�e le r�pertoire out/livre et y ajoute le contenu et les m�tadonn�es)
- cleanFiles (nettoie tous les fichiers xhtml dans le r�pertoire out/livre)
- pack (zip le r�pertoire out/livre vers out/livre.epub et supprime le r�pertoire temporaire)
- check (lance l'application epubcheck)
(autre t�che : "clean" va supprimer tous les fichiers g�n�r�s en vidant le r�pertoire out)

Un param�tre optionnel "overwrite" a �t� ajout�, il permet de faire de l'�dition de version sp�ciale en d�finissant un 
r�pertoire dans lequel chercher des fichiers pour remplacer la version par d�faut. Il est possible de remplacer un fichier texte, image ou css.
On ne peut pas remplacer les m�tadonn�es ou ajouter des �l�ments au spine.
Il est possible de renommer avec un param�tre le fichier de sortie avec la propri�t� "outfile" (sans oublier l'extension .epub 
ou le chemin du r�pertoire de sortie)

La t�che loop permet d'appeler r�cursivement la t�che principale en it�rant sur l'ensemble des sous-dossiers de celui pass� en param�tre overwriteDir
