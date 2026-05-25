package testsview;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import model.Cercle;
import model.Coordonnees;
import model.Fleur;
import model.Jeu;
import model.Joueur;
import model.JoueurHumain;
import model.Pion;
import view.CoordonneeConverter;
import view.animations.AnimationContext;
import view.animations.AnimationState;

/**
 * Tests unitaires pour la logique pure de JeuGraphique.
 *
 * On ne teste PAS le rendu Swing (paintComponent, drawXxx) car cela
 * nécessiterait un environnement graphique.
 * On teste :
 *  - CoordonneeConverter (modelToScreen / screenToModel / valeurs dérivées)
 *  - AnimationContext (state, génération, phase2, gagnant, reset)
 *  - Cohérence des flags (isAnimationEnCours, enAttenteAdversaire, etc.)
 *  - Délégation vers le modèle (refreshScores ne plante pas, feedbackMessage)
 */
public class TestsJeuGraphique {

    
    // Helpers                                                             //
    

    private static final int CENTRE_X = 330;
    private static final int CENTRE_Y = 220;
    private static final int RAYON    = 200;

    private CoordonneeConverter converter;
    private AnimationContext    ctx;

    /** Jeu prêt à jouer (avantage initial appliqué). */
    private Jeu jeuPret() {
        Jeu jeu = new Jeu(800, 600,
                new Cercle(new Coordonnees(CENTRE_X, CENTRE_Y), RAYON));
        if (!jeu.getFleurs().isEmpty()) {
            jeu.appliquerAvantageAvecFleur(jeu.getFleurs().get(0));
        }
        return jeu;
    }

    @BeforeEach
    void setUp() {
        converter = new CoordonneeConverter(
                new Cercle(new Coordonnees(CENTRE_X, CENTRE_Y), RAYON),
                800, 600);
        ctx = new AnimationContext();
    }

    // ================================================================== //
    // CoordonneeConverter — valeurs dérivées                              //
    // ================================================================== //

