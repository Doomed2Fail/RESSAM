import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;

public class FenetrePaint extends JFrame implements ActionListener {
    private static final long serialVersionUID = 1L;
    private ResourceBundle messages;
    private String langue = "fr";
    private PanneauDessin dessin;
    private JScrollPane defilementDessin;
    private File fichierCourant;
    private JMenu fichier, outils, langueMenu, aide;
    private JMenuItem nouveau, ouvrir, enregistrer, enregistrerSous, quitter, effacer, apropos, annuler, retablir;
    private JRadioButtonMenuItem francais, anglais;
    private JButton boutonNouveau, boutonOuvrir, boutonEnregistrer, boutonCouleur, boutonAnnuler, boutonRetablir, boutonSupprimer;
    private JCheckBox grille;
    private JLabel titreFormes, titrePinceau;
    private JComboBox<String> stylesPinceau;
    private JComboBox<Integer> taillesSelection;
    private JButton appliquerCouleur, appliquerTaille;
    private JLabel titreOutils, labelEpaisseur, statut, position;
    private JCheckBox remplissage;
    private JComboBox<Integer> epaisseurs;
    private final JToggleButton[] boutonsOutils = new JToggleButton[11];
    private final String[] clesOutils = {"tool.pencil", "tool.eraser", "tool.line",
            "tool.rectangle", "tool.ellipse", "tool.text", "tool.select", "tool.triangle", "tool.diamond", "tool.rounded", "tool.pentagon"};

