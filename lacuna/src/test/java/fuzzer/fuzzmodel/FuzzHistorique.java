package fuzzer.fuzzmodel;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;

import model.Cercle;
import model.Coordonnees;
import model.Fleur;
import model.Historique;
import model.Jeu;

/**
 * Fuzz test de la classe Historique.
 * Vérifie la robustesse des opérations undo/redo, ainsi que la cohérence
 * de la sauvegarde et du chargement de partie dans un contexte de jeu.
 */
public class FuzzHistorique {

    /**
     * Point d’entrée du fuzzing Jazzer.
     * Génère des scénarios aléatoires d’actions de jeu, puis teste les
     * opérations d’historique (undo, redo, sauvegarde, chargement).
     */
    public static void fuzzerTestOneInput(FuzzedDataProvider data) {

        
        // Mise en place d'un jeu minimal utilisé comme support de l'historique
        
        int centreX = data.consumeInt(100, 500);
        int centreY = data.consumeInt(100, 500);
        int rayon   = data.consumeInt(50, 300);

        Cercle cercle = new Cercle(new Coordonnees(centreX, centreY), rayon);
        Jeu jeu = new Jeu(800, 600, cercle);
        Historique h = jeu.getHistorique();

        // Phase d'avantage initial
        if (!jeu.getFleurs().isEmpty()) {
            int idx = data.consumeInt(0, jeu.getFleurs().size() - 1);
            jeu.appliquerAvantageAvecFleur(jeu.getFleurs().get(idx));
        }

        
        // Enregistrement de coups aléatoires dans l'historique
        
        int nbActions = data.consumeInt(0, 10);
        for (int i = 0; i < nbActions; i++) {

            if (jeu.getFleurs().size() < 2) break;

            int idx1 = data.consumeInt(0, jeu.getFleurs().size() - 1);
            int idx2 = data.consumeInt(0, jeu.getFleurs().size() - 1);
            Fleur f1 = jeu.getFleurs().get(idx1);
            Fleur f2 = jeu.getFleurs().get(idx2);

            jeu.toucherFleur(f1.getPosition());
            jeu.toucherFleur(f2.getPosition());

            int clicX = data.consumeInt(centreX - rayon, centreX + rayon);
            int clicY = data.consumeInt(centreY - rayon, centreY + rayon);
            jeu.jouerCoupAvecFleursSelectionnees(new Coordonnees(clicX, clicY));
        }

        
        // Undo aléatoires
        
        int nbUndo = data.consumeInt(0, 8);
        for (int i = 0; i < nbUndo; i++) {
            h.undo(jeu);
        }

        
        // Redo aléatoires
        
        int nbRedo = data.consumeInt(0, 8);
        for (int i = 0; i < nbRedo; i++) {
            h.redo(jeu);
        }

        
        // Sauvegarde puis rechargement depuis un fichier temporaire
        
        try {
            java.io.File tmp = java.io.File.createTempFile("fuzz_historique_", ".txt");
            tmp.deleteOnExit();
            String chemin = tmp.getAbsolutePath();

            h.sauvegarderPartie(jeu, chemin);
            h.chargerPartie(jeu, chemin);

        } catch (java.io.IOException e) {
            // IOException normale (espace disque, droits)
        }

        
        // Vérifications d'invariants
        

        // Les piles ne doivent jamais être null
        if (h.getUndoStack() == null || h.getRedoStack() == null) {
            throw new RuntimeException("Pile undo ou redo nulle");
        }

        // Après un undo sur pile vide le jeu doit afficher le bon message
        h.clear();
        h.undo(jeu);
        if (!"Rien à annuler.".equals(jeu.getFeedbackMessage())) {
            throw new RuntimeException("Message inattendu après undo sur pile vide : "
                    + jeu.getFeedbackMessage());
        }

        // Après un redo sur pile vide le jeu doit afficher le bon message
        h.redo(jeu);
        if (!"Rien à refaire.".equals(jeu.getFeedbackMessage())) {
            throw new RuntimeException("Message inattendu après redo sur pile vide : "
                    + jeu.getFeedbackMessage());
        }

        // Crash sentinelle
        if (rayon == 88888) {
            throw new RuntimeException("Crash sentinelle FuzzHistorique");
        }
    }
}