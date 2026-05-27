package view.component;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Champ de texte personnalisé avec coins arrondis.
 *
 * Ce composant gère :
 * - un fond arrondi,
 * - une bordure personnalisée,
 * - un texte indicatif (placeholder).
 */
public class RoundedTextField extends JTextField {

    /**
     * Texte affiché lorsque le champ est vide.
     */
    private String placeholder;

    /**
     * Rayon des coins arrondis.
     */
    private int radius;

    /**
     * Crée un champ de texte arrondi.
     *
     * @param placeholder Texte indicatif affiché si le champ est vide
     * @param radius      Rayon des coins arrondis
     */
    public RoundedTextField(String placeholder, int radius) {

        this.placeholder = placeholder;
        this.radius = radius;

        setOpaque(false);

        setForeground(Color.WHITE);

        setCaretColor(Color.WHITE);

        setFont(
                new Font("Arial", Font.PLAIN, 18)
        );

        setBorder(
                new EmptyBorder(
                        5,
                        15,
                        5,
                        15
                )
        );

        setBackground(
                new Color(50, 50, 50)
        );
    }

    /**
     * Dessine le composant personnalisé.
     *
     * Cette méthode affiche :
     * - le fond arrondi,
     * - le texte saisi,
     * - le placeholder si le champ est vide.
     *
     * @param g Contexte graphique
     */
    @Override
    protected void paintComponent(Graphics g) {

        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        // -------------------------- FOND ------------------------------------- //

        g2.setColor(getBackground());

        g2.fillRoundRect(
                0,
                0,
                getWidth() - 1,
                getHeight() - 1,
                radius,
                radius
        );

        super.paintComponent(g);

        // -------------------------- PLACEHOLDER ------------------------------ //

        if (getText().isEmpty()) {

            g2.setColor(
                    new Color(150, 150, 150)
            );

            FontMetrics fm = g2.getFontMetrics();

            int y =
                    (getHeight() - fm.getHeight()) / 2
                            + fm.getAscent();

            g2.drawString(
                    placeholder,
                    15,
                    y
            );
        }

        g2.dispose();
    }

    /**
     * Dessine la bordure arrondie du champ.
     *
     * @param g Contexte graphique
     */
    @Override
    protected void paintBorder(Graphics g) {

        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        g2.setColor(
                new Color(100, 100, 100)
        );

        g2.drawRoundRect(
                0,
                0,
                getWidth() - 1,
                getHeight() - 1,
                radius,
                radius
        );

        g2.dispose();
    }
}