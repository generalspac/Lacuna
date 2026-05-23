package model;

// Regroupe les constantes communes utilisées par le jeu et par l'IA.
public final class ConstantesJeu {

    private ConstantesJeu() {
    }

    public static final int NB_JOUEURS = 2;
    public static final int MAX_PIONS_PAR_JOUEUR = 6;
    public static final int NB_COULEURS = Fleur.TypeFleur.values().length;
    public static final int NB_FLEURS_PAR_COULEUR = 7;
    public static final int NB_FLEURS = NB_COULEURS * NB_FLEURS_PAR_COULEUR;

    public static final double DIST_MIN_FLEURS = 30; // distance min entre les fleurs
    public static final double DIAMETRE_FLEUR_MODELE = DIST_MIN_FLEURS;
    public static final double DIAMETRE_PION_MODELE = 22;
    public static final double MARGE_COLLISION = 5;

    public static final int NB_POINTS_SEGMENT = 24; // le nombre de points
    public static final double TOLERANCE_SEGMENT = 8;
    public static final double TOLERANCE_DROP_SEGMENT = 22;
    public static final double MARGE_EXTREMITES_SEGMENT_JEU = MARGE_COLLISION;

    public static final double DIST_MIN_PION_IA = 24;
    public static final double MARGE_EXTREMITES_SEGMENT_IA = 26;//marge pour eviter de placer
                                                                //un pion trop proche des fleurs choisies
    public static final int MIN_POINTS_SEGMENT_IA = 6;
    public static final int MAX_POINTS_SEGMENT_IA = 24;
    public static final double ESPACEMENT_POINTS_SEGMENT_IA = 18;
    public static final int MAX_PAIRES_CANDIDATES_IA = 14;
    public static final int MAX_POINTS_PAR_PAIRE_IA = 3;
    public static final int MAX_COUPS_GENERES_IA = 42;
    public static final int MAX_COUPS_MINIMAX_PROFONDEUR_HAUTE = 14;
    public static final int MAX_COUPS_MINIMAX_PROFONDEUR_BASSE = 8;
}
