package view.render;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.List;

import model.Coordonnees;
import model.Fleur;
import model.Jeu;
import view.JeuGraphique;
import view.JeuTutoriel;
import view.manager.ImageManager;

public class FlowerRenderer {

    private final Jeu jeu;

    private final JeuGraphique vue;

    private final ImageManager imageManager;

    private Fleur suggestedFlower = null;

    private List<Fleur> otherSuggestedFleurs =
            new ArrayList<>();

    public FlowerRenderer(
            Jeu jeu,
            JeuGraphique vue,
            ImageManager imageManager) {

        this.jeu = jeu;

        this.vue = vue;

        this.imageManager = imageManager;
    }

    /**
     * Définit la fleur suggérée.
     *
     * @param f fleur.
     */
    public void setSuggestedFlower(Fleur f) {
        this.suggestedFlower = f;
    }

    /**
     * Définit les autres fleurs suggérées.
     *
     * @param fleurs fleurs.
     */
    public void setOtherSuggestedFleurs(
            List<Fleur> fleurs) {

        this.otherSuggestedFleurs =
                fleurs != null
                        ? fleurs
                        : new ArrayList<>();
    }

    /**
     * Retourne la fleur suggérée.
     *
     * @return fleur suggérée.
     */
    public Fleur getHighlightedFlower() {
        return suggestedFlower;
    }

    /**
     * Retourne l'image d'une fleur.
     *
     * @param type type.
     * @return image.
     */
    public Image getFlowerImage(
            Fleur.TypeFleur type) {

        switch (type) {

            case ROUGE:
                return imageManager.getRougeImg();

            case BLEUE:
                return imageManager.getBleueImg();

            case JAUNE:
                return imageManager.getJauneImg();

            case VERTE:
                return imageManager.getVerteImg();

            case ORANGE:
                return imageManager.getOrangeImg();

            case VIOLETTE:
                return imageManager.getVioletteImg();

            case ROSE:
                return imageManager.getRoseImg();

            default:
                return null;
        }
    }

    /**
     * Dessine le cercle animé.
     *
     * @param g2 contexte graphique.
     * @param f fleur.
     * @param color couleur.
     */
    public void drawAnimatedCercle(
            Graphics2D g2,
            Fleur f,
            Color color) {

        if (f == null
                || f.getPosition() == null)
            return;

        Coordonnees p =
                vue.modelToScreen(
                        f.getPosition().x,
                        f.getPosition().y);

        int maxSz =
                Math.max(
                        30,
                        (int) (vue.getWidth() * 0.08));

        double time =
                System.currentTimeMillis()
                        / 1000.0;

        float alpha =
                (float) (Math.sin(
                        time * 6.0)
                        * 0.4
                        + 0.6);

        alpha =
                Math.max(
                        0.1f,
                        Math.min(
                                1.0f,
                                alpha));

        Stroke oldStroke =
                g2.getStroke();

        Color oldColor =
                g2.getColor();

        g2.setStroke(
                new BasicStroke(
                        4,
                        BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND));

        Color pulseColor =
                new Color(
                        color.getRed(),
                        color.getGreen(),
                        color.getBlue(),
                        (int) (alpha * 255));

        g2.setColor(pulseColor);

        g2.drawOval(
                p.getX() - maxSz / 2,
                p.getY() - maxSz / 2,
                maxSz,
                maxSz);

        g2.setStroke(oldStroke);

        g2.setColor(oldColor);
    }

    /**
     * Dessine les suggestions.
     *
     * @param g2 contexte graphique.
     */
    public void drawSuggestions(
            Graphics2D g2) {

        if (jeu.isAfficherSuggestions()) {

            drawAnimatedCercle(
                    g2,
                    getHighlightedFlower(),
                    Color.GREEN);

            for (Fleur f : otherSuggestedFleurs) {

                drawAnimatedCercle(
                        g2,
                        f,
                        Color.WHITE);
            }

        } else if (vue instanceof JeuTutoriel) {

            drawAnimatedCercle(
                    g2,
                    getHighlightedFlower(),
                    Color.GREEN);
        }
    }
}