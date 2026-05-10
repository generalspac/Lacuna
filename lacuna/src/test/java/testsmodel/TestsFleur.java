/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package testsmodel;

import model.Coordonnees;
import model.Fleur;
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
public class TestsFleur {
    
    public TestsFleur() {
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
    
    

    // Tests pour Fleur
    @Test
    public void testFleurConstructor() {
        Coordonnees pos = new Coordonnees(1, 1);
        Fleur f = new Fleur(Types.TypeFleur.ROUGE, pos);
        assertEquals(Types.TypeFleur.ROUGE, f.getType());
        assertEquals(pos, f.getPosition());
    }

    @Test
    public void testFleurTypeFleurToChar() {
        Coordonnees pos = new Coordonnees(0, 0);
        assertEquals('R', new Fleur(Types.TypeFleur.ROUGE, pos).typeFleurToChar());
        assertEquals('B', new Fleur(Types.TypeFleur.BLEUE, pos).typeFleurToChar());
        assertEquals('J', new Fleur(Types.TypeFleur.JAUNE, pos).typeFleurToChar());
        assertEquals('V', new Fleur(Types.TypeFleur.VERTE, pos).typeFleurToChar());
        assertEquals('O', new Fleur(Types.TypeFleur.ORANGE, pos).typeFleurToChar());
        assertEquals('P', new Fleur(Types.TypeFleur.VIOLETTE, pos).typeFleurToChar());
        assertEquals('S', new Fleur(Types.TypeFleur.ROSE, pos).typeFleurToChar());
    }
}
