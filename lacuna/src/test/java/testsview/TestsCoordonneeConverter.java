package testsview;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import model.Cercle;
import model.Coordonnees;
import view.CoordonneeConverter;

/**
 * @class TestsCoordonneeConverter
 * @brief Tests unitaires pour la classe CoordonneeConverter.
 *
 * Cette classe vérifie la validité des transformations entre le modèle
 * (coordonnées logiques du jeu) et l’affichage écran.
 *
 * Les tests couvrent :
 *  - les valeurs dérivées (size, offset, centre écran, rayon)
 *  - la conversion modèle → écran et écran → modèle
 *  - la stabilité des transformations (aller-retour)
 *  - le comportement lors des redimensionnements
 *
 * Hypothèses principales :
 *  - Le cercle du modèle est correctement centré
 *  - Le rendu est un carré centré dans le composant
 *  - Les conversions sont bijectives à une précision de ±1 pixel
 */
public class TestsCoordonneeConverter {

    /** Centre du cercle dans le modèle (coordonnées logiques). */
    private static final Coordonnees CENTRE_MODELE = new Coordonnees(300, 220);

    /** Rayon du cercle dans le modèle. */
    private static final int RAYON_MODELE  = 200;

    /** Largeur du composant d’affichage. */
    private static final int COMP_W        = 800;

    /** Hauteur du composant d’affichage. */
    private static final int COMP_H        = 600;

    /** Cercle utilisé pour les tests de conversion. */
    private Cercle cercle;

    /** Convertisseur testé (modèle ↔ écran). */
    private CoordonneeConverter conv;

    /**
     * Initialise un nouveau convertisseur avant chaque test.
     */
    @BeforeEach
    void setUp() {
        cercle = new Cercle(CENTRE_MODELE, RAYON_MODELE);
        conv   = new CoordonneeConverter(cercle, COMP_W, COMP_H);
    }

    // ---------------------- VALEURS DÉRIVÉES ------------------------------------ //

