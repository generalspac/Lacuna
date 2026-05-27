/*
 * Tests unitaires pour la classe Cercle.
 * Vérifie le bon fonctionnement des opérations géométriques de base :
 * constructeur, accesseurs, aire, périmètre, distance et appartenance d’un point.
 */
package testsmodel;

import model.Cercle;
import model.Coordonnees;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TestCercle {
    
    public TestCercle() {
    }
    
    @BeforeAll
    public static void setUpClass() {
    }
    
    @AfterAll
    public static void tearDownClass() {
    }
    
    @BeforeEach
    public void setUp() {
    }
    
    @AfterEach
    public void tearDown() {
    }

    /**
     * Vérifie que le constructeur initialise correctement le centre et le rayon.
     */
    @Test
    public void testCercleConstructor() {
        Coordonnees centre = new Coordonnees(0, 0);
        Cercle c = new Cercle(centre, 5);
        assertEquals(centre, c.getCentre());
        assertEquals(5, c.getRayon());
    }

    /**
     * Vérifie les setters de centre et de rayon.
     */
    @Test
    public void testCercleSetters() {
        Coordonnees centre1 = new Coordonnees(0, 0);
        Coordonnees centre2 = new Coordonnees(1, 1);
        Cercle c = new Cercle(centre1, 5);
        c.setCentre(centre2);
        c.setRayon(10);
        assertEquals(centre2, c.getCentre());
        assertEquals(10, c.getRayon());
    }

    /**
     * Vérifie le calcul de l’aire du cercle.
     */
    @Test
    public void testCercleAire() {
        Coordonnees centre = new Coordonnees(0, 0);
        Cercle c = new Cercle(centre, 2);
        double expectedAire = Math.PI * 4;
        assertEquals(expectedAire, c.aire(), 0.001);
    }

    /**
     * Vérifie le calcul du périmètre du cercle.
     */
    @Test
    public void testCerclePerimetre() {
        Coordonnees centre = new Coordonnees(0, 0);
        Cercle c = new Cercle(centre, 3);
        double expectedPerimetre = 2 * Math.PI * 3;
        assertEquals(expectedPerimetre, c.perimetre(), 0.001);
    }

    /**
     * Vérifie le calcul de la distance d’un point au centre du cercle.
     */
    @Test
    public void testCercleDistanceAuCentre() {
        Coordonnees centre = new Coordonnees(5, 5);
        Cercle c = new Cercle(centre, 5);
        Coordonnees p = new Coordonnees(8, 9);
        assertEquals(5.0, c.distanceAuCentre(p), 0.001);
    }

    /**
     * Vérifie si un point est correctement détecté comme intérieur, sur la frontière ou extérieur.
     */
    @Test
    public void testCercleContientPoint() {
        Coordonnees centre = new Coordonnees(10, 10);
        Cercle c = new Cercle(centre, 5);
        Coordonnees inside = new Coordonnees(6, 6);
        Coordonnees onBoundary = new Coordonnees(5, 15);
        Coordonnees outside = new Coordonnees(3, 0);
        assertTrue(c.contientPoint(inside));
        assertTrue(c.contientPoint(onBoundary)); // <= boundary included
        assertFalse(c.contientPoint(outside));
    }

    /**
     * Vérifie que la méthode copie crée un nouvel objet équivalent.
     */
    @Test
    public void testCercleCopie() {
        Coordonnees centre = new Coordonnees(10, 15);
        Cercle original = new Cercle(centre, 10);
        Cercle copie = original.copie();
        assertEquals(original.getCentre(), copie.getCentre());
        assertEquals(original.getRayon(), copie.getRayon());
        assertNotSame(original, copie);
    }
}