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
    private static final int NB_POINTS_SEGMENT = 3; // le nombre de points
    private static final int MAX_COUPS_GENERES = 80;

    public static List<CoupIA> genererCoups(EtatJeu etat) {
        List<CoupIA> coups = new ArrayList<>();

        for (int i = 0; i < etat.fleursRestantes.size(); i++) {
            for (int j = i + 1; j < etat.fleursRestantes.size(); j++) {
                Fleur f1 = etat.fleursRestantes.get(i);
                Fleur f2 = etat.fleursRestantes.get(j);

                //Même couleur + segment libre
                if (f1.getType() == f2.getType() && segmentLibre(etat, f1.getPosition(), f2.getPosition())) {
                    List<Coordonnees> points = pointsDuSegment(etat, f1.getPosition(), f2.getPosition());

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

    //Genère plusieurs positions possibles sur me segment [A,B] pour placer les pions
    private static List<Coordonnees> pointsDuSegment(EtatJeu etat, Coordonnees A, Coordonnees B) {
        List<Coordonnees> points = new ArrayList<>();

        //On découpe le segment en plusieurs morceaux
        for (Coordonnees pos : Coordonnees.pointsDuSegment(A, B, NB_POINTS_SEGMENT)) {

            // Vérification:le pion peut être placé ici?
            if (positionLibrePourPion(etat, pos)) {
                points.add(pos);
            }
        }

        return points;
    }

    private static boolean segmentLibre(EtatJeu etat, Coordonnees A, Coordonnees B) {

        // Vérifier les fleurs comme obstacles
        for (Fleur f : etat.fleursRestantes) {
            Coordonnees C = f.getPosition();

            // Ignorer les extrémités (les fleurs sélectionnées)
            if (C.equals(A) || C.equals(B))
                continue;

            if (Coordonnees.surSegment(A, B, C, TOLERANCE_SEGMENT)) {
                return false; // obstacle found
            }
        }

        // On vérifier les pions comme obstacles
        for (Pion p : etat.pions) {
            Coordonnees C = p.getPosition();
            if (C == null)
                continue;

            if (Coordonnees.surSegment(A, B, C, TOLERANCE_SEGMENT)) {
                return false; // un pion bloque le chemin
            }
        }

        return true;
    }

    // On vérifie si la position où on place le pion est libre
    private static boolean positionLibrePourPion(EtatJeu etat, Coordonnees pos) {
        // on vérifie qu'il y a une distance aussi avec les pions
        for (Pion p : etat.pions) {
            if (p.getPosition() != null && Coordonnees.distance(pos, p.getPosition()) < DIST_MIN_FLEURS) {
                return false;
            }
        }

        return true;
    }
}