    public FenetrePaint() {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ex) {
        }
        Locale.setDefault(new Locale(langue));
        JComponent.setDefaultLocale(new Locale(langue));
        messages = ResourceBundle.getBundle("messages.Messages", new Locale(langue));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        JPanel fond = new JPanel(new BorderLayout());
        fond.setBackground(new Color(242, 243, 245));
        setContentPane(fond);
        dessin = new PanneauDessin(this);
        creerMenus();
        JPanel commandes = new JPanel();
        commandes.setLayout(new BoxLayout(commandes, BoxLayout.Y_AXIS));
        commandes.setBackground(new Color(250, 250, 251));
        commandes.add(creerBarreOutils());
        JScrollPane ruban = new JScrollPane(creerPanneauOutils(),
                ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        ruban.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(224, 226, 229)));
        ruban.setAlignmentX(LEFT_ALIGNMENT);
        commandes.add(ruban);
        add(commandes, BorderLayout.NORTH);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "supprimer-selection");
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "supprimer-selection");
        getRootPane().getActionMap().put("supprimer-selection", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { supprimerSelection(); }
        });
        JScrollPane defilement = new JScrollPane(dessin);
        defilementDessin = defilement;
        defilement.getViewport().addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                Dimension surface = defilement.getViewport().getExtentSize();
                dessin.agrandirSiNecessaire(surface.width, surface.height);
            }
        });
        defilement.getViewport().setBackground(new Color(235, 237, 239));
        defilement.setBorder(BorderFactory.createEmptyBorder(18, 30, 12, 20));
        defilement.setPreferredSize(new Dimension(850, 580));
        add(defilement, BorderLayout.CENTER);
        JPanel bas = new JPanel(new BorderLayout());
        bas.setBackground(new Color(250, 250, 251));
        bas.setBorder(BorderFactory.createEmptyBorder(4, 10, 6, 10));
        statut = new JLabel();
        position = new JLabel(" ");
        bas.add(statut, BorderLayout.CENTER);
        bas.add(position, BorderLayout.EAST);
        add(bas, BorderLayout.SOUTH);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) { fermer(); }
        });
        traduire();
        setMinimumSize(new Dimension(760, 500));
        pack();
        setLocationRelativeTo(null);
    }

    public String texte(String cle) { return messages.getString(cle); }

    private JMenuItem item(String commande, int touche) {
        JMenuItem item = new JMenuItem();
        item.setActionCommand(commande);
        item.addActionListener(this);
        if (touche != 0) {
            item.setAccelerator(KeyStroke.getKeyStroke(touche, InputEvent.CTRL_DOWN_MASK));
        }
        return item;
    }

    private void creerMenus() {
        JMenuBar barre = new JMenuBar();
        fichier = new JMenu(); outils = new JMenu(); langueMenu = new JMenu(); aide = new JMenu();
        nouveau = item("new", KeyEvent.VK_N);
        ouvrir = item("open", KeyEvent.VK_O);
        enregistrer = item("save", KeyEvent.VK_S);
        enregistrerSous = item("saveAs", 0);
        quitter = item("exit", 0);
        fichier.add(nouveau); fichier.add(ouvrir); fichier.addSeparator();
        fichier.add(enregistrer); fichier.add(enregistrerSous); fichier.addSeparator(); fichier.add(quitter);
        annuler = item("undo", KeyEvent.VK_Z); retablir = item("redo", KeyEvent.VK_Y);
        outils.add(annuler); outils.add(retablir); outils.addSeparator();
        effacer = item("clear", 0); outils.add(effacer);
        francais = new JRadioButtonMenuItem("Français", true);
        anglais = new JRadioButtonMenuItem("English");
        francais.setActionCommand("fr"); anglais.setActionCommand("en");
        francais.addActionListener(this); anglais.addActionListener(this);
        ButtonGroup langues = new ButtonGroup(); langues.add(francais); langues.add(anglais);
        langueMenu.add(francais); langueMenu.add(anglais);
        apropos = item("about", 0); aide.add(apropos);
        barre.add(fichier); barre.add(outils); barre.add(langueMenu); barre.add(aide);
        setJMenuBar(barre);
    }

    private JButton bouton(String commande) {
        JButton bouton = new JButton();
        bouton.setUI(new StyleBouton());
        bouton.setContentAreaFilled(false);
        bouton.setFocusPainted(false);
        bouton.setBorder(BorderFactory.createEmptyBorder(9, 15, 9, 15));
        bouton.setFont(new Font("SansSerif", Font.BOLD, 12));
        bouton.setActionCommand(commande);
        bouton.addActionListener(this);
        return bouton;
    }

    private JToolBar creerBarreOutils() {
        JToolBar barre = new JToolBar();
        barre.setFloatable(false);
        barre.setOpaque(true);
        barre.setBackground(new Color(250, 250, 251));
        barre.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        barre.setAlignmentX(LEFT_ALIGNMENT);
        JLabel marque = new JLabel("  ◉  RESSAM     ");
        marque.setFont(new Font("SansSerif", Font.BOLD, 14));
        marque.setForeground(new Color(46, 67, 75));
        barre.add(marque);
        boutonNouveau = bouton("new"); boutonOuvrir = bouton("open"); boutonEnregistrer = bouton("save");
        barre.add(boutonNouveau); barre.add(boutonOuvrir); barre.add(boutonEnregistrer);
        barre.addSeparator();
        boutonAnnuler = bouton("undo"); boutonRetablir = bouton("redo");
        barre.add(boutonAnnuler); barre.add(boutonRetablir);
        boutonSupprimer = bouton("deleteSelection");
        barre.add(boutonSupprimer);
        return barre;
    }

    private JPanel creerPanneauOutils() {
        JPanel ruban = new JPanel(new BorderLayout()) {
            @Override public Dimension getPreferredSize() {
                Dimension taille = super.getPreferredSize();
                return new Dimension(taille.width, taille.height + 16);
            }
        };
        ruban.setBackground(new Color(250, 250, 251));
        ruban.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        JPanel rangeeOutils = new JPanel();
        rangeeOutils.setLayout(new BoxLayout(rangeeOutils, BoxLayout.X_AXIS));
        rangeeOutils.setOpaque(false);
        ruban.add(rangeeOutils, BorderLayout.CENTER);
        JPanel blocOutils = new JPanel(new BorderLayout(0, 5));
        blocOutils.setOpaque(false);
        blocOutils.setMaximumSize(new Dimension(110, 105));
        titreOutils = new JLabel();
        titreOutils.setForeground(new Color(103, 108, 115));
        titreOutils.setFont(new Font("SansSerif", Font.PLAIN, 11));
        JPanel choix = new JPanel(new GridLayout(2, 2, 5, 5));
        choix.setOpaque(false);
        JPanel choixFormes = new JPanel(new GridLayout(0, 3, 2, 2));
        choixFormes.setOpaque(false);
        ButtonGroup groupe = new ButtonGroup();
        for (int i = 0; i < boutonsOutils.length; i++) {
            JToggleButton b = new JToggleButton();
            b.setUI(new StyleBouton());
            b.setContentAreaFilled(false);
            b.setFocusPainted(false);
            b.setFont(new Font("SansSerif", Font.PLAIN, 12));
            b.setBorder(BorderFactory.createEmptyBorder(7, 10, 7, 10));
            b.setActionCommand("tool" + i);
            b.setIcon(new IconeOutil(i));
            b.setIconTextGap(6);
            b.addActionListener(this);
            b.putClientProperty("iconeSeule", Boolean.TRUE);
            b.setPreferredSize(new Dimension(32, 29));
            b.setMinimumSize(new Dimension(32, 29));
            b.setMargin(new java.awt.Insets(0, 0, 0, 0));
            b.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
            boutonsOutils[i] = b;
            groupe.add(b);
            if (i == 0 || i == 1 || i == 5 || i == 6) {
                choix.add(b);
            } else {
                choixFormes.add(b);
            }
        }
        boutonsOutils[0].setSelected(true);
        blocOutils.add(choix, BorderLayout.CENTER);
        blocOutils.add(titreOutils, BorderLayout.SOUTH);
        rangeeOutils.add(blocOutils);
        JPanel blocFormes = new JPanel(new BorderLayout(0, 5));
        blocFormes.setOpaque(false);
        blocFormes.setMaximumSize(new Dimension(155, 105));
        blocFormes.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
        titreFormes = new JLabel();
        titreFormes.setFont(new Font("SansSerif", Font.PLAIN, 11));
        titreFormes.setForeground(new Color(103, 108, 115));
        JScrollPane defilementFormes = new JScrollPane(choixFormes,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        defilementFormes.setPreferredSize(new Dimension(120, 64));
        defilementFormes.setMinimumSize(new Dimension(120, 64));
        defilementFormes.setBorder(BorderFactory.createLineBorder(new Color(227, 229, 231)));
        defilementFormes.getVerticalScrollBar().setUnitIncrement(24);
        defilementFormes.getVerticalScrollBar().setPreferredSize(new Dimension(9, 0));
        defilementFormes.getVerticalScrollBar().setUI(new BarreDiscrete());
        blocFormes.add(defilementFormes, BorderLayout.CENTER);
        blocFormes.add(titreFormes, BorderLayout.SOUTH);
        rangeeOutils.add(blocFormes);
        JPanel blocOptions = new JPanel(new GridLayout(2, 2, 8, 5));
        blocOptions.setOpaque(false);
        blocOptions.setMaximumSize(new Dimension(235, 95));
        blocOptions.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(229, 230, 232)),
                BorderFactory.createEmptyBorder(3, 16, 3, 16)));
        labelEpaisseur = new JLabel();
        labelEpaisseur.setFont(new Font("SansSerif", Font.PLAIN, 11));
        epaisseurs = new JComboBox<Integer>(new Integer[]{1, 3, 5, 8, 12, 20, 30});
        epaisseurs.setSelectedItem(Integer.valueOf(3));
        epaisseurs.setActionCommand("width");
        epaisseurs.addActionListener(this);
        remplissage = new JCheckBox();
        remplissage.setOpaque(false);
        remplissage.setActionCommand("fill");
        remplissage.addActionListener(this);
        grille = new JCheckBox();
        grille.setOpaque(false);
        grille.setActionCommand("grid");
        grille.addActionListener(this);
        blocOptions.add(labelEpaisseur);
        blocOptions.add(epaisseurs);
        blocOptions.add(remplissage);
        blocOptions.add(grille);
        rangeeOutils.add(blocOptions);
        JPanel blocPinceau = new JPanel(new GridLayout(2, 1, 2, 4));
        blocPinceau.setOpaque(false);
        blocPinceau.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
        blocPinceau.setMinimumSize(new Dimension(145, 70));
        blocPinceau.setPreferredSize(new Dimension(145, 70));
        blocPinceau.setMaximumSize(new Dimension(145, 95));
        titrePinceau = new JLabel();
        stylesPinceau = new JComboBox<String>();
        stylesPinceau.setActionCommand("brushStyle");
        stylesPinceau.addActionListener(this);
        blocPinceau.add(titrePinceau); blocPinceau.add(stylesPinceau);
        rangeeOutils.add(blocPinceau);
        JPanel blocCouleurs = new JPanel(new BorderLayout(8, 5));
        blocCouleurs.setOpaque(false);
        blocCouleurs.setMaximumSize(new Dimension(270, 95));
        blocCouleurs.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(229, 230, 232)),
                BorderFactory.createEmptyBorder(3, 16, 3, 8)));
        JPanel palette = new JPanel(new GridLayout(2, 6, 4, 4));
        palette.setOpaque(false);
        Color[] couleurs = {Color.BLACK, new Color(105,105,105), new Color(204,45,59),
                new Color(240,127,47), new Color(243,202,48), new Color(37,165,95),
                Color.WHITE, new Color(164,172,183), new Color(219,126,151),
                new Color(166,113,197), new Color(48,139,208), new Color(52,65,162)};
        for (final Color couleur : couleurs) {
            JButton pastille = new JButton() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                            java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(couleur);
                    g2.fillOval(3, 3, 18, 18);
                    g2.setColor(new Color(150, 155, 160));
                    g2.drawOval(3, 3, 18, 18);
                    g2.dispose();
                }
            };
            pastille.setPreferredSize(new Dimension(24, 24));
            pastille.setToolTipText(String.format("#%06X", couleur.getRGB() & 0xffffff));
            pastille.setContentAreaFilled(false);
            pastille.setBorderPainted(false);
            pastille.setFocusPainted(false);
            pastille.addActionListener(new ActionListener() {
                @Override public void actionPerformed(ActionEvent e) { choisirCouleur(couleur); }
            });
            palette.add(pastille);
        }
        boutonCouleur = bouton("color");
        blocCouleurs.add(palette, BorderLayout.CENTER);
        blocCouleurs.add(boutonCouleur, BorderLayout.EAST);
        rangeeOutils.add(blocCouleurs);
        JPanel blocSelection = new JPanel(new GridLayout(2, 2, 5, 5));
        blocSelection.setOpaque(false);
        blocSelection.setMaximumSize(new Dimension(300, 95));
        blocSelection.setBorder(BorderFactory.createEmptyBorder(3, 12, 3, 12));
        appliquerCouleur = bouton("recolorSelection");
        appliquerTaille = bouton("resizeSelection");
        taillesSelection = new JComboBox<Integer>(new Integer[]{50, 75, 125, 150, 200});
        blocSelection.add(appliquerCouleur); blocSelection.add(appliquerTaille);
        blocSelection.add(new JLabel("%")); blocSelection.add(taillesSelection);
        rangeeOutils.add(blocSelection);
        return ruban;
    }


    public void changerLangue(String code) {
        if (!"fr".equals(code) && !"en".equals(code)) { return; }
        langue = code;
        Locale.setDefault(new Locale(langue));
        JComponent.setDefaultLocale(new Locale(langue));
        messages = ResourceBundle.getBundle("messages.Messages", new Locale(langue));
        traduire();
    }

    private void traduire() {
        fichier.setText(texte("menu.file")); outils.setText(texte("menu.drawing"));
        langueMenu.setText(texte("menu.language")); aide.setText(texte("menu.help"));
        nouveau.setText(texte("file.new")); ouvrir.setText(texte("file.open"));
        enregistrer.setText(texte("file.save")); enregistrerSous.setText(texte("file.saveAs"));
        quitter.setText(texte("file.exit")); effacer.setText(texte("drawing.clear"));
        annuler.setText(texte("drawing.undo")); retablir.setText(texte("drawing.redo"));
        boutonAnnuler.setText(texte("drawing.undo")); boutonRetablir.setText(texte("drawing.redo"));
        boutonSupprimer.setText(texte("drawing.deleteSelection"));
        grille.setText(texte("drawing.grid"));
        titreFormes.setText(texte("shapes.title"));
        titrePinceau.setText(texte("brush.title"));
        appliquerCouleur.setText(texte("select.recolor"));
        appliquerTaille.setText(texte("select.resize"));
        int style = Math.max(0, stylesPinceau.getSelectedIndex());
        stylesPinceau.removeActionListener(this);
        stylesPinceau.removeAllItems();
        stylesPinceau.addItem(texte("brush.solid"));
        stylesPinceau.addItem(texte("brush.marker"));
        stylesPinceau.addItem(texte("brush.dotted"));
        stylesPinceau.addItem(texte("brush.calligraphy"));
        stylesPinceau.addItem(texte("brush.spray"));
        stylesPinceau.setSelectedIndex(style);
        stylesPinceau.addActionListener(this);
        apropos.setText(texte("help.about"));
        boutonNouveau.setText(texte("file.new")); boutonOuvrir.setText(texte("file.open"));
        boutonEnregistrer.setText(texte("file.save"));
        titreOutils.setText(texte("tools.title")); labelEpaisseur.setText(texte("tools.width"));
        boutonCouleur.setText(texte("tools.color"));
        boutonCouleur.setToolTipText(texte("color.current") + " " + couleurHex());
        remplissage.setText(texte("tools.fill"));
        for (int i = 0; i < boutonsOutils.length; i++) {
            boutonsOutils[i].setText(texte(clesOutils[i]));
            boutonsOutils[i].setToolTipText(texte(clesOutils[i]));
        }
        francais.setSelected("fr".equals(langue)); anglais.setSelected("en".equals(langue));
        UIManager.put("OptionPane.yesButtonText", texte("dialog.yes"));
        UIManager.put("OptionPane.noButtonText", texte("dialog.no"));
        UIManager.put("OptionPane.cancelButtonText", texte("dialog.cancel"));
        UIManager.put("OptionPane.okButtonText", texte("dialog.ok"));
        actualiserEtat();
        revalidate(); repaint();
    }

    private String couleurHex() {
        return String.format("#%06X", dessin.getCouleur().getRGB() & 0xFFFFFF);
    }

    public void actualiserEtat() {
        if (dessin == null || statut == null) { return; }
        String nom = fichierCourant == null ? texte("document.untitled") : fichierCourant.getName();
        setTitle((dessin.estModifie() ? "* " : "") + nom + " - " + texte("app.title"));
        String outil = clesOutils[0];
        for (int i = 0; i < boutonsOutils.length; i++) {
            if (boutonsOutils[i].isSelected()) { outil = clesOutils[i]; }
        }
        statut.setText(dessin.getLargeurImage() + " x " + dessin.getHauteurImage() + " px  |  "
                + texte(outil) + "  |  " + couleurHex()
                + (boutonsOutils[5].isSelected() ? "  |  " + texte("text.hint") : "")
                + (boutonsOutils[6].isSelected() ? "  |  " + texte("select.hint") : ""));
    }

    public void afficherPosition(int x, int y) {
        if (position != null) { position.setText(x < 0 ? " " : "x : " + x + "   y : " + y); }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();
        if (action.startsWith("tool")) {
            dessin.setOutil(Integer.parseInt(action.substring(4)));
            actualiserEtat();
        } else if ("undo".equals(action)) { dessin.annuler();
        } else if ("redo".equals(action)) { dessin.retablir();
        } else if ("deleteSelection".equals(action)) { supprimerSelection();
        } else if ("grid".equals(action)) { dessin.setGrille(grille.isSelected());
        } else if ("brushStyle".equals(action)) { dessin.setStylePinceau(stylesPinceau.getSelectedIndex());
        } else if ("recolorSelection".equals(action)) {
            if (!dessin.recolorerSelection()) { messageSelection(); }
        } else if ("resizeSelection".equals(action)) {
            if (!dessin.redimensionnerSelection(((Integer) taillesSelection.getSelectedItem()).intValue())) {
                JOptionPane.showMessageDialog(this, texte("select.resizeError"));
            }
        } else if ("new".equals(action)) {
            if (autoriserRemplacement()) {
                fichierCourant = null;
                dessin.nouveau(1000, 650);
                adapterFeuille();
            }
        } else if ("open".equals(action)) { ouvrirImage();
        } else if ("save".equals(action)) { enregistrerImage(false);
        } else if ("saveAs".equals(action)) { enregistrerImage(true);
        } else if ("exit".equals(action)) { fermer();
        } else if ("clear".equals(action)) {
            int reponse = JOptionPane.showConfirmDialog(this, texte("clear.prompt"),
                    texte("drawing.clear"), JOptionPane.YES_NO_OPTION);
            if (reponse == JOptionPane.YES_OPTION) { dessin.effacer(); }
        } else if ("color".equals(action)) {
            Color couleur = JColorChooser.showDialog(this, texte("tools.color"), dessin.getCouleur());
            if (couleur != null) {
                choisirCouleur(couleur);
            }
        } else if ("width".equals(action)) {
            dessin.setEpaisseur(((Integer) epaisseurs.getSelectedItem()).intValue());
        } else if ("fill".equals(action)) { dessin.setRempli(remplissage.isSelected());
        } else if ("fr".equals(action) || "en".equals(action)) { changerLangue(action);
        } else if ("about".equals(action)) {
            JOptionPane.showMessageDialog(this, texte("about.message"), texte("help.about"),
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void choisirCouleur(Color couleur) {
        dessin.setCouleur(couleur);
        boutonCouleur.setBackground(couleur);
        boutonCouleur.setOpaque(true);
        boutonCouleur.setForeground(couleur.getRed() + couleur.getGreen() + couleur.getBlue() < 380
                ? Color.WHITE : Color.BLACK);
        boutonCouleur.setToolTipText(texte("color.current") + " " + couleurHex());
        actualiserEtat();
    }

    private void supprimerSelection() {
        if (!dessin.supprimerSelection()) {
            messageSelection();
        }
    }

    private void messageSelection() {
        JOptionPane.showMessageDialog(this, texte("select.required"),
                texte("tool.select"), JOptionPane.INFORMATION_MESSAGE);
    }

    private void adapterFeuille() {
        Dimension surface = defilementDessin.getViewport().getExtentSize();
        dessin.agrandirSiNecessaire(surface.width, surface.height);
    }

    private JFileChooser selecteur(String titre, boolean sauvegarde) {
        JFileChooser choix = new JFileChooser();
        choix.setLocale(new Locale(langue));
        choix.updateUI();
        choix.setDialogTitle(titre);
        choix.setApproveButtonText(texte(sauvegarde ? "file.save" : "file.open"));
        if (fichierCourant != null) { choix.setSelectedFile(fichierCourant); }
        return choix;
    }

    private void ouvrirImage() {
        if (!autoriserRemplacement()) { return; }
        JFileChooser choix = selecteur(texte("open.title"), false);
        if (choix.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) { return; }
        try {
            dessin.ouvrir(choix.getSelectedFile());
            adapterFeuille();
            fichierCourant = null;
            actualiserEtat();
        } catch (IOException ex) { erreur("error.open"); }
    }

    private boolean enregistrerImage(boolean sous) {
        File destination = fichierCourant;
        if (sous || destination == null) {
            JFileChooser choix = selecteur(texte("save.title"), true);
            if (destination == null) { choix.setSelectedFile(new File("dessin.png")); }
            if (choix.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) { return false; }
            destination = choix.getSelectedFile();
            if (!destination.getName().toLowerCase(Locale.ROOT).endsWith(".png")) {
                destination = new File(destination.getParentFile(), destination.getName() + ".png");
            }
            if (destination.exists()) {
                int reponse = JOptionPane.showConfirmDialog(this, texte("overwrite.prompt"),
                        texte("file.save"), JOptionPane.YES_NO_OPTION);
                if (reponse != JOptionPane.YES_OPTION) { return false; }
            }
        }
        try {
            dessin.enregistrer(destination);
            fichierCourant = destination;
            actualiserEtat();
            return true;
        } catch (IOException ex) {
            erreur("error.save");
            return false;
        }
    }

    private boolean autoriserRemplacement() {
        if (!dessin.estModifie()) { return true; }
        int reponse = JOptionPane.showConfirmDialog(this, texte("unsaved.prompt"),
                texte("app.title"), JOptionPane.YES_NO_CANCEL_OPTION);
        if (reponse == JOptionPane.YES_OPTION) { return enregistrerImage(false); }
        return reponse == JOptionPane.NO_OPTION;
    }

    private void erreur(String cle) {
        JOptionPane.showMessageDialog(this, texte(cle), texte("error.title"), JOptionPane.ERROR_MESSAGE);
    }

    private void fermer() {
        if (autoriserRemplacement()) { dispose(); }
    }
}
