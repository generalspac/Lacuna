package model.IA;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import model.Coordonnees;
import model.ConstantesJeu;
import model.Fleur;
import model.ReglesPlateau;

// Liste tous les coups de l'IA depuis un état
public class GenerateurCoup {

    private static final double DIST_MIN_FLEURS = ConstantesJeu.DIST_MIN_PION_IA;
    private static final double TOLERANCE_SEGMENT = ConstantesJeu.TOLERANCE_SEGMENT;
    private static final double MARGE_EXTREMITES_SEGMENT = ConstantesJeu.MARGE_EXTREMITES_SEGMENT_IA;
    private static final int NB_POINTS_SEGMENT = ConstantesJeu.NB_POINTS_SEGMENT;
    private static final int MAX_COUPS_GENERES = ConstantesJeu.MAX_COUPS_GENERES_IA;

    public static List<CoupIA> genererCoups(EtatJeu etat) {

        List<CoupIA> coups = new ArrayList<>();

        int taille = etat.fleursRestantes.size();

        for (int i = 0; i < taille; i++) {
            for (int j = i + 1; j < taille; j++) {
                 Fleur f1 = etat.fleursRestantes.get(i);
                Fleur f2 = etat.fleursRestantes.get(j);

                if (f1.getType() == f2.getType() && segmentLibre(etat, f1.getPosition(), f2.getPosition())) {

                    List<Coordonnees> points = pointsDuSegment(etat,f1.getPosition(), f2.getPosition());
                    
                    Collections.shuffle(points);

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

    private static List<Coordonnees> pointsDuSegment( EtatJeu etat,Coordonnees A, Coordonnees B) {
        return ReglesPlateau.pointsDuSegment(etat, A, B, NB_POINTS_SEGMENT,
                DIST_MIN_FLEURS, MARGE_EXTREMITES_SEGMENT);
    }

    private static boolean segmentLibre(EtatJeu etat, Coordonnees A, Coordonnees B) {
        return ReglesPlateau.segmentLibre(etat, A, B, TOLERANCE_SEGMENT);
    }
}
