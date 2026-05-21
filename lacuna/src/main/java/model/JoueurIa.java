package model;

/**
 * Représente un joueur contrôlé par une IA.
 */
public class JoueurIa extends Joueur {

    /**
     * Crée un joueur IA avec le nom affiché dans l'interface.
     *
     * @param nom nom lisible de l'IA, par exemple "IA Facile".
     * @param typePion couleur jouée par cette IA.
     */
    public JoueurIa(String nom, Pion.TypePion typePion) {
        super(nom, typePion);
    }
}
