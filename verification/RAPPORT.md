# Vérification du projet Paint

Vérification effectuée le 13 novembre 2026. Compilation et exécution des tests avec OpenJDK 27, sous Windows. Les trois classes de l’application sont compilées avec `--release 8` : leur syntaxe, leurs API et leurs fichiers `.class` ciblent Java 8. Le programme n’a pas été exécuté sur une installation Java 8 distincte.

## Résultats

Le script `verifier.ps1` termine avec succès. Il compile l’application, construit le JAR avec les trois dictionnaires de traduction, puis utilise ce JAR pour les tests.

**32 contrôles de comportement passent**, dont :

- Feuille blanche, crayon continu et effacement local par la gomme.
- Rectangle rempli dessiné dans le sens inverse et ellipse remplie.
- Contours de rectangle et d’ellipse, avec conservation du centre blanc.
- Prévisualisation d’une ligne avant relâchement, puis mémorisation à la validation.
- Clic droit ignoré, début hors feuille ignoré et glissement hors feuille borné.
- Saisie de texte annulée/vide ignorée et texte non vide effectivement rendu.
- Enregistrement et relecture PNG identiques pixel par pixel.
- Rejet d’un fichier invalide sans perdre le dessin ni l’indicateur de modification.
- Échec d’écriture sans perdre l’indicateur de modification.
- Image transparente importée sur blanc et effacement complet.
- Clés des langues identiques, textes français/anglais disponibles et accents lisibles.

La vérification d’interface construit la vraie `FenetrePaint`, active un outil via son bouton, change la langue et contrôle les menus, les libellés, la sélection de l’outil et l’indicateur de modification du titre. Elle compare aussi les pixels du document avant et après le changement de langue. Elle n’affiche pas de fenêtre interactive sur le bureau.

Les aperçus [français](interface-fr.png) et [anglais](interface-en.png) ont été ouverts et inspectés : menus, palette, zone de dessin et barre d’état sont lisibles, sans chevauchement visible. Le dessin affiché est généré par les mêmes méthodes et événements que l’application.

## Portée des contrôles

Les tests vérifient les inscriptions d’écouteurs souris en envoyant des `MouseEvent` au panneau. Ils ne simulent pas un parcours manuel complet dans les dialogues natifs de fichiers, de couleur ou de confirmation. Pour le texte, ils vérifient le rendu et le traitement des valeurs annulées/vides via la méthode appelée après le dialogue. Aucun fichier personnel n’est utilisé pour les tests.

Les archives de livraison sont contrôlées par leur inventaire et leur intégrité ZIP. Le JAR est livré séparément dans le dossier `dist`, avec sa classe principale et ses ressources. Les scripts de compilation, lancement et test sont fournis pour reproduire les opérations.
