package model.IA;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import model.Coordonnees;
import model.ConstantesJeu;
import model.Fleur;
import model.Pion;
import model.ReglesPlateau;

/**
 * Génère les coups que l'IA peut envisager depuis un état donné.
 *
 * Le générateur garde seulement les meilleures paires et quelques positions
 * fortes par paire. Cela évite de donner trop de branches au minimax tout en
 * conservant des coups naturels à regarder pendant une partie graphique.
 */
public class GenerateurCoup {

    private static final double DIST_MIN_FLEURS = ConstantesJeu.DIST_MIN_PION_IA;
    private static final double TOLERANCE_SEGMENT = ConstantesJeu.TOLERANCE_SEGMENT;
    private static final double MARGE_EXTREMITES_SEGMENT = ConstantesJeu.MARGE_EXTREMITES_SEGMENT_IA;
    private static final int MIN_POINTS_SEGMENT = ConstantesJeu.MIN_POINTS_SEGMENT_IA;
    private static final int MAX_POINTS_SEGMENT = ConstantesJeu.MAX_POINTS_SEGMENT_IA;
    private static final double ESPACEMENT_POINTS_SEGMENT = ConstantesJeu.ESPACEMENT_POINTS_SEGMENT_IA;
    private static final int MAX_PAIRES_CANDIDATES = ConstantesJeu.MAX_PAIRES_CANDIDATES_IA;
    private static final int MAX_POINTS_PAR_PAIRE = ConstantesJeu.MAX_POINTS_PAR_PAIRE_IA;
    private static final int MAX_COUPS_GENERES = ConstantesJeu.MAX_COUPS_GENERES_IA;

    /**
     * Construit une liste de coups candidats pour le joueur courant de l'état.
     *
     * @param etat copie légère du jeu utilisée par l'IA.
     * @return coups jouables, triés implicitement par intérêt décroissant.
     */
    public static List<CoupIA> genererCoups(EtatJeu etat) {

        List<CoupIA> coups = new ArrayList<>();
        List<PaireCandidate> paires = new ArrayList<>();

        int taille = etat.fleursRestantes.size();

        for (int i = 0; i < taille; i++) {
            for (int j = i + 1; j < taille; j++) {
                Fleur f1 = etat.fleursRestantes.get(i);
                Fleur f2 = etat.fleursRestantes.get(j);

                if (f1.getType() == f2.getType() && segmentLibre(etat, f1.getPosition(), f2.getPosition())) {
                    paires.add(new PaireCandidate(f1, f2, scorePaire(etat, f1, f2)));
                }
            }
        }

        paires.sort(Comparator.comparingInt((PaireCandidate paire) -> paire.score).reversed());

        int nbPaires = Math.min(paires.size(), MAX_PAIRES_CANDIDATES);
        for (int i = 0; i < nbPaires; i++) {
            PaireCandidate paire = paires.get(i);
            List<Coordonnees> points = pointsDuSegment(etat, paire.f1.getPosition(), paire.f2.getPosition());

            points.sort(Comparator.comparingInt((Coordonnees pos) -> scorePosition(etat, pos, paire.f1, paire.f2)).reversed());

            int nbPoints = Math.min(points.size(), MAX_POINTS_PAR_PAIRE);
            for (int j = 0; j < nbPoints; j++) {
                coups.add(new CoupIA(paire.f1, paire.f2, points.get(j)));

                if (coups.size() >= MAX_COUPS_GENERES) {
                    return coups;
                }
            }
        }

        return coups;
    }

    /**
     * Calcule les positions possibles pour un pion entre deux fleurs.
     *
     * @param etat état analysé.
     * @param A première extrémité du segment.
     * @param B seconde extrémité du segment.
     * @return positions libres et assez éloignées des extrémités.
     */
    private static List<Coordonnees> pointsDuSegment(EtatJeu etat, Coordonnees A, Coordonnees B) {
        int nbPoints = nombrePointsPourSegment(A, B);
        return ReglesPlateau.pointsDuSegment(etat, A, B, nbPoints,
                DIST_MIN_FLEURS, MARGE_EXTREMITES_SEGMENT);
    }

    /**
     * Adapte le nombre de points testés à la longueur du segment.
     *
     * @param A première extrémité.
     * @param B seconde extrémité.
     * @return nombre de points à essayer, borné par les constantes IA.
     */
    private static int nombrePointsPourSegment(Coordonnees A, Coordonnees B) {
        int nbPoints = (int) Math.round(Coordonnees.distance(A, B) / ESPACEMENT_POINTS_SEGMENT);
        return Math.max(MIN_POINTS_SEGMENT, Math.min(MAX_POINTS_SEGMENT, nbPoints));
    }

    /**
     * Vérifie qu'aucune fleur ou pion ne bloque la ligne entre deux fleurs.
     *
     * @param etat état analysé.
     * @param A première extrémité.
     * @param B seconde extrémité.
     * @return {@code true} si le segment peut être joué.
     */
    private static boolean segmentLibre(EtatJeu etat, Coordonnees A, Coordonnees B) {
        return ReglesPlateau.segmentLibre(etat, A, B, TOLERANCE_SEGMENT);
    }

