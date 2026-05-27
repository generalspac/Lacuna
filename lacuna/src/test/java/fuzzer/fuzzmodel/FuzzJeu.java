package fuzzer.fuzzmodel;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;

import model.Cercle;
import model.Coordonnees;
import model.Fleur;
import model.Jeu;
import model.Pion;

/**
 * Fuzz test de la classe {@link Jeu}.
 *
 * Objectif :
 * - Tester la robustesse du moteur de jeu face à des séquences d’actions aléatoires
 * - Vérifier la cohérence des règles (coups, sélection, placement, undo/redo)
 * - Détecter les états incohérents dans les entités du jeu
 */
public class FuzzJeu {

    /**
     * Point d’entrée du fuzzing Jazzer.
     *
     * @param data Fournisseur de données fuzzées utilisées pour générer des scénarios de jeu aléatoires
     */
    public static void fuzzerTestOneInput(FuzzedDataProvider data) {

        
        // Création d'un jeu dans un cercle de taille aléatoire
        
        int centreX = data.consumeInt(100, 500);
        int centreY = data.consumeInt(100, 500);
        int rayon   = data.consumeInt(50, 300);

        Cercle cercle = new Cercle(new Coordonnees(centreX, centreY), rayon);
        Jeu jeu = new Jeu(800, 600, cercle);

        // Phase d'avantage initial : on choisit une fleur aléatoire
        if (!jeu.getFleurs().isEmpty()) {
            int idx = data.consumeInt(0, jeu.getFleurs().size() - 1);
            jeu.appliquerAvantageAvecFleur(jeu.getFleurs().get(idx));
        }

        
        // Série de coups aléatoires
        int nbCoups = data.consumeInt(0, 15);
        for (int i = 0; i < nbCoups; i++) {

            if (jeu.getFleurs().size() < 2) break;

            // Choisir deux fleurs quelconques (peuvent être invalides)
            int idx1 = data.consumeInt(0, jeu.getFleurs().size() - 1);
            int idx2 = data.consumeInt(0, jeu.getFleurs().size() - 1);
            Fleur f1 = jeu.getFleurs().get(idx1);
            Fleur f2 = jeu.getFleurs().get(idx2);

            // Sélectionner les fleurs via toucherFleur
            jeu.toucherFleur(f1.getPosition());
            jeu.toucherFleur(f2.getPosition());

            // Tenter de jouer avec un clic aléatoire dans le cercle
            int clicX = data.consumeInt(centreX - rayon, centreX + rayon);
            int clicY = data.consumeInt(centreY - rayon, centreY + rayon);
            jeu.jouerCoupAvecFleursSelectionnees(new Coordonnees(clicX, clicY));
        }

        
        // Undo / Redo aléatoires
        
        int nbUndo = data.consumeInt(0, 5);
        for (int i = 0; i < nbUndo; i++) {
            jeu.undo();
        }

        int nbRedo = data.consumeInt(0, 5);
        for (int i = 0; i < nbRedo; i++) {
            jeu.redo();
        }

        
        // Placement libre si aucun coup connecté
        
        int px = data.consumeInt(centreX - rayon, centreX + rayon);
        int py = data.consumeInt(centreY - rayon, centreY + rayon);
        jeu.placerPionLibreSiAucunCoup(new Coordonnees(px, py));

        
        // Vérifications d'invariants : aucune valeur NaN ni état incohérent
        
        for (Pion p : jeu.getPions()) {
            if (p.getPosition() != null) {
                if (Double.isNaN(p.getPosition().getX()) || Double.isNaN(p.getPosition().getY())) {
                    throw new RuntimeException("NaN dans la position d'un pion");
                }
            }
        }

        for (Fleur f : jeu.getFleurs()) {
            if (f.getPosition() == null) {
                throw new RuntimeException("Fleur sans position sur le plateau");
            }
        }

        // Le score total de chaque joueur ne doit jamais être négatif
        for (int j = 0; j < jeu.getNbJoueurs(); j++) {
            if (jeu.getJoueurs()[j].getScoreTotal() < 0) {
                throw new RuntimeException("Score négatif détecté pour le joueur " + j);
            }
        }

        // Crash sentinelle : rayon exact = 99999 → crash volontaire pour Jazzer
        if (rayon == 99999) {
            throw new RuntimeException("Crash sentinelle FuzzJeu");
        }
    }
}