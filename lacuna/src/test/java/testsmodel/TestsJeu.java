package testsmodel;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Timeout;

import model.Cercle;
import model.Coordonnees;
import model.Fleur;
import model.Jeu;
import model.Joueur;
import model.Pion;
import testsmodel.*;

/**
 *
 * @author thsba
 */
public class TestsJeu {
    
    public TestsJeu() {
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
    
    // TestsJeu pour Jeu
    @Test
    public void init(){
        // TODO may be if we want to use the same board game for all tests
    }
    
    @Test
    public void testJeuConstructor() {
        Cercle cercle = new Cercle(new Coordonnees(400, 300), 100);
        Jeu jeu = new Jeu(800, 600, cercle);
        assertEquals(cercle, jeu.getCercleDeJeu());
        assertNotNull(jeu.getPions());
        assertNotNull(jeu.getFleurs());
        assertEquals(2, jeu.getNbJoueurs());
        assertNotNull(jeu.getJoueurs());
        assertEquals(0, jeu.getJoueurActuel().getScoreTotal());
    }

    @Test
    public void testJeuJoueurSuivant() {
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            Cercle cercle = new Cercle(new Coordonnees(400, 300), 100);
            Jeu jeu = new Jeu(800, 600, cercle);
            Joueur premier = jeu.getJoueurActuel();
            jeu.joueurSuivant();
            Joueur deuxieme = jeu.getJoueurActuel();
            assertNotEquals(premier, deuxieme);
            jeu.joueurSuivant();
            assertEquals(premier, jeu.getJoueurActuel());
        });
    }

    @Test
    public void testJeuInitFleurs() {
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
                Cercle cercle = new Cercle(new Coordonnees(200, 250), 50);
            Jeu jeu = new Jeu(800, 600, cercle);
            jeu.initFleurs();
            assertEquals(49, jeu.getFleurs().size()); // 7 types * 7 fleurs
            // Check that all types are present
            int[] counts = new int[Fleur.TypeFleur.values().length];
            for (Fleur f : jeu.getFleurs()) {
                counts[f.getType().ordinal()]++;
            }
            for (int count : counts) {
                assertEquals(7, count);
            }
        });
    }

    @Test
    public void testJeuInitPions() {
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {Cercle cercle = new Cercle(new Coordonnees(200, 250), 50);
                
            Jeu jeu = new Jeu(800, 600, cercle);
            jeu.initPions();
            assertEquals(12, jeu.getPions().size()); // 2 types * 6 pions
            int orCount = 0, argentCount = 0;
            for (Pion p : jeu.getPions()) {
                if (p.getType() == Pion.TypePion.OR) orCount++;
                else if (p.getType() == Pion.TypePion.ARGENT) argentCount++;
            }
            assertEquals(6, orCount);
            assertEquals(6, argentCount);
        });
    }
    
    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void placePionValide() {

        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {

            Jeu jeu = new Jeu(300, 300,
                    new Cercle(new Coordonnees(150, 150), 150));

            Pion pion = new Pion(Pion.TypePion.OR, null);

            boolean resultat = jeu.placePion(
                    pion,
                    new Coordonnees(150, 150)
            );

            assertTrue(resultat);
            assertEquals(new Coordonnees(150, 150), pion.getPosition());
        });
    }

    @Test
    void placePionInvalide() {

        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {

            Jeu jeu = new Jeu(300, 300,
                    new Cercle(new Coordonnees(150, 150), 100));

            Pion pion = new Pion(Pion.TypePion.OR, null);

            boolean resultat = jeu.placePion(
                    pion,
                    new Coordonnees(300, 300)
            );

            assertFalse(resultat);
            assertNull(pion.getPosition());
        });
    }

    @Test
    void mangerFleurs() {
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {

            Jeu jeu = new Jeu(300, 300,
                new Cercle(new Coordonnees(150, 150), 150));
            jeu.getFleurs().clear();

            Fleur f1 = new Fleur(
                    Fleur.TypeFleur.ROUGE,
                    new Coordonnees(100, 100)
            );

            Fleur f2 = new Fleur(
                    Fleur.TypeFleur.JAUNE,
                    new Coordonnees(200, 200)
            );

            jeu.getFleurs().add(f1);
            jeu.getFleurs().add(f2);

            Joueur joueur = jeu.getJoueurs()[0];

            assertTrue(jeu.mangerFleurs(joueur, f1, f2));

            assertFalse(jeu.getFleurs().contains(f1));
            assertFalse(jeu.getFleurs().contains(f2));

            assertEquals(2, joueur.getScoreTotal());
        });
    }

    @Test
    void joueurSuivant(){
        // TODO
         assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {

            Jeu jeu = new Jeu(300, 300,
            new Cercle(new Coordonnees(150, 150), 150));

            int courant = jeu.currentPlayerIndex;

            jeu.joueurSuivant();

            assertEquals(
                    (courant + 1) % jeu.getNbJoueurs(),
                    jeu.currentPlayerIndex
            );
        });
    }
    
    
}