    @Test
    void sizeEstLeMinimumDesDeuxDimensions() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () ->
            assertEquals(600, converter.size()));
    }

    @Test
    void offsetXCentreLeCarreDansLaLargeur() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () ->
            assertEquals((800 - 600) / 2, converter.offsetX()));
    }

    @Test
    void offsetYEstZeroQuandHauteurEstLeMinimum() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () ->
            assertEquals(0, converter.offsetY()));
    }

    @Test
    void centerXEstOffsetXPlusDemiSize() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () ->
            assertEquals(converter.offsetX() + converter.size() / 2, converter.centerX()));
    }

    @Test
    void centerYEstOffsetYPlusDemiSize() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () ->
            assertEquals(converter.offsetY() + converter.size() / 2, converter.centerY()));
    }

    @Test
    void rayonScreenEst40PourcentDuSize() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () ->
            assertEquals(converter.size() * 0.4, converter.rayonScreen(), 1e-9));
    }

    @Test
    void fleurSizeEst5PourcentDuSize() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () ->
            assertEquals((int) (converter.size() * 0.05), converter.fleurSize()));
    }

    // ================================================================== //
    // CoordonneeConverter — modelToScreen                                 //
    // ================================================================== //

    @Test
    void centreDuModeleMappeExactementSurCentreEcran() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            Coordonnees screen = converter.modelToScreen(CENTRE_X, CENTRE_Y);
            assertEquals(converter.centerX(), screen.getX());
            assertEquals(converter.centerY(), screen.getY());
        });
    }

    @Test
    void bordDroitDuCercleMappeCorrectement() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            Coordonnees screen = converter.modelToScreen(CENTRE_X + RAYON, CENTRE_Y);
            assertEquals((int)(converter.centerX() + converter.rayonScreen()), screen.getX());
            assertEquals(converter.centerY(), screen.getY());
        });
    }

    @Test
    void bordGaucheDuCercleMappeCorrectement() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            Coordonnees screen = converter.modelToScreen(CENTRE_X - RAYON, CENTRE_Y);
            assertEquals((int)(converter.centerX() - converter.rayonScreen()), screen.getX());
            assertEquals(converter.centerY(), screen.getY());
        });
    }

    // ================================================================== //
    // CoordonneeConverter — screenToModel                                 //
    // ================================================================== //

    @Test
    void centreDeLEcranMappeExactementSurCentreModele() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            Coordonnees model = converter.screenToModel(converter.centerX(), converter.centerY());
            assertEquals(CENTRE_X, model.getX());
            assertEquals(CENTRE_Y, model.getY());
        });
    }

    // ================================================================== //
    // CoordonneeConverter — aller-retour                                  //
    // ================================================================== //

    @Test
    void allerRetourModelToScreenToModel() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            int mx = CENTRE_X + 60;
            int my = CENTRE_Y - 40;
            Coordonnees screen = converter.modelToScreen(mx, my);
            Coordonnees back   = converter.screenToModel(screen.getX(), screen.getY());
            // Tolérance ±1 pixel due aux arrondis int
            assertTrue(Math.abs(mx - back.getX()) <= 1);
            assertTrue(Math.abs(my - back.getY()) <= 1);
        });
    }

    @Test
    void allerRetourScreenToModelToScreen() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            int sx = converter.centerX() + 40;
            int sy = converter.centerY() - 25;
            Coordonnees model = converter.screenToModel(sx, sy);
            Coordonnees back  = converter.modelToScreen(model.getX(), model.getY());
            assertTrue(Math.abs(sx - back.getX()) <= 1);
            assertTrue(Math.abs(sy - back.getY()) <= 1);
        });
    }

    // ================================================================== //
    // CoordonneeConverter — setDimensions                                 //
    // ================================================================== //

    @Test
    void setDimensionsMiseAJourSize() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            converter.setDimensions(400, 400);
            assertEquals(400, converter.size());
        });
    }

    @Test
    void setDimensionsCentreRecalcule() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            converter.setDimensions(500, 400);
            // size=400, offsetX=50, centerX=250
            assertEquals(250, converter.centerX());
            assertEquals(200, converter.centerY());
        });
    }

    @Test
    void setDimensionsCarreExactPasDeDecalage() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            converter.setDimensions(500, 500);
            assertEquals(0, converter.offsetX());
            assertEquals(0, converter.offsetY());
        });
    }

    @Test
    void setDimensionsPortraitSizeEstLargeur() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            converter.setDimensions(300, 700);
            assertEquals(300, converter.size());
            assertEquals(0, converter.offsetX());
            assertEquals(200, converter.offsetY());
        });
    }

    @Test
    void apresRedimensionnementCentreMappeToujours() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            converter.setDimensions(400, 400);
            Coordonnees screen = converter.modelToScreen(CENTRE_X, CENTRE_Y);
            assertEquals(converter.centerX(), screen.getX());
            assertEquals(converter.centerY(), screen.getY());
        });
    }

    // ================================================================== //
    // AnimationContext — état initial                                     //
    // ================================================================== //

    @Test
    void etatInitialAucuneAnimation() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            assertEquals(AnimationState.AUCUNE, ctx.getState());
            assertFalse(ctx.isEnCours());
        });
    }

    @Test
    void etatInitialGenerationZero() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () ->
            assertEquals(0, ctx.getGeneration()));
    }

    @Test
    void etatInitialTousVisuelsNuls() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            assertNull(ctx.getFleurPhase2Illustree());
            assertNull(ctx.getPionPhase2Illustre());
            assertNull(ctx.getPionPhase2Compare());
            assertEquals(0, ctx.getPhase2ComparePulse());
            assertNull(ctx.getGagnantAnime());
            assertEquals(0, ctx.getWinnerPulse());
            assertFalse(ctx.isSelectionIAVisuelle());
        });
    }

    // ================================================================== //
    // AnimationContext — state et isEnCours                               //
    // ================================================================== //

    @Test
    void setStatePHASE2ActiveIsEnCours() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            ctx.setState(AnimationState.PHASE2);
            assertTrue(ctx.isEnCours());
        });
    }

    @Test
    void setStateAUCUNEDesactiveIsEnCours() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            ctx.setState(AnimationState.COUP_IA);
            ctx.setState(AnimationState.AUCUNE);
            assertFalse(ctx.isEnCours());
        });
    }

    // ================================================================== //
    // AnimationContext — génération                                       //
    // ================================================================== //

    @Test
    void nouvelleGenerationIncrementeEtRetourne() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            assertEquals(1, ctx.nouvelleGeneration());
            assertEquals(2, ctx.nouvelleGeneration());
            assertEquals(2, ctx.getGeneration());
        });
    }

    @Test
    void annulerIncrementeGenerationSansRetour() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            ctx.annuler();
            assertEquals(1, ctx.getGeneration());
        });
    }

    @Test
    void estGenerationValideVraiSurGenerationCourante() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            int gen = ctx.nouvelleGeneration();
            assertTrue(ctx.estGenerationValide(gen));
        });
    }

    @Test
    void estGenerationValideFauxApresAnnulation() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            int gen = ctx.nouvelleGeneration();
            ctx.annuler();
            assertFalse(ctx.estGenerationValide(gen));
        });
    }

    // ================================================================== //
    // AnimationContext — visuels phase 2                                  //
    // ================================================================== //

    @Test
    void setGetFleurPhase2Illustree() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            Fleur f = new Fleur(Fleur.TypeFleur.ROUGE, new Coordonnees(10, 20));
            ctx.setFleurPhase2Illustree(f);
            assertEquals(f, ctx.getFleurPhase2Illustree());
        });
    }

    @Test
    void incrementerPhase2ComparePulseEtClear() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            ctx.incrementerPhase2ComparePulse();
            ctx.incrementerPhase2ComparePulse();
            assertEquals(2, ctx.getPhase2ComparePulse());

            ctx.clearPhase2();

            assertNull(ctx.getFleurPhase2Illustree());
            assertNull(ctx.getPionPhase2Illustre());
            assertNull(ctx.getPionPhase2Compare());
            assertEquals(0, ctx.getPhase2ComparePulse());
        });
    }

    // ================================================================== //
    // AnimationContext — gagnant / winnerPulse                            //
    // ================================================================== //

    @Test
    void setGetGagnantAnime() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            Joueur j = new JoueurHumain("Alice", Pion.TypePion.OR);
            ctx.setGagnantAnime(j);
            assertEquals(j, ctx.getGagnantAnime());
        });
    }

    @Test
    void incrementerWinnerPulseEtSetWinnerPulse() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            assertEquals(1, ctx.incrementerWinnerPulse());
            assertEquals(2, ctx.incrementerWinnerPulse());
            ctx.setWinnerPulse(0);
            assertEquals(0, ctx.getWinnerPulse());
        });
    }

    // ================================================================== //
    // AnimationContext — reset complet                                    //
    // ================================================================== //

    @Test
    void resetRemetsEtatCompletAZero() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            ctx.setState(AnimationState.PHASE2);
            ctx.nouvelleGeneration();
            ctx.setFleurPhase2Illustree(new Fleur(Fleur.TypeFleur.ROUGE, new Coordonnees(0, 0)));
            ctx.setPionPhase2Illustre(new Pion(Pion.TypePion.OR, new Coordonnees(1, 1)));
            ctx.setGagnantAnime(new JoueurHumain("X", Pion.TypePion.OR));
            ctx.setWinnerPulse(15);
            ctx.setSelectionIAVisuelle(true);

            int genAvant = ctx.getGeneration();
            ctx.reset();

            assertEquals(AnimationState.AUCUNE, ctx.getState());
            assertFalse(ctx.isEnCours());
            assertTrue(ctx.getGeneration() > genAvant);
            assertFalse(ctx.estGenerationValide(genAvant));
            assertNull(ctx.getFleurPhase2Illustree());
            assertNull(ctx.getPionPhase2Illustre());
            assertNull(ctx.getGagnantAnime());
            assertEquals(0, ctx.getWinnerPulse());
            assertFalse(ctx.isSelectionIAVisuelle());
        });
    }

    @Test
    void resetInvalideLesGenerationsActives() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            int g1 = ctx.nouvelleGeneration();
            int g2 = ctx.nouvelleGeneration();
            ctx.reset();
            assertFalse(ctx.estGenerationValide(g1));
            assertFalse(ctx.estGenerationValide(g2));
        });
    }

    @Test
    void multipleResetsStable() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            ctx.reset();
            ctx.reset();
            ctx.reset();
            assertEquals(AnimationState.AUCUNE, ctx.getState());
            assertNull(ctx.getFleurPhase2Illustree());
        });
    }

    // ================================================================== //
    // Logique jeu — flags feedbackMessage et avantage                    //
    // ================================================================== //

    @Test
    void avantageInitialAppliqueMajFlags() {
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            Jeu jeu = new Jeu(800, 600,
                    new Cercle(new Coordonnees(CENTRE_X, CENTRE_Y), RAYON));
            assertTrue(jeu.isEnPhaseSelectionAvantage());
            assertFalse(jeu.isAvantageInitialApplique());

            Fleur f = jeu.getFleurs().get(0);
            jeu.appliquerAvantageAvecFleur(f);

            assertTrue(jeu.isAvantageInitialApplique());
            assertFalse(jeu.isEnPhaseSelectionAvantage());
            assertFalse(jeu.getFleurs().contains(f));
        });
    }

    @Test
    void feedbackMessageNonVideApresAction() {
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            Jeu jeu = jeuPret();
            jeu.setFeedbackMessage("Test");
            assertEquals("Test", jeu.getFeedbackMessage());
        });
    }

    // ================================================================== //
    // Logique jeu — getGagnant cohérent avec les scores                  //
    // ================================================================== //

    @Test
    void getGagnantNullSiAucuneFleurCollectee() {
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            Jeu jeu = new Jeu(800, 600,
                    new Cercle(new Coordonnees(CENTRE_X, CENTRE_Y), RAYON));
            // Aucune fleur collectée par personne → égalité
            assertNull(jeu.getGagnant());
        });
    }

    @Test
    void getGagnantRetourneJoueurAvecPlusDeFleurs() {
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            Jeu jeu = jeuPret();
            Joueur j0 = jeu.getJoueurs()[0];
            Joueur j1 = jeu.getJoueurs()[1];

            j0.ajouterFleur(new Fleur(Fleur.TypeFleur.ROUGE, new Coordonnees(0, 0)));
            j0.ajouterFleur(new Fleur(Fleur.TypeFleur.ROUGE, new Coordonnees(1, 0)));
            j0.ajouterFleur(new Fleur(Fleur.TypeFleur.ROUGE, new Coordonnees(2, 0)));
            j1.ajouterFleur(new Fleur(Fleur.TypeFleur.BLEUE, new Coordonnees(3, 0)));

            assertEquals(j0, jeu.getGagnant());
        });
    }

    // ================================================================== //
    // Logique jeu — partieTerminee / abandon                             //
    // ================================================================== //

    @Test
    void abandonPartieChangeEtatPartie() {
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            Jeu jeu = jeuPret();
            assertFalse(jeu.isPartieTerminee());

            jeu.abandonnerPartie();

            assertTrue(jeu.isPartieTerminee());
            assertTrue(jeu.isPartieAbandonnee());
        });
    }

    @Test
    void abandonDeuxFoisNePlantePas() {
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            Jeu jeu = jeuPret();
            jeu.abandonnerPartie();
            assertDoesNotThrow(() -> jeu.abandonnerPartie());
        });
    }

    // ================================================================== //
    // CoordonneeConverter — composant portrait                            //
    // ================================================================== //

    @Test
    void allerRetourPortrait() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            converter.setDimensions(300, 700);
            int mx = CENTRE_X + 30;
            int my = CENTRE_Y + 20;
            Coordonnees screen = converter.modelToScreen(mx, my);
            Coordonnees back   = converter.screenToModel(screen.getX(), screen.getY());
            assertTrue(Math.abs(mx - back.getX()) <= 1);
            assertTrue(Math.abs(my - back.getY()) <= 1);
        });
    }
}
