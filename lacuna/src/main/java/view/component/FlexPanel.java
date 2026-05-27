package view.component;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * --------------------- FLEX PANEL -------------------------- //
 *
 * Panel personnalisé inspiré du comportement Flexbox du web.
 *
 * Permet :
 * - l'alignement horizontal ou vertical,
 * - la gestion des espacements,
 * - l'étirement des composants,
 * - la répartition dynamique de l'espace via le flex-grow.
 */
public class FlexPanel extends JPanel {

    // --------------------- ENUM : DIRECTION -------------------------- //

    /**
     * Direction principale du layout.
     */
    public enum Direction {

        /**
         * Alignement horizontal.
         */
        ROW,

        /**
         * Alignement vertical.
         */
        COLUMN
    }

    // --------------------- ENUM : JUSTIFY -------------------------- //

    /**
     * Alignement principal des composants.
     */
    public enum Justify {

        /**
         * Alignement au début.
         */
        START,

        /**
         * Alignement centré.
         */
        CENTER,

        /**
         * Alignement à la fin.
         */
        END,

        /**
         * Répartition avec espaces.
         */
        SPACE_BETWEEN
    }

    // --------------------- ENUM : ALIGN -------------------------- //

    /**
     * Alignement secondaire des composants.
     */
    public enum Align {

        /**
         * Alignement au début.
         */
        START,

        /**
         * Alignement centré.
         */
        CENTER,

        /**
         * Alignement à la fin.
         */
        END,

        /**
         * Étirement maximal.
         */
        STRETCH
    }

    // --------------------- CONFIGURATION -------------------------- //

    /**
     * Direction du layout.
     */
    private Direction direction = Direction.ROW;

    /**
     * Alignement principal.
     */
    private Justify justify = Justify.CENTER;

    /**
     * Alignement secondaire.
     */
    private Align align = Align.STRETCH;

    /**
     * Espacement entre composants.
     */
    private int gap = 10;

    /**
     * Valeurs de flex-grow associées aux composants.
     */
    private final Map<Component, Integer> flexGrow = new HashMap<>();

    // --------------------- CONSTRUCTEUR -------------------------- //

    /**
     * Crée un FlexPanel vide.
     */
    public FlexPanel() {

        setLayout(null);

        setBackground(Color.BLACK);
    }

    // ================= CONFIGURATION ================= //

    /**
     * Définit la direction principale.
     *
     * @param d Direction du layout
     */
    public void setDirection(Direction d) {

        this.direction = d;
    }

    /**
     * Définit l'alignement principal.
     *
     * @param j Alignement principal
     */
    public void setJustify(Justify j) {

        this.justify = j;
    }

    /**
     * Définit l'alignement secondaire.
     *
     * @param a Alignement secondaire
     */
    public void setAlign(Align a) {

        this.align = a;
    }

    /**
     * Définit l'espacement entre composants.
     *
     * @param g Taille de l'espacement
     */
    public void setGap(int g) {

        this.gap = g;
    }

    /**
     * Définit le facteur de croissance d'un composant.
     *
     * @param c      Composant concerné
     * @param value  Valeur de flex-grow
     */
    public void setFlexGrow(Component c, int value) {

        flexGrow.put(c, value);
    }

    // ================= LAYOUT ENGINE ================= //

    /**
     * Recalcule la position et la taille
     * de tous les composants du panel.
     */
    @Override
    public void doLayout() {

        Component[] comps = getComponents();

        if (comps.length == 0) {
            return;
        }

        int width = getWidth();

        int height = getHeight();

        if (width <= 0 || height <= 0) {
            return;
        }

        boolean row = direction == Direction.ROW;

        int totalFixed = 0;

        int totalFlex = 0;

        int count = comps.length;

        // --------------------- CALCUL DES TAILLES -------------------------- //

        for (int i = 0; i < count; i++) {

            Component c = comps[i];

            Dimension d = c.getPreferredSize();

            int grow = flexGrow.getOrDefault(c, 0);

            if (row) {

                if (grow == 0) {
                    totalFixed += d.width;
                } else {
                    totalFlex += grow;
                }

            } else {

                if (grow == 0) {
                    totalFixed += d.height;
                } else {
                    totalFlex += grow;
                }
            }
        }

        int space = row ? width : height;

        int cursorX = 0;

        int cursorY = 0;

        // --------------------- ALIGNEMENT CENTER -------------------------- //

        int offset = (justify == Justify.CENTER)
                ? (space - totalFixed - gap * (count - 1)) / 2
                : 0;

        /*
         * Si des composants flexibles existent,
         * ils occupent déjà tout l'espace disponible.
         */
        if (totalFlex > 0) {
            offset = 0;
        }

        if (row) {
            cursorX = offset;
        } else {
            cursorY = offset;
        }

        // --------------------- POSITIONNEMENT -------------------------- //

        for (int i = 0; i < count; i++) {

            Component c = comps[i];

            Dimension d = c.getPreferredSize();

            int grow = flexGrow.getOrDefault(c, 0);

            if (row) {

                int w = (grow > 0)
                        ? (width - totalFixed - gap * (count - 1))
                                * grow
                                / Math.max(1, totalFlex)
                        : d.width;

                int h = (align == Align.STRETCH)
                        ? height
                        : d.height;

                int y = (align == Align.CENTER)
                        ? (height - h) / 2
                        : 0;

                c.setBounds(cursorX, y, w, h);

                cursorX += w + gap;

            } else {

                int h = (grow > 0)
                        ? (height - totalFixed - gap * (count - 1))
                                * grow
                                / Math.max(1, totalFlex)
                        : d.height;

                int w = (align == Align.STRETCH)
                        ? width
                        : d.width;

                int x = (align == Align.CENTER)
                        ? (width - w) / 2
                        : 0;

                c.setBounds(x, cursorY, w, h);

                cursorY += h + gap;
            }
        }
    }
}