
package view;

import javax.swing.*;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.awt.geom.Point2D;
import java.text.AttributedString;

public class LacunaGradientLabel extends JLabel {

    private AttributedString attributedText;

    public LacunaGradientLabel(String text) {
        super(text);

        Font serifFont = new Font("Serif", Font.BOLD, 100);

        attributedText = new AttributedString(text);
        attributedText.addAttribute(TextAttribute.FONT, serifFont);

        // Espacement des lettres
        attributedText.addAttribute(TextAttribute.TRACKING, -0.02f);

        setHorizontalAlignment(SwingConstants.CENTER);

        // IMPORTANT
        setOpaque(false);

        // Taille préférée
        setPreferredSize(new Dimension(800, 150));
    }

    @Override
    protected void paintComponent(Graphics g) {

        Graphics2D g2d = (Graphics2D) g.create();

        g2d.setRenderingHint(
            RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_ON
        );

        Font font = new Font("Serif", Font.BOLD, 100);

        g2d.setFont(font);

        FontMetrics fm = g2d.getFontMetrics();

        String text = getText();

        int textWidth = fm.stringWidth(text);

        int x = (getWidth() - textWidth) / 2;
        int y = getHeight() / 2 + fm.getAscent() / 3;

        // Gradient
        float[] fractions = {
            0.0f,
            0.25f,
            0.5f,
            0.75f,
            1.0f
        };

        Color[] colors = {
            new Color(140, 0, 255),
            new Color(0, 120, 255),
            new Color(0, 255, 180),
            new Color(255, 180, 0),
            new Color(255, 60, 60)
        };

        Point2D start = new Point2D.Float(x, y);
        Point2D end = new Point2D.Float(x + textWidth, y);

        LinearGradientPaint gradient = new LinearGradientPaint(
            start,
            end,
            fractions,
            colors
        );

        // Glow léger
        g2d.setColor(new Color(255,255,255,30));

        for(int i = 8; i >= 1; i--) {
            g2d.setStroke(new BasicStroke(i));
            g2d.drawString(text, x, y);
        }

        // Gradient principal
        g2d.setPaint(gradient);

        g2d.drawString(
            attributedText.getIterator(),
            x,
            y
        );

        g2d.dispose();
    }
}
