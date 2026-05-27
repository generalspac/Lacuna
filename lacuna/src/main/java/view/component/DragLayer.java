package view.component;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * ---------------------- DRAG LAYER ------------------------ //
 *
 * Couche graphique utilisée pour :
 * - le drag & drop visuel
 * - les animations de déplacement d’images
 * - les effets visuels temporaires
 *
 * Cette couche est généralement utilisée
 * comme GlassPane au-dessus de l’interface.
 */
public class DragLayer extends JComponent {

    // ---------------------- DRAG COURANT ------------------------ //

    /**
     * Image actuellement déplacée.
     */
    private Image draggedImage;

    /**
     * Position actuelle de la souris.
     */
    private Point mouse;

    /**
     * Objet transporté pendant le drag.
     */
    private Object payload;

    // ---------------------- ANIMATION INTERNE ------------------------ //

    /**
     * Représente une animation d’image.
     */
    private static class SpriteAnimation {

        /**
         * Image animée.
         */
        Image image;

        /**
         * Position de départ.
         */
        Point start;

        /**
         * Position finale.
         */
        Point end;

        /**
         * Temps de début de l’animation.
         */
        long startTime;

        /**
         * Durée de l’animation.
         */
        int durationMs;

        /**
         * Taille d’affichage.
         */
        int size;

        /**
         * Action exécutée à la fin.
         */
        Runnable onDone;

        /**
         * Crée une animation de sprite.
         *
         * @param image       Image animée
         * @param start       Point de départ
         * @param end         Point d’arrivée
         * @param durationMs  Durée en millisecondes
         * @param size        Taille d’affichage
         * @param onDone      Action de fin
         */
        SpriteAnimation(
                Image image,
                Point start,
                Point end,
                int durationMs,
                int size,
                Runnable onDone) {

            this.image = image;
            this.start = start;
            this.end = end;
            this.durationMs = durationMs;
            this.size = size;
            this.onDone = onDone;

            this.startTime =
                    System.currentTimeMillis();
        }
    }

    // ---------------------- LISTE DES ANIMATIONS ------------------------ //

    /**
     * Liste des animations actives.
     */
    private final List<SpriteAnimation> animations =
            new ArrayList<>();

    /**
     * Timer principal des animations.
     */
    private Timer animationTimer;

    // ---------------------- GESTION DU DRAG ------------------------ //

    /**
     * Démarre un drag visuel.
     *
     * @param img      Image déplacée
     * @param payload  Objet associé
     * @param p        Position initiale
     */
    public void startDrag(
            Image img,
            Object payload,
            Point p) {

        this.draggedImage = img;
        this.payload = payload;
        this.mouse = p;

        setVisible(true);

        repaint();
    }

    /**
     * Met à jour la position de la souris.
     *
     * @param p Nouvelle position
     */
    public void updateMouse(Point p) {

        this.mouse = p;

        repaint();
    }

    /**
     * Retourne l’objet transporté.
     *
     * @return Objet du drag
     */
    public Object getPayload() {
        return payload;
    }

    /**
     * Réinitialise complètement
     * le drag visuel.
     */
    public void clear() {

        draggedImage = null;
        mouse = null;
        payload = null;

        repaint();
    }

    // ---------------------- GESTION DES ANIMATIONS ------------------------ //

    /**
     * Annule toutes les animations actives.
     */
    public void cancelAnimations() {

        animations.clear();

        if (animationTimer != null) {
            animationTimer.stop();
        }

        clear();
    }

    /**
     * Indique si des animations
     * sont actuellement actives.
     *
     * @return true si au moins une animation existe
     */
    public boolean hasAnimations() {
        return !animations.isEmpty();
    }

    /**
     * Lance une animation de déplacement d’image.
     *
     * @param img         Image animée
     * @param start       Point de départ
     * @param end         Point d’arrivée
     * @param durationMs  Durée de l’animation
     * @param size        Taille d’affichage
     * @param onDone      Action exécutée à la fin
     */
    public void animateImage(
            Image img,
            Point start,
            Point end,
            int durationMs,
            int size,
            Runnable onDone) {

        if (img == null
                || start == null
                || end == null) {

            if (onDone != null) {
                onDone.run();
            }

            return;
        }

        animations.add(
                new SpriteAnimation(
                        img,
                        start,
                        end,
                        durationMs,
                        size,
                        onDone
                )
        );

        setVisible(true);

        if (animationTimer == null) {

            animationTimer =
                    new Timer(16, e -> tickAnimations());
        }

        if (!animationTimer.isRunning()) {
            animationTimer.start();
        }

        repaint();
    }

    /**
     * Met à jour toutes les animations actives.
     */
    private void tickAnimations() {

        long now =
                System.currentTimeMillis();

        Iterator<SpriteAnimation> it =
                animations.iterator();

        while (it.hasNext()) {

            SpriteAnimation a = it.next();

            if (now - a.startTime >= a.durationMs) {

                it.remove();

                if (a.onDone != null) {

                    SwingUtilities.invokeLater(a.onDone);
                }
            }
        }

        if (animations.isEmpty()
                && animationTimer != null) {

            animationTimer.stop();
        }

        repaint();
    }

    // ---------------------- RENDU GRAPHIQUE ------------------------ //

    /**
     * Dessine toutes les animations
     * ainsi que le drag visuel.
     *
     * @param g Contexte graphique
     */
    @Override
    protected void paintComponent(Graphics g) {

        Graphics2D g2 =
                (Graphics2D) g;

        long now =
                System.currentTimeMillis();

        for (SpriteAnimation a : animations) {

            double t =
                    Math.min(
                            1.0,
                            (now - a.startTime)
                                    / (double) a.durationMs
                    );

            // ralentissement progressif
            double eased =
                    1.0 - Math.pow(1.0 - t, 3);

            int x = (int) (
                    a.start.x
                            + (a.end.x - a.start.x) * eased
            );

            int y = (int) (
                    a.start.y
                            + (a.end.y - a.start.y) * eased
            );

            g2.drawImage(
                    a.image,
                    x - a.size / 2,
                    y - a.size / 2,
                    a.size,
                    a.size,
                    null
            );
        }

        if (draggedImage == null
                || mouse == null) {

            return;
        }

        int size = 40;

        g2.drawImage(
                draggedImage,
                mouse.x - size / 2,
                mouse.y - size / 2,
                size,
                size,
                null
        );
    }
}