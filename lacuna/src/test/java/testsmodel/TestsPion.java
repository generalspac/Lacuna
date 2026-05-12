/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package testsmodel;

import model.Coordonnees;
import model.Pion;
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
public class TestsPion {
    
    public TestsPion() {
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
    
    

    // Tests pour Pion
    @Test
    public void testPionConstructor() {
        Coordonnees pos = new Coordonnees(2, 3);
        Pion p = new Pion(Pion.TypePion.OR, pos);
        assertEquals(Pion.TypePion.OR, p.getType());
        assertEquals(pos, p.getPosition());
    }

    @Test
    public void testPionSetters() {
        Coordonnees pos1 = new Coordonnees(0, 0);
        Coordonnees pos2 = new Coordonnees(5, 5);
        Pion p = new Pion(Pion.TypePion.ARGENT, pos1);
        p.setType(Pion.TypePion.OR);
        p.setPosition(pos2);
        assertEquals(Pion.TypePion.OR, p.getType());
        assertEquals(pos2, p.getPosition());
    }

    @Test
    public void testPionTypePionToChar() {
        Coordonnees pos = new Coordonnees(0, 0);
        assertEquals('X', new Pion(Pion.TypePion.OR, pos).typePionToChar());
        assertEquals('A', new Pion(Pion.TypePion.ARGENT, pos).typePionToChar());
    }
}
