Le but de cet ensemble de script est de créer un epub "propre" en partant d'un ensemble de fichiers (xhtml, css, images + metadonnées).

Il nécessite que soit installé :
- un environnement Java (jre 1.6 min)
- le programme ant (1.9) (téléchargeable : http://ant.apache.org/)

Avant d'utiliser le script il est nécessaire de l'initialiser en tapant la ligne de commande suivante :
> ant -f compile.xml


à cause de la structure zip du format opf que la tâche "zip" de ant ne permet pas de respecter,
 il faut utiliser le script avec l'option -DuseSystemZip=true pour utiliser la commande zip du système...
sous linux, zip est par défaut, sous windows il faut télécharger les éxécutables unix (http://sourceforge.net/projects/unxutils/files/latest/download)
et les ajouter au PATH pour le même résultat.

NB : j'ai testé une demi-douzaine de solutions avec la tâche zip de ant sans succès mais si vous avez une solution je suis preneur.

NB2 : pour les utilisateurs de Libre Office ou Open Office, le plugin writer2epub fait un export xhtml clean, il suffit ensuite de dézipper
le fichier pour récupérer lesdits fichiers.

---

Etape 1 : Créer la structure
voir le répertoire example : 
 - un répertoire OEBPS avec tous les fichiers de métadonnées et des sous-répertoires contenant les textes, les images et les styles.
 - des fichiers de métadonnées qui permettront de générer l'epub

Quelques notes sur la structure source :
- les noms de fichiers doivent être unique car utilisés comme id (on peut avoir toto.xhtml et toto.jpg dans deux répertoires ou un même
mais pas deux fichiers toto.xhtml dans deux répertoire différents).

---

Etape 2 :
Créer un fichier metadata.properties (de préférence en ISO pour les accents mais je sais pas trop pourquoi... à jongler entre windows et linux j'y pige queud')
Le fichier metadata.properties contient la définition des données suivantes 
epb.filename = nom du fichier qui sera généré (sauf extension .epub qui sera ajoutée automatiquement)
epb.author = nom de l'auteur
epb.publisher = nom de l'éditeur
epb.title = Titre du livre
epb.lang = la langue du bouquin ("fr" pour le français)
epb.tags = liste de tags, séparé par des virgules, pour les référencements des métadonnées
epb.isbn = 978-2-00000-000-0
epb.coverFile = chemin/vers/le/fichier/de/couverture.jpg (typiquement OEBPS/images/cover.jpg)

(optionnel)
Créer le fichier des contributeurs (pour ajouter aux métadonnées une liste de contributeur en epub 3)
contributors.properties contient les ensembles de paires (role, liste de personnes).
Les rôles sont des "relators" MARC (faites vous plaisir avec la liste : http://www.loc.gov/marc/relators/relaterm.html).
Par exemple :
aut = auteur1, auteur2, auteur3
ill = illustrateur1, illustrateur2 
crr = correcteur
cov = cover designer

---

Etape 3 :
Créer le fichier index.csv (en UTF-8... va comprendre) recensant l'enchaînement des fichiers xhtml sous le format suivant :
(une ligne :)fichier;profondeur;type*;titre
par exemple :
1;OEBPS/text/titlePage.xhtml;0;titlepage;Page de titre
2;toc;0;toc;Sommaire
3;OEBPS/text/content0001.xhtml;0;part;Partie 1
4;OEBPS/text/content0002.xhtml;1;chapter;Chapitre 1
5;OEBPS/text/content0003.xhtml;1;chapter;Chapitre 2
6;OEBPS/text/content0004.xhtml;0;part;Partie 2
7;OEBPS/text/content0005.xhtml;1;chapter;Chapitre 3

NB : Un élément n'ayant pas de titre sera référencé dans le manifest et le spine mais sera invisible du sommaire dans toc.xhtml.

vous pouvez également remplacer le fichier par "toc" (ou "TOC") si vous voulez faire apparaitre à cet endroit la table des matières
automatiquement créée à partir de ce fichier (le type est alors superflu).
Concernant la génération vous pouvez 
- ajouter du contenu dans le header du fichier toc.xhtml (par exemple pour inclure un css personnalisé) en créant un fichier header-toc.part
- ajouter du contenu avant le contenu généré du fichier toc.xhtml (par exemple pour personnaliser le titre) en créant un fichier pre-toc.part
(si le fichier n'est pas présent c'est le titre défini dans index.csv pour la ligne toc qui sera inséré comme titre dans la page)
- ajouter du contenu après le contenu généré du fichier toc.xhtml (par exemple pour ajouter un NB) en créant un fichier post-toc.part


* Pour la valeur de type acceptée : voir http://idpf.org/epub/vocab/structure/ 
(nb : le type ne sert à rien si la cible est epub 2)

(optionnel)
un fichier replace.properties (UTF-8)
la partie "cleanFiles" va remplacer automatiquement les motifs (surtout utilisé pour customiser les classes css d'éléments)... par exemple :
<p>— : <p class\="dialogue">—
<p>« : <p class\="dialogue">«
<p class\="center">*</p> : <p class\="separateur">*</p>

---

Etape 4, dernière étape :
Il faut lancer le script avec le paramètre "base" indiquant le chemin vers le répertoire contenant la structure.
Un paramètre optionnel "target" acceptant pour valeur 2 ou 3 permet de spécifier la version de l'epub généré (par défaut 3).
Par exemple :
ant -Dbase=in/example -Dtarget=2
il est possible de n'appeler ensuite que certaines tâches intermédiaires (voir le script ant, ou appeler ant -p)
S'il n'y a pas de problème, un fichier livre.epub a bien été créé dans le répertoire out.
L'enchaînement des tâches est :
- skeleton (crée le répertoire out/livre et y ajoute le contenu et les métadonnées)
- cleanFiles (nettoie tous les fichiers xhtml dans le répertoire out/livre)
- pack (zip le répertoire out/livre vers out/livre.epub et supprime le répertoire temporaire)
- check (lance l'application epubcheck)
(autre tâche : "clean" va supprimer tous les fichiers générés en vidant le répertoire out)

Un paramètre optionnel "overwrite" a été ajouté, il permet de faire de l'édition de version spéciale en définissant un 
répertoire dans lequel chercher des fichiers pour remplacer la version par défaut. Il est possible de remplacer un fichier texte, image ou css.
On ne peut pas remplacer les métadonnées ou ajouter des éléments au spine.
Il est possible de renommer avec un paramètre le fichier de sortie avec la propriété "outfile" (sans oublier l'extension .epub 
ou le chemin du répertoire de sortie)

La tâche loop permet d'appeler récursivement la tâche principale en itérant sur l'ensemble des sous-dossiers de celui passé en paramètre overwriteDir
