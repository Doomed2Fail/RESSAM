import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;

public class VerificationInterface {
    private static File dossier;
    private static PanneauDessin trouverDessin(Container parent) {
        for (Component enfant : parent.getComponents()) {
            if (enfant instanceof PanneauDessin) { return (PanneauDessin) enfant; }
            if (enfant instanceof Container) {
                PanneauDessin resultat = trouverDessin((Container) enfant);
                if (resultat != null) { return resultat; }
            }
        }
        return null;
    }

    private static JToggleButton trouverBouton(Container parent, String commande) {
        for (Component enfant : parent.getComponents()) {
            if (enfant instanceof JToggleButton && commande.equals(((JToggleButton)enfant).getActionCommand())) {
                return (JToggleButton) enfant;
            }
            if (enfant instanceof Container) {
                JToggleButton resultat=trouverBouton((Container)enfant,commande);
                if(resultat!=null) {return resultat;}
            }
        }
        return null;
    }

    private static void tracer(PanneauDessin p,int outil,boolean plein,java.awt.Color couleur,int x1,int y1,int x2,int y2) {
        p.setOutil(outil);p.setRempli(plein);p.setCouleur(couleur);
        p.dispatchEvent(new MouseEvent(p,MouseEvent.MOUSE_PRESSED,1,0,x1,y1,1,false,MouseEvent.BUTTON1));
        p.dispatchEvent(new MouseEvent(p,MouseEvent.MOUSE_DRAGGED,2,InputEvent.BUTTON1_DOWN_MASK,x2,y2,1,false,MouseEvent.NOBUTTON));
        p.dispatchEvent(new MouseEvent(p,MouseEvent.MOUSE_RELEASED,3,0,x2,y2,1,false,MouseEvent.BUTTON1));
    }

    private static void capture(FenetrePaint fenetre,String nom) throws IOException {
        int w=fenetre.getRootPane().getWidth(),h=fenetre.getRootPane().getHeight();
        BufferedImage image=new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
        Graphics2D g=image.createGraphics();fenetre.getRootPane().printAll(g);g.dispose();
        ImageIO.write(image,"png",new File(dossier,nom));
    }

    private static void tester() throws Exception {
        FenetrePaint f=new FenetrePaint();
        try {
            f.setSize(1120,790);f.validate();
            PanneauDessin p=trouverDessin(f.getContentPane());
            if(p==null) {throw new AssertionError("Panneau absent");}
            javax.swing.InputMap raccourcis = f.getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW);
            Object supprimer = raccourcis.get(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DELETE, 0));
            Object retour = raccourcis.get(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_BACK_SPACE, 0));
            if (!"supprimer-selection".equals(supprimer) || !supprimer.equals(retour)) {
                throw new AssertionError("Suppr et Retour arriere doivent partager l'action de suppression");
            }
            javax.swing.JTextArea editeur = new javax.swing.JTextArea();
            if (editeur.getInputMap(javax.swing.JComponent.WHEN_FOCUSED).get(
                    javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_BACK_SPACE, 0)) == null) {
                throw new AssertionError("Retour arriere doit rester utilisable dans le texte");
            }
            p.ajouterTexte("Mon premier dessin",90,90);
            tracer(p,PanneauDessin.RECTANGLE,true,new java.awt.Color(60,130,220),100,160,360,330);
            tracer(p,PanneauDessin.ELLIPSE,true,new java.awt.Color(240,175,60),430,160,650,330);
            tracer(p,PanneauDessin.LIGNE,false,new java.awt.Color(40,155,100),100,420,650,420);
            p.setCouleur(java.awt.Color.BLACK);p.ajouterTexte("Swing + Graphics2D",180,500);
            trouverBouton(f.getContentPane(),"tool2").doClick();
            if(!f.getTitle().startsWith("* ")) {throw new AssertionError("Titre modifie manquant");}
            capture(f,"interface-fr.png");
            File avant=new File(dossier,"dessin-demo.png");p.enregistrer(avant);
            f.changerLangue("en");
            if(!"File".equals(f.getJMenuBar().getMenu(0).getText())) {throw new AssertionError("Menu non traduit");}
            if(!"Line".equals(trouverBouton(f.getContentPane(),"tool2").getText())) {throw new AssertionError("Outil non traduit");}
            if(!trouverBouton(f.getContentPane(),"tool2").isSelected()) {throw new AssertionError("Outil perdu au changement de langue");}
            capture(f,"interface-en.png");
            File apres=new File(dossier,"dessin-apres-langue.png");p.enregistrer(apres);
            BufferedImage a=ImageIO.read(avant),b=ImageIO.read(apres);
            for(int y=0;y<a.getHeight();y++) {for(int x=0;x<a.getWidth();x++) {
                if(a.getRGB(x,y)!=b.getRGB(x,y)) {throw new AssertionError("Dessin altere par la langue");}
            }}
            System.out.println("Interface FR/EN : menus, outils, titre, dessin preserve et apercus verifies.");
        } finally {f.dispose();}
    }

    public static void main(String[] args) throws Exception {
        dossier=new File(args.length==0 ? "verification" : args[0]);dossier.mkdirs();
        final Exception[] erreur=new Exception[1];
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override public void run() {try {tester();} catch(Exception ex) {erreur[0]=ex;}}
        });
        if(erreur[0]!=null) {throw erreur[0];}
    }
}
