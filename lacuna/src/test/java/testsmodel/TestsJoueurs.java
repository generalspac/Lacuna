/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package testsmodel;

import model.Coordonnees;
import model.Fleur;
import model.JoueurHumain;
import model.JoueurIa;
import model.Types;
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
public class TestsJoueurs {
    
    public TestsJoueurs() {
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
    
    // Tests pour Joueur
    @Test
    public void testJoueurConstructor() {
        JoueurHumain j = new JoueurHumain("Alice", Types.TypePion.OR);
        assertEquals("Alice", j.getNom());
        assertEquals(Types.TypePion.OR, j.getTypePion());
        assertNotNull(j.getFleursGagnees());
        assertEquals(0, j.getFleursGagnees().size());
    }

    @Test
    public void testJoueurAjouterFleur() {
        JoueurHumain j = new JoueurHumain("Bob", Types.TypePion.ARGENT);
        Fleur f1 = new Fleur(Types.TypeFleur.ROUGE, new Coordonnees(0, 0));
        Fleur f2 = new Fleur(Types.TypeFleur.BLEUE, new Coordonnees(1, 1));
        j.ajouterFleur(f1);
        j.ajouterFleur(f2);
        assertEquals(2, j.getFleursGagnees().size());
        assertEquals(f1, j.getFleursGagnees().get(0));
        assertEquals(f2, j.getFleursGagnees().get(1));
    }

    @Test
    public void testJoueurGetScoreTotal() {
        JoueurIa j = new JoueurIa("IA", Types.TypePion.OR);
        assertEquals(0, j.getScoreTotal());
        j.ajouterFleur(new Fleur(Types.TypeFleur.JAUNE, new Coordonnees(0, 0)));
        assertEquals(1, j.getScoreTotal());
    }

    @Test
    public void testJoueurNbFleursDeCouleur() {
        JoueurHumain j = new JoueurHumain("Charlie", Types.TypePion.ARGENT);
        j.ajouterFleur(new Fleur(Types.TypeFleur.ROUGE, new Coordonnees(0, 0)));
        j.ajouterFleur(new Fleur(Types.TypeFleur.ROUGE, new Coordonnees(1, 1)));
        j.ajouterFleur(new Fleur(Types.TypeFleur.BLEUE, new Coordonnees(2, 2)));
        assertEquals(2, j.nbFleursDeCouleur(Types.TypeFleur.ROUGE));
        assertEquals(1, j.nbFleursDeCouleur(Types.TypeFleur.BLEUE));
    }
}
