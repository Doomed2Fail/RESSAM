import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

public class VerificationDessin {
    private static int controles;
    private static File sortie;

    private static void verifier(boolean condition, String message) {
        controles++;
        if (!condition) { throw new AssertionError(message); }
    }

    private static void souris(PanneauDessin p, int type, int x, int y, int bouton) {
        int masque = type == MouseEvent.MOUSE_DRAGGED ? InputEvent.BUTTON1_DOWN_MASK : 0;
        p.dispatchEvent(new MouseEvent(p, type, System.currentTimeMillis(), masque, x, y, 1, false, bouton));
    }

    private static void tracer(PanneauDessin p, int x1, int y1, int x2, int y2) {
        souris(p, MouseEvent.MOUSE_PRESSED, x1, y1, MouseEvent.BUTTON1);
        souris(p, MouseEvent.MOUSE_DRAGGED, x2, y2, MouseEvent.NOBUTTON);
        souris(p, MouseEvent.MOUSE_RELEASED, x2, y2, MouseEvent.BUTTON1);
    }

    private static BufferedImage pixels(PanneauDessin p, String nom) throws IOException {
        File fichier = new File(sortie, nom + ".png");
        p.enregistrer(fichier);
        return ImageIO.read(fichier);
    }

    private static boolean couleur(BufferedImage image, int x, int y, Color valeur) {
        return (image.getRGB(x,y) & 0xFFFFFF) == (valeur.getRGB() & 0xFFFFFF);
    }

