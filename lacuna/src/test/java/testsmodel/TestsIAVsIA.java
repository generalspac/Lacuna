package testsmodel;

import java.sql.SQLException;

import global.Configuration;
import model.Cercle;
import model.Coordonnees;
import model.DatabaseManager;
import model.Jeu;
import model.Pion;
import model.IA.CoupIA;
import model.IA.EtatJeu;
import model.IA.IA;
import model.IA.NiveauxIA;


public class TestsIAVsIA {

    public TestsIAVsIA() {
    }

    // joue une partie compléte ia vs ia et retourne le type du pion qui a gagné ou null en cas d'égalité 
    private Pion.TypePion jouerUnePartie(NiveauxIA niveauOr, NiveauxIA niveauArgent, Pion.TypePion joueurQuiCommence) {
        
        // Config the game
        Jeu jeu = new Jeu(800, 800, new Cercle(new Coordonnees(400, 400), 300));
        jeu.setMarge(15);
        jeu.configurerIAContreIA(niveauOr, niveauArgent, joueurQuiCommence);

        IA iaOr = niveauOr.creerIA(0);
        IA iaArgent = niveauArgent.creerIA(1);

        jeu.jouerAvantageIA();

        int tours = 0;
        while (!jeu.isPartieTerminee() && tours < 100) {
            int indexJoueur = jeu.currentPlayerIndex;

            EtatJeu etat = EtatJeu.depuisJeu(jeu, indexJoueur);

            CoupIA coup;
            if (indexJoueur == 0) {
                coup = iaOr.choisirCoup(etat);
            } else {
                coup = iaArgent.choisirCoup(etat);
            }

            jeu.jouerCoupIA(coup);
            tours++;
        }

     
        if (jeu.estSecondePhaseDisponible()) {
            jeu.attribuerFleursRestantes();
        }

        if (jeu.getGagnant() == null) {
            return null;
        }
        return jeu.getGagnant().getTypePion();
    }

    private void tester(NiveauxIA niveauOr, NiveauxIA niveauArgent, int nbParties) {
        int victoiresOr = 0;
        int victoiresArgent = 0;
        int egalites = 0;

        // alternance du joueur qui commence pour etre juste :)
        for (int i = 0; i < nbParties; i++) {
            Pion.TypePion joueurQuiCommence = (i % 2 == 0) ? Pion.TypePion.OR : Pion.TypePion.ARGENT;
            Pion.TypePion gagnant = jouerUnePartie(niveauOr, niveauArgent, joueurQuiCommence);

            if (gagnant == null) {
                egalites++;
            } else if (gagnant == Pion.TypePion.OR) {
                victoiresOr++;
            } else {
                victoiresArgent++;
            }
        }

        DatabaseManager db = new DatabaseManager();
        try {
            db.ouvrirConnexion();
            db.sauvegarderResultatsIA(niveauOr.name(), niveauArgent.name(), nbParties,
                    victoiresOr, victoiresArgent, egalites);
        } catch (SQLException e) {
            System.out.println("Erreur lors de la sauvegarde des résultats : " + e.getMessage());
        } finally {
            try {
                db.fermerConnexion();
            } catch (SQLException e) {
                System.out.println("Erreur lors de la fermeture de la connexion : " + e.getMessage());
            }
        }

        // afficher un résumé
        System.out.println("=== Résultats " + niveauOr.name() + " (OR) vs " + niveauArgent.name() + " (ARGENT) ===");
        System.out.println("Parties          : " + nbParties);
        System.out.println("Victoires OR     : " + victoiresOr);
        System.out.println("Victoires ARGENT : " + victoiresArgent);
        System.out.println("Égalités         : " + egalites);
    }

    public static void main(String[] args) {
        Configuration.setModeAffichage(0);

        int nbParties = 100;
        if (args.length > 0) {
            nbParties = Integer.parseInt(args[0]);
        }

        TestsIAVsIA tests = new TestsIAVsIA();
        
        tests.tester(NiveauxIA.FACILE, NiveauxIA.MOYEN, nbParties);
        tests.tester(NiveauxIA.MOYEN, NiveauxIA.DIFFICILE, nbParties);
        tests.tester(NiveauxIA.FACILE, NiveauxIA.DIFFICILE, nbParties);
       
    }
}
