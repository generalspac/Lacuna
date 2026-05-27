package testsmodel;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Timeout;

import model.Cercle;
import model.ConstantesJeu;
import model.Coordonnees;
import model.Fleur;
import model.Jeu;
import model.Joueur;
import model.Pion;

/**
 * Classe de tests unitaires pour la logique principale du jeu {@link Jeu}.
 * Vérifie le bon fonctionnement des règles, du placement, des fleurs,
 * des pions, du système de score et des conditions de victoire.
 */
public class TestsJeu {

    // -------------------- SETUP ------------------------------ //

    /**
     * Crée une instance standard de jeu utilisée pour les tests.
     * @return une instance de {@link Jeu} initialisée avec des dimensions par défaut
     */
    private Jeu jeuStandard() {
        return new Jeu(800, 600, new Cercle(new Coordonnees(400, 300), 250));
    }

    @BeforeAll
    public static void setUpClass() {}

    @AfterAll
    public static void tearDownClass() {}

    @BeforeEach
    public void setUp() {}

    @AfterEach
    public void tearDown() {}

    // ------------------- CONSTRUCTEUR ------------------------ //

    /**
     * Vérifie que le constructeur initialise correctement le jeu.
     */
    @Test
    public void testJeuConstructor() {
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            Cercle cercle = new Cercle(new Coordonnees(400, 300), 200);
            Jeu jeu = new Jeu(800, 600, cercle);
            assertEquals(cercle, jeu.getCercleDeJeu());
            assertNotNull(jeu.getPions());
            assertNotNull(jeu.getFleurs());
            assertEquals(2, jeu.getNbJoueurs());
            assertNotNull(jeu.getJoueurs());
            assertEquals(0, jeu.getJoueurActuel().getScoreTotal());
            assertNotNull(jeu.getHistorique());
        });
    }

    /**
     * Vérifie que la phase d'avantage initial est active au démarrage.
     */
    @Test
    public void testConstructeurPhaseAvantageActif() {
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            Jeu jeu = jeuStandard();
            assertTrue(jeu.isEnPhaseSelectionAvantage());
            assertFalse(jeu.isAvantageInitialApplique());
        });
    }

    // ------------------- INITIALISATION ---------------------- //

    /**
     * Vérifie l'initialisation correcte des fleurs.
     */
    @Test
    public void testInitFleurs() {
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            Jeu jeu = jeuStandard();
            jeu.initFleurs();
            assertEquals(ConstantesJeu.NB_FLEURS, jeu.getFleurs().size());
            int[] counts = new int[Fleur.TypeFleur.values().length];
            for (Fleur f : jeu.getFleurs()) {
                counts[f.getType().ordinal()]++;
            }
            for (int count : counts) {
                assertEquals(ConstantesJeu.NB_FLEURS_PAR_COULEUR, count);
            }
        });
    }

    /**
     * Vérifie l'initialisation correcte des pions.
     */
    @Test
    public void testInitPions() {
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            Jeu jeu = jeuStandard();
            jeu.initPions();
            assertEquals(12, jeu.getPions().size());
            int orCount = 0, argentCount = 0;
            for (Pion p : jeu.getPions()) {
                if (p.getType() == Pion.TypePion.OR) orCount++;
                else if (p.getType() == Pion.TypePion.ARGENT) argentCount++;
            }
            assertEquals(6, orCount);
            assertEquals(6, argentCount);
        });
    }

    /**
     * Vérifie qu'aucun pion n'est placé après initialisation.
     */
    @Test
    public void testInitPionsTousNonPlaces() {
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            Jeu jeu = jeuStandard();
            jeu.initPions();
            for (Pion p : jeu.getPions()) {
                assertNull(p.getPosition(), "Le pion " + p.getType() + " devrait être non placé après init.");
            }
        });
    }

    /**
     * Vérifie la réinitialisation complète du jeu.
     */
    @Test
    public void testResetReinitialise() {
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            Jeu jeu = jeuStandard();
            jeu.setAvantageInitialApplique(true);
            jeu.setEnPhaseSelectionAvantage(false);

            jeu.reset();

            assertEquals(ConstantesJeu.NB_FLEURS, jeu.getFleurs().size());
            assertEquals(12, jeu.getPions().size());
            assertTrue(jeu.isEnPhaseSelectionAvantage());
            assertFalse(jeu.isAvantageInitialApplique());
            assertTrue(jeu.getHistorique().getUndoStack().isEmpty());
        });
    }

    // -------------------- JOUEURS ---------------------------- //

    /**
     * Vérifie le passage au joueur suivant.
     */
    @Test
    public void testJoueurSuivant() {
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            Jeu jeu = jeuStandard();
            Joueur premier = jeu.getJoueurActuel();
            jeu.joueurSuivant();
            assertNotEquals(premier, jeu.getJoueurActuel());
            jeu.joueurSuivant();
            assertEquals(premier, jeu.getJoueurActuel());
        });
    }

    /**
     * Vérifie le retour au joueur précédent.
     */
    @Test
    public void testJoueurPrecedent() {
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            Jeu jeu = jeuStandard();
            Joueur premier = jeu.getJoueurActuel();
            jeu.joueurSuivant();
            jeu.joueurPrecedent();
            assertEquals(premier, jeu.getJoueurActuel());
        });
    }

    /**
     * Vérifie la symétrie entre joueur suivant et précédent.
     */
    @Test
    public void testJoueurSuivantPrecedentSymetrie() {
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            Jeu jeu = jeuStandard();
            int indexDepart = jeu.currentPlayerIndex;
            jeu.joueurSuivant();
            jeu.joueurPrecedent();
            assertEquals(indexDepart, jeu.currentPlayerIndex);
        });
    }

    // -------------------- PLACEMENT -------------------------- //

    /**
     * Vérifie le placement valide d'un pion.
     */
    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void placePionValide() {
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            Jeu jeu = new Jeu(300, 300, new Cercle(new Coordonnees(150, 150), 150));
            Pion pion = new Pion(Pion.TypePion.OR, null);
            boolean resultat = jeu.placePion(pion, new Coordonnees(150, 150));
            assertTrue(resultat);
            assertEquals(new Coordonnees(150, 150), pion.getPosition());
        });
    }

    /**
     * Vérifie qu'un pion hors du cercle ne peut pas être placé.
     */
    @Test
    void placePionHorsDuCercle() {
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            Jeu jeu = new Jeu(300, 300, new Cercle(new Coordonnees(150, 150), 100));
            Pion pion = new Pion(Pion.TypePion.OR, null);
            boolean resultat = jeu.placePion(pion, new Coordonnees(300, 300));
            assertFalse(resultat);
            assertNull(pion.getPosition());
        });
    }

    /**
     * Vérifie que deux pions trop proches ne peuvent pas être placés.
     */
    @Test
    void placePionTropProcheAutrePion() {
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            Jeu jeu = new Jeu(300, 300, new Cercle(new Coordonnees(150, 150), 150));
            Pion p1 = new Pion(Pion.TypePion.OR, null);
            Pion p2 = new Pion(Pion.TypePion.ARGENT, null);
            jeu.placePion(p1, new Coordonnees(150, 150));
            boolean resultat = jeu.placePion(p2, new Coordonnees(152, 152));
            assertFalse(resultat);
            assertNull(p2.getPosition());
        });
    }

    /**
     * Vérifie la récupération d'un pion libre.
     */
    @Test
    void getPionLibreRetournePionNonPlace() {
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            Jeu jeu = jeuStandard();
            Pion libre = jeu.getPionLibre(Pion.TypePion.OR);
            assertNotNull(libre);
            assertNull(libre.getPosition());
            assertEquals(Pion.TypePion.OR, libre.getType());
        });
    }

    // -------------------- FLEURS ----------------------------- //

    /**
     * Vérifie la capture de fleurs de même type.
     */
    @Test
    void mangerFleursMemeType() {
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            Jeu jeu = new Jeu(300, 300, new Cercle(new Coordonnees(150, 150), 150));
            jeu.getFleurs().clear();

            Fleur f1 = new Fleur(Fleur.TypeFleur.ROUGE, new Coordonnees(100, 100));
            Fleur f2 = new Fleur(Fleur.TypeFleur.ROUGE, new Coordonnees(200, 200));
            jeu.getFleurs().add(f1);
            jeu.getFleurs().add(f2);

            Joueur joueur = jeu.getJoueurs()[0];
            assertTrue(jeu.mangerFleurs(joueur, f1, f2));
            assertFalse(jeu.getFleurs().contains(f1));
            assertFalse(jeu.getFleurs().contains(f2));
            assertEquals(2, joueur.getScoreTotal());
        });
    }

    /**
     * Vérifie l'échec de capture si couleurs différentes.
     */
    @Test
    void mangerFleursCouleursDifferentesEchoue() {
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            Jeu jeu = new Jeu(300, 300, new Cercle(new Coordonnees(150, 150), 150));
            jeu.getFleurs().clear();

            Fleur f1 = new Fleur(Fleur.TypeFleur.ROUGE, new Coordonnees(100, 100));
            Fleur f2 = new Fleur(Fleur.TypeFleur.JAUNE, new Coordonnees(200, 200));
            jeu.getFleurs().add(f1);
            jeu.getFleurs().add(f2);

            Joueur joueur = jeu.getJoueurs()[0];
            assertFalse(jeu.mangerFleurs(joueur, f1, f2));
            assertTrue(jeu.getFleurs().contains(f1));
            assertTrue(jeu.getFleurs().contains(f2));
            assertEquals(0, joueur.getScoreTotal());
        });
    }

    /**
     * Vérifie l'échec de capture si fleurs absentes du plateau.
     */
    @Test
    void mangerFleursAbsentesEchoue() {
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            Jeu jeu = new Jeu(300, 300, new Cercle(new Coordonnees(150, 150), 150));
            jeu.getFleurs().clear();

            Fleur f1 = new Fleur(Fleur.TypeFleur.ROUGE, new Coordonnees(100, 100));
            Fleur f2 = new Fleur(Fleur.TypeFleur.ROUGE, new Coordonnees(200, 200));

            Joueur joueur = jeu.getJoueurs()[0];
            assertFalse(jeu.mangerFleurs(joueur, f1, f2));
            assertEquals(0, joueur.getScoreTotal());
        });
    }

    // ------------------ AVANTAGE INITIAL --------------------- //

    /**
     * Vérifie l'application de l'avantage initial.
     */
    @Test
    void appliquerAvantageAvecFleur() {
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            Jeu jeu = jeuStandard();
            assertTrue(jeu.isEnPhaseSelectionAvantage());

            Fleur fleurCible = jeu.getFleurs().get(0);
            Joueur joueurDeDepart = jeu.getJoueurActuel();

            jeu.appliquerAvantageAvecFleur(fleurCible);

            assertTrue(jeu.isAvantageInitialApplique());
            assertFalse(jeu.isEnPhaseSelectionAvantage());
            assertFalse(jeu.getFleurs().contains(fleurCible));
            assertTrue(joueurDeDepart.getFleursGagnees().contains(fleurCible));
        });
    }

    /**
     * Vérifie qu'un second avantage est ignoré.
     */
    @Test
    void appliquerAvantageDeuxFoisIgnore() {
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            Jeu jeu = jeuStandard();
            Fleur f1 = jeu.getFleurs().get(0);
            Fleur f2 = jeu.getFleurs().get(1);

            jeu.appliquerAvantageAvecFleur(f1);
            int taille = jeu.getFleurs().size();

            jeu.appliquerAvantageAvecFleur(f2);

            assertEquals(taille, jeu.getFleurs().size());
        });
    }

    // ----------------- FLEURS CONNECTEES ----------------------- //

    /**
     * Vérifie la connexion entre deux fleurs de même type.
     */
    @Test
    void fleursConnecteesMemeTypeSansObstacle() {
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            Jeu jeu = new Jeu(600, 600, new Cercle(new Coordonnees(300, 300), 300));
            jeu.getFleurs().clear();
            jeu.getPions().clear();

            Fleur f1 = new Fleur(Fleur.TypeFleur.ROUGE, new Coordonnees(250, 300));
            Fleur f2 = new Fleur(Fleur.TypeFleur.ROUGE, new Coordonnees(350, 300));
            jeu.getFleurs().add(f1);
            jeu.getFleurs().add(f2);

            assertTrue(jeu.fleursConnectees(f1, f2));
        });
    }

    /**
     * Vérifie l'échec de connexion entre fleurs de couleurs différentes.
     */
    @Test
    void fleursConnecteesCouleursDifferentesEchoue() {
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            Jeu jeu = new Jeu(600, 600, new Cercle(new Coordonnees(300, 300), 300));
            jeu.getFleurs().clear();
            jeu.getPions().clear();

            Fleur f1 = new Fleur(Fleur.TypeFleur.ROUGE, new Coordonnees(250, 300));
            Fleur f2 = new Fleur(Fleur.TypeFleur.BLEUE, new Coordonnees(350, 300));
            jeu.getFleurs().add(f1);
            jeu.getFleurs().add(f2);

            assertFalse(jeu.fleursConnectees(f1, f2));
        });
    }

    /**
     * Vérifie la gestion des fleurs nulles.
     */
    @Test
    void fleursConnecteesNullRetourneFaux() {
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            Jeu jeu = jeuStandard();
            assertFalse(jeu.fleursConnectees(null, null));
            assertFalse(jeu.fleursConnectees(new Fleur(Fleur.TypeFleur.ROUGE, new Coordonnees(100, 100)), null));
        });
    }

    // ----------------- COUPS POSSIBLES ----------------------- //

    /**
     * Vérifie qu'il n'y a aucun coup possible sans paires.
     */
    @Test
    void coupsPossiblesVideSiAucunePaireMemeType() {
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            Jeu jeu = new Jeu(600, 600, new Cercle(new Coordonnees(300, 300), 300));
            jeu.getFleurs().clear();
            jeu.getPions().clear();

            for (Fleur.TypeFleur t : Fleur.TypeFleur.values()) {
                jeu.getFleurs().add(new Fleur(t, new Coordonnees(100 + t.ordinal() * 40, 300)));
            }

            List<Coordonnees> coups = jeu.coupsPossibles();
            assertTrue(coups.isEmpty());
        });
    }

    /**
     * Vérifie qu'un coup connecté existe si paire de fleurs.
     */
    @Test
    void existeCoupConnecteAvecPaireMemeType() {
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            Jeu jeu = new Jeu(600, 600, new Cercle(new Coordonnees(300, 300), 300));
            jeu.getFleurs().clear();
            jeu.getPions().clear();

            Fleur f1 = new Fleur(Fleur.TypeFleur.ROUGE, new Coordonnees(280, 300));
            Fleur f2 = new Fleur(Fleur.TypeFleur.ROUGE, new Coordonnees(320, 300));
            jeu.getFleurs().add(f1);
            jeu.getFleurs().add(f2);

            assertTrue(jeu.existeCoupConnecte());
        });
    }

    // -------------------- SCORE / GAGNANT -------------------- //

    /**
     * Vérifie le calcul du gagnant par score.
     */
    @Test
    void getGagnantParScore() {
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            Jeu jeu = jeuStandard();
            jeu.getFleurs().clear();

            Joueur j0 = jeu.getJoueurs()[0];
            Joueur j1 = jeu.getJoueurs()[1];

            j0.ajouterFleur(new Fleur(Fleur.TypeFleur.ROUGE, new Coordonnees(0, 0)));
            j0.ajouterFleur(new Fleur(Fleur.TypeFleur.ROUGE, new Coordonnees(1, 0)));
            j0.ajouterFleur(new Fleur(Fleur.TypeFleur.ROUGE, new Coordonnees(2, 0)));

            j1.ajouterFleur(new Fleur(Fleur.TypeFleur.BLEUE, new Coordonnees(3, 0)));

            Joueur gagnant = jeu.getGagnant();
            assertEquals(j0, gagnant);
        });
    }

    /**
     * Vérifie le cas d'égalité.
     */
    @Test
    void getGagnantNulEnCasEgalite() {
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            Jeu jeu = jeuStandard();
            Joueur gagnant = jeu.getGagnant();
            assertNull(gagnant);
        });
    }

    // ----------------- ATTRIBUTION FLEURS RESTANTES ---------------- //

    /**
     * Vérifie l'attribution des fleurs restantes en fin de partie.
     */
    @Test
    void attribuerFleursRestantesVideLePlateau() {
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            Jeu jeu = new Jeu(600, 600, new Cercle(new Coordonnees(300, 300), 300));
            jeu.getFleurs().clear();
            jeu.getPions().clear();

            Pion p1 = new Pion(Pion.TypePion.OR, new Coordonnees(280, 300));
            Pion p2 = new Pion(Pion.TypePion.ARGENT, new Coordonnees(320, 300));
            jeu.getPions().add(p1);
            jeu.getPions().add(p2);

            jeu.getFleurs().add(new Fleur(Fleur.TypeFleur.ROUGE, new Coordonnees(282, 300)));

            jeu.attribuerFleursRestantes();

            assertTrue(jeu.getFleurs().isEmpty(), "Toutes les fleurs doivent être attribuées.");
        });
    }
}