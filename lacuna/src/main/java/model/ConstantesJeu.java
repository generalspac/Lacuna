package model;

// Regroupe les constantes communes utilisées par le jeu et par l'IA.
public final class ConstantesJeu {

    private ConstantesJeu() {
    }

    public static final int NB_JOUEURS = 2;
    public static final int MAX_PIONS_PAR_JOUEUR = 6;
    public static final int NB_COULEURS = 6;

    public static final double DIST_MIN_FLEURS = 30; // distance min entre les fleurs
    public static final double DIAMETRE_FLEUR_MODELE = DIST_MIN_FLEURS;
    public static final double DIAMETRE_PION_MODELE = 22;
    public static final double MARGE_COLLISION = 2;

    public static final int NB_POINTS_SEGMENT = 24; // le nombre de points
    public static final double TOLERANCE_SEGMENT = 12;
    public static final double TOLERANCE_DROP_SEGMENT = 18;
    public static final double MARGE_EXTREMITES_SEGMENT_JEU = MARGE_COLLISION;

    public static final double DIST_MIN_PION_IA = 24;
    public static final double MARGE_EXTREMITES_SEGMENT_IA = 26;
    public static final int MAX_COUPS_GENERES_IA = 80;
}
