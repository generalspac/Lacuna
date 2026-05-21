package model.IA;

/**
 * IA intermédiaire : elle regarde plus loin et active les bonus heuristiques.
 */
public class IAMoyen implements IA {
    private final Minimax minimax;

    /**
     * Prépare une IA moyenne pour un joueur donné.
     *
     * @param indexIA index du joueur contrôlé par cette IA.
     */
    public IAMoyen(int indexIA) {
        minimax = new Minimax(new EvaluateurJeu(true), 4, indexIA);
    }

    /**
     * Choisit un coup avec une recherche de profondeur moyenne.
     *
     * @param etat état courant copié pour l'IA.
     * @return meilleur coup trouvé, ou {@code null} si aucun coup n'est possible.
     */
    public CoupIA choisirCoup(EtatJeu etat) {
        return minimax.meilleurCoup(etat);

    }
}
