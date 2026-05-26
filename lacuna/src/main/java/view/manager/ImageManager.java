package view.manager;

import java.awt.Image;

import javax.imageio.ImageIO;

import global.Configuration;

/**
 * Gestionnaire de chargement des images du jeu.
 */
public class ImageManager {

    // ---------------- IMAGES FLEURS ---------------- //

    private Image rougeImg;
    private Image bleueImg;
    private Image jauneImg;
    private Image verteImg;
    private Image orangeImg;
    private Image violetteImg;
    private Image roseImg;

    // ---------------- IMAGES PIONS ---------------- //

    private Image orImg;
    private Image argentImg;

    // ---------------- IMAGES PLATEAU ---------------- //

    private Image plateauImg;

    /**
     * Charge toutes les images du jeu.
     */
    public void initImages() {

        try {

            rougeImg = load("fleur_rouge.png");
            bleueImg = load("fleur_bleue.png");
            jauneImg = load("fleur_jaune.png");
            verteImg = load("fleur_verte.png");
            orangeImg = load("fleur_orange.png");
            violetteImg = load("fleur_violette.png");
            roseImg = load("fleur_rose.png");

            orImg = load("pion_or.png");
            argentImg = load("pion_argent.png");

            plateauImg = load("plateau.png");

        } catch (Exception e) {

            Configuration.debugeurErreur(
                    "Erreur chargement images : " + e.getMessage());

            System.exit(3);
        }
    }

    /**
     * Charge une image.
     *
     * @param name nom du fichier.
     * @return image chargée.
     * @throws Exception erreur de lecture.
     */
    private Image load(String name) throws Exception {

        return ImageIO.read(Configuration.ouvre(name));
    }

    /**
     * Retourne l'image rouge.
     *
     * @return image rouge.
     */
    public Image getRougeImg() {
        return rougeImg;
    }

    /**
     * Retourne l'image bleue.
     *
     * @return image bleue.
     */
    public Image getBleueImg() {
        return bleueImg;
    }

    /**
     * Retourne l'image jaune.
     *
     * @return image jaune.
     */
    public Image getJauneImg() {
        return jauneImg;
    }

    /**
     * Retourne l'image verte.
     *
     * @return image verte.
     */
    public Image getVerteImg() {
        return verteImg;
    }

    /**
     * Retourne l'image orange.
     *
     * @return image orange.
     */
    public Image getOrangeImg() {
        return orangeImg;
    }

    /**
     * Retourne l'image violette.
     *
     * @return image violette.
     */
    public Image getVioletteImg() {
        return violetteImg;
    }

    /**
     * Retourne l'image rose.
     *
     * @return image rose.
     */
    public Image getRoseImg() {
        return roseImg;
    }

    /**
     * Retourne l'image or.
     *
     * @return image or.
     */
    public Image getPionOrImg() {
        return orImg;
    }

    /**
     * Retourne l'image argent.
     *
     * @return image argent.
     */
    public Image getPionArgentImg() {
        return argentImg;
    }

    /**
     * Retourne l'image du plateau.
     *
     * @return image du plateau.
     */
    public Image getPlateauImg() {
        return plateauImg;
    }
}