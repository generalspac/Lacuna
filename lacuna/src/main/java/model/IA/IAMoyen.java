package model.IA;

/**
 * IA intermédiaire : elle regarde plus loin et active les bonus heuristiques.
 */
public class IAMoyen implements IA {
    private final Minimax minimax;

    /**
     * Prépare une IA moyenne pour un joueur donné.
     *
     * @param indexIA index du joueur  IA.
     */
    public IAMoyen(int indexIA) {
        minimax = new Minimax(new EvaluateurJeu(true), 3, indexIA);
    }

    /**
     * Choisit un coup.
     *
     * @param etat état courant copié pour l'IA.
     * @return meilleur coup trouvé, ou {@code null} si aucun coup n'est possible.
     */
    public CoupIA choisirCoup(EtatJeu etat) {
        return minimax.meilleurCoup(etat);

    }
}
