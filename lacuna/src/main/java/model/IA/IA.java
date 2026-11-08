package model.IA;

/**
 * Interface (Contrat) commune à tous les niveaux d'IA.
 */
public interface IA {

    /**
     * Choisit le prochain coup à jouer depuis une copie de l'état du jeu.
     *
     * @param etat état visible par l'IA.
     * @return coup choisi, ou {@code null} si aucun coup complet n'est disponible.
     */
    CoupIA choisirCoup(EtatJeu etat);
}
