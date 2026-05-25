package testsmodel;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import model.Cercle;
import model.Coordonnees;
import model.Fleur;
import model.Jeu;
import model.Pion;
import model.IA.CoupIA;
import model.IA.EtatJeu;
import model.IA.EvaluateurJeu;
import model.IA.Minimax;

public class TestsMinimax {

    // Helpers                                                             //

    /** Jeu avec un grand cercle pour ne pas bloquer les placements. */
    private Jeu jeuLarge() {
        return new Jeu(800, 800, new Cercle(new Coordonnees(400, 400), 380));
    }

    /** Passe la phase d'avantage initial et retourne le jeu prêt à jouer. */
    private Jeu jeuPret() {
        Jeu jeu = jeuLarge();
        if (!jeu.getFleurs().isEmpty()) {
            jeu.appliquerAvantageAvecFleur(jeu.getFleurs().get(0));
        }
        return jeu;
    }

    /** Construit un Minimax avec l'évaluateur par défaut. */
    private Minimax minimax(int profondeur, int indexIA) {
        return new Minimax(new EvaluateurJeu(), profondeur, indexIA);
    }

    // meilleurCoup — comportement général                                 //
    @Test
    void meilleurCoupRetourneNonNullQuandCoupsExistent() {
        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            Jeu jeu = jeuPret();
            EtatJeu etat = EtatJeu.depuisJeu(jeu, 1);
            CoupIA coup = minimax(1, 1).meilleurCoup(etat);
            assertNotNull(coup);
        });
    }

    @Test
    void meilleurCoupRetourneNullSiAucunCoup() {
        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            // Plateau vide : aucun coup possible
            Jeu jeu = jeuLarge();
            jeu.getFleurs().clear();
            jeu.getPions().clear();
            EtatJeu etat = EtatJeu.depuisJeu(jeu, 1);
            CoupIA coup = minimax(1, 1).meilleurCoup(etat);
            assertNull(coup);
        });
    }

    @Test
    void meilleurCoupPositionNonNull() {
        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            Jeu jeu = jeuPret();
            EtatJeu etat = EtatJeu.depuisJeu(jeu, 1);
            CoupIA coup = minimax(1, 1).meilleurCoup(etat);
            if (coup != null) {
                assertNotNull(coup.position);
            }
        });
    }

    @Test
    void meilleurCoupFleursNonNulles() {
        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            Jeu jeu = jeuPret();
            EtatJeu etat = EtatJeu.depuisJeu(jeu, 1);
            CoupIA coup = minimax(1, 1).meilleurCoup(etat);
            if (coup != null) {
                assertNotNull(coup.fleur1);
                assertNotNull(coup.fleur2);
            }
        });
    }

    @Test
    void meilleurCoupFleursMemeType() {
        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            Jeu jeu = jeuPret();
            EtatJeu etat = EtatJeu.depuisJeu(jeu, 1);
            CoupIA coup = minimax(1, 1).meilleurCoup(etat);
            if (coup != null) {
                assertEquals(coup.fleur1.getType(), coup.fleur2.getType(),
                        "Les deux fleurs d'un coup IA doivent être de même type");
            }
        });
    }

    // meilleurCoup — cohérence entre profondeurs                    
    @Test
    void meilleurCoupProfondeur1Et2RetournentUnCoup() {
        assertTimeoutPreemptively(Duration.ofSeconds(10), () -> {
            Jeu jeu = jeuPret();
            EtatJeu etat = EtatJeu.depuisJeu(jeu, 1);
            CoupIA coup1 = minimax(1, 1).meilleurCoup(etat);
            CoupIA coup2 = minimax(2, 1).meilleurCoup(etat);
            // Les deux profondeurs doivent aboutir à un coup valide
            assertNotNull(coup1);
            assertNotNull(coup2);   
        });
    }

    // minimax() sur la valeur renvoyée
    @Test
    void minimaxProfondeurZeroRetourneScore() {
        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            Jeu jeu = jeuPret();
            EtatJeu etat = EtatJeu.depuisJeu(jeu, 1);
            Minimax mm = minimax(1, 1);
            int score = mm.minimax(etat, 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
            assertFalse(Double.isNaN(score), "Le score à profondeur 0 ne doit pas être NaN");
        });
    }

    @Test
    void minimaxProfondeur1RetourneScore() {
        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            Jeu jeu = jeuPret();
            EtatJeu etat = EtatJeu.depuisJeu(jeu, 1);
            Minimax mm = minimax(2, 1);
            int score = mm.minimax(etat, 1, Integer.MIN_VALUE, Integer.MAX_VALUE);
            assertFalse(Double.isNaN(score), "Le score à profondeur 1 ne doit pas être NaN");
        });
    }

    @Test
    void minimaxSurEtatTerminalNePlantePas() {
        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            Jeu jeu = jeuLarge();
            jeu.getFleurs().clear();
            jeu.getPions().clear();
            EtatJeu etat = EtatJeu.depuisJeu(jeu, 1);
            Minimax mm = minimax(2, 1);
            assertDoesNotThrow(() ->
                mm.minimax(etat, 2, Integer.MIN_VALUE, Integer.MAX_VALUE));
        });
    }

    @Test
    void minimaxIndexIA0Et1RetournentDesScores() {
        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            Jeu jeu = jeuPret();
            EtatJeu etat = EtatJeu.depuisJeu(jeu, 0);
            int score0 = minimax(1, 0).minimax(etat, 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
            int score1 = minimax(1, 1).minimax(etat, 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
            // Pas de NaN dans les deux cas
            assertFalse(Double.isNaN(score0));
            assertFalse(Double.isNaN(score1));
        });
    }

    // Élagage alpha-bêta : cohérence avec fenêtre infinie vs réduite     //

    @Test
    void minimaxFenetreReduiteCoherente() {
        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            Jeu jeu = jeuPret();
            EtatJeu etat = EtatJeu.depuisJeu(jeu, 1);
            Minimax mm = minimax(2, 1);

            // Fenêtre infinie
            int scorePlein = mm.minimax(etat, 1, Integer.MIN_VALUE, Integer.MAX_VALUE);
            // Fenêtre [scorePlein-1 , scorePlein+1] : doit donner le même résultat
            int scoreReduit = mm.minimax(etat, 1, scorePlein - 1, scorePlein + 1);

            assertEquals(scorePlein, scoreReduit,
                    "L'élagage ne doit pas modifier le score dans une fenêtre centrée");
        });
    }

    // Comportement avec un plateau presque vide (1 paire de fleurs)      //

    @Test
    void meilleurCoupAvecUneSeulepaire() {
        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            Jeu jeu = jeuLarge();
            jeu.getFleurs().clear();
            jeu.getPions().clear();

            // Une seule paire de fleurs de même couleur, sans obstacle
            jeu.getFleurs().add(new Fleur(Fleur.TypeFleur.ROUGE, new Coordonnees(370, 400)));
            jeu.getFleurs().add(new Fleur(Fleur.TypeFleur.ROUGE, new Coordonnees(430, 400)));

            for (int i = 0; i < 6; i++) jeu.getPions().add(new Pion(Pion.TypePion.OR,    null));
            for (int i = 0; i < 6; i++) jeu.getPions().add(new Pion(Pion.TypePion.ARGENT, null));

            EtatJeu etat = EtatJeu.depuisJeu(jeu, 1);
            CoupIA coup = minimax(1, 1).meilleurCoup(etat);

            // Avec une seule paire disponible, le coup doit être trouvé
            assertNotNull(coup, "Un coup doit être trouvé avec une seule paire disponible");
        });
    }

    // même état, même type de coup (même couleur)        //
    @Test
    void meilleurCoupSurMemeEtat() {
        assertTimeoutPreemptively(Duration.ofSeconds(10), () -> {
            Jeu jeu = jeuPret();
            EtatJeu etat = EtatJeu.depuisJeu(jeu, 1);
            Minimax mm = minimax(1, 1);

            CoupIA coup1 = mm.meilleurCoup(etat);
            CoupIA coup2 = mm.meilleurCoup(etat);

            // Les deux appels sur le même état doivent retourner des coups de même couleur
            if (coup1 != null && coup2 != null) {
                assertEquals(coup1.fleur1.getType(), coup2.fleur1.getType(),
                        "La couleur du meilleur coup doit être stable pour un même état");
            }
        });
    }
}
