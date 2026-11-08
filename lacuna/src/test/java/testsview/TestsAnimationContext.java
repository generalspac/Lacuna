/**
 * @file TestsAnimationContext.java
 * @brief Tests unitaires pour la classe AnimationContext.
 */

package testsview;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import model.Coordonnees;
import model.Fleur;
import model.Joueur;
import model.JoueurHumain;
import model.Pion;
import view.animations.AnimationContext;
import view.animations.AnimationState;

/**
 * @class TestsAnimationContext
 * @brief Vérifie le comportement de l'état d'animation du jeu (phases, génération, visuels, reset).
 */
public class TestsAnimationContext {

    /**
     * @brief Contexte d'animation testé.
     */
    private AnimationContext ctx;

    /**
     * @brief Initialise un nouveau contexte avant chaque test.
     */
    @BeforeEach
    void setUp() {
        ctx = new AnimationContext();
    }

    // --------------------------------------------------------------------- //
    // ÉTAT INITIAL
    // --------------------------------------------------------------------- //

    /**
     * @brief Vérifie que l'état initial est sans animation.
     */
    @Test
    void etatInitialAucuneAnimation() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            assertEquals(AnimationState.AUCUNE, ctx.getState());
            assertFalse(ctx.isEnCours());
        });
    }

    /**
     * @brief Vérifie que la génération initiale est à zéro.
     */
    @Test
    void etatInitialGenerationZero() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () ->
            assertEquals(0, ctx.getGeneration()));
    }

    /**
     * @brief Vérifie que les éléments visuels de phase 2 sont initialement nuls.
     */
    @Test
    void etatInitialVisuelsPhase2Nuls() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            assertNull(ctx.getFleurPhase2Illustree());
            assertNull(ctx.getPionPhase2Illustre());
            assertNull(ctx.getPionPhase2Compare());
            assertEquals(0, ctx.getPhase2ComparePulse());
        });
    }

    /**
     * @brief Vérifie que les éléments liés au gagnant sont initialement nuls.
     */
    @Test
    void etatInitialVisuelsGagnantNuls() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            assertNull(ctx.getGagnantAnime());
            assertEquals(0, ctx.getWinnerPulse());
        });
    }

    /**
     * @brief Vérifie que la sélection IA visuelle est désactivée au départ.
     */
    @Test
    void etatInitialSelectionIAFalse() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () ->
            assertFalse(ctx.isSelectionIAVisuelle()));
    }

    // --------------------------------------------------------------------- //
    // STATE
    // --------------------------------------------------------------------- //

    /**
     * @brief Vérifie que setState active correctement l'animation.
     */
    @Test
    void setStateMetAJourIsEnCours() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            ctx.setState(AnimationState.PHASE2);
            assertTrue(ctx.isEnCours());
            assertEquals(AnimationState.PHASE2, ctx.getState());
        });
    }

    /**
     * @brief Vérifie que passer à AUCUNE désactive l'animation.
     */
    @Test
    void setStateAucuneDesactiveIsEnCours() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            ctx.setState(AnimationState.COUP_IA);
            ctx.setState(AnimationState.AUCUNE);
            assertFalse(ctx.isEnCours());
        });
    }

    // --------------------------------------------------------------------- //
    // GÉNÉRATION
    // --------------------------------------------------------------------- //

    /**
     * @brief Vérifie l'incrémentation de génération.
     */
    @Test
    void nouvelleGenerationIncremente() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            int g1 = ctx.nouvelleGeneration();
            int g2 = ctx.nouvelleGeneration();
            assertEquals(1, g1);
            assertEquals(2, g2);
            assertEquals(2, ctx.getGeneration());
        });
    }

    /**
     * @brief Vérifie que annuler incrémente la génération.
     */
    @Test
    void annulerIncrementeGenerationSansRetour() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            ctx.annuler();
            assertEquals(1, ctx.getGeneration());
        });
    }

    /**
     * @brief Vérifie la validité d'une génération.
     */
    @Test
    void estGenerationValideVraiSiIdentique() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            int gen = ctx.nouvelleGeneration();
            assertTrue(ctx.estGenerationValide(gen));
        });
    }

    /**
     * @brief Vérifie l'invalidation après annulation.
     */
    @Test
    void estGenerationValideFauxApresAnnulation() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            int gen = ctx.nouvelleGeneration();
            ctx.annuler();
            assertFalse(ctx.estGenerationValide(gen));
        });
    }

    /**
     * @brief Vérifie l'invalidation après nouvelle génération.
     */
    @Test
    void estGenerationValideFauxApresNouvelleGeneration() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            int ancienneGen = ctx.nouvelleGeneration();
            ctx.nouvelleGeneration();
            assertFalse(ctx.estGenerationValide(ancienneGen));
        });
    }

    // --------------------------------------------------------------------- //
    // VISUELS PHASE 2
    // --------------------------------------------------------------------- //

    /**
     * @brief Test getter/setter de fleur illustrée en phase 2.
     */
    @Test
    void setGetFleurPhase2() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            Fleur f = new Fleur(Fleur.TypeFleur.ROUGE, new Coordonnees(10, 20));
            ctx.setFleurPhase2Illustree(f);
            assertEquals(f, ctx.getFleurPhase2Illustree());
        });
    }

    /**
     * @brief Test getter/setter du pion illustré en phase 2.
     */
    @Test
    void setGetPionPhase2Illustre() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            Pion p = new Pion(Pion.TypePion.OR, new Coordonnees(5, 5));
            ctx.setPionPhase2Illustre(p);
            assertEquals(p, ctx.getPionPhase2Illustre());
        });
    }

    /**
     * @brief Test getter/setter du pion comparé en phase 2.
     */
    @Test
    void setGetPionPhase2Compare() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            Pion p = new Pion(Pion.TypePion.ARGENT, new Coordonnees(7, 7));
            ctx.setPionPhase2Compare(p);
            assertEquals(p, ctx.getPionPhase2Compare());
        });
    }

    /**
     * @brief Vérifie l'incrémentation du pulse de comparaison phase 2.
     */
    @Test
    void incrementerPhase2ComparePulse() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            assertEquals(1, ctx.incrementerPhase2ComparePulse());
            assertEquals(2, ctx.incrementerPhase2ComparePulse());
            assertEquals(2, ctx.getPhase2ComparePulse());
        });
    }

    /**
     * @brief Vérifie la réinitialisation des éléments de phase 2.
     */
    @Test
    void clearPhase2RemetsVisuelsAZero() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            ctx.setFleurPhase2Illustree(new Fleur(Fleur.TypeFleur.BLEUE, new Coordonnees(1, 1)));
            ctx.setPionPhase2Illustre(new Pion(Pion.TypePion.OR, new Coordonnees(2, 2)));
            ctx.setPionPhase2Compare(new Pion(Pion.TypePion.ARGENT, new Coordonnees(3, 3)));
            ctx.setPhase2ComparePulse(12);

            ctx.clearPhase2();

            assertNull(ctx.getFleurPhase2Illustree());
            assertNull(ctx.getPionPhase2Illustre());
            assertNull(ctx.getPionPhase2Compare());
            assertEquals(0, ctx.getPhase2ComparePulse());
        });
    }

    // --------------------------------------------------------------------- //
    // FIN DE PARTIE
    // --------------------------------------------------------------------- //

    /**
     * @brief Test getter/setter du gagnant animé.
     */
    @Test
    void setGetGagnantAnime() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            Joueur j = new JoueurHumain("TestJoueur", Pion.TypePion.OR);
            ctx.setGagnantAnime(j);
            assertEquals(j, ctx.getGagnantAnime());
        });
    }

    /**
     * @brief Vérifie le pulse du gagnant.
     */
    @Test
    void setGetWinnerPulse() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            ctx.setWinnerPulse(15);
            assertEquals(15, ctx.getWinnerPulse());
        });
    }

    /**
     * @brief Vérifie l'incrémentation du pulse gagnant.
     */
    @Test
    void incrementerWinnerPulse() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            assertEquals(1, ctx.incrementerWinnerPulse());
            assertEquals(2, ctx.incrementerWinnerPulse());
            assertEquals(2, ctx.getWinnerPulse());
        });
    }

    // --------------------------------------------------------------------- //
    // IA VISUELLE
    // --------------------------------------------------------------------- //

    /**
     * @brief Test de l'affichage de sélection IA.
     */
    @Test
    void setGetSelectionIAVisuelle() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            ctx.setSelectionIAVisuelle(true);
            assertTrue(ctx.isSelectionIAVisuelle());
            ctx.setSelectionIAVisuelle(false);
            assertFalse(ctx.isSelectionIAVisuelle());
        });
    }

    // --------------------------------------------------------------------- //
    // RESET
    // --------------------------------------------------------------------- //

    /**
     * @brief Vérifie que reset remet tout à zéro proprement.
     */
    @Test
    void resetRemetsEtatCompletAZero() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            ctx.setState(AnimationState.PHASE2);
            ctx.nouvelleGeneration();
            ctx.setFleurPhase2Illustree(new Fleur(Fleur.TypeFleur.ROUGE, new Coordonnees(0, 0)));
            ctx.setPionPhase2Illustre(new Pion(Pion.TypePion.OR, new Coordonnees(1, 1)));
            ctx.setPionPhase2Compare(new Pion(Pion.TypePion.ARGENT, new Coordonnees(2, 2)));
            ctx.setPhase2ComparePulse(9);
            ctx.setGagnantAnime(new JoueurHumain("J", Pion.TypePion.OR));
            ctx.setWinnerPulse(20);
            ctx.setSelectionIAVisuelle(true);

            int genAvantReset = ctx.getGeneration();
            ctx.reset();

            assertEquals(AnimationState.AUCUNE, ctx.getState());
            assertFalse(ctx.isEnCours());
            assertTrue(ctx.getGeneration() > genAvantReset);
            assertFalse(ctx.estGenerationValide(genAvantReset));
            assertNull(ctx.getFleurPhase2Illustree());
            assertNull(ctx.getPionPhase2Illustre());
            assertNull(ctx.getPionPhase2Compare());
            assertEquals(0, ctx.getPhase2ComparePulse());
            assertNull(ctx.getGagnantAnime());
            assertEquals(0, ctx.getWinnerPulse());
            assertFalse(ctx.isSelectionIAVisuelle());
        });
    }

    /**
     * @brief Vérifie que plusieurs reset restent cohérents.
     */
    @Test
    void multipleResetsResterStable() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            ctx.reset();
            ctx.reset();
            ctx.reset();
            assertEquals(AnimationState.AUCUNE, ctx.getState());
            assertNull(ctx.getFleurPhase2Illustree());
        });
    }
}