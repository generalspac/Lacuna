package testsview;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import model.Cercle;
import model.Coordonnees;
import view.CoordonneeConverter;

/**
 *
 * @author thsba
 * Tests unitaires pour CoordonneeConverter.
 * Propriétés clés vérifiées :
 *  1. Aller-retour : screenToModel(modelToScreen(p)) ≈ p  (et vice-versa)
 *  2. Le centre du modèle mappe exactement sur le centre de l'écran
 *  3. Le rayon modèle occupe exactement 40 % du côté du composant
 *  4. Les valeurs dérivées (size, offsetX, centerX…) sont correctes
 *  5. setDimensions met bien à jour tous les calculs
 */
public class TestsCoordonneeConverter {

    // Cercle centré en (300, 220) de rayon 200 dans un modèle 600×440
    private static final Coordonnees CENTRE_MODELE = new Coordonnees(300, 220);
    private static final int         RAYON_MODELE  = 200;
    private static final int         COMP_W        = 800;
    private static final int         COMP_H        = 600;

    private Cercle             cercle;
    private CoordonneeConverter conv;

    @BeforeEach
    void setUp() {
        cercle = new Cercle(CENTRE_MODELE, RAYON_MODELE);
        conv   = new CoordonneeConverter(cercle, COMP_W, COMP_H);
    }

    // ---------------------- VALEURS DÉRIVÉES ------------------------------------ //

    @Test
    void sizeEstLeMinimumDesDimensions() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () ->
            assertEquals(Math.min(COMP_W, COMP_H), conv.size()));
    }

    @Test
    void offsetXCentreLeCarreDansLaLargeur() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            int expected = (COMP_W - conv.size()) / 2;
            assertEquals(expected, conv.offsetX());
        });
    }

    @Test
    void offsetYCentreLeCarreDansLaHauteur() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            int expected = (COMP_H - conv.size()) / 2;
            assertEquals(expected, conv.offsetY());
        });
    }

    @Test
    void centerXEstOffsetXPlusDemiSize() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () ->
            assertEquals(conv.offsetX() + conv.size() / 2, conv.centerX()));
    }

    @Test
    void centerYEstOffsetYPlusDemiSize() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () ->
            assertEquals(conv.offsetY() + conv.size() / 2, conv.centerY()));
    }

    @Test
    void rayonScreenEst40PourcentDuSize() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () ->
            assertEquals(conv.size() * 0.4, conv.rayonScreen(), 1e-9));
    }

    @Test
    void fleurSizeEst5PourcentDuSize() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () ->
            assertEquals((int) (conv.size() * 0.05), conv.fleurSize()));
    }

    // ------------------ CENTRE MODÈLE → CENTRE ÉCRAN ---------------------------- //

    @Test
    void centreDuModeleMapperSurCentreEcran() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            Coordonnees screen = conv.modelToScreen(CENTRE_MODELE.getX(), CENTRE_MODELE.getY());
            assertEquals(conv.centerX(), screen.getX());
            assertEquals(conv.centerY(), screen.getY());
        });
    }

    @Test
    void centreDeLEcranMapperSurCentreModele() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            Coordonnees model = conv.screenToModel(conv.centerX(), conv.centerY());
            assertEquals(CENTRE_MODELE.getX(), model.getX());
            assertEquals(CENTRE_MODELE.getY(), model.getY());
        });
    }

    // ------------ ALLER-RETOUR modelToScreen → screenToModel -------------------- //

    @Test
    void allerRetourCentreExact() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            Coordonnees screen = conv.modelToScreen(CENTRE_MODELE.getX(), CENTRE_MODELE.getY());
            Coordonnees back   = conv.screenToModel(screen.getX(), screen.getY());
            assertEquals(CENTRE_MODELE.getX(), back.getX(), "X aller-retour centre");
            assertEquals(CENTRE_MODELE.getY(), back.getY(), "Y aller-retour centre");
        });
    }

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

    @Test
    void setDimensionsMiseAJourSize() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            conv.setDimensions(400, 400);
            assertEquals(400, conv.size());
        });
    }

    @Test
    void setDimensionsMiseAJourCenterX() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            conv.setDimensions(500, 400);
            assertEquals(250, conv.centerX());
        });
    }

    @Test
    void setDimensionsMiseAJourCentreMappingApresRedimensionnement() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            conv.setDimensions(400, 400);

            Coordonnees screen = conv.modelToScreen(CENTRE_MODELE.getX(), CENTRE_MODELE.getY());

            assertEquals(conv.centerX(), screen.getX());
            assertEquals(conv.centerY(), screen.getY());
        });
    }

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

    @Test
    void composantPlusHautQueLargeSizeEstLargeur() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            conv.setDimensions(300, 700);

            assertEquals(300, conv.size());
            assertEquals(0, conv.offsetX());
            assertEquals(200, conv.offsetY());
        });
    }

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