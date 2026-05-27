/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package testsmodel;

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
public class TestsCoordonnees {
    
    public TestsCoordonnees() {
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
     * Test du constructeur de la classe Coordonnees.
     * Vérifie que les valeurs x et y sont correctement initialisées.
     */
    @Test
    public void testCoordonneesConstructor() {
        Coordonnees c = new Coordonnees(5, 10);
        assertEquals(5, c.getX());
        assertEquals(10, c.getY());
    }

    /**
     * Test des setters de la classe Coordonnees.
     * Vérifie que les valeurs x et y sont correctement modifiées.
     */
    @Test
    public void testCoordonneesSetters() {
        Coordonnees c = new Coordonnees(0, 0);
        c.setX(3);
        c.setY(7);
        assertEquals(3, c.getX());
        assertEquals(7, c.getY());
    }

    /**
     * Test de la méthode equals de Coordonnees.
     * Vérifie l'égalité entre objets et les cas négatifs.
     */
    @Test
    public void testCoordonneesEquals() {
        Coordonnees c1 = new Coordonnees(1, 2);
        Coordonnees c2 = new Coordonnees(1, 2);
        Coordonnees c3 = new Coordonnees(2, 1);
        assertTrue(c1.equals(c2));
        assertFalse(c1.equals(c3));
        assertFalse(c1.equals(null));
        assertFalse(c1.equals("string"));
    }

    /**
     * Test de la méthode hashCode de Coordonnees.
     * Vérifie la cohérence entre objets égaux.
     */
    @Test
    public void testCoordonneesHashCode() {
        Coordonnees c1 = new Coordonnees(1, 2);
        Coordonnees c2 = new Coordonnees(1, 2);
        assertEquals(c1.hashCode(), c2.hashCode());
    }

    /**
     * Test de la méthode compareTo de Coordonnees.
     * Vérifie l'ordre naturel des coordonnées.
     */
    @Test
    public void testCoordonneesCompareTo() {
        Coordonnees c1 = new Coordonnees(1, 2);
        Coordonnees c2 = new Coordonnees(1, 2);
        Coordonnees c3 = new Coordonnees(2, 1);
        Coordonnees c4 = new Coordonnees(1, 3);
        assertEquals(0, c1.compareTo(c2));
        assertTrue(c1.compareTo(c3) < 0);
        assertTrue(c1.compareTo(c4) < 0);
        assertTrue(c3.compareTo(c1) > 0);
    }
}