package view.manager;

import javax.swing.*;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.awt.geom.Point2D;
import java.text.AttributedString;

/**
 * \class LacunaGradientLabel
 * \brief JLabel personnalisé avec rendu de texte en dégradé et effet lumineux.
 *
 * \details Ce composant Swing permet d’afficher un texte centré
 *          avec un dégradé multicolore et un effet de glow.
 */
public class LacunaGradientLabel extends JLabel {

    /**
     * Texte enrichi avec attributs (police, espacement, etc.).
     */
    private AttributedString attributedText;

    /**
     * Constructeur du label graphique avec dégradé.
     *
     * @param text texte à afficher
     */
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

    /**
     * Dessine le label avec dégradé et effet lumineux.
     *
     * @param g contexte graphique Swing
     */
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
        g2d.setColor(new Color(255, 255, 255, 30));

        for (int i = 8; i >= 1; i--) {
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