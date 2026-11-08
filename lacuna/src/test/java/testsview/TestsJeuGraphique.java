package testsview;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import model.Cercle;
import model.Coordonnees;
import model.Fleur;
import model.Jeu;
import model.JoueurHumain;
import model.Pion;
import view.CoordonneeConverter;
import view.animations.AnimationContext;
import view.animations.AnimationState;

/**
 * @class TestsJeuGraphique
 * @brief Tests unitaires de la couche graphique (logique non-Swing).
 *
 * Cette classe teste la logique de présentation et de conversion sans
 * exécuter de rendu graphique réel (pas de Swing / paintComponent).
 *
 * Les composants testés sont :
 *  - CoordonneeConverter (conversion modèle ↔ écran)
 *  - AnimationContext (gestion des états d’animation)
 *  - Intégration légère avec le modèle (Jeu)
 *
 * Objectifs :
 *  - Vérifier la cohérence des transformations géométriques
 *  - Vérifier la stabilité des états d’animation
 *  - Vérifier les comportements de reset et de génération
 *  - Valider les flags d’état du jeu et de l’UI
 */
public class TestsJeuGraphique {

    
    // Helpers                                                             //
    

    /** Coordonnée X du centre du cercle du modèle. */
    private static final int CENTRE_X = 330;

    /** Coordonnée Y du centre du cercle du modèle. */
    private static final int CENTRE_Y = 220;

    /** Rayon du cercle utilisé pour les tests. */
    private static final int RAYON    = 200;

    /** Convertisseur modèle ↔ écran testé. */
    private CoordonneeConverter converter;

    /** Contexte d’animation UI. */
    private AnimationContext    ctx;

    /**
     * Crée un jeu prêt à jouer (avantage initial appliqué si nécessaire).
     *
     * @return instance de Jeu initialisée pour les tests.
     */
    private Jeu jeuPret() {
        Jeu jeu = new Jeu(800, 600,
                new Cercle(new Coordonnees(CENTRE_X, CENTRE_Y), RAYON));
        if (!jeu.getFleurs().isEmpty()) {
            jeu.appliquerAvantageAvecFleur(jeu.getFleurs().get(0));
        }
        return jeu;
    }

    /**
     * Initialise les objets nécessaires avant chaque test.
     */
    @BeforeEach
    void setUp() {
        converter = new CoordonneeConverter(
                new Cercle(new Coordonnees(CENTRE_X, CENTRE_Y), RAYON),
                800, 600);
        ctx = new AnimationContext();
    }


    // CoordonneeConverter — valeurs dérivées                              //


