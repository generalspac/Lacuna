package model;

import java.util.Map;

// Calcule les scores par couleur et le gagnant d'une partie.
public final class ScoreJeu {

    private ScoreJeu() {
    }

    public static Joueur getGagnant(Joueur[] joueurs, Joueur gagnantParAbandon) {
        if (gagnantParAbandon != null) {
            return gagnantParAbandon;
        }

        int couleursJ0 = joueurs[0].nbCouleursGagnees();
        int couleursJ1 = joueurs[1].nbCouleursGagnees();
        if (couleursJ0 > couleursJ1) return joueurs[0];
        if (couleursJ1 > couleursJ0) return joueurs[1];

        int fleursJ0 = joueurs[0].getScoreTotal();
        int fleursJ1 = joueurs[1].getScoreTotal();
        if (fleursJ0 > fleursJ1) return joueurs[0];
        if (fleursJ1 > fleursJ0) return joueurs[1];
        return null;
    }

    public static Map<Fleur.TypeFleur, Integer> getScore(Joueur joueur) {

        Map<Fleur.TypeFleur, Integer> map = new java.util.HashMap<>();

        for (Fleur.TypeFleur t : Fleur.TypeFleur.values()) {
            map.put(t, 0);
        }

        for (Fleur f : joueur.getFleursGagnees()) {
            map.put(f.getType(), map.get(f.getType()) + 1);
        }

        return map;
    }
}
