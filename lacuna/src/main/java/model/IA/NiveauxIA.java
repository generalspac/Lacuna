package model.IA;

/**
 * Niveaux de difficulté disponibles pour une IA.
 */
public enum NiveauxIA {
    FACILE,
    MOYEN,
    DIFFICILE;

    /**
     * Crée l'implémentation d'IA associée au niveau.
     *
     * @param indexIA index du joueur IA.
     * @return instance d'IA prête à choisir des coups.
     */
    public IA creerIA(int indexIA) {
        switch (this) {
            case FACILE:
                return new IAFacile(indexIA);
            case MOYEN:
                return new IAMoyen(indexIA);
            case DIFFICILE:
                return new IADifficile(indexIA);
            default:
                return new IAFacile(indexIA);
        }
    }
}