    /** Vérifie que la taille correspond au minimum des dimensions. */
    @Test
    void sizeEstLeMinimumDesDeuxDimensions() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () ->
            assertEquals(600, converter.size()));
    }

    /** Vérifie que le carré est correctement centré horizontalement. */
    @Test
    void offsetXCentreLeCarreDansLaLargeur() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () ->
            assertEquals((800 - 600) / 2, converter.offsetX()));
    }

    /** Vérifie que le décalage vertical est nul quand hauteur = size. */
    @Test
    void offsetYEstZeroQuandHauteurEstLeMinimum() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () ->
            assertEquals(0, converter.offsetY()));
    }

    /** Vérifie la cohérence du centre X. */
    @Test
    void centerXEstOffsetXPlusDemiSize() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () ->
            assertEquals(converter.offsetX() + converter.size() / 2, converter.centerX()));
    }

    /** Vérifie la cohérence du centre Y. */
    @Test
    void centerYEstOffsetYPlusDemiSize() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () ->
            assertEquals(converter.offsetY() + converter.size() / 2, converter.centerY()));
    }

    /** Vérifie le calcul du rayon écran (40% du size). */
    @Test
    void rayonScreenEst40PourcentDuSize() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () ->
            assertEquals(converter.size() * 0.4, converter.rayonScreen(), 1e-9));
    }

    /** Vérifie la taille des fleurs (5% du size). */
    @Test
    void fleurSizeEst5PourcentDuSize() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () ->
            assertEquals((int) (converter.size() * 0.05), converter.fleurSize()));
    }


    // CoordonneeConverter — modelToScreen                                 //


    /** Vérifie le mapping du centre du modèle vers l’écran. */
    @Test
    void centreDuModeleMappeExactementSurCentreEcran() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            Coordonnees screen = converter.modelToScreen(CENTRE_X, CENTRE_Y);
            assertEquals(converter.centerX(), screen.getX());
            assertEquals(converter.centerY(), screen.getY());
        });
    }

    /** Vérifie le mapping du bord droit du cercle. */
    @Test
    void bordDroitDuCercleMappeCorrectement() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            Coordonnees screen = converter.modelToScreen(CENTRE_X + RAYON, CENTRE_Y);
            assertEquals((int)(converter.centerX() + converter.rayonScreen()), screen.getX());
            assertEquals(converter.centerY(), screen.getY());
        });
    }

    /** Vérifie le mapping du bord gauche du cercle. */
    @Test
    void bordGaucheDuCercleMappeCorrectement() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            Coordonnees screen = converter.modelToScreen(CENTRE_X - RAYON, CENTRE_Y);
            assertEquals((int)(converter.centerX() - converter.rayonScreen()), screen.getX());
            assertEquals(converter.centerY(), screen.getY());
        });
    }

    // CoordonneeConverter — screenToModel                                 //

    /** Vérifie le mapping écran → modèle pour le centre. */
    @Test
    void centreDeLEcranMappeExactementSurCentreModele() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            Coordonnees model = converter.screenToModel(converter.centerX(), converter.centerY());
            assertEquals(CENTRE_X, model.getX());
            assertEquals(CENTRE_Y, model.getY());
        });
    }

    // CoordonneeConverter — aller-retour                                  //

    /** Vérifie la cohérence modèle → écran → modèle. */
    @Test
    void allerRetourModelToScreenToModel() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            int mx = CENTRE_X + 60;
            int my = CENTRE_Y - 40;
            Coordonnees screen = converter.modelToScreen(mx, my);
            Coordonnees back   = converter.screenToModel(screen.getX(), screen.getY());
            assertTrue(Math.abs(mx - back.getX()) <= 1);
            assertTrue(Math.abs(my - back.getY()) <= 1);
        });
    }

    /** Vérifie la cohérence écran → modèle → écran. */
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

    // CoordonneeConverter — setDimensions                                 //

    /** Vérifie la mise à jour de la taille après redimensionnement. */
    @Test
    void setDimensionsMiseAJourSize() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            converter.setDimensions(400, 400);
            assertEquals(400, converter.size());
        });
    }

    /** Vérifie le recalcul du centre après redimensionnement. */
    @Test
    void setDimensionsCentreRecalcule() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            converter.setDimensions(500, 400);
            assertEquals(250, converter.centerX());
            assertEquals(200, converter.centerY());
        });
    }

    /** Vérifie l’absence de décalage dans un carré parfait. */
    @Test
    void setDimensionsCarreExactPasDeDecalage() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            converter.setDimensions(500, 500);
            assertEquals(0, converter.offsetX());
            assertEquals(0, converter.offsetY());
        });
    }

    /** Vérifie le comportement en mode portrait. */
    @Test
    void setDimensionsPortraitSizeEstLargeur() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            converter.setDimensions(300, 700);
            assertEquals(300, converter.size());
            assertEquals(0, converter.offsetX());
            assertEquals(200, converter.offsetY());
        });
    }

    /** Vérifie la cohérence après redimensionnement. */
    @Test
    void apresRedimensionnementCentreMappeToujours() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            converter.setDimensions(400, 400);
            Coordonnees screen = converter.modelToScreen(CENTRE_X, CENTRE_Y);
            assertEquals(converter.centerX(), screen.getX());
            assertEquals(converter.centerY(), screen.getY());
        });
    }

    // AnimationContext — état initial                                     //

    /** Vérifie l’état initial du contexte d’animation. */
    @Test
    void etatInitialAucuneAnimation() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            assertEquals(AnimationState.AUCUNE, ctx.getState());
            assertFalse(ctx.isEnCours());
        });
    }

    /** Vérifie que la génération initiale vaut 0. */
    @Test
    void etatInitialGenerationZero() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () ->
            assertEquals(0, ctx.getGeneration()));
    }

    /** Vérifie que les visuels sont initialement nuls. */
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

    // AnimationContext — state et isEnCours                               //

    /** Vérifie activation de l’état PHASE2. */
    @Test
    void setStatePHASE2ActiveIsEnCours() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            ctx.setState(AnimationState.PHASE2);
            assertTrue(ctx.isEnCours());
        });
    }

    /** Vérifie désactivation de l’état AUCUNE. */
    @Test
    void setStateAUCUNEDesactiveIsEnCours() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            ctx.setState(AnimationState.COUP_IA);
            ctx.setState(AnimationState.AUCUNE);
            assertFalse(ctx.isEnCours());
        });
    }

    // AnimationContext — génération                                       //

    /** Vérifie l’incrémentation de génération. */
    @Test
    void nouvelleGenerationIncrementeEtRetourne() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            assertEquals(1, ctx.nouvelleGeneration());
            assertEquals(2, ctx.nouvelleGeneration());
        });
    }

    /** Vérifie annulation de génération. */
    @Test
    void annulerIncrementeGenerationSansRetour() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            ctx.annuler();
            assertEquals(1, ctx.getGeneration());
        });
    }

    // AnimationContext — reset                                           //

    /** Vérifie le reset complet du contexte. */
    @Test
    void resetRemetsEtatCompletAZero() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            ctx.setState(AnimationState.PHASE2);
            ctx.nouvelleGeneration();
            ctx.setFleurPhase2Illustree(new Fleur(Fleur.TypeFleur.ROUGE, new Coordonnees(0, 0)));
            ctx.setPionPhase2Illustre(new Pion(Pion.TypePion.OR, new Coordonnees(1, 1)));
            ctx.setGagnantAnime(new JoueurHumain("X", Pion.TypePion.OR));

            ctx.reset();

            assertEquals(AnimationState.AUCUNE, ctx.getState());
            assertFalse(ctx.isEnCours());
            assertNull(ctx.getFleurPhase2Illustree());
            assertNull(ctx.getPionPhase2Illustre());
            assertNull(ctx.getGagnantAnime());
        });
    }
}