    /**
     * Vérifie que la taille du carré d’affichage correspond au minimum
     * entre largeur et hauteur du composant.
     */
    @Test
    void sizeEstLeMinimumDesDimensions() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () ->
            assertEquals(Math.min(COMP_W, COMP_H), conv.size()));
    }

    /**
     * Vérifie que le carré est correctement centré horizontalement.
     */
    @Test
    void offsetXCentreLeCarreDansLaLargeur() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            int expected = (COMP_W - conv.size()) / 2;
            assertEquals(expected, conv.offsetX());
        });
    }

    /**
     * Vérifie que le carré est correctement centré verticalement.
     */
    @Test
    void offsetYCentreLeCarreDansLaHauteur() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            int expected = (COMP_H - conv.size()) / 2;
            assertEquals(expected, conv.offsetY());
        });
    }

    /**
     * Vérifie que le centre X correspond bien au centre du carré affiché.
     */
    @Test
    void centerXEstOffsetXPlusDemiSize() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () ->
            assertEquals(conv.offsetX() + conv.size() / 2, conv.centerX()));
    }

    /**
     * Vérifie que le centre Y correspond bien au centre du carré affiché.
     */
    @Test
    void centerYEstOffsetYPlusDemiSize() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () ->
            assertEquals(conv.offsetY() + conv.size() / 2, conv.centerY()));
    }

    /**
     * Vérifie que le rayon écran correspond à 40% de la taille du carré.
     */
    @Test
    void rayonScreenEst40PourcentDuSize() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () ->
            assertEquals(conv.size() * 0.4, conv.rayonScreen(), 1e-9));
    }

    /**
     * Vérifie que la taille d’une fleur correspond à 5% de la taille écran.
     */
    @Test
    void fleurSizeEst5PourcentDuSize() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () ->
            assertEquals((int) (conv.size() * 0.05), conv.fleurSize()));
    }

    // ------------------ CENTRE MODÈLE → CENTRE ÉCRAN ---------------------------- //

    /**
     * Vérifie que le centre du modèle est correctement mappé sur le centre écran.
     */
    @Test
    void centreDuModeleMapperSurCentreEcran() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            Coordonnees screen = conv.modelToScreen(CENTRE_MODELE.getX(), CENTRE_MODELE.getY());
            assertEquals(conv.centerX(), screen.getX());
            assertEquals(conv.centerY(), screen.getY());
        });
    }

    /**
     * Vérifie que le centre écran correspond bien au centre du modèle.
     */
    @Test
    void centreDeLEcranMapperSurCentreModele() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            Coordonnees model = conv.screenToModel(conv.centerX(), conv.centerY());
            assertEquals(CENTRE_MODELE.getX(), model.getX());
            assertEquals(CENTRE_MODELE.getY(), model.getY());
        });
    }

    // ------------ ALLER-RETOUR modelToScreen → screenToModel -------------------- //

    /**
     * Vérifie la cohérence aller-retour pour le centre du modèle.
     */
    @Test
    void allerRetourCentreExact() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            Coordonnees screen = conv.modelToScreen(CENTRE_MODELE.getX(), CENTRE_MODELE.getY());
            Coordonnees back   = conv.screenToModel(screen.getX(), screen.getY());
            assertEquals(CENTRE_MODELE.getX(), back.getX(), "X aller-retour centre");
            assertEquals(CENTRE_MODELE.getY(), back.getY(), "Y aller-retour centre");
        });
    }

    /**
     * Vérifie la cohérence aller-retour pour un point quelconque.
     */
    @Test
    void allerRetourPointQuelconque() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            int mx = CENTRE_MODELE.getX() + 80;
            int my = CENTRE_MODELE.getY() - 60;
            Coordonnees screen = conv.modelToScreen(mx, my);
            Coordonnees back   = conv.screenToModel(screen.getX(), screen.getY());

            assertTrue(Math.abs(mx - back.getX()) <= 1, "X aller-retour point quelconque");
            assertTrue(Math.abs(my - back.getY()) <= 1, "Y aller-retour point quelconque");
        });
    }

    /**
     * Vérifie la cohérence écran → modèle → écran.
     */
    @Test
    void allerRetourEcranVersModeleVersEcran() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            int sx = conv.centerX() + 50;
            int sy = conv.centerY() - 30;

            Coordonnees model = conv.screenToModel(sx, sy);
            Coordonnees back  = conv.modelToScreen(model.getX(), model.getY());

            assertTrue(Math.abs(sx - back.getX()) <= 1, "X aller-retour écran→modèle→écran");
            assertTrue(Math.abs(sy - back.getY()) <= 1, "Y aller-retour écran→modèle→écran");
        });
    }

    // ---------------------- RAYON : BORD DU CERCLE ------------------------------- //

    /**
     * Vérifie le mapping du bord droit du cercle.
     */
    @Test
    void bordDroitDuCercleMappeCorrectement() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            int mx = CENTRE_MODELE.getX() + RAYON_MODELE;
            int my = CENTRE_MODELE.getY();

            Coordonnees screen = conv.modelToScreen(mx, my);

            int expected = (int) (conv.centerX() + conv.rayonScreen());

            assertEquals(expected, screen.getX());
            assertEquals(conv.centerY(), screen.getY());
        });
    }

    /**
     * Vérifie le mapping du bord gauche du cercle.
     */
    @Test
    void bordGaucheDuCercleMappeCorrectement() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            int mx = CENTRE_MODELE.getX() - RAYON_MODELE;
            int my = CENTRE_MODELE.getY();

            Coordonnees screen = conv.modelToScreen(mx, my);

            int expected = (int) (conv.centerX() - conv.rayonScreen());

            assertEquals(expected, screen.getX());
            assertEquals(conv.centerY(), screen.getY());
        });
    }

    // ---------------------- setDimensions MET À JOUR ----------------------------- //

    /**
     * Vérifie que setDimensions met correctement à jour la taille.
     */
    @Test
    void setDimensionsMiseAJourSize() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            conv.setDimensions(400, 400);
            assertEquals(400, conv.size());
        });
    }

    /**
     * Vérifie que setDimensions met à jour le centre X.
     */
    @Test
    void setDimensionsMiseAJourCenterX() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            conv.setDimensions(500, 400);
            assertEquals(250, conv.centerX());
        });
    }

    /**
     * Vérifie la cohérence après redimensionnement.
     */
    @Test
    void setDimensionsMiseAJourCentreMappingApresRedimensionnement() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            conv.setDimensions(400, 400);

            Coordonnees screen = conv.modelToScreen(CENTRE_MODELE.getX(), CENTRE_MODELE.getY());

            assertEquals(conv.centerX(), screen.getX());
            assertEquals(conv.centerY(), screen.getY());
        });
    }

    /**
     * Vérifie le cas d’un carré parfait sans offset.
     */
    @Test
    void setDimensionsCarreExactPasDeDecalage() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            conv.setDimensions(500, 500);

            assertEquals(0, conv.offsetX());
            assertEquals(0, conv.offsetY());
            assertEquals(250, conv.centerX());
            assertEquals(250, conv.centerY());
        });
    }

    // ---------------- COMPOSANT PLUS HAUT QUE LARGE ------------------------------ //

    /**
     * Vérifie le comportement quand la hauteur est plus grande que la largeur.
     */
    @Test
    void composantPlusHautQueLargeSizeEstLargeur() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            conv.setDimensions(300, 700);

            assertEquals(300, conv.size());
            assertEquals(0, conv.offsetX());
            assertEquals(200, conv.offsetY());
        });
    }

    /**
     * Vérifie la cohérence aller-retour dans un composant non carré.
     */
    @Test
    void composantPlusHautQueLargeAllerRetour() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            conv.setDimensions(300, 700);

            int mx = CENTRE_MODELE.getX() + 50;
            int my = CENTRE_MODELE.getY() + 30;

            Coordonnees screen = conv.modelToScreen(mx, my);
            Coordonnees back   = conv.screenToModel(screen.getX(), screen.getY());

            assertTrue(Math.abs(mx - back.getX()) <= 1);
            assertTrue(Math.abs(my - back.getY()) <= 1);
        });
    }
}