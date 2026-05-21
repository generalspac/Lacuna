package model.IA;

/**
 * IA la plus exigeante (IA DIFFICILE) : profondeur élevée et heuristiques complètes.
 */
public class IADifficile implements IA {
    private final Minimax minimax;

    /**
     * Prépare une IA difficile pour un joueur donné.
     *
     * @param indexIA index du joueur contrôlé par cette IA.
     */
    public IADifficile(int indexIA) {
        minimax = new Minimax(new EvaluateurJeu(true), 6, indexIA);

    }

    /**
     * Choisit un coup avec la recherche la plus profonde.
     *
     * @param etat état courant copié pour l'IA.
     * @return meilleur coup trouvé, ou {@code null} si aucun coup n'est possible.
     */
    public CoupIA choisirCoup(EtatJeu etat) {
        return minimax.meilleurCoup(etat);
    }
}
