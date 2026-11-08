package view.component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Bouton personnalisé avec coins arrondis.
 *
 * Ce composant gère :
 * - l'affichage arrondi,
 * - les effets de survol,
 * - les effets de clic,
 * - le rendu personnalisé du texte et des bordures.
 */
public class RoundedButton extends JButton {

    /**
     * Rayon des coins arrondis.
     */
    private int radius = 40;

    /**
     * Indique si la souris survole le bouton.
     */
    private boolean hover = false;

    /**
     * Indique si le bouton est actuellement pressé.
     */
    private boolean pressed = false;

    /**
     * Crée un bouton arrondi personnalisé.
     *
     * @param text Texte affiché sur le bouton
     */
    public RoundedButton(String text) {

        super(text);

        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);

        setCursor(
                new Cursor(Cursor.HAND_CURSOR)
        );

        addMouseListener(new MouseAdapter() {

            /**
             * Active l'effet de survol.
             *
             * @param e Événement souris
             */
            @Override
            public void mouseEntered(MouseEvent e) {
                hover = true;
                repaint();
            }

            /**
             * Désactive l'effet de survol.
             *
             * @param e Événement souris
             */
            @Override
            public void mouseExited(MouseEvent e) {
                hover = false;
                repaint();
            }

            /**
             * Active l'effet de pression.
             *
             * @param e Événement souris
             */
            @Override
            public void mousePressed(MouseEvent e) {
                pressed = true;
                repaint();
            }

            /**
             * Désactive l'effet de pression.
             *
             * @param e Événement souris
             */
            @Override
            public void mouseReleased(MouseEvent e) {
                pressed = false;
                repaint();
            }
        });
    }

    /**
     * Dessine le contenu du bouton.
     *
     * Cette méthode gère :
     * - le fond arrondi,
     * - les changements de couleur,
     * - le texte centré.
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

        Color base = getBackground();

        // -------------------------- ÉTAT : PRESSÉ ---------------------------- //

        if (pressed) {

            if (base.equals(Color.BLACK)) {
                base = new Color(60, 60, 60);
            } else {
                base = base.darker();
            }

        // -------------------------- ÉTAT : SURVOL ---------------------------- //

        } else if (hover) {

            if (base.equals(Color.BLACK)) {
                base = new Color(80, 80, 80);
            } else {
                base = base.brighter();
            }
        }

        // -------------------------- FOND ------------------------------------- //

        g2.setColor(base);

        g2.fillRoundRect(
                0,
                0,
                getWidth(),
                getHeight(),
                radius,
                radius
        );

        // -------------------------- TEXTE ------------------------------------ //

        FontMetrics fm = g2.getFontMetrics();

        Rectangle r = fm.getStringBounds(
                getText(),
                g2
        ).getBounds();

        int x = (getWidth() - r.width) / 2;

        int y = (getHeight() - r.height) / 2
                + fm.getAscent();

        g2.setColor(getForeground());

        g2.setFont(getFont());

        g2.drawString(getText(), x, y);

        g2.dispose();
    }

    /**
     * Dessine la bordure du bouton.
     *
     * La bordure change de couleur lors du survol.
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

        // -------------------------- SURVOL ----------------------------------- //

        if (hover) {

            g2.setColor(new Color(0, 255, 255));

            g2.setStroke(new BasicStroke(2.5f));

        // -------------------------- NORMAL ----------------------------------- //

        } else {

            g2.setColor(Color.WHITE);

            g2.setStroke(new BasicStroke(1.5f));
        }

        g2.drawRoundRect(
                1,
                1,
                getWidth() - 3,
                getHeight() - 3,
                radius,
                radius
        );

        g2.dispose();
    }
}