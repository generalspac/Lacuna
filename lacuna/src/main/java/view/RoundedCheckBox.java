package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class RoundedCheckBox extends JCheckBox {

    private boolean hover = false;

    public RoundedCheckBox(String text) {
        super(text);
        setOpaque(false);
        setFocusPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setForeground(Color.WHITE);
        setFont(new Font("Arial", Font.BOLD, 14));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hover = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hover = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int h = getHeight();
        int w = 45; // Largeur du switch
        int swH = 22; // Hauteur du switch
        int swY = (h - swH) / 2;

        // Fond du switch
        if (isSelected()) {
            g2.setColor(new Color(46, 204, 113)); // Vert moderne
        } else {
            g2.setColor(new Color(120, 120, 120)); // Gris
        }
        
        if (hover) {
            g2.setColor(g2.getColor().brighter());
        }

        g2.fillRoundRect(0, swY, w, swH, swH, swH);

        // Cercle blanc (le curseur)
        g2.setColor(Color.WHITE);
        int dotSz = swH - 4;
        int dotX = isSelected() ? (w - dotSz - 2) : 2;
        g2.fillOval(dotX, swY + 2, dotSz, dotSz);

        // Texte
        g2.setFont(getFont());
        g2.setColor(getForeground());
        FontMetrics fm = g2.getFontMetrics();
        int textY = (h - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(getText(), w + 10, textY);

        g2.dispose();
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(d.width + 30, d.height);
    }
}
