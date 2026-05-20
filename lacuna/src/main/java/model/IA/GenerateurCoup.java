package model.IA;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import model.Coordonnees;
import model.ConstantesJeu;
import model.Fleur;
import model.Pion;
import model.ReglesPlateau;

// Liste tous les coups de l'IA depuis un état
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

            points.sort(Comparator.comparingInt(Coordonnees pos) -> scorePosition(etat, pos, paire.f1, paire.f2)).reversed());

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

    private static List<Coordonnees> pointsDuSegment( EtatJeu etat,Coordonnees A, Coordonnees B) {
        int nbPoints = nombrePointsPourSegment(A, B);
        return ReglesPlateau.pointsDuSegment(etat, A, B, nbPoints,
                DIST_MIN_FLEURS, MARGE_EXTREMITES_SEGMENT);
    }

    private static int nombrePointsPourSegment(Coordonnees A, Coordonnees B) {
        int nbPoints = (int) Math.round(Coordonnees.distance(A, B) / ESPACEMENT_POINTS_SEGMENT);
        return Math.max(MIN_POINTS_SEGMENT, Math.min(MAX_POINTS_SEGMENT, nbPoints));
    }

    private static boolean segmentLibre(EtatJeu etat, Coordonnees A, Coordonnees B) {
        return ReglesPlateau.segmentLibre(etat, A, B, TOLERANCE_SEGMENT);
    }

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

    private static int scorePhase2Fleur(EtatJeu etat, int indexJoueur, Fleur fleur) {
        Pion pionProche = ReglesPlateau.pionLePlusProche(etat, fleur);
        if (pionProche == null) {
            return 0;
        }
        return pionProche.getType() == etat.typePionDuJoueur(indexJoueur) ? -25 : 35;
    }

    private static int nombreFleursDeCouleur(EtatJeu etat, int index, Fleur.TypeFleur type) {
        int compteur = 0;
        for (Fleur f : etat.fleursParJoueur[index]) {
            if (f.getType() == type) {
                compteur++;
            }
        }
        return compteur;
    }

    private static boolean memeFleur(Fleur a, Fleur b) {
        return a.getType() == b.getType() && a.getPosition().equals(b.getPosition());
    }

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
