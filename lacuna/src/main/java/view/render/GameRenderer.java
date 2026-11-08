package view.render;

import java.awt.Graphics2D;
import java.awt.Image;
import java.util.function.BiFunction;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;

import model.Coordonnees;
import model.Fleur;
import model.Jeu;
import model.Pion;
import view.manager.ImageManager;

/**
 * Gestionnaire de rendu du jeu.
 */
public class GameRenderer {

    // ---------------- ATTRIBUTS ---------------- //

    private final Jeu jeu;
    private final ImageManager imageManager;

    // ---------------- CONSTRUCTEUR ---------------- //

    /**
     * Construit le renderer.
     *
     * @param jeu partie courante.
     * @param imageManager gestionnaire images.
     */
    public GameRenderer(
            Jeu jeu,
            ImageManager imageManager) {

        this.jeu = jeu;

        this.imageManager = imageManager;
    }

    // ---------------- PLATEAU ---------------- //

    /**
     * Dessine le plateau.
     *
     * @param g2 graphics.
     * @param offsetX offset X.
     * @param offsetY offset Y.
     * @param size taille plateau.
     */
    public void drawBoard(
            Graphics2D g2,
            int offsetX,
            int offsetY,
            int size) {

        g2.drawImage(
                imageManager.getPlateauImg(),
                offsetX,
                offsetY,
                size,
                size,
                null);
    }

    /**
     * Dessine les pions placés.
     *
     * @param g2 graphics.
     * @param modelToScreen conversion coordonnées.
     * @param pionSz taille pion.
     */
    public void drawPions(
            Graphics2D g2,
            BiFunction<Integer, Integer, Coordonnees> modelToScreen,
            int pionSz) {

        for (Pion p : jeu.getPions()) {

            if (p.getPosition() == null) {
                continue;
            }

            Image img =
                    getPionImage(
                            p.getType());

            if (img == null) {
                continue;
            }

            Coordonnees pos =
                    modelToScreen.apply(
                            p.getPosition().x,
                            p.getPosition().y);

            g2.drawImage(
                    img,
                    pos.getX() - pionSz / 2,
                    pos.getY() - pionSz / 2,
                    pionSz,
                    pionSz,
                    null);
        }
    }

    // ---------------- FLEURS ---------------- //

    /**
     * Dessine les fleurs.
     *
     * @param g2 graphics.
     * @param modelToScreen conversion coordonnées.
     * @param fleurSz taille fleurs.
     */
    public void drawFlowers(
            Graphics2D g2,
            BiFunction<Integer, Integer, Coordonnees> modelToScreen,
            int fleurSz) {

        for (Fleur f : jeu.getFleurs()) {

            if (f == null) {
                continue;
            }

            Image img =
                    getFlowerImage(
                            f.getType());

            if (img == null) {
                continue;
            }

            Coordonnees p =
                    modelToScreen.apply(
                            f.getPosition().x,
                            f.getPosition().y);

            g2.drawImage(
                    img,
                    p.getX() - fleurSz / 2,
                    p.getY() - fleurSz / 2,
                    fleurSz,
                    fleurSz,
                    null);
        }
    }

    // ---------------- IMAGES ---------------- //

    /**
     * Retourne l'image d'une fleur.
     *
     * @param type type fleur.
     * @return image correspondante.
     */
    private Image getFlowerImage(
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
     * Retourne l'image d'un pion.
     *
     * @param type type pion.
     * @return image associée.
     */
    private Image getPionImage(
            Pion.TypePion type) {

        switch (type) {

            case OR:
                return imageManager.getPionOrImg();

            case ARGENT:
                return imageManager.getPionArgentImg();

            default:
                return null;
        }
    }

    // ---------------- SELECTION ---------------- //

    /**
     * Dessine la sélection.
     *
     * @param g2 graphics.
     * @param modelToScreen conversion coordonnées.
     * @param fleur1 première fleur.
     * @param fleur2 deuxième fleur.
     * @param fleurSz taille fleurs.
     */
    public void drawSelection(
            Graphics2D g2,
            BiFunction<Integer, Integer, Coordonnees> modelToScreen,
            Fleur fleur1,
            Fleur fleur2,
            int fleurSz) {

        drawSelectedFlower(
                g2,
                modelToScreen,
                fleur1,
                fleurSz);

        drawSelectedFlower(
                g2,
                modelToScreen,
                fleur2,
                fleurSz);
    }

    /**
     * Dessine une fleur sélectionnée.
     *
     * @param g2 graphics.
     * @param modelToScreen conversion coordonnées.
     * @param fleur fleur cible.
     * @param fleurSz taille fleurs.
     */
    private void drawSelectedFlower(
            Graphics2D g2,
            BiFunction<Integer, Integer, Coordonnees> modelToScreen,
            Fleur fleur,
            int fleurSz) {

        if (fleur == null) {
            return;
        }

        Coordonnees p =
                modelToScreen.apply(
                        fleur.getPosition().x,
                        fleur.getPosition().y);

        int r = fleurSz / 2 + 8;

        g2.setColor(Color.WHITE);

        g2.setStroke(new BasicStroke(4));

        g2.drawOval(
                p.getX() - r,
                p.getY() - r,
                r * 2,
                r * 2);
    }

    // ---------------- COUPS POSSIBLES ---------------- //

    /**
     * Dessine les coups possibles.
     *
     * @param g2 graphics.
     * @param modelToScreen conversion coordonnées.
     * @param coups coups possibles.
     * @param fleurSz taille fleurs.
     */
    public void drawPossibleMoves(
            Graphics2D g2,
            BiFunction<Integer, Integer, Coordonnees> modelToScreen,
            java.util.List<Coordonnees> coups,
            int fleurSz) {

        g2.setColor(
                new Color(
                        255,
                        255,
                        255,
                        120));

        for (Coordonnees c : coups) {

            Coordonnees p =
                    modelToScreen.apply(
                            c.x,
                            c.y);

            int r = fleurSz / 4;

            g2.fillOval(
                    p.getX() - r,
                    p.getY() - r,
                    r * 2,
                    r * 2);
        }
    }

    // ---------------- GAGNANT ---------------- //

    /**
     * Dessine l'overlay gagnant.
     *
     * @param g2 graphics.
     * @param visible visibilité.
     * @param message message affiché.
     * @param width largeur.
     * @param height hauteur.
     */
    public void drawWinnerOverlay(
            Graphics2D g2,
            boolean visible,
            String message,
            int width,
            int height) {

        if (!visible) {
            return;
        }

        g2.setColor(
                new Color(
                        0,
                        0,
                        0,
                        170));

        g2.fillRect(
                0,
                0,
                width,
                height);

        g2.setColor(Color.WHITE);

        g2.setFont(
                g2.getFont()
                        .deriveFont(
                                Font.BOLD,
                                36f));

        FontMetrics fm =
                g2.getFontMetrics();

        int x =
                (width - fm.stringWidth(message)) / 2;

        int y =
                height / 2;

        g2.drawString(
                message,
                x,
                y);
    }
}