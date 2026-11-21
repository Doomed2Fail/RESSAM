import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.basic.*;

public class StyleBouton extends BasicButtonUI {
    @Override protected void paintText(Graphics g, JComponent composant, Rectangle zone, String texte) {
        if (!Boolean.TRUE.equals(composant.getClientProperty("iconeSeule"))) {
            super.paintText(g, composant, zone, texte);
        }
    }
    @Override public void paint(Graphics g, JComponent composant) {
        AbstractButton bouton = (AbstractButton) composant;
        Graphics2D dessin = (Graphics2D) g.create();
        dessin.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        boolean actif = bouton.isSelected() || bouton.getModel().isPressed();
        dessin.setColor(actif ? new Color(221, 239, 241) :
                bouton.getModel().isRollover() ? new Color(235, 240, 241) : new Color(250, 250, 251));
        dessin.fillRoundRect(0, 0, composant.getWidth() - 1, composant.getHeight() - 1, 8, 8);
        if (actif || bouton.getModel().isRollover()) {
            dessin.setColor(actif ? new Color(91, 150, 160) : new Color(220, 225, 226));
            dessin.drawRoundRect(0, 0, composant.getWidth() - 1, composant.getHeight() - 1, 8, 8);
        }
        bouton.setForeground(new Color(49, 55, 60));
        dessin.dispose();
        if (Boolean.TRUE.equals(composant.getClientProperty("iconeSeule")) && bouton.getIcon() != null) {
            javax.swing.Icon icone = bouton.getIcon();
            icone.paintIcon(composant, g, (composant.getWidth() - icone.getIconWidth()) / 2,
                    (composant.getHeight() - icone.getIconHeight()) / 2);
        } else {
            super.paint(g, composant);
        }
    }
}
