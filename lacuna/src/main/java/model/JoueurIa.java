package model;

/**
 * Représente un joueur contrôlé par une IA.
 */
public class JoueurIa extends Joueur {

    /**
     * Crée un joueur IA.
     *
     * @param nom nom lisible de l'IA.
     * @param typePion couleur jouée par cette IA.
     */
    public JoueurIa(String nom, Pion.TypePion typePion) {
        super(nom, typePion);
    }
}
