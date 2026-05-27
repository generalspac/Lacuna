package view.component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Case à cocher personnalisée avec apparence de switch.
 *
 * Ce composant affiche :
 * - un interrupteur arrondi,
 * - un effet de survol,
 * - un curseur mobile indiquant l'état activé/désactivé.
 */
public class RoundedCheckBox extends JCheckBox {

    /**
     * Indique si la souris survole le composant.
     */
    private boolean hover = false;

    /**
     * Crée une case à cocher personnalisée.
     *
     * @param text Texte affiché à droite du switch
     */
    public RoundedCheckBox(String text) {

        super(text);

        setOpaque(false);
        setFocusPainted(false);

        setCursor(
                new Cursor(Cursor.HAND_CURSOR)
        );

        setForeground(Color.WHITE);

        setFont(
                new Font("Arial", Font.BOLD, 14)
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
        });
    }

    /**
     * Dessine le composant personnalisé.
     *
     * Cette méthode gère :
     * - le fond du switch,
     * - le curseur mobile,
     * - le texte associé.
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

        int h = getHeight();

        int w = 45;

        int swH = 22;

        int swY = (h - swH) / 2;

        // -------------------------- COULEUR SWITCH --------------------------- //

        if (isSelected()) {

            g2.setColor(
                    new Color(46, 204, 113)
            );

        } else {

            g2.setColor(
                    new Color(120, 120, 120)
            );
        }

        // -------------------------- SURVOL ----------------------------------- //

        if (hover) {
            g2.setColor(g2.getColor().brighter());
        }

        // -------------------------- FOND SWITCH ------------------------------ //

        g2.fillRoundRect(
                0,
                swY,
                w,
                swH,
                swH,
                swH
        );

        // -------------------------- CURSEUR ---------------------------------- //

        g2.setColor(Color.WHITE);

        int dotSz = swH - 4;

        int dotX = isSelected()
                ? (w - dotSz - 2)
                : 2;

        g2.fillOval(
                dotX,
                swY + 2,
                dotSz,
                dotSz
        );

        // -------------------------- TEXTE ------------------------------------ //

        g2.setFont(getFont());

        g2.setColor(getForeground());

        FontMetrics fm = g2.getFontMetrics();

        int textY =
                (h - fm.getHeight()) / 2
                        + fm.getAscent();

        g2.drawString(
                getText(),
                w + 10,
                textY
        );

        g2.dispose();
    }

    /**
     * Retourne la taille préférée du composant.
     *
     * Un espace supplémentaire est ajouté
     * afin de laisser la place au switch.
     *
     * @return Taille préférée
     */
    @Override
    public Dimension getPreferredSize() {

        Dimension d = super.getPreferredSize();

        return new Dimension(
                d.width + 30,
                d.height
        );
    }
}