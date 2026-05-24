package model;

import java.util.ArrayList;
import java.util.List;

//Contient les règles géométriques communes du plateau
public final class ReglesPlateau {

    private ReglesPlateau() {
    }

    public static boolean objetsSeSuperposent(Coordonnees a, Coordonnees b, double distanceMin) {
        return Coordonnees.distance(a, b) <= distanceMin;
    }

    public static boolean fleurTropProcheDuneAutre(Coordonnees pos, List<Fleur> fleurs, double distanceMin) {
        for (Fleur f : fleurs) {
            if (objetsSeSuperposent(pos, f.getPosition(), distanceMin)) {
                return true;
            }
        }
        return false;
    }

    public static boolean pionTropProcheDunAutre(Coordonnees pos, List<Pion> pions, double distanceMin) {
        for (Pion p : pions) {
            if (p.getPosition() != null
                    && objetsSeSuperposent(pos, p.getPosition(), distanceMin)) {
                return true;
            }
        }
        return false;
    }

    public static boolean positionLibrePourPion(Coordonnees pos, List<Pion> pions, double distanceMin) {
        for (Pion p : pions) {
            if (p.getPosition() != null && objetsSeSuperposent(pos, p.getPosition(), distanceMin)) {
                return false;
            }
        }
        return true;
    }

    public static boolean segmentLibre(List<Fleur> fleurs, List<Pion> pions, Coordonnees A, Coordonnees B,
            double toleranceSegment) {

        // Vérifier les fleurs comme obstacles
        for (Fleur f : fleurs) {

            Coordonnees C = f.getPosition();

            // ignorer les extrémités (les fleurs sélectionnées)
            if (C.equals(A) || C.equals(B))
                continue;

            if (Coordonnees.surSegment(A, B, C, toleranceSegment)) {
                return false; // obstacle trouvé
            }
        }

        // Vérifier les pions comme obstacles
        for (Pion p : pions) {
            Coordonnees C = p.getPosition();
            if (C == null)
                continue;

            if (Coordonnees.surSegment(A, B, C, toleranceSegment)) {
                return false; // un pion bloque le chemin
            }
        }

        return true;
    }

    public static boolean segmentLibre(EtatPlateau etat, Coordonnees A, Coordonnees B, double toleranceSegment) {
        return segmentLibre(etat.fleursPlateau(), etat.pionsPlateau(), A, B, toleranceSegment);
    }

    public static List<Coordonnees> pointsDuSegment(List<Pion> pions, Cercle cercleDeJeu, Coordonnees A, Coordonnees B,
            int nbPointsSegment, double distanceMinPion, double margeExtremitesSegment) {

        List<Coordonnees> points = new ArrayList<>();

        for (Coordonnees pos : Coordonnees.pointsDuSegment(A, B, nbPointsSegment)) {

            // Vérification:le pion peut être placé ici?
            if (positionLibrePourPion(pos, pions, distanceMinPion)
                    && (cercleDeJeu == null || cercleDeJeu.contientPoint(pos))
                    && horsZoneFleursExtremites(A, B, pos, margeExtremitesSegment)) {
                points.add(pos);
            }
        }

        return points;
    }

    public static List<Coordonnees> pointsDuSegment(EtatPlateau etat, Coordonnees A, Coordonnees B,
            int nbPointsSegment, double distanceMinPion, double margeExtremitesSegment) {
        return pointsDuSegment(etat.pionsPlateau(), etat.cerclePlateau(), A, B, nbPointsSegment,
                distanceMinPion, margeExtremitesSegment);
    }

    public static boolean horsZoneFleursExtremites(Coordonnees A, Coordonnees B, Coordonnees pos,
            double margeExtremitesSegment) {
        return Coordonnees.distance(A, pos) >= margeExtremitesSegment
                && Coordonnees.distance(B, pos) >= margeExtremitesSegment;
    }

    public static Pion pionLePlusProche(List<Pion> pions, Fleur fleur) {
        if (fleur == null || fleur.getPosition() == null) {
            return null;
        }

        Pion meilleur = null;
        double min = Double.MAX_VALUE;

        for (Pion p : pions) {
            if (p.getPosition() != null) {
                double d = Coordonnees.distance(fleur.getPosition(), p.getPosition());
                if (d < min) {
                    min = d;
                    meilleur = p;
                }
            }
        }
        return meilleur;
    }

    public static Pion pionLePlusProche(EtatPlateau etat, Fleur fleur) {
        return pionLePlusProche(etat.pionsPlateau(), fleur);
    }
}
