package view.manager;

import java.awt.Image;

import javax.imageio.ImageIO;

import global.Configuration;

/**
 * \class ImageManager
 * \brief Gestionnaire centralisé du chargement des images du jeu.
 *
 * \details Cette classe évite de recharger plusieurs fois les mêmes ressources
 *          et fournit un accès simple aux différentes images utilisées
 *          dans l’interface graphique.
 */
public class ImageManager {

    
    // IMAGES DES FLEURS
    

    /**
     * Image de la fleur rouge.
     */
    private Image rougeImg;

    /**
     * Image de la fleur bleue.
     */
    private Image bleueImg;

    /**
     * Image de la fleur jaune.
     */
    private Image jauneImg;

    /**
     * Image de la fleur verte.
     */
    private Image verteImg;

    /**
     * Image de la fleur orange.
     */
    private Image orangeImg;

    /**
     * Image de la fleur violette.
     */
    private Image violetteImg;

    /**
     * Image de la fleur rose.
     */
    private Image roseImg;

    
    // IMAGES DES PIONS
    

    /**
     * Image du pion OR.
     */
    private Image orImg;

    /**
     * Image du pion ARGENT.
     */
    private Image argentImg;

    
    // IMAGE DU PLATEAU
    

    /**
     * Image du plateau principal.
     */
    private Image plateauImg;

    /**
     * Charge toutes les images du jeu.
     *
     * \details Cette méthode doit être appelée une seule fois
     *          au démarrage de l’application.
     */
    public void initImages() {

        try {

            
            // CHARGEMENT FLEURS
            

            rougeImg = load("fleur_rouge.png");

            bleueImg = load("fleur_bleue.png");

            jauneImg = load("fleur_jaune.png");

            verteImg = load("fleur_verte.png");

            orangeImg = load("fleur_orange.png");

            violetteImg = load("fleur_violette.png");

            roseImg = load("fleur_rose.png");

            
            // CHARGEMENT PIONS
            

            orImg = load("pion_or.png");

            argentImg = load("pion_argent.png");

            
            // CHARGEMENT PLATEAU
            

            plateauImg = load("plateau.png");

        } catch (Exception e) {

            // Affiche l’erreur dans le débogueur
            Configuration.debugeurErreur(
                    "Erreur chargement images : "
                            + e.getMessage());

            // Arrêt du programme :
            // les images sont indispensables au jeu
            System.exit(3);
        }
    }

    /**
     * Charge une image depuis les ressources.
     *
     * @param name nom du fichier image.
     *
     * @return image chargée.
     *
     * \throws Exception erreur de lecture du fichier.
     */
    private Image load(String name) throws Exception {

        return ImageIO.read(
                Configuration.ouvre(name));
    }

    
    // GETTERS FLEURS
    

    /**
     * Retourne l’image de la fleur rouge.
     */
    public Image getRougeImg() {
        return rougeImg;
    }

    /**
     * Retourne l’image de la fleur bleue.
     */
    public Image getBleueImg() {
        return bleueImg;
    }

    /**
     * Retourne l’image de la fleur jaune.
     */
    public Image getJauneImg() {
        return jauneImg;
    }

    /**
     * Retourne l’image de la fleur verte.
     */
    public Image getVerteImg() {
        return verteImg;
    }

    /**
     * Retourne l’image de la fleur orange.
     */
    public Image getOrangeImg() {
        return orangeImg;
    }

    /**
     * Retourne l’image de la fleur violette.
     */
    public Image getVioletteImg() {
        return violetteImg;
    }

    /**
     * Retourne l’image de la fleur rose.
     */
    public Image getRoseImg() {
        return roseImg;
    }

    
    // GETTERS PIONS
    
    /**
     * Retourne l’image du pion OR.
     */
    public Image getPionOrImg() {
        return orImg;
    }

    /**
     * Retourne l’image du pion ARGENT.
     */
    public Image getPionArgentImg() {
        return argentImg;
    }

    
    // GETTER PLATEAU

    /**
     * Retourne l’image du plateau principal.
     */
    public Image getPlateauImg() {
        return plateauImg;
    }
}