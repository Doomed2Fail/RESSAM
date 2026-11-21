import java.awt.BasicStroke;
import java.util.ArrayList;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Rectangle;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.AlphaComposite;
import java.awt.geom.RoundRectangle2D;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.BorderFactory;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import javax.swing.JComponent;
import javax.swing.border.Border;

public class PanneauDessin extends JPanel implements MouseListener, MouseMotionListener {
    private static final long serialVersionUID = 1L;
    public static final int CRAYON = 0;
    public static final int GOMME = 1;
    public static final int LIGNE = 2;
    public static final int RECTANGLE = 3;
    public static final int ELLIPSE = 4;
    public static final int TEXTE = 5;
    public static final int SELECTION = 6;
    public static final int TRIANGLE = 7;
    public static final int LOSANGE = 8;
    public static final int ARRONDI = 9;
    public static final int PENTAGONE = 10;

    private final ArrayList<BufferedImage> annulations = new ArrayList<BufferedImage>();
    private final ArrayList<BufferedImage> retablissements = new ArrayList<BufferedImage>();
    private boolean grille;
    private BufferedImage image;
    private int outil = CRAYON;
    private Color couleur = Color.BLACK;
    private int epaisseur = 3;
    private int stylePinceau;
    private boolean rempli;
    private boolean modifie;
    private boolean traceEnCours;
    private int debutX, debutY, finX, finY, dernierX, dernierY;
    private final FenetrePaint fenetre;
    private Rectangle selection;
    private boolean deplacement;
    private int decalageX, decalageY, origineSelectionX, origineSelectionY;
    private BufferedImage copieSelection;
    private JTextArea editeur;

    public PanneauDessin(FenetrePaint fenetre) {
        this.fenetre = fenetre;
        setLayout(null);
        setBackground(Color.WHITE);
        addMouseListener(this);
        addMouseMotionListener(this);
        nouveau(1000, 650);
    }

    public void nouveau(int largeur, int hauteur) {
        annulerTexte(); selection = null;
        if (largeur <= 0 || hauteur <= 0) {
            throw new IllegalArgumentException("Dimensions positives requises");
        }
        annulations.clear();
        retablissements.clear();
        image = new BufferedImage(largeur, hauteur, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, largeur, hauteur);
        g.dispose();
        modifie = false;
        traceEnCours = false;
        actualiserDimensions();
        signalerModification();
    }

