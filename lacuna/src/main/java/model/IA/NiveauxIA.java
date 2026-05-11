package model.IA;

public enum NiveauxIA {
    FACILE,
    MOYEN,
    DIFFICILE;

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
