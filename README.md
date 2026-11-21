# RESSAM — projet Java II

Copyright © 2026 Taha Amine ER-RAFIY. Distribué sous [licence MIT](LICENSE).

## Aperçu de l’interface

### Interface de dessin

![Interface de dessin de RESSAM](verification/interface-vide.png)

### Exemple de dessin : SALAM

![Dessin SALAM réalisé avec RESSAM](verification/dessin-salam.png)

## Versions Windows et macOS

Les versions téléchargeables sont disponibles dans la section **Releases** du dépôt GitHub. Chaque archive contient l’application et son environnement Java. La version macOS est construite pour les Mac Apple Silicon (ARM64). La configuration GitHub Actions crée les archives lors de la publication d’un tag `v*`.

Sur macOS, l’application non signée peut nécessiter une autorisation explicite dans les réglages de sécurité. La génération des versions est automatisée ; leur utilisation sur les deux systèmes n’a pas encore été vérifiée manuellement.

Cette version moderne conserve le projet d'origine dans le dossier `ProjetPaint`.
L'interface utilise Swing et Graphics2D : un ruban horizontal clair regroupe les outils, les options et une palette ronde au-dessus de la feuille. Les icônes sont dessinées en Java. L'apparence s'inspire de Paint ; les boîtes de dialogue restent celles de Swing.

## Nouveautés

- **Annuler / Rétablir** : les huit derniers gestes sont conservés en mémoire ; raccourcis Ctrl+Z et Ctrl+Y. Un nouveau geste après annulation efface la possibilité de rétablir. L'ouverture ou la création d'un dessin vide l'historique.
- **Grille de repère** : aide visuelle facultative, jamais incluse dans le PNG.
- **Palette rapide** : douze couleurs accessibles en un clic ; le sélecteur de couleur reste disponible.
- **Interface modernisée** : ruban horizontal, icônes vectorielles et tons clairs, sans bibliothèque externe.

La feuille s'agrandit automatiquement quand la zone visible grandit, sans étirer le dessin. Elle ne rétrécit pas automatiquement ; ses dimensions agrandies sont celles du PNG enregistré. L'agrandissement marque le document comme modifié. Une annulation ou un rétablissement marque aussi le document comme modifié, même si son contenu redevient identique à la dernière sauvegarde. Les copies d'historique utilisent de la mémoire, surtout pour les grandes images.

Application de dessin internationalisée réalisée avec **Java Swing, AWT, Graphics2D, événements et ResourceBundle**. Aucune bibliothèque externe. Interface français/anglais ; trois classes principales et deux classes de présentation.

## Lancer l’application

Sous Windows, double-cliquer sur **Lancer.bat**. Il utilise le JAR déjà compilé dans `dist/Paint-ruban-compact.jar`. Java 8 ou supérieur doit être installé ; le script cherche Java dans PATH, JAVA_HOME puis dans le dossier `.jdks` du profil utilisateur.

On peut aussi lancer le JAR depuis un terminal :

```sh
java -jar dist/Paint-ruban-compact.jar
```

Le JAR constitue le livrable Java exécutable ; il nécessite une installation de Java sur l’ordinateur. Ce n’est pas un exécutable Windows autonome avec Java embarqué.

## Fonctionnalités

