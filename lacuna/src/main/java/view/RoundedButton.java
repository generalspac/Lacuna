package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class RoundedButton extends JButton {

    private int radius = 40;
    private boolean hover = false;
    private boolean pressed = false;

    public RoundedButton(String text) {
        super(text);

        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

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

            @Override
            public void mousePressed(MouseEvent e) {
                pressed = true;
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                pressed = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        Color base = getBackground();
        
      
        if (pressed) {
            if (base.equals(Color.BLACK)) base = new Color(60, 60, 60);
            else base = base.darker();
        } else if (hover) {
            if (base.equals(Color.BLACK)) base = new Color(80, 80, 80);
            else base = base.brighter();
        }

     
        g2.setColor(base);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);

        FontMetrics fm = g2.getFontMetrics();
        Rectangle r = fm.getStringBounds(getText(), g2).getBounds();

        int x = (getWidth() - r.width) / 2;
        int y = (getHeight() - r.height) / 2 + fm.getAscent();

        g2.setColor(getForeground());
        g2.setFont(getFont());
        g2.drawString(getText(), x, y);

        g2.dispose();
    }

    @Override
    protected void paintBorder(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // Bordure qui change de couleur au survol
        if (hover) {
            g2.setColor(new Color(0, 255, 255)); // Cyan pour le hover
            g2.setStroke(new BasicStroke(2.5f));
        } else {
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(1.5f));
        }
        
        g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3,
                radius, radius);

        g2.dispose();
    }
}