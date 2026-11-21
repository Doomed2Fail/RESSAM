# Comprendre et présenter le projet Paint

## Les ajouts de la version moderne

Pour le **texte**, la souris délimite un rectangle et crée un `JTextArea` directement sur le panneau. Quand on valide ou clique ailleurs, le texte est dessiné dans le `BufferedImage` et la zone éditable disparaît. Échap annule la saisie. Le texte validé fait partie des pixels ; il ne reste pas éditable comme objet.

Pour la **sélection**, deux points de souris créent un rectangle. Un glissement à l'intérieur copie les pixels, remplit l'ancienne zone en blanc puis dessine la copie à sa destination. `memoriser()` prend une copie avant le déplacement pour permettre Annuler/Rétablir. La sélection est rectangulaire et ne distingue pas les objets ni les calques.

La présentation est réalisée avec Swing et `Graphics2D` : `StyleBouton` hérite de `BasicButtonUI` et dessine les états des boutons, tandis que `IconeOutil` dessine de petites icônes. Un ruban horizontal regroupe les outils et la palette. Le thème Nimbus est utilisé pour les composants Swing standards ; les dialogues conservent leur apparence Swing.

Pour **Annuler / Rétablir**, `PanneauDessin` copie son `BufferedImage` avant un geste. Deux listes de copies permettent de revenir à l'image précédente ou de la rétablir. L'historique est limité à huit gestes ; un nouveau geste après une annulation efface les états à rétablir. `paintComponent` affiche toujours la feuille courante.

La **grille de repère** est dessinée dans `paintComponent` après l'image. Elle n'est pas dessinée sur le `BufferedImage`, donc ne figure pas dans le PNG. La **palette rapide** utilise des boutons et des `ActionListener` pour changer la couleur de dessin.

Le sujet demande une application de dessin internationalisée, similaire à Paint. Cette version réutilise les composants du cours et les assemble dans trois classes. Les numéros ci-dessous désignent les **pages du PDF**, et non les numéros imprimés des diapositives ; ils se décalent dans la dernière partie du document.

## Correspondance avec le cours

| Partie du projet | Notions utilisées | Pages PDF |
|---|---|---|
| Fenêtre et panneaux | `JFrame`, `JPanel`, composants Swing | 13–24 |
| Boutons, choix, défilement | Boutons, cases, listes de choix et conteneurs Swing | 25–51 |
| Menus et barre d’outils | `JMenuBar`, `JMenuItem`, `JToolBar`, commandes | 52–74 |
| Placement de l’interface | `BorderLayout`, `GridLayout` | 83–99 |
| Questions et saisie du texte | `JOptionPane` | 100–109 |
| Choix de couleur et de fichier | `JColorChooser`, `JFileChooser` | 114–120 |
| Réaction aux commandes | `ActionListener`, inscription et `actionPerformed` | 121–173 |
| Crayon, gomme et formes | `MouseListener`, `MouseMotionListener` | 174–178 |
| Fermeture de fenêtre | `WindowAdapter`, `windowClosing` | 183–187 |
| Écouteur partagé pour les menus | Commandes et identification de l’action | 194–203 |
| Redessiner le panneau | `paintComponent`, coordonnées, `repaint` | 204–215 |
| Formes, contours et remplissage | Lignes, rectangles, ellipses, `BasicStroke` | 216–231 |
| Texte | `Font`, `drawString` | 232–242 |
| Mémoire et fichiers du dessin | `BufferedImage`, `createGraphics`, `ImageIO` | 243–250 |
| Qualité visuelle | `RenderingHints` | 263–265 |
| Interaction graphique | Coordonnées de souris et mise à jour du dessin | 270–274 |
| Plusieurs langues | `Locale`, `ResourceBundle`, fichiers `.properties` | 314–317 |
| Sujet du projet | Application de dessin internationalisée ; code source et exécutable | 318 |

Les méthodes ajoutées au projet, telles que `segment`, `dessinerForme` ou `actualiserEtat`, regroupent des instructions pour rendre le programme lisible. Ce sont des méthodes Java ordinaires. L’organisation en trois classes est un choix pour ce projet ; elle n’est pas imposée mot pour mot par le cours.

## 1. Le rôle des trois classes

`Paint` contient `main`. Elle demande à Swing de construire la fenêtre dans son thread de gestion de l’interface avec `SwingUtilities.invokeLater`. Le traitement est exprimé avec une classe anonyme.

`FenetrePaint` hérite de `JFrame` et implémente `ActionListener`. Elle construit l’interface et reçoit les actions des menus, boutons, options et choix de langue. Les composants ont une commande, par exemple `new`, `save` ou `tool2`. `actionPerformed` consulte cette commande pour effectuer le traitement correspondant. Le changement de langue modifie les libellés visibles ; les commandes restent identiques.

`PanneauDessin` hérite de `JPanel` et implémente `MouseListener` et `MouseMotionListener`. Il s’inscrit lui-même auprès des deux catégories avec `addMouseListener(this)` et `addMouseMotionListener(this)`. Il conserve les pixels, les paramètres de dessin et les coordonnées du glissement.

## 2. Pourquoi conserver un BufferedImage ?

