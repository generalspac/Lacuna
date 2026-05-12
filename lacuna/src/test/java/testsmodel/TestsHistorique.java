package testsmodel;

import java.io.File;
import java.io.IOException;
import java.time.Duration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import model.Cercle;
import model.Coordonnees;
import model.Fleur;
import model.Historique;
import model.Jeu;
import model.Joueur;
import model.Pion;

public class TestsHistorique {


    private Jeu jeu;
    private File fichierTemp;

    /** Jeu avec un grand cercle pour ne pas contraindre les tests de placement. */
    private Jeu jeuLarge() {
        return new Jeu(800, 800, new Cercle(new Coordonnees(400, 400), 380));
    }

    @BeforeEach
    public void setUp() throws IOException {
        jeu = jeuLarge();
        // Passer la phase d'avantage pour ne pas la gérer dans chaque test
        if (jeu.isEnPhaseSelectionAvantage() && !jeu.getFleurs().isEmpty()) {
            jeu.appliquerAvantageAvecFleur(jeu.getFleurs().get(0));
        }
        fichierTemp = File.createTempFile("partie_test", ".txt");
    }

    @AfterEach
    public void tearDown() {
        if (fichierTemp != null) {
            fichierTemp.delete();
        }
    }

    // ------------------ ENREGISTREMENT D'ACTIONS ------------------ // 

