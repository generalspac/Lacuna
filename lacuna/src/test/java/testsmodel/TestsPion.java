/**
 * @file TestsPion.java
 * @brief Tests unitaires pour la classe Pion.
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
 * @class TestsPion
 * @brief Ensemble de tests unitaires pour vérifier le comportement des pions.
 */
public class TestsPion {
    
    /**
     * @brief Constructeur de la classe de test.
     */
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

    // --------------------------------------------------------------------- //
    // Tests Pion
    // --------------------------------------------------------------------- //

    /**
     * @brief Vérifie que le constructeur initialise correctement le type et la position.
     */
    @Test
    public void testPionConstructor() {
        Coordonnees pos = new Coordonnees(2, 3);
        Pion p = new Pion(Pion.TypePion.OR, pos);
        assertEquals(Pion.TypePion.OR, p.getType());
        assertEquals(pos, p.getPosition());
    }

    /**
     * @brief Vérifie les setters de type et de position du pion.
     */
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

    /**
     * @brief Vérifie la conversion du type de pion en caractère.
     */
    @Test
    public void testPionTypePionToChar() {
        Coordonnees pos = new Coordonnees(0, 0);
        assertEquals('X', new Pion(Pion.TypePion.OR, pos).typePionToChar());
        assertEquals('A', new Pion(Pion.TypePion.ARGENT, pos).typePionToChar());
    }
}