    public void effacer() {
        validerTexte(); selection = null;
        memoriser();
        Graphics2D g = image.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, image.getWidth(), image.getHeight());
        g.dispose();
        traceEnCours = false;
        modifie = true;
        repaint();
        signalerModification();
    }

    public void ouvrir(File fichier) throws IOException {
        BufferedImage lue = ImageIO.read(fichier);
        if (lue == null) {
            throw new IOException("Image non reconnue");
        }
        BufferedImage nouvelle = new BufferedImage(lue.getWidth(), lue.getHeight(),
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g = nouvelle.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, nouvelle.getWidth(), nouvelle.getHeight());
        g.drawImage(lue, 0, 0, null);
        g.dispose();
        annulations.clear();
        retablissements.clear();
        image = nouvelle;
        annulerTexte(); selection = null;
        traceEnCours = false;
        modifie = false;
        actualiserDimensions();
        signalerModification();
    }

    public void enregistrer(File fichier) throws IOException {
        validerTexte();
        File parent = fichier.getAbsoluteFile().getParentFile();
        if (fichier.isDirectory() || parent == null || !parent.isDirectory()) {
            throw new IOException("Destination inaccessible");
        }
        if (!ImageIO.write(image, "png", fichier)) {
            throw new IOException("Ecriture PNG indisponible");
        }
        modifie = false;
        signalerModification();
    }

    private void actualiserDimensions() {
        setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
        revalidate();
        repaint();
    }

    public void agrandirSiNecessaire(int largeur, int hauteur) {
        if (image == null || largeur <= 0 || hauteur <= 0) { return; }
        int nouvelleLargeur = Math.max(image.getWidth(), largeur);
        int nouvelleHauteur = Math.max(image.getHeight(), hauteur);
        if (nouvelleLargeur == image.getWidth() && nouvelleHauteur == image.getHeight()) { return; }
        image = agrandirImage(image, nouvelleLargeur, nouvelleHauteur);
        modifie = true;
        actualiserDimensions();
        signalerModification();
    }

    private BufferedImage agrandirImage(BufferedImage source, int largeur, int hauteur) {
        BufferedImage resultat = new BufferedImage(largeur, hauteur, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resultat.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, largeur, hauteur);
        g.drawImage(source, 0, 0, null);
        g.dispose();
        return resultat;
    }

    public void setOutil(int outil) {
        if (outil < CRAYON || outil > PENTAGONE) {
            throw new IllegalArgumentException("Outil inconnu");
        }
        validerTexte();
        selection = null;
        this.outil = outil;
        traceEnCours = false;
        repaint();
    }

    public void setCouleur(Color couleur) {
        if (couleur != null) {
            this.couleur = couleur;
        }
    }

    public Color getCouleur() { return couleur; }

    public void setStylePinceau(int style) {
        if (style >= 0 && style <= 4) { stylePinceau = style; }
    }

    public boolean recolorerSelection() {
        if (selection == null || deplacement) { return false; }
        memoriser();
        int cible = couleur.getRGB() & 0xffffff;
        for (int y = selection.y; y < selection.y + selection.height; y++) {
            for (int x = selection.x; x < selection.x + selection.width; x++) {
                int pixel = image.getRGB(x, y) & 0xffffff;
                if (pixel != 0xffffff) { image.setRGB(x, y, cible); }
            }
        }
        modifie = true; repaint(); signalerModification(); return true;
    }

    public boolean redimensionnerSelection(int pourcentage) {
        if (selection == null || deplacement || pourcentage < 10 || pourcentage > 400) { return false; }
        int largeur = Math.max(1, selection.width * pourcentage / 100);
        int hauteur = Math.max(1, selection.height * pourcentage / 100);
        if (selection.x + largeur > image.getWidth() || selection.y + hauteur > image.getHeight()) { return false; }
        BufferedImage copie = new BufferedImage(selection.width, selection.height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = copie.createGraphics(); g.drawImage(image, -selection.x, -selection.y, null); g.dispose();
        memoriser();
        g = image.createGraphics();
        g.setColor(Color.WHITE); g.fillRect(selection.x, selection.y, selection.width, selection.height);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(copie, selection.x, selection.y, largeur, hauteur, null);
        g.dispose(); selection.width = largeur; selection.height = hauteur;
        modifie = true; repaint(); signalerModification(); return true;
    }

    public void setEpaisseur(int epaisseur) {
        if (epaisseur < 1 || epaisseur > 30) {
            throw new IllegalArgumentException("Epaisseur entre 1 et 30");
        }
        this.epaisseur = epaisseur;
    }

    public void setRempli(boolean rempli) { this.rempli = rempli; }
    public boolean estModifie() { return modifie; }
    public int getLargeurImage() { return image.getWidth(); }
    public int getHauteurImage() { return image.getHeight(); }

    private BufferedImage copieImage() {
        BufferedImage copie = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = copie.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return copie;
    }

    private void memoriser() {
        annulations.add(copieImage());
        if (annulations.size() > 8) { annulations.remove(0); }
        retablissements.clear();
    }

    public void annuler() {
        validerTexte(); selection = null; deplacement = false;
        if (annulations.isEmpty()) { return; }
        retablissements.add(copieImage());
        BufferedImage precedent = annulations.remove(annulations.size() - 1);
        image = agrandirImage(precedent, Math.max(image.getWidth(), precedent.getWidth()),
                Math.max(image.getHeight(), precedent.getHeight()));
        modifie = true; traceEnCours = false; repaint(); signalerModification();
    }

    public void retablir() {
        validerTexte(); selection = null; deplacement = false;
        if (retablissements.isEmpty()) { return; }
        annulations.add(copieImage());
        BufferedImage suivant = retablissements.remove(retablissements.size() - 1);
        image = agrandirImage(suivant, Math.max(image.getWidth(), suivant.getWidth()),
                Math.max(image.getHeight(), suivant.getHeight()));
        modifie = true; traceEnCours = false; repaint(); signalerModification();
    }

    public void setGrille(boolean visible) { grille = visible; repaint(); }

    public boolean supprimerSelection() {
        if (selection == null || deplacement) { return false; }
        memoriser();
        Graphics2D g = image.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(selection.x, selection.y, selection.width, selection.height);
        g.dispose();
        selection = null;
        modifie = true;
        repaint();
        signalerModification();
        return true;
    }

    private Rectangle rectangle(int x1, int y1, int x2, int y2) {
        return new Rectangle(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x2 - x1), Math.abs(y2 - y1));
    }

    private void ouvrirTexte(Rectangle zone) {
        if (zone.width < 10 || zone.height < 10) { return; }
        editeur = new JTextArea();
        editeur.setLineWrap(true);
        editeur.setWrapStyleWord(true);
        editeur.setOpaque(false);
        editeur.setForeground(couleur);
        editeur.setFont(new Font("SansSerif", Font.PLAIN, 16 + epaisseur * 2));
        editeur.setBorder(BorderFactory.createLineBorder(new Color(30, 140, 155)));
        editeur.setBounds(zone);
        editeur.setToolTipText(fenetre == null ? "Ctrl+Enter : OK, Esc : cancel" : fenetre.texte("text.hint"));
        editeur.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.CTRL_DOWN_MASK), "validerTexte");
        editeur.getActionMap().put("validerTexte", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { validerTexte(); }
        });
        editeur.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "annulerTexte");
        editeur.getActionMap().put("annulerTexte", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { annulerTexte(); }
        });
        editeur.addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) { validerTexte(); }
        });
        add(editeur);
        editeur.requestFocusInWindow();
        repaint();
    }

    public void validerTexte() {
        if (editeur == null) { return; }
        JTextArea zone = editeur;
        editeur = null;
        if (!zone.getText().trim().isEmpty()) {
            memoriser();
            Border bordure = zone.getBorder();
            zone.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
            zone.getCaret().setVisible(false);
            Graphics2D g = image.createGraphics();
            g.translate(zone.getX(), zone.getY());
            g.clipRect(0, 0, zone.getWidth(), zone.getHeight());
            zone.printAll(g);
            g.dispose();
            zone.setBorder(bordure);
            modifie = true;
        }
        remove(zone); repaint(); signalerModification();
    }

    private void annulerTexte() {
        if (editeur != null) { JTextArea ancien = editeur; editeur = null; remove(ancien); repaint(); }
    }

    public void ajouterTexte(String texte, int x, int y) {
        if (texte == null || texte.trim().isEmpty() || !dansImage(x, y)) { return; }
        memoriser();
        Graphics2D g = image.createGraphics();
        preparer(g);
        g.setColor(couleur);
        g.setFont(new Font("SansSerif", Font.PLAIN, 16 + epaisseur * 2));
        g.drawString(texte, x, y);
        g.dispose();
        modifie = true;
        repaint();
        signalerModification();
    }

    private void signalerModification() {
        if (fenetre != null) {
            fenetre.actualiserEtat();
        }
    }

    private boolean dansImage(int x, int y) {
        return x >= 0 && y >= 0 && x < image.getWidth() && y < image.getHeight();
    }

    private void preparer(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(outil == GOMME ? Color.WHITE : couleur);
        int taille = outil == GOMME ? epaisseur * 4 : epaisseur;
        g.setStroke(new BasicStroke(taille, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    }

    private void segment(int x1, int y1, int x2, int y2) {
        Graphics2D g = image.createGraphics();
        preparer(g);
        if (outil == CRAYON && stylePinceau == 1) {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.40f));
            g.setStroke(new BasicStroke(Math.max(4, epaisseur * 3), BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND));
        } else if (outil == CRAYON && stylePinceau == 2) {
            g.setStroke(new BasicStroke(epaisseur, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10,
                    new float[]{Math.max(2, epaisseur), Math.max(3, epaisseur * 2)}, 0));
        } else if (outil == CRAYON && stylePinceau == 3) {
            g.setStroke(new BasicStroke(Math.max(3, epaisseur * 2), BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL));
        } else if (outil == CRAYON && stylePinceau == 4) {
            java.util.Random hasard = new java.util.Random();
            int pas = Math.max(1, (int) Math.hypot(x2 - x1, y2 - y1) / 4);
            int rayon = Math.max(3, epaisseur * 2);
            for (int i = 0; i <= pas; i++) {
                int x = x1 + (x2 - x1) * i / pas;
                int y = y1 + (y2 - y1) * i / pas;
                for (int n = 0; n < 8; n++) {
                    int dx = hasard.nextInt(rayon * 2 + 1) - rayon;
                    int dy = hasard.nextInt(rayon * 2 + 1) - rayon;
                    if (dx * dx + dy * dy <= rayon * rayon) { g.fillRect(x + dx, y + dy, 1, 1); }
                }
            }
            g.dispose(); modifie = true; return;
        }
        g.drawLine(x1, y1, x2, y2);
        g.dispose();
        modifie = true;
    }

    private void dessinerForme(Graphics2D g) {
        preparer(g);
        int x = Math.min(debutX, finX);
        int y = Math.min(debutY, finY);
        int largeur = Math.abs(finX - debutX);
        int hauteur = Math.abs(finY - debutY);
        if (outil == LIGNE) {
            g.drawLine(debutX, debutY, finX, finY);
        } else if (outil == RECTANGLE) {
            if (rempli) { g.fillRect(x, y, largeur, hauteur); }
            else { g.drawRect(x, y, largeur, hauteur); }
        } else if (outil == ELLIPSE) {
            if (rempli) { g.fillOval(x, y, largeur, hauteur); }
            else { g.drawOval(x, y, largeur, hauteur); }
        } else {
            Shape forme = null;
            if (outil == ARRONDI) { forme = new RoundRectangle2D.Double(x, y, largeur, hauteur, 22, 22); }
            else if (outil == TRIANGLE) { forme = new Polygon(new int[]{x + largeur/2, x, x + largeur}, new int[]{y, y + hauteur, y + hauteur}, 3); }
            else if (outil == LOSANGE) { forme = new Polygon(new int[]{x + largeur/2, x + largeur, x + largeur/2, x}, new int[]{y, y + hauteur/2, y + hauteur, y + hauteur/2}, 4); }
            else if (outil == PENTAGONE) { forme = new Polygon(new int[]{x + largeur/2, x + largeur, x + largeur*4/5, x + largeur/5, x}, new int[]{y, y + hauteur*2/5, y + hauteur, y + hauteur, y + hauteur*2/5}, 5); }
            if (forme != null) { if (rempli) { g.fill(forme); } else { g.draw(forme); } }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image, 0, 0, null);
        if (deplacement && selection != null && copieSelection != null) {
            g.setColor(Color.WHITE);
            g.fillRect(origineSelectionX, origineSelectionY, selection.width, selection.height);
            g.drawImage(copieSelection, selection.x, selection.y, null);
        }
        if (grille) {
            Graphics2D repere = (Graphics2D) g.create();
            repere.setColor(new Color(70, 95, 140, 45));
            for (int x = 0; x < image.getWidth(); x += 25) { repere.drawLine(x, 0, x, image.getHeight()); }
            for (int y = 0; y < image.getHeight(); y += 25) { repere.drawLine(0, y, image.getWidth(), y); }
            repere.dispose();
        }
        if (traceEnCours && (outil >= LIGNE && outil <= ELLIPSE || outil >= TRIANGLE)) {
            Graphics2D copie = (Graphics2D) g.create();
            copie.clipRect(0, 0, image.getWidth(), image.getHeight());
            dessinerForme(copie);
            copie.dispose();
        }
        Rectangle cadre = selection;
        if (traceEnCours && !deplacement && (outil == TEXTE || outil == SELECTION)) {
            cadre = rectangle(debutX, debutY, finX, finY);
        }
        if (cadre != null && (outil == SELECTION || (outil == TEXTE && traceEnCours))) {
            Graphics2D repere = (Graphics2D) g.create();
            repere.setColor(new Color(24, 125, 150));
            repere.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, new float[]{5, 4}, 0));
            repere.drawRect(cadre.x, cadre.y, cadre.width, cadre.height);
            repere.dispose();
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() != MouseEvent.BUTTON1 || !dansImage(e.getX(), e.getY())) {
            return;
        }
        validerTexte();
        if (outil == SELECTION && selection != null && selection.contains(e.getPoint())) {
            deplacement = true;
            origineSelectionX = selection.x; origineSelectionY = selection.y;
            decalageX = e.getX() - selection.x; decalageY = e.getY() - selection.y;
            copieSelection = new BufferedImage(selection.width, selection.height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = copieSelection.createGraphics();
            g.drawImage(image, -selection.x, -selection.y, null); g.dispose();
        } else {
            deplacement = false;
            if (outil == SELECTION) { selection = null; }
            if (outil != TEXTE && outil != SELECTION) { memoriser(); }
        }
        debutX = finX = dernierX = e.getX();
        debutY = finY = dernierY = e.getY();
        traceEnCours = true;
        if (outil == CRAYON || outil == GOMME) {
            segment(debutX, debutY, debutX, debutY);
            signalerModification();
        }
        repaint();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (fenetre != null) { fenetre.afficherPosition(e.getX(), e.getY()); }
        if (!traceEnCours) { return; }
        if (deplacement) {
            selection.x = Math.max(0, Math.min(image.getWidth() - selection.width, e.getX() - decalageX));
            selection.y = Math.max(0, Math.min(image.getHeight() - selection.height, e.getY() - decalageY));
            repaint(); return;
        }
        finX = Math.max(0, Math.min(image.getWidth() - 1, e.getX()));
        finY = Math.max(0, Math.min(image.getHeight() - 1, e.getY()));
        if (outil == CRAYON || outil == GOMME) {
            segment(dernierX, dernierY, finX, finY);
            dernierX = finX;
            dernierY = finY;
            signalerModification();
        }
        repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (!traceEnCours || e.getButton() != MouseEvent.BUTTON1) { return; }
        if (deplacement) {
            if (selection.x != origineSelectionX || selection.y != origineSelectionY) {
                memoriser();
                Graphics2D g = image.createGraphics();
                g.setColor(Color.WHITE);
                g.fillRect(origineSelectionX, origineSelectionY, selection.width, selection.height);
                g.drawImage(copieSelection, selection.x, selection.y, null);
                g.dispose(); modifie = true;
            }
            deplacement = false; copieSelection = null; traceEnCours = false;
            repaint(); signalerModification(); return;
        }
        finX = Math.max(0, Math.min(image.getWidth() - 1, e.getX()));
        finY = Math.max(0, Math.min(image.getHeight() - 1, e.getY()));
        if (outil == TEXTE || outil == SELECTION) {
            Rectangle zone = rectangle(debutX, debutY, finX, finY);
            if (outil == TEXTE) { ouvrirTexte(zone); }
            else if (zone.width > 0 && zone.height > 0) { selection = zone; }
        } else if (outil >= LIGNE && outil <= ELLIPSE || outil >= TRIANGLE) {
            Graphics2D g = image.createGraphics();
            dessinerForme(g);
            g.dispose();
            modifie = true;
        } else if (outil == CRAYON || outil == GOMME) {
            segment(dernierX, dernierY, finX, finY);
        }
        traceEnCours = false;
        repaint();
        signalerModification();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (fenetre != null) { fenetre.afficherPosition(e.getX(), e.getY()); }
    }

    @Override public void mouseClicked(MouseEvent e) { }
    @Override public void mouseEntered(MouseEvent e) { }
    @Override public void mouseExited(MouseEvent e) {
        if (fenetre != null) { fenetre.afficherPosition(-1, -1); }
    }
}