    @Test
    void enregistrerActionAjouteDansUndoStack() {
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            Historique h = jeu.getHistorique();
            int tailleBefore = h.getUndoStack().size();

            Joueur joueur = jeu.getJoueurs()[0];
            Pion pion = new Pion(Pion.TypePion.OR, new Coordonnees(300, 400));
            Fleur f1 = new Fleur(Fleur.TypeFleur.ROUGE, new Coordonnees(100, 100));
            Fleur f2 = new Fleur(Fleur.TypeFleur.ROUGE, new Coordonnees(200, 200));

            h.enregistrerAction(pion, f1, f2, joueur);

            assertEquals(tailleBefore + 1, h.getUndoStack().size());
        });
    }

    @Test
    void enregistrerActionVideRedoStack() {
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            Historique h = jeu.getHistorique();
            Joueur joueur = jeu.getJoueurs()[0];
            Pion p = new Pion(Pion.TypePion.OR, new Coordonnees(300, 400));
            Fleur f1 = new Fleur(Fleur.TypeFleur.ROUGE, new Coordonnees(100, 100));
            Fleur f2 = new Fleur(Fleur.TypeFleur.ROUGE, new Coordonnees(200, 200));

            // Pré-remplir la redo stack manuellement
            h.enregistrerAction(p, f1, f2, joueur);
            h.getUndoStack().pop(); // simuler un undo manuel
            h.getRedoStack().push(new Historique.ActionJeu(p, f1, f2, joueur));

            // Un nouvel enregistrement doit vider redo
            h.enregistrerAction(p, f1, f2, joueur);
            assertTrue(h.getRedoStack().isEmpty());
        });
    }

    @Test
    void enregistrerActionAvantageAjouteDansUndoStack() {
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            // Réinitialiser pour retomber en phase avantage
            jeu.reset();
            Historique h = jeu.getHistorique();
            Fleur fleur = jeu.getFleurs().get(0);
            Joueur joueur = jeu.getJoueurActuel();

            h.enregistrerActionAvantage(fleur, joueur);

            assertFalse(h.getUndoStack().isEmpty());
            Historique.ActionJeu action = h.getUndoStack().peek();
            assertNull(action.getPion());
            assertEquals(fleur, action.getFleur1());
        });
    }

    // ------------------------- UNDO ------------------------- //

    @Test
    void undoSurStackVideAfficheMessage() {
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            jeu.getHistorique().clear();
            jeu.undo();
            assertEquals("Rien à annuler.", jeu.getFeedbackMessage());
        });
    }

    @Test
    void undoRetirePionDuPlateau() {
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            // Préparer deux fleurs de même couleur sur le plateau
            jeu.getFleurs().clear();
            jeu.getPions().clear();
            jeu.getHistorique().clear();

            Fleur f1 = new Fleur(Fleur.TypeFleur.ROUGE, new Coordonnees(360, 400));
            Fleur f2 = new Fleur(Fleur.TypeFleur.ROUGE, new Coordonnees(440, 400));
            jeu.getFleurs().add(f1);
            jeu.getFleurs().add(f2);

            Pion pion = new Pion(Pion.TypePion.OR, null);
            jeu.getPions().add(pion);
            for (int i = 1; i < 6; i++) {
                jeu.getPions().add(new Pion(Pion.TypePion.OR, null));
            }
            for (int i = 0; i < 6; i++) {
                jeu.getPions().add(new Pion(Pion.TypePion.ARGENT, null));
            }

            Joueur joueur = jeu.getJoueurs()[0];
            jeu.jouerCoup(joueur, pion, new Coordonnees(400, 400), f1, f2);

            // Pion posé et fleurs collectées
            assertNotNull(pion.getPosition());

            jeu.undo();

            // Pion retiré
            assertNull(pion.getPosition());
            // Fleurs remises sur le plateau
            assertTrue(jeu.getFleurs().contains(f1));
            assertTrue(jeu.getFleurs().contains(f2));
            // Fleurs retirées du joueur
            assertFalse(joueur.getFleursGagnees().contains(f1));
            assertFalse(joueur.getFleursGagnees().contains(f2));
        });
    }

    @Test
    void undoDeplaceLActionDansRedoStack() {
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            jeu.getFleurs().clear();
            jeu.getPions().clear();
            jeu.getHistorique().clear();

            Fleur f1 = new Fleur(Fleur.TypeFleur.VERTE, new Coordonnees(360, 400));
            Fleur f2 = new Fleur(Fleur.TypeFleur.VERTE, new Coordonnees(440, 400));
            jeu.getFleurs().add(f1);
            jeu.getFleurs().add(f2);

            for (int i = 0; i < 6; i++) jeu.getPions().add(new Pion(Pion.TypePion.OR, null));
            for (int i = 0; i < 6; i++) jeu.getPions().add(new Pion(Pion.TypePion.ARGENT, null));

            Joueur joueur = jeu.getJoueurs()[0];
            Pion pion = jeu.getPionLibre(Pion.TypePion.OR);
            jeu.jouerCoup(joueur, pion, new Coordonnees(400, 400), f1, f2);

            assertTrue(jeu.getHistorique().getRedoStack().isEmpty());
            jeu.undo();
            assertFalse(jeu.getHistorique().getRedoStack().isEmpty());
        });
    }

    @Test
    void undoAvantageRemetEnPhaseSelection() {
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            jeu.reset(); // retour en phase avantage
            Fleur fleur = jeu.getFleurs().get(0);
            jeu.appliquerAvantageAvecFleur(fleur);

            assertTrue(jeu.isAvantageInitialApplique());

            jeu.undo();

            assertFalse(jeu.isAvantageInitialApplique());
            assertTrue(jeu.isEnPhaseSelectionAvantage());
            assertTrue(jeu.getFleurs().contains(fleur));
        });
    }

    // ------------------------- REDO ------------------------ //

    @Test
    void redoSurStackVideAfficheMessage() {
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            jeu.getHistorique().clear();
            jeu.redo();
            assertEquals("Rien à refaire.", jeu.getFeedbackMessage());
        });
    }

    @Test
    void redoReplacePionEtRedonneFleurs() {
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            jeu.getFleurs().clear();
            jeu.getPions().clear();
            jeu.getHistorique().clear();

            Fleur f1 = new Fleur(Fleur.TypeFleur.JAUNE, new Coordonnees(360, 400));
            Fleur f2 = new Fleur(Fleur.TypeFleur.JAUNE, new Coordonnees(440, 400));
            jeu.getFleurs().add(f1);
            jeu.getFleurs().add(f2);

            for (int i = 0; i < 6; i++) jeu.getPions().add(new Pion(Pion.TypePion.OR, null));
            for (int i = 0; i < 6; i++) jeu.getPions().add(new Pion(Pion.TypePion.ARGENT, null));

            Joueur joueur = jeu.getJoueurs()[0];
            Pion pion = jeu.getPionLibre(Pion.TypePion.OR);
            jeu.jouerCoup(joueur, pion, new Coordonnees(400, 400), f1, f2);

            jeu.undo();
            assertNull(pion.getPosition());

            jeu.redo();

            assertNotNull(pion.getPosition());
            assertFalse(jeu.getFleurs().contains(f1));
            assertFalse(jeu.getFleurs().contains(f2));
            assertTrue(joueur.getFleursGagnees().contains(f1));
            assertTrue(joueur.getFleursGagnees().contains(f2));
        });
    }

    @Test
    void redoDeplaceLActionDansUndoStack() {
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            jeu.getFleurs().clear();
            jeu.getPions().clear();
            jeu.getHistorique().clear();

            Fleur f1 = new Fleur(Fleur.TypeFleur.BLEUE, new Coordonnees(360, 400));
            Fleur f2 = new Fleur(Fleur.TypeFleur.BLEUE, new Coordonnees(440, 400));
            jeu.getFleurs().add(f1);
            jeu.getFleurs().add(f2);

            for (int i = 0; i < 6; i++) jeu.getPions().add(new Pion(Pion.TypePion.OR, null));
            for (int i = 0; i < 6; i++) jeu.getPions().add(new Pion(Pion.TypePion.ARGENT, null));

            Joueur joueur = jeu.getJoueurs()[0];
            Pion pion = jeu.getPionLibre(Pion.TypePion.OR);
            jeu.jouerCoup(joueur, pion, new Coordonnees(400, 400), f1, f2);

            int tailleUndoBefore = jeu.getHistorique().getUndoStack().size();
            jeu.undo();
            jeu.redo();

            assertEquals(tailleUndoBefore, jeu.getHistorique().getUndoStack().size());
            assertTrue(jeu.getHistorique().getRedoStack().isEmpty());
        });
    }

    // ----------------------- CLEAR ---------------------------

    @Test
    void clearVideLesDeuxtacks() {
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            Historique h = jeu.getHistorique();
            Joueur joueur = jeu.getJoueurs()[0];
            Pion p = new Pion(Pion.TypePion.OR, new Coordonnees(300, 300));
            Fleur f = new Fleur(Fleur.TypeFleur.ROUGE, new Coordonnees(100, 100));

            h.enregistrerAction(p, f, null, joueur);
            assertFalse(h.getUndoStack().isEmpty());

            h.clear();

            assertTrue(h.getUndoStack().isEmpty());
            assertTrue(h.getRedoStack().isEmpty());
        });
    }

    // -------------- SAUVEGARDE et CHARGEMENT ------------------- //

    @Test
    void sauvegardeEtChargementConserventNombrePions() {
        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            String chemin = fichierTemp.getAbsolutePath();

            // Jouer quelques coups pour avoir un état non trivial
            preparerEtatPourSauvegarde(jeu);

            int nbPionsAvant = jeu.getPions().size();
            jeu.sauvegarderPartie(chemin);

            Jeu jeu2 = jeuLarge();
            jeu2.chargerPartie(chemin);

            assertEquals(nbPionsAvant, jeu2.getPions().size());
        });
    }

    @Test
    void sauvegardeEtChargementConserventNombreFleursPlateau() {
        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            String chemin = fichierTemp.getAbsolutePath();
            preparerEtatPourSauvegarde(jeu);

            int nbFleursAvant = jeu.getFleurs().size();
            jeu.sauvegarderPartie(chemin);

            Jeu jeu2 = jeuLarge();
            jeu2.chargerPartie(chemin);

            assertEquals(nbFleursAvant, jeu2.getFleurs().size());
        });
    }

    @Test
    void sauvegardeEtChargementConserventIndexJoueurActuel() {
        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            String chemin = fichierTemp.getAbsolutePath();
            preparerEtatPourSauvegarde(jeu);

            int indexAvant = jeu.currentPlayerIndex;
            jeu.sauvegarderPartie(chemin);

            Jeu jeu2 = jeuLarge();
            jeu2.chargerPartie(chemin);

            assertEquals(indexAvant, jeu2.currentPlayerIndex);
        });
    }

    @Test
    void sauvegardeEtChargementConserventScoresJoueurs() {
        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            String chemin = fichierTemp.getAbsolutePath();
            preparerEtatPourSauvegarde(jeu);

            int score0Avant = jeu.getJoueurs()[0].getScoreTotal();
            int score1Avant = jeu.getJoueurs()[1].getScoreTotal();
            jeu.sauvegarderPartie(chemin);

            Jeu jeu2 = jeuLarge();
            jeu2.chargerPartie(chemin);

            assertEquals(score0Avant, jeu2.getJoueurs()[0].getScoreTotal());
            assertEquals(score1Avant, jeu2.getJoueurs()[1].getScoreTotal());
        });
    }

    @Test
    void sauvegardeEtChargementConserventUndoStack() {
        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            String chemin = fichierTemp.getAbsolutePath();
            preparerEtatPourSauvegarde(jeu);

            int tailleUndo = jeu.getHistorique().getUndoStack().size();
            jeu.sauvegarderPartie(chemin);

            Jeu jeu2 = jeuLarge();
            jeu2.chargerPartie(chemin);

            assertEquals(tailleUndo, jeu2.getHistorique().getUndoStack().size());
        });
    }

    @Test
    void chargementFichierInexistantNePlantePas() {
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            // Doit terminer sans exception (erreur loggée en interne)
            assertDoesNotThrow(() -> jeu.chargerPartie("/fichier/qui/nexiste/pas.txt"));
        });
    }

    // ------------- UNDO APRES CHARGEMENT --------------------- //

    @Test
    void undoFonctionneApresChargement() {
        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            String chemin = fichierTemp.getAbsolutePath();
            preparerEtatPourSauvegarde(jeu);

            int scoreAvantUndo = jeu.getJoueurs()[0].getScoreTotal();
            jeu.sauvegarderPartie(chemin);

            Jeu jeu2 = jeuLarge();
            jeu2.chargerPartie(chemin);

            if (!jeu2.getHistorique().getUndoStack().isEmpty()) {
                jeu2.undo();
                // Après undo, le score ne peut qu'être ≤ à avant
                assertTrue(jeu2.getJoueurs()[0].getScoreTotal() <= scoreAvantUndo);
            }
        });
    }


    /**
     * Joue un coup valide sur le jeu fourni pour préparer un état de partie avancé,
     * avec des éléments sur le plateau et dans les mains des joueurs,
     * ainsi qu'un historique d'actions
     * (au moins un pion placé et deux fleurs collectées).
     */
    private void preparerEtatPourSauvegarde(Jeu jeu) {
        jeu.getFleurs().clear();
        jeu.getPions().clear();
        jeu.getHistorique().clear();

        // Créer deux paires de fleurs
        Fleur fa1 = new Fleur(Fleur.TypeFleur.ROUGE, new Coordonnees(360, 400));
        Fleur fa2 = new Fleur(Fleur.TypeFleur.ROUGE, new Coordonnees(440, 400));
        Fleur fb1 = new Fleur(Fleur.TypeFleur.BLEUE, new Coordonnees(360, 350));
        Fleur fb2 = new Fleur(Fleur.TypeFleur.BLEUE, new Coordonnees(440, 350));
        jeu.getFleurs().add(fa1);
        jeu.getFleurs().add(fa2);
        jeu.getFleurs().add(fb1);
        jeu.getFleurs().add(fb2);

        // Créer les pions
        for (int i = 0; i < 6; i++) jeu.getPions().add(new Pion(Pion.TypePion.OR, null));
        for (int i = 0; i < 6; i++) jeu.getPions().add(new Pion(Pion.TypePion.ARGENT, null));

        // Jouer un coup avec le joueur 0
        Joueur joueur = jeu.getJoueurs()[0];
        Pion pion = jeu.getPionLibre(Pion.TypePion.OR);
        jeu.jouerCoup(joueur, pion, new Coordonnees(400, 400), fa1, fa2);
    }
}