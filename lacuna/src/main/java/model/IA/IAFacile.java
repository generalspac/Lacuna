package model.IA;

/**
 * IA légère : profondeur faible, utile pour une partie rapide ou pédagogique.
 */
public class IAFacile implements IA {

    private final Minimax minimax;

    /**
     * Prépare une IA facile pour un joueur donné.
     *
     * @param indexIA index du joueur contrôlé par cette IA.
     */
    public IAFacile(int indexIA) {
        minimax = new Minimax(new EvaluateurJeu(false), 1, indexIA);

    }

    /**
     * Choisit un coup avec une recherche courte.
     *
     * @param etat état courant copié pour l'IA.
     * @return meilleur coup trouvé, ou {@code null} si aucun coup n'est possible.
     */
    public CoupIA choisirCoup(EtatJeu etat) {
        return minimax.meilleurCoup(etat);
    }
}