    private static void tests() throws Exception {
        PanneauDessin transformations = new PanneauDessin(null);
        transformations.nouveau(200, 150); transformations.setSize(200, 150);
        transformations.setCouleur(Color.RED); transformations.setEpaisseur(5);
        tracer(transformations, 20, 30, 60, 30);
        transformations.setOutil(PanneauDessin.SELECTION);
        tracer(transformations, 10, 15, 80, 50);
        transformations.setCouleur(Color.BLUE);
        verifier(transformations.recolorerSelection(), "Recoloration d'une selection");
        verifier(couleur(pixels(transformations, "recolore"), 30, 30, Color.BLUE), "Les pixels du dessin deviennent bleus");
        verifier(transformations.redimensionnerSelection(150), "Redimensionnement selection");
        verifier(couleur(pixels(transformations, "redimensionne"), 40, 38, Color.BLUE), "La selection a ete agrandie");
        transformations.annuler();
        verifier(couleur(pixels(transformations, "taille-annulee"), 30, 30, Color.BLUE), "Annuler le redimensionnement");
        transformations.nouveau(200, 150);
        transformations.setOutil(PanneauDessin.TRIANGLE); transformations.setRempli(true);
        tracer(transformations, 20, 20, 100, 100);
        verifier(couleur(pixels(transformations, "triangle"), 60, 80, Color.BLUE), "Triangle rempli dessine");
        transformations.setOutil(PanneauDessin.CRAYON); transformations.setStylePinceau(1);
        tracer(transformations, 120, 30, 170, 30);
        BufferedImage marqueur = pixels(transformations, "marqueur");
        verifier(!couleur(marqueur, 140, 30, Color.WHITE) && !couleur(marqueur, 140, 30, Color.BLUE), "Marqueur translucide");
        PanneauDessin outils = new PanneauDessin(null);
        outils.nouveau(240, 180); outils.setSize(240, 180);
        outils.setCouleur(Color.RED); outils.setEpaisseur(5);
        tracer(outils, 20, 30, 70, 30);
        outils.setOutil(PanneauDessin.SELECTION);
        tracer(outils, 10, 15, 90, 50);
        tracer(outils, 40, 30, 120, 90);
        BufferedImage deplace = pixels(outils, "selection-deplacee");
        verifier(couleur(deplace, 40, 30, Color.WHITE) && couleur(deplace, 120, 90, Color.RED),
                "Selection deplace les pixels et efface la zone initiale");
        outils.annuler();
        BufferedImage avantDeplacement = pixels(outils, "selection-annulee");
        verifier(couleur(avantDeplacement, 40, 30, Color.RED) && couleur(avantDeplacement, 120, 90, Color.WHITE),
                "Annuler restaure la selection avant deplacement");
        outils.retablir();
        verifier(couleur(pixels(outils, "selection-retablie"), 120, 90, Color.RED), "Retablir restaure le deplacement");
        tracer(outils, 90, 75, 160, 110);
        verifier(outils.supprimerSelection(), "Suppression accepte une selection");
        verifier(couleur(pixels(outils, "selection-supprimee"), 120, 90, Color.WHITE), "La zone selectionnee est effacee");
        outils.annuler();
        verifier(couleur(pixels(outils, "suppression-annulee"), 120, 90, Color.RED), "Annuler restaure la zone supprimee");
        verifier(!outils.supprimerSelection(), "Suppression sans selection refusee");
        outils.setOutil(PanneauDessin.TEXTE);
        tracer(outils, 10, 100, 180, 160);
        verifier(outils.getComponentCount() == 1 && outils.getComponent(0) instanceof javax.swing.JTextArea,
                "Une zone de texte editable apparait apres glissement");
        ((javax.swing.JTextArea) outils.getComponent(0)).setText("Bonjour");
        outils.validerTexte();
        BufferedImage avecTexte = pixels(outils, "zone-texte");
        int pixelsTexte = 0;
        for (int y=100; y<160; y++) { for (int x=10; x<180; x++) {
            if (!couleur(avecTexte, x, y, Color.WHITE)) { pixelsTexte++; }
        }}
        verifier(pixelsTexte > 10 && outils.getComponentCount() == 0, "Le texte valide est inscrit sur l'image");
        outils.annuler();
        verifier(couleur(pixels(outils, "texte-annule"), 20, 110, Color.WHITE), "Annuler retire le texte");
        PanneauDessin historique = new PanneauDessin(null);
        historique.nouveau(100, 100);
        historique.setCouleur(Color.RED);
        tracer(historique, 10, 20, 80, 20);
        historique.annuler();
        verifier(couleur(pixels(historique, "annuler"), 40, 20, Color.WHITE), "Annuler retire le trait");
        historique.retablir();
        verifier(couleur(pixels(historique, "retablir"), 40, 20, Color.RED), "Retablir restaure le trait");
        historique.setGrille(true);
        verifier(couleur(pixels(historique, "grille-export"), 25, 25, Color.WHITE), "Grille absente du PNG");
        historique.annuler();
        tracer(historique, 10, 40, 80, 40);
        historique.retablir();
        verifier(couleur(pixels(historique, "branche"), 40, 20, Color.WHITE), "Nouvelle action vide retablir");
        PanneauDessin redimension = new PanneauDessin(null);
        redimension.nouveau(100, 100);
        redimension.setCouleur(Color.RED);
        tracer(redimension, 10, 20, 80, 20);
        redimension.agrandirSiNecessaire(180, 140);
        verifier(redimension.getLargeurImage() == 180 && redimension.getHauteurImage() == 140,
                "La feuille grandit avec la surface visible");
        BufferedImage agrandie = pixels(redimension, "agrandie");
        verifier(couleur(agrandie, 40, 20, Color.RED) && couleur(agrandie, 150, 120, Color.WHITE),
                "Le dessin est conserve et la nouvelle zone est blanche");
        redimension.annuler();
        verifier(redimension.getLargeurImage() == 180 && couleur(pixels(redimension, "agrandie-annulation"), 40, 20, Color.WHITE),
                "Annuler conserve les nouvelles dimensions et retire le trait");
        redimension.agrandirSiNecessaire(90, 90);
        verifier(redimension.getLargeurImage() == 180, "Retrecir la fenetre ne coupe pas la feuille");
        PanneauDessin p = new PanneauDessin(null);
        p.nouveau(240, 180); p.setSize(p.getPreferredSize());
        verifier(!p.estModifie(), "Document neuf non modifie");
        verifier(couleur(pixels(p,"blanc"),100,100,Color.WHITE), "Fond blanc");

        p.setCouleur(Color.RED); p.setEpaisseur(5);
        tracer(p,20,20,100,20);
        verifier(p.estModifie(), "Le crayon marque le document modifie");
        BufferedImage rouge = pixels(p,"crayon");
        verifier(couleur(rouge,60,20,Color.RED), "Trait de crayon continu");
        verifier(!p.estModifie(), "Enregistrer remet le document a jour");

        p.setOutil(PanneauDessin.GOMME); tracer(p,50,20,75,20);
        BufferedImage gomme = pixels(p,"gomme");
        verifier(couleur(gomme,60,20,Color.WHITE), "La gomme efface au blanc");
        verifier(couleur(gomme,25,20,Color.RED), "La gomme conserve les autres pixels");

        p.setOutil(PanneauDessin.RECTANGLE); p.setRempli(true); p.setCouleur(Color.BLUE);
        tracer(p,180,150,110,80);
        verifier(couleur(pixels(p,"rectangle"),140,110,Color.BLUE), "Rectangle rempli en glissement inverse");
        p.setOutil(PanneauDessin.ELLIPSE); p.setCouleur(Color.GREEN);
        tracer(p,20,60,90,130);
        BufferedImage ellipse = pixels(p,"ellipse");
        verifier(couleur(ellipse,55,95,Color.GREEN), "Centre de l'ellipse rempli");
        verifier(couleur(ellipse,21,61,Color.WHITE), "Coin externe a l'ellipse intact");

        p.nouveau(240,180); p.setOutil(PanneauDessin.LIGNE); p.setCouleur(Color.BLACK);
        souris(p,MouseEvent.MOUSE_PRESSED,20,20,MouseEvent.BUTTON1);
        souris(p,MouseEvent.MOUSE_DRAGGED,100,100,MouseEvent.NOBUTTON);
        BufferedImage apercu = new BufferedImage(240,180,BufferedImage.TYPE_INT_RGB);
        Graphics2D g = apercu.createGraphics(); p.paint(g); g.dispose();
        verifier(couleur(apercu,60,60,Color.BLACK), "La forme temporaire est affichee");
        verifier(couleur(pixels(p,"avant-validation"),60,60,Color.WHITE), "L'apercu ne modifie pas l'image avant relachement");
        souris(p,MouseEvent.MOUSE_RELEASED,100,100,MouseEvent.BUTTON1);
        verifier(couleur(pixels(p,"ligne"),60,60,Color.BLACK), "La ligne est memorisee au relachement");

        p.nouveau(240,180); p.setOutil(PanneauDessin.RECTANGLE); p.setRempli(false);
        tracer(p,20,20,100,100);
        BufferedImage contour = pixels(p,"contour");
        verifier(couleur(contour,20,60,Color.BLACK), "Contour du rectangle trace");
        verifier(couleur(contour,60,60,Color.WHITE), "Centre du contour non rempli");
        p.setOutil(PanneauDessin.ELLIPSE); tracer(p,120,20,220,120);
        BufferedImage ovale = pixels(p,"ellipse-contour");
        verifier(couleur(ovale,170,20,Color.BLACK), "Contour de l'ellipse trace");
        verifier(couleur(ovale,170,70,Color.WHITE), "Centre de l'ellipse non rempli");

        p.nouveau(240,180); p.setOutil(PanneauDessin.CRAYON);
        souris(p,MouseEvent.MOUSE_PRESSED,30,30,MouseEvent.BUTTON3);
        verifier(!p.estModifie(), "Clic droit ignore");
        tracer(p,-5,-5,80,80);
        verifier(!p.estModifie(), "Debut hors de la feuille ignore");
        tracer(p,20,20,300,200);
        verifier(p.estModifie(), "Glissement hors de la feuille accepte sans exception");
        p.nouveau(240,180); p.ajouterTexte(null,20,30); p.ajouterTexte("  ",20,30);
        verifier(!p.estModifie(), "Saisie annulee ou vide ignoree");
        p.ajouterTexte("Bonjour",20,60);
        verifier(p.estModifie(), "Texte ajoute au document");
        BufferedImage texte = pixels(p,"texte");
        int points = 0;
        for (int y=20;y<65;y++) { for(int x=15;x<200;x++) {
            if (!couleur(texte,x,y,Color.WHITE)) { points++; }
        }}
        verifier(points>50, "Texte effectivement rendu en pixels");

        PanneauDessin recharge = new PanneauDessin(null);
        recharge.ouvrir(new File(sortie,"texte.png"));
        BufferedImage relu = pixels(recharge,"relecture");
        boolean identique = relu.getWidth()==texte.getWidth() && relu.getHeight()==texte.getHeight();
        for(int y=0;y<texte.getHeight();y++) { for(int x=0;x<texte.getWidth();x++) {
            if(texte.getRGB(x,y)!=relu.getRGB(x,y)) { identique=false; }
        }}
        verifier(identique, "PNG enregistre puis recharge sans perte de pixels");

        p.setOutil(PanneauDessin.CRAYON); tracer(p,5,5,15,5);
        File invalide = new File(sortie,"invalide.txt");
        FileOutputStream flux = new FileOutputStream(invalide); flux.write(new byte[]{65,66,67}); flux.close();
        boolean rejet = false;
        try { p.ouvrir(invalide); } catch(IOException ex) { rejet=true; }
        verifier(rejet && p.estModifie(), "Image invalide rejetee sans perdre le document modifie");
        boolean erreurEcriture = false;
        try { p.enregistrer(new File(new File(sortie,"absent"),"impossible.png")); }
        catch(IOException ex) { erreurEcriture=true; }
        verifier(erreurEcriture && p.estModifie(), "Echec d'ecriture conserve l'etat modifie");

        BufferedImage transparent = new BufferedImage(20,20,BufferedImage.TYPE_INT_ARGB);
        File alpha = new File(sortie,"transparent.png"); ImageIO.write(transparent,"png",alpha);
        recharge.ouvrir(alpha);
        verifier(couleur(pixels(recharge,"fond-alpha"),10,10,Color.WHITE), "Transparence importee sur fond blanc");
        p.effacer();
        verifier(p.estModifie(), "Effacement signale une modification");
        verifier(couleur(pixels(p,"efface"),30,50,Color.WHITE), "Effacement complet");

        ResourceBundle fr = ResourceBundle.getBundle("messages.Messages",new Locale("fr"));
        ResourceBundle en = ResourceBundle.getBundle("messages.Messages",new Locale("en"));
        verifier(fr.keySet().equals(en.keySet()), "Toutes les cles de traduction sont presentes");
        verifier("Fichier".equals(fr.getString("menu.file")) && "File".equals(en.getString("menu.file")), "Changement de langue disponible");
        verifier(fr.getString("help.about").startsWith("À"), "Accents lisibles depuis les properties Java 8");
        System.out.println(controles + " controles reussis (dessin, fichiers, traductions).");
    }

    public static void main(String[] args) throws Exception {
        sortie = new File(args.length==0 ? "verification/tests" : args[0]);
        if (!sortie.isDirectory() && !sortie.mkdirs()) { throw new IOException("Dossier de test inaccessible"); }
        final Exception[] erreur = new Exception[1];
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override public void run() {
                try { tests(); } catch(Exception ex) { erreur[0]=ex; }
            }
        });
        if (erreur[0]!=null) { throw erreur[0]; }
    }
}
