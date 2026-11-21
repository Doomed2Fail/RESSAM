import java.awt.*;
import javax.swing.*;

public class IconeOutil implements Icon {
    private final int outil;
    public IconeOutil(int outil) { this.outil = outil; }
    @Override public int getIconWidth() { return 18; }
    @Override public int getIconHeight() { return 18; }
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D d = (Graphics2D) g.create();
        d.translate(x, y);
        d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        d.setColor(new Color(55, 75, 80));
        d.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        switch (outil) {
            case 0: d.drawLine(3, 15, 14, 4); d.drawLine(6, 16, 16, 6); d.drawLine(14, 4, 16, 6); d.drawLine(3, 15, 6, 16); break;
            case 1: d.rotate(-0.5, 9, 9); d.drawRoundRect(3, 4, 12, 10, 3, 3); d.drawLine(3, 10, 15, 10); break;
            case 2: d.drawLine(3, 15, 15, 3); break;
            case 3: d.drawRoundRect(2, 4, 14, 10, 2, 2); break;
            case 4: d.drawOval(2, 3, 14, 12); break;
            case 5: d.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 14)); d.drawString("T", 4, 14); break;
            case 6: d.setStroke(new BasicStroke(1.3f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, new float[]{3, 2}, 0)); d.drawRect(3, 3, 12, 12); break;
            case 7: d.drawPolygon(new int[]{9, 2, 16}, new int[]{2, 15, 15}, 3); break;
            case 8: d.drawPolygon(new int[]{9, 16, 9, 2}, new int[]{2, 9, 16, 9}, 4); break;
            case 9: d.drawRoundRect(2, 4, 14, 10, 6, 6); break;
            case 10: d.drawPolygon(new int[]{9, 16, 13, 5, 2}, new int[]{2, 7, 15, 15, 7}, 5); break;
            default: break;
        }
        d.dispose();
    }
}