- **Crayon** : cliquer et glisser pour dessiner à main levée.
- **Gomme** : cliquer et glisser pour effacer au blanc. Sa largeur vaut quatre fois l’épaisseur sélectionnée.
- **Ligne, rectangle, ellipse** : appuyer au point de départ, glisser pour prévisualiser, relâcher pour valider. Le rectangle et l’ellipse acceptent aussi un glissement vers le haut ou la gauche.
- **Texte** : choisir l'outil, tracer un rectangle, puis écrire directement dans la zone. Cliquer ailleurs ou utiliser Ctrl+Entrée pour valider ; Échap annule la saisie. Les lignes qui dépassent la hauteur du rectangle ne sont pas visibles sur le dessin final.
- **Sélection** : tracer un rectangle autour d'une partie du dessin, puis glisser à l'intérieur pour déplacer ses pixels. Le bouton « Supprimer la sélection », Suppr ou Retour arrière efface la zone choisie. Pour la recolorer, choisir une couleur puis « Colorer sélection » ; tous les pixels non blancs deviennent cette couleur. « Redimensionner » applique le pourcentage choisi à toute la zone, dans la limite de la feuille. La zone d'origine devient blanche. Ces actions sont annulables ; elles ne séparent pas les objets ou les calques.
- **Styles de pinceau** : crayon, marqueur translucide, pointillé, calligraphie et aérographe, utilisés avec l'outil Crayon.
- **Formes** : ligne, rectangle, ellipse, triangle, losange, rectangle arrondi et pentagone, regroupés dans une grille d'icônes défilante. Survoler une icône pour afficher son nom.
- **Couleur** : choisir avec `JColorChooser` ; l’épaisseur va de 1 à 30 pixels. Pour le texte, la taille de police est calculée à partir de cette épaisseur.
- **Forme remplie** : remplit les rectangles et ellipses ; décocher pour ne tracer que le contour.
- **Fichier** : nouveau dessin, ouverture d’une image, enregistrement PNG et enregistrement sous un autre nom. Raccourcis Ctrl+N, Ctrl+O et Ctrl+S.
- **Dessin → Effacer tout** : efface la feuille après confirmation.
- **Langue → Français / English** : traduit l’interface sans perdre le dessin ni l’outil sélectionné.

La feuille initiale mesure **1000 × 650 pixels**. Les barres de défilement permettent d’accéder aux zones hors de la fenêtre. À l’ouverture d’une image, la feuille prend ses dimensions. La transparence importée est posée sur un fond blanc. Les dessins s’enregistrent toujours en PNG ; l’extension `.png` est ajoutée si nécessaire. Une image importée est enregistrée via un choix de destination pour éviter de modifier automatiquement l’original.

L’étoile dans le titre indique des modifications non enregistrées. Nouveau, Ouvrir et Quitter proposent de les enregistrer ; Annuler conserve le document. La gomme efface les pixels ; la sélection déplace une zone rectangulaire de pixels. Cette version ne contient pas de gestion de calques ni de sélection individuelle des objets.

## Comprendre le code

1. [Paint.java](src/Paint.java) : démarre l’interface Swing.
2. [FenetrePaint.java](src/FenetrePaint.java) : crée les menus, outils et dialogues ; gère les commandes et les langues.
3. [PanneauDessin.java](src/PanneauDessin.java) : conserve l’image, traite la souris et dessine dans `paintComponent`.
4. [Fichiers de traduction](resources/messages/Messages_fr.properties) : textes séparés du code Java.

Lire ensuite [EXPLICATIONS.md](EXPLICATIONS.md), qui relie chaque partie aux pages du cours.

## Recompiler

Un **JDK** est nécessaire pour compiler. Double-cliquer sur `Compiler.bat`, puis utiliser `Lancer.bat`. Les scripts ne nécessitent ni Maven ni Gradle.

Pour compiler manuellement depuis ce dossier avec un JDK 9 ou supérieur :

```sh
javac -encoding UTF-8 --release 8 -d build/classes src/Paint.java src/FenetrePaint.java src/PanneauDessin.java src/StyleBouton.java src/IconeOutil.java
```

Créer d’abord `build/classes`, puis copier le dossier `resources/messages` dans `build/classes/messages` et créer le JAR :

```sh
jar cfe dist/Paint-ruban-compact.jar Paint -C build/classes .
```

Le script fourni réalise aussi la création des dossiers et la copie des traductions. Avec un JDK 8, il utilise `-source 8 -target 8`. Avec un JDK plus récent, il utilise `--release 8` pour vérifier la compatibilité des API.

Dans IntelliJ : ouvrir ce dossier, marquer `src` comme dossier de sources et `resources` comme dossier de ressources, sélectionner un JDK, puis lancer la classe `Paint`.

## Vérifications

Le script `verifier.ps1` compile le projet et teste le JAR avec ses traductions. Les tests envoient des événements souris au panneau, vérifient les pixels et enregistrent des aperçus de l’interface construite en français et en anglais. Ils ne dessinent pas dans des fichiers personnels.

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\verifier.ps1
```

Voir le [rapport de vérification](verification/RAPPORT.md), l’[aperçu français](verification/interface-fr.png) et l’[aperçu anglais](verification/interface-en.png).
