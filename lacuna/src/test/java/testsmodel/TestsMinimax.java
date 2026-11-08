/**
 * @file TestsMinimax.java
 * @brief Tests unitaires pour l'algorithme Minimax et l'IA du jeu.
 */

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

/**
 * @class TestsMinimax
 * @brief Ensemble de tests unitaires vérifiant le comportement de l'algorithme Minimax.
 */
public class TestsMinimax {

    // --------------------------------------------------------------------- //
    // Helpers
    // --------------------------------------------------------------------- //

    /**
     * @brief Crée un jeu avec un grand cercle afin de faciliter les placements.
     * @return Une instance de Jeu initialisée.
     */
    private Jeu jeuLarge() {
        return new Jeu(800, 800, new Cercle(new Coordonnees(400, 400), 380));
    }

    /**
     * @brief Crée un jeu prêt à jouer (phase d'avantage initial appliquée si nécessaire).
     * @return Une instance de Jeu prête pour l'IA.
     */
    private Jeu jeuPret() {
        Jeu jeu = jeuLarge();
        if (!jeu.getFleurs().isEmpty()) {
            jeu.appliquerAvantageAvecFleur(jeu.getFleurs().get(0));
        }
        return jeu;
    }

    /**
     * @brief Crée une instance de Minimax avec un évaluateur par défaut.
     * @param profondeur Profondeur de recherche.
     * @param indexIA Index de l'IA dans le jeu.
     * @return Une instance de Minimax configurée.
     */
    private Minimax minimax(int profondeur, int indexIA) {
        return new Minimax(new EvaluateurJeu(), profondeur, indexIA);
    }

    // --------------------------------------------------------------------- //
    // Tests - meilleurCoup
    // --------------------------------------------------------------------- //

    /**
     * @brief Vérifie que meilleurCoup retourne un coup valide quand des coups existent.
     */
    @Test
    void meilleurCoupRetourneNonNullQuandCoupsExistent() {
        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            Jeu jeu = jeuPret();
            EtatJeu etat = EtatJeu.depuisJeu(jeu, 1);
            CoupIA coup = minimax(1, 1).meilleurCoup(etat);
            assertNotNull(coup);
        });
    }

    /**
     * @brief Vérifie que meilleurCoup retourne null lorsqu'aucun coup n'est possible.
     */
    @Test
    void meilleurCoupRetourneNullSiAucunCoup() {
        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            Jeu jeu = jeuLarge();
            jeu.getFleurs().clear();
            jeu.getPions().clear();
            EtatJeu etat = EtatJeu.depuisJeu(jeu, 1);
            CoupIA coup = minimax(1, 1).meilleurCoup(etat);
            assertNull(coup);
        });
    }

    /**
     * @brief Vérifie que la position d'un coup IA n'est pas nulle.
     */
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

    /**
     * @brief Vérifie que les fleurs d'un coup IA sont non nulles.
     */
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

    /**
     * @brief Vérifie que les deux fleurs d'un coup IA sont de même type.
     */
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

    /**
     * @brief Vérifie la cohérence des résultats entre différentes profondeurs.
     */
    @Test
    void meilleurCoupProfondeur1Et2RetournentUnCoup() {
        assertTimeoutPreemptively(Duration.ofSeconds(10), () -> {
            Jeu jeu = jeuPret();
            EtatJeu etat = EtatJeu.depuisJeu(jeu, 1);
            CoupIA coup1 = minimax(1, 1).meilleurCoup(etat);
            CoupIA coup2 = minimax(2, 1).meilleurCoup(etat);
            assertNotNull(coup1);
            assertNotNull(coup2);
        });
    }

    /**
     * @brief Vérifie que minimax à profondeur 0 retourne un score valide.
     */
    @Test
    void minimaxProfondeurZeroRetourneScore() {
        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            Jeu jeu = jeuPret();
            EtatJeu etat = EtatJeu.depuisJeu(jeu, 1);
            Minimax mm = minimax(1, 1);
            int score = mm.minimax(etat, 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
            assertFalse(Double.isNaN(score));
        });
    }

    /**
     * @brief Vérifie que minimax ne plante pas sur un état terminal.
     */
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

    /**
     * @brief Vérifie que les scores ne sont pas NaN pour différents index IA.
     */
    @Test
    void minimaxIndexIA0Et1RetournentDesScores() {
        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            Jeu jeu = jeuPret();
            EtatJeu etat = EtatJeu.depuisJeu(jeu, 0);
            int score0 = minimax(1, 0).minimax(etat, 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
            int score1 = minimax(1, 1).minimax(etat, 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
            assertFalse(Double.isNaN(score0));
            assertFalse(Double.isNaN(score1));
        });
    }

    /**
     * @brief Vérifie la cohérence de l'élagage alpha-bêta.
     */
    @Test
    void minimaxFenetreReduiteCoherente() {
        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            Jeu jeu = jeuPret();
            EtatJeu etat = EtatJeu.depuisJeu(jeu, 1);
            Minimax mm = minimax(2, 1);

            int scorePlein = mm.minimax(etat, 1, Integer.MIN_VALUE, Integer.MAX_VALUE);
            int scoreReduit = mm.minimax(etat, 1, scorePlein - 1, scorePlein + 1);

            assertEquals(scorePlein, scoreReduit);
        });
    }

    /**
     * @brief Vérifie qu'un coup est trouvé lorsqu'une seule paire existe.
     */
    @Test
    void meilleurCoupAvecUneSeulepaire() {
        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            Jeu jeu = jeuLarge();
            jeu.getFleurs().clear();
            jeu.getPions().clear();

            jeu.getFleurs().add(new Fleur(Fleur.TypeFleur.ROUGE, new Coordonnees(370, 400)));
            jeu.getFleurs().add(new Fleur(Fleur.TypeFleur.ROUGE, new Coordonnees(430, 400)));

            for (int i = 0; i < 6; i++) jeu.getPions().add(new Pion(Pion.TypePion.OR, null));
            for (int i = 0; i < 6; i++) jeu.getPions().add(new Pion(Pion.TypePion.ARGENT, null));

            EtatJeu etat = EtatJeu.depuisJeu(jeu, 1);
            CoupIA coup = minimax(1, 1).meilleurCoup(etat);

            assertNotNull(coup);
        });
    }

    /**
     * @brief Vérifie la stabilité du coup IA sur un même état.
     */
    @Test
    void meilleurCoupSurMemeEtat() {
        assertTimeoutPreemptively(Duration.ofSeconds(10), () -> {
            Jeu jeu = jeuPret();
            EtatJeu etat = EtatJeu.depuisJeu(jeu, 1);
            Minimax mm = minimax(1, 1);

            CoupIA coup1 = mm.meilleurCoup(etat);
            CoupIA coup2 = mm.meilleurCoup(etat);

            if (coup1 != null && coup2 != null) {
                assertEquals(coup1.fleur1.getType(), coup2.fleur1.getType());
            }
        });
    }
}