    /**
     * Donne une note à une paire de fleurs avant de chercher la position du pion.
     *
     * @param etat état analysé.
     * @param f1 première fleur.
     * @param f2 seconde fleur.
     * @return score heuristique de la paire.
     */
    private static int scorePaire(EtatJeu etat, Fleur f1, Fleur f2) {
        int joueur = etat.joueurCourant;
        int adversaire = 1 - joueur;
        Fleur.TypeFleur type = f1.getType();

        int score = 0;
        int nbJoueur = nombreFleursDeCouleur(etat, joueur, type);
        int nbAdversaire = nombreFleursDeCouleur(etat, adversaire, type);

        score += progressionMajorite(nbJoueur) * 90;
        score += progressionMajorite(nbAdversaire) * 70;
        score += (int) Math.max(0, 180 - Coordonnees.distance(f1.getPosition(), f2.getPosition()));
        score += scorePhase2Fleur(etat, joueur, f1) + scorePhase2Fleur(etat, joueur, f2);
        return score;
    }

    /**
     * Valorise les paires qui rapprochent d'une majorité de couleur.
     *
     * @param fleursDejaGagnees nombre de fleurs déjà collectées dans cette couleur.
     * @return bonus de progression.
     */
    private static int progressionMajorite(int fleursDejaGagnees) {
        int apresCoup = fleursDejaGagnees + 2;
        if (apresCoup >= 4) {
            return 8;
        }
        if (apresCoup == 3) {
            return 4;
        }
        if (apresCoup == 2) {
            return 1;
        }
        return 0;
    }

    /**
     * Note une position de pion pour une paire donnée.
     *
     * @param etat état analysé.
     * @param pos position testée pour le pion.
     * @param f1 première fleur de la paire.
     * @param f2 seconde fleur de la paire.
     * @return score heuristique de la position.
     */
    private static int scorePosition(EtatJeu etat, Coordonnees pos, Fleur f1, Fleur f2) {
        int score = 0;
        Coordonnees milieu = Coordonnees.milieu(f1.getPosition(), f2.getPosition());
        score -= (int) Coordonnees.distance(pos, milieu);

        Pion.TypePion typeJoueur = etat.typePionDuJoueur(etat.joueurCourant);
        for (Fleur fleur : etat.fleursRestantes) {
            if (memeFleur(fleur, f1) || memeFleur(fleur, f2)) {
                continue;
            }
            double distanceNouveauPion = Coordonnees.distance(pos, fleur.getPosition());
            Pion pionProche = ReglesPlateau.pionLePlusProche(etat, fleur);
            if (pionProche == null || distanceNouveauPion < Coordonnees.distance(pionProche.getPosition(), fleur.getPosition())) {
                score += 12;
            } else if (pionProche.getType() == typeJoueur) {
                score += 3;
            }
        }

        return score;
    }

    /**
     * Estime l'effet d'une fleur sur la future attribution de phase 2.
     *
     * @param etat état analysé.
     * @param indexJoueur joueur évalué.
     * @param fleur fleur étudiée.
     * @return bonus ou malus selon le pion actuellement le plus proche.
     */
    private static int scorePhase2Fleur(EtatJeu etat, int indexJoueur, Fleur fleur) {
        Pion pionProche = ReglesPlateau.pionLePlusProche(etat, fleur);
        if (pionProche == null) {
            return 0;
        }
        return pionProche.getType() == etat.typePionDuJoueur(indexJoueur) ? -25 : 35;
    }

    /**
     * Compte les fleurs d'une couleur déjà gagnées par un joueur.
     *
     * @param etat état analysé.
     * @param index index du joueur.
     * @param type couleur recherchée.
     * @return nombre de fleurs collectées.
     */
    private static int nombreFleursDeCouleur(EtatJeu etat, int index, Fleur.TypeFleur type) {
        int compteur = 0;
        for (Fleur f : etat.fleursParJoueur[index]) {
            if (f.getType() == type) {
                compteur++;
            }
        }
        return compteur;
    }

    /**
     * Compare deux fleurs sans dépendre de leur identité objet.
     *
     * @param a première fleur.
     * @param b seconde fleur.
     * @return {@code true} si elles ont le même type et la même position.
     */
    private static boolean memeFleur(Fleur a, Fleur b) {
        return a.getType() == b.getType() && a.getPosition().equals(b.getPosition());
    }

    /**
     * Petite structure interne pour trier les paires avant de créer les coups.
     */
    private static class PaireCandidate {
        private final Fleur f1;
        private final Fleur f2;
        private final int score;

        private PaireCandidate(Fleur f1, Fleur f2, int score) {
            this.f1 = f1;
            this.f2 = f2;
            this.score = score;
        }
    }
}
