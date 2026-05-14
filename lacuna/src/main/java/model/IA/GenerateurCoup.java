package model.IA;

import java.util.ArrayList;
import java.util.List;

import model.Coordonnees;
import model.Fleur;
import model.Pion;

// Liste tous les coups de l'IA depuis un état
public class GenerateurCoup {

    private static final double DIST_MIN_FLEURS = 30;
    private static final double TOLERANCE_SEGMENT = 12;
    private static final int NB_POINTS_SEGMENT = 24;
    private static final int MAX_COUPS_GENERES = 80;

    public static List<CoupIA> genererCoups(EtatJeu etat) {

        List<CoupIA> coups = new ArrayList<>();

        int taille = etat.fleursRestantes.size();

        for (int i = 0; i < taille; i++) {
            for (int j = i + 1; j < taille; j++) {
                 Fleur f1 = etat.fleursRestantes.get(i);
                Fleur f2 = etat.fleursRestantes.get(j);

                if (f1.getType() == f2.getType()
                        && segmentLibre(etat, f1.getPosition(), f2.getPosition())) {

                    List<Coordonnees> points = pointsDuSegment(
                            etat,
                            f1.getPosition(),
                            f2.getPosition());

                    for (Coordonnees pos : points) {

                        coups.add(new CoupIA(f1, f2, pos));

                        if (coups.size() >= MAX_COUPS_GENERES) {
                            return coups;
                        }
                    }
                }
            }
        }

        return coups;
    }

      private static List<Coordonnees> pointsDuSegment(
            EtatJeu etat,
            Coordonnees A,
            Coordonnees B) {

        List<Coordonnees> points = new ArrayList<>();

        for (Coordonnees pos : Coordonnees.pointsDuSegment(A, B, NB_POINTS_SEGMENT)) {

            if (positionLibrePourPion(etat, pos)) {
                points.add(pos);
            }
        }

        return points;
    }

    private static boolean segmentLibre(EtatJeu etat, Coordonnees A, Coordonnees B) {

        for (Fleur f : etat.fleursRestantes) {

            Coordonnees C = f.getPosition();

            if (C.equals(A) || C.equals(B))
                continue;

            if (Coordonnees.surSegment(A, B, C, TOLERANCE_SEGMENT)) {
                return false;
            }
              }

        for (Pion p : etat.pions) {

            Coordonnees C = p.getPosition();

            if (C == null)
                continue;

            if (Coordonnees.surSegment(A, B, C, TOLERANCE_SEGMENT)) {
                return false;
            }
        }

        return true;
    }

    private static boolean positionLibrePourPion(EtatJeu etat, Coordonnees pos) {

        for (Pion p : etat.pions) {

            if (p.getPosition() != null && Coordonnees.distance(pos, p.getPosition()) < DIST_MIN_FLEURS) {
                return false;
            }
        }

        return true;
    }
}