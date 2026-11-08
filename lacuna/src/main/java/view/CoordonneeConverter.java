package view;

import model.Cercle;
import model.Coordonnees;

/**
 * Convertit des coordonnées entre l'espace modèle et l'espace écran.
 *
 */
public class CoordonneeConverter {

    private final Cercle cercle;
    private int componentWidth;
    private int componentHeight;

    /**
     * @param cercle           Le cercle de jeu (source de vérité modèle)
     * @param componentWidth   Largeur courante du composant d'affichage en pixels
     * @param componentHeight  Hauteur courante du composant d'affichage en pixels
     */
    public CoordonneeConverter(Cercle cercle, int componentWidth, int componentHeight) {
        this.cercle = cercle;
        this.componentWidth  = componentWidth;
        this.componentHeight = componentHeight;
    }

    // ==================== MISE À JOUR ========================

    /** À appeler à chaque redimensionnement du composant. */
    public void setDimensions(int width, int height) {
        this.componentWidth  = width;
        this.componentHeight = height;
    }

    // =========================================================
    // ================ VALEURS DÉRIVÉES =======================
    // =========================================================

    /** Côté du carré inscrit dans la zone d'affichage. */
    public int size() {
        return Math.min(componentWidth, componentHeight);
    }

    /** Décalage horizontal pour centrer le carré. */
    public int offsetX() {
        return (componentWidth - size()) / 2;
    }

    /** Décalage vertical pour centrer le carré. */
    public int offsetY() {
        return (componentHeight - size()) / 2;
    }

    /** Coordonnée X du centre de l'écran (pixels). */
    public int centerX() {
        return offsetX() + size() / 2;
    }

    /** Coordonnée Y du centre de l'écran (pixels). */
    public int centerY() {
        return offsetY() + size() / 2;
    }

    /** Rayon utilisé pour la projection (40 % du côté). */
    public double rayonScreen() {
        return size() * 0.4;
    }

    // =========================================================
    // ================ CONVERSIONS ============================
    // =========================================================

    /**
     * Convertit un point du modèle en coordonnées écran.
     *
     * @param modelX  X dans l'espace modèle
     * @param modelY  Y dans l'espace modèle
     * @return Coordonnées en pixels dans l'espace composant
     */
    public Coordonnees modelToScreen(int modelX, int modelY) {
        double rayonModel = cercle.getRayon();
        Coordonnees centre = cercle.getCentre();

        double relX = (modelX - centre.getX()) / rayonModel;
        double relY = (modelY - centre.getY()) / rayonModel;

        return new Coordonnees(
                (int) (centerX() + relX * rayonScreen()),
                (int) (centerY() + relY * rayonScreen())
        );
    }

    /**
     * Convertit un point écran en coordonnées modèle.
     *
     * @param screenX  X en pixels dans le composant
     * @param screenY  Y en pixels dans le composant
     * @return Coordonnées dans l'espace modèle
     */
    public Coordonnees screenToModel(int screenX, int screenY) {
        double rayonModel  = cercle.getRayon();
        Coordonnees centre = cercle.getCentre();

        double relX = (screenX - centerX()) / rayonScreen();
        double relY = (screenY - centerY()) / rayonScreen();

        return new Coordonnees(
                (int) (centre.getX() + relX * rayonModel),
                (int) (centre.getY() + relY * rayonModel)
        );
    }

    /**
     * Retourne la taille recommandée pour dessiner une fleur
     * (5 % du côté du composant).
     */
    public int fleurSize() {
        return (int) (size() * 0.05);
    }
}