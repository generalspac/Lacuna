/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
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

/**
 *
 * @author thsba
 */
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

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
    
    // Tests pour Cercle
    @Test
    public void testCercleConstructor() {
        Coordonnees centre = new Coordonnees(0, 0);
        Cercle c = new Cercle(centre, 5);
        assertEquals(centre, c.getCentre());
        assertEquals(5, c.getRayon());
    }

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

    @Test
    public void testCercleAire() {
        Coordonnees centre = new Coordonnees(0, 0);
        Cercle c = new Cercle(centre, 2);
        double expectedAire = Math.PI * 4;
        assertEquals(expectedAire, c.aire(), 0.001);
    }

    @Test
    public void testCerclePerimetre() {
        Coordonnees centre = new Coordonnees(0, 0);
        Cercle c = new Cercle(centre, 3);
        double expectedPerimetre = 2 * Math.PI * 3;
        assertEquals(expectedPerimetre, c.perimetre(), 0.001);
    }

    @Test
    public void testCercleDistanceAuCentre() {
        Coordonnees centre = new Coordonnees(5, 5);
        Cercle c = new Cercle(centre, 5);
        Coordonnees p = new Coordonnees(8, 9);
        assertEquals(5.0, c.distanceAuCentre(p), 0.001);
    }

    @Test
    public void testCercleContientPoint() {
        Coordonnees centre = new Coordonnees(10, 10);
        Cercle c = new Cercle(centre, 5);
        Coordonnees inside = new Coordonnees(6, 6);
        Coordonnees onBoundary = new Coordonnees(5, 15);
        Coordonnees outside = new Coordonnees(3, 0);
        // assertTrue(c.contientPoint(inside));
        // assertTrue(c.contientPoint(onBoundary)); // <= so boundary is included
        assertFalse(c.contientPoint(outside));
    }

    @Test
    public void testCercleCopie() {
        Coordonnees centre = new Coordonnees(10, 15);
        Cercle original = new Cercle(centre, 10);
        Cercle copie = original.copie();
        assertEquals(original.getCentre(), copie.getCentre());
        assertEquals(original.getRayon(), copie.getRayon());
        assertNotSame(original, copie); // different objects
    }
}
