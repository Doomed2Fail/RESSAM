import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.basic.*;

public class BarreDiscrete extends BasicScrollBarUI {
    @Override protected JButton createDecreaseButton(int direction) { return boutonInvisible(); }
    @Override protected JButton createIncreaseButton(int direction) { return boutonInvisible(); }
    private JButton boutonInvisible() {
        JButton bouton = new JButton();
        bouton.setPreferredSize(new Dimension(0, 0));
        bouton.setMinimumSize(new Dimension(0, 0));
        bouton.setMaximumSize(new Dimension(0, 0));
        return bouton;
    }
    @Override protected void paintTrack(Graphics g, JComponent c, Rectangle r) { }
    @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
        if (r.isEmpty() || !scrollbar.isEnabled()) { return; }
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(154, 162, 165));
        g2.fillRoundRect(r.x + 2, r.y + 2, Math.max(3, r.width - 4), Math.max(4, r.height - 4), 5, 5);
        g2.dispose();
    }
}