Dessiner uniquement sur l’écran au moment d’un clic ne suffit pas à conserver le résultat après un rafraîchissement. Le projet conserve donc un `BufferedImage` qui joue le rôle de feuille.

Le chemin suivi est :

```text
Action souris
    -> modification du BufferedImage
    -> repaint()
    -> paintComponent(Graphics g)
    -> affichage du BufferedImage sur le panneau
```

`paintComponent` commence par `super.paintComponent(g)` et affiche l’image avec `drawImage`. Les modifications permanentes sont réalisées dans le contexte `Graphics2D` obtenu avec `image.createGraphics()`. Après son utilisation, ce contexte est libéré avec `dispose()`.

Le cours montre à la fois le dessin dans un composant et la création d’une image tampon. Le projet combine ces deux notions pour garder le document visible et pouvoir l’enregistrer.

## 3. Crayon et gomme

Dans `mousePressed`, le panneau retient la position initiale. Dans `mouseDragged`, il trace un segment entre la position précédente et la position actuelle. Relier les positions permet d’obtenir un trait continu, même si les événements souris ne correspondent pas à chaque pixel traversé.

La couleur est appliquée avec `setColor`. L’épaisseur et les extrémités arrondies viennent de `BasicStroke`. La gomme utilise le même mécanisme, avec une couleur blanche et une épaisseur plus grande. Le fond du document étant blanc, elle efface visuellement les pixels couverts.

## 4. Ligne, rectangle et ellipse

Pendant le glissement, le point de départ et le point courant déterminent la forme. Celle-ci est dessinée temporairement par `paintComponent`, au-dessus de l’image existante. Elle n’est pas encore enregistrée dans les pixels du document.

Au relâchement, la même forme est tracée dans le `BufferedImage`. Le programme arrête alors l’aperçu et demande un nouveau dessin du panneau.

Pour le rectangle et l’ellipse :

```java
int x = Math.min(debutX, finX);
int y = Math.min(debutY, finY);
int largeur = Math.abs(finX - debutX);
int hauteur = Math.abs(finY - debutY);
```

Le coin supérieur gauche est ainsi retrouvé quel que soit le sens du glissement. Le contour utilise `drawRect` ou `drawOval` ; le remplissage utilise `fillRect` ou `fillOval`.

## 5. Texte et dialogues

Avec l’outil Texte, le glissement crée une zone éditable `JTextArea`. À la validation, son contenu est peint dans l’image avec la police et la couleur choisies.

Le choix de couleur utilise `JColorChooser.showDialog`. Si l’utilisateur annule, la couleur précédente est conservée. Les confirmations de fermeture et d’effacement utilisent `showConfirmDialog` et ses constantes de réponse.

## 6. Ouverture et enregistrement

`JFileChooser` demande un chemin à l’utilisateur. Il n’effectue pas lui-même la lecture ou l’écriture des pixels.

`ImageIO.read` lit une image existante. Si le format n’est pas reconnu, le projet affiche une erreur et conserve le dessin actuel. L’image importée est copiée sur un fond blanc, puis devient la nouvelle feuille avec ses dimensions propres.

`ImageIO.write(image, "png", fichier)` enregistre la feuille. Le choix du PNG conserve les pixels sans compression avec perte. L’extension correspond au format écrit. Un échec d’enregistrement laisse le document marqué comme modifié.

L’indicateur `modifie` devient vrai après un dessin ou un effacement ; il devient faux après un enregistrement réussi, une ouverture ou la création d’un document. Avant de remplacer le document ou fermer la fenêtre, le programme propose d’enregistrer les modifications. Annuler interrompt l’action demandée.

## 7. Internationalisation

Chaque texte possède une clé indépendante de la langue, par exemple `file.save`. Le projet charge un dictionnaire de traductions :

```java
messages = ResourceBundle.getBundle("messages.Messages", new Locale("fr"));
enregistrer.setText(messages.getString("file.save"));
```

Les traductions sont dans `Messages_fr.properties` et `Messages_en.properties`. `Messages.properties` fournit le français comme langue de repli. Les caractères accentués sont encodés sous la forme `\u00e9`, etc., pour rester compatibles avec la lecture des propriétés sur Java 8.

Lors d’un changement de langue, le programme recharge les traductions et actualise les composants existants. Il ne recrée pas la feuille : les pixels, la couleur, l’épaisseur et l’outil restent disponibles. Un texte déjà dessiné est une partie du document ; il ne change donc pas de langue automatiquement.

## 8. Démonstration possible

1. Tracer une ligne au crayon et en effacer une partie.
2. Dessiner un rectangle rempli et une ellipse avec un contour.
3. Changer la couleur et l’épaisseur, puis ajouter du texte.
4. Passer en anglais et montrer que le dessin reste présent.
5. Enregistrer en PNG, puis ouvrir le fichier pour retrouver le dessin.
6. Modifier le dessin et demander un nouveau document : expliquer les réponses Oui, Non et Annuler.

Pour présenter le code, partir d’un exemple de clic et suivre la source, l’inscription de l’écouteur, le callback, la modification de l’image et l’appel à `repaint`. Il est utile que chaque membre du groupe puisse expliquer cette chaîne, les traductions et la différence entre l’aperçu et le dessin validé.
