package fuzzer.fuzzmodel;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;

import model.Cercle;
import model.Coordonnees;
import model.Jeu;
import model.IA.CoupIA;
import model.IA.EtatJeu;
import model.IA.EvaluateurJeu;
import model.IA.Minimax;

public class FuzzMinimax {

    public static void fuzzerTestOneInput(FuzzedDataProvider data) {

        // Mise en place d'un jeu dans un cercle aléatoire
        int centreX = data.consumeInt(100, 500);
        int centreY = data.consumeInt(100, 500);
        int rayon   = data.consumeInt(50, 300);

        Cercle cercle = new Cercle(new Coordonnees(centreX, centreY), rayon);
        Jeu jeu = new Jeu(800, 600, cercle);

        // Phase d'avantage initial
        if (!jeu.getFleurs().isEmpty()) {
            int idx = data.consumeInt(0, jeu.getFleurs().size() - 1);
            jeu.appliquerAvantageAvecFleur(jeu.getFleurs().get(idx));
        }

        // Quelques coups pour créer un état non trivial
        int nbCoups = data.consumeInt(0, 6);
        for (int i = 0; i < nbCoups; i++) {
            if (jeu.getFleurs().size() < 2) break;
            int idx1 = data.consumeInt(0, jeu.getFleurs().size() - 1);
            int idx2 = data.consumeInt(0, jeu.getFleurs().size() - 1);
            jeu.toucherFleur(jeu.getFleurs().get(idx1).getPosition());
            jeu.toucherFleur(jeu.getFleurs().get(idx2).getPosition());
            int cx = data.consumeInt(centreX - rayon, centreX + rayon);
            int cy = data.consumeInt(centreY - rayon, centreY + rayon);
            jeu.jouerCoupAvecFleursSelectionnees(new Coordonnees(cx, cy));
        }

        // Construction de Minimax avec profondeur et index aléatoires
        int profondeur = data.consumeInt(1, 3);
        int indexIA    = data.consumeInt(0, 1);

        EvaluateurJeu evaluateur = new EvaluateurJeu();
        Minimax minimax = new Minimax(evaluateur, profondeur, indexIA);

        // Appel de meilleurCoup sur l'état courant                   
        EtatJeu etat = EtatJeu.depuisJeu(jeu, indexIA);
        CoupIA coup = minimax.meilleurCoup(etat);

        // Vérifications d'invariants sur le coup retourné
        if (coup != null) {
            // La position du coup ne doit pas contenir de NaN
            if (coup.position == null) {
                throw new RuntimeException("Coup IA avec position null");
            }
            if (Double.isNaN(coup.position.getX()) || Double.isNaN(coup.position.getY())) {
                throw new RuntimeException("NaN dans la position du coup IA");
            }

            // Les fleurs du coup doivent être non nulles
            if (coup.fleur1 == null || coup.fleur2 == null) {
                throw new RuntimeException("Coup IA avec fleur(s) nulle(s)");
            }
        }

        // minimax() sur l'état terminal ne doit pas planter
        int score = minimax.minimax(etat, 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
        if (Double.isNaN(score)) {
            throw new RuntimeException("NaN retourné par minimax()");
        }

        // Crash sentinelle
        if (rayon == 77777) {
            throw new RuntimeException("Crash sentinelle FuzzMinimax");
        }
    }
}
