package testsmodel;

import model.Coordonnees;
import model.Fleur;
import model.Pion;
import model.JoueurHumain;
import model.JoueurIa;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Classe de tests unitaires pour les classes de joueurs.
 * Vérifie le bon fonctionnement des joueurs humains et IA,
 * notamment la gestion des fleurs, du score et des statistiques.
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

    /**
     * Vérifie l'initialisation correcte d'un joueur humain.
     */
    @Test
    public void testJoueurConstructor() {
        JoueurHumain j = new JoueurHumain("Alice", Pion.TypePion.OR);
        assertEquals("Alice", j.getNom());
        assertEquals(Pion.TypePion.OR, j.getTypePion());
        assertNotNull(j.getFleursGagnees());
        assertEquals(0, j.getFleursGagnees().size());
    }

    /**
     * Vérifie l'ajout de fleurs dans la liste du joueur.
     */
    @Test
    public void testJoueurAjouterFleur() {
        JoueurHumain j = new JoueurHumain("Bob", Pion.TypePion.ARGENT);
        Fleur f1 = new Fleur(Fleur.TypeFleur.ROUGE, new Coordonnees(0, 0));
        Fleur f2 = new Fleur(Fleur.TypeFleur.BLEUE, new Coordonnees(1, 1));
        j.ajouterFleur(f1);
        j.ajouterFleur(f2);
        assertEquals(2, j.getFleursGagnees().size());
        assertEquals(f1, j.getFleursGagnees().get(0));
        assertEquals(f2, j.getFleursGagnees().get(1));
    }

    /**
     * Vérifie le calcul du score total d'un joueur IA.
     */
    @Test
    public void testJoueurGetScoreTotal() {
        JoueurIa j = new JoueurIa("IA", Pion.TypePion.OR);
        assertEquals(0, j.getScoreTotal());
        j.ajouterFleur(new Fleur(Fleur.TypeFleur.JAUNE, new Coordonnees(0, 0)));
        assertEquals(1, j.getScoreTotal());
    }

    /**
     * Vérifie le comptage des fleurs par couleur.
     */
    @Test
    public void testJoueurNbFleursDeCouleur() {
        JoueurHumain j = new JoueurHumain("Charlie", Pion.TypePion.ARGENT);
        j.ajouterFleur(new Fleur(Fleur.TypeFleur.ROUGE, new Coordonnees(0, 0)));
        j.ajouterFleur(new Fleur(Fleur.TypeFleur.ROUGE, new Coordonnees(1, 1)));
        j.ajouterFleur(new Fleur(Fleur.TypeFleur.BLEUE, new Coordonnees(2, 2)));
        assertEquals(2, j.nbFleursDeCouleur(Fleur.TypeFleur.ROUGE));
        assertEquals(1, j.nbFleursDeCouleur(Fleur.TypeFleur.BLEUE));
    }
}