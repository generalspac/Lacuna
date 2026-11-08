package testsmodel;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;

import global.Configuration;
import model.Cercle;
import model.Coordonnees;
import model.DatabaseManager;
import model.Fleur;
import model.Jeu;
import model.Pion;
import model.IA.CoupIA;
import model.IA.EtatJeu;
import model.IA.IA;
import model.IA.NiveauxIA;

/**
 * Classe de tests et d'expérimentation des IA contre IA.
 * Génère des rapports statistiques, des bases de données de parties,
 * et permet d'inspecter les comportements des heuristiques.
 */
public class TestsIAVsIA {

    private static final int NB_PARTIES_PAR_SCENARIO = 50;
    private static final int NB_PARTIES_DIAGNOSTIC = 20;
    private static final int MAX_TOURS = 100;
    private static final int MODEL_WIDTH = 660;
    private static final int MODEL_HEIGHT = 440;
    private static final int NB_PARTIES_HEURISTIQUES = 6;
    private static final Path DOSSIER_RAPPORTS = Paths.get("target", "ia-reports");
    private static final Path DB_STATS = DOSSIER_RAPPORTS.resolve("rapport_stats_ia_50_parties.db");
    private static final Path DB_HEURISTIQUES = DOSSIER_RAPPORTS.resolve("parties_demo_heuristiques_ia.db");

    private enum ModeSeed {
        MEME_REPARTITION,
        REPARTITION_DIFFERENTE
    }

    private enum ModeDepart {
        MEME_JOUEUR,
        JOUEUR_ALTERNE
    }

    private static class ResultatPartie {
        Pion.TypePion gagnant;
        int tours;
        int fleursOr;
        int fleursArgent;
    }

    private static class ResultatScenario {
        int victoiresOr;
        int victoiresArgent;
        int egalites;
        int totalTours;
        int totalFleursOr;
        int totalFleursArgent;
    }

    private static class Duel {
        final NiveauxIA niveauOr;
        final NiveauxIA niveauArgent;

        Duel(NiveauxIA niveauOr, NiveauxIA niveauArgent) {
            this.niveauOr = niveauOr;
            this.niveauArgent = niveauArgent;
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration.setModeAffichage(0);
        Files.createDirectories(DOSSIER_RAPPORTS);

        TestsIAVsIA tests = new TestsIAVsIA();

        if (args.length > 0 && "heuristiques".equalsIgnoreCase(args[0])) {
            Files.deleteIfExists(DB_HEURISTIQUES);
            tests.genererPartiesHeuristiques();
            System.out.println("Base demos heuristiques IA : " + DB_HEURISTIQUES.toAbsolutePath());
            return;
        }

        if (args.length > 0 && "verifier-heuristiques".equalsIgnoreCase(args[0])) {
            tests.verifierPartiesHeuristiques();
            return;
        }

        if (args.length > 0 && "inspecter-heuristiques".equalsIgnoreCase(args[0])) {
            tests.inspecterChoixHeuristiques();
            return;
        }

        if (args.length > 0 && "diagnostic-niveaux".equalsIgnoreCase(args[0])) {
            tests.diagnostiquerNiveauxIA();
            return;
        }

        if (args.length > 0 && "scenario".equalsIgnoreCase(args[0])) {
            if (args.length < 3) {
                throw new IllegalArgumentException("Usage : scenario <duelIndex 0..2> <scenarioIndex 0..3>");
            }
            int duelIndex = Integer.parseInt(args[1]);
            int scenarioIndex = Integer.parseInt(args[2]);
            tests.genererScenarioStatistique(duelIndex, scenarioIndex);
            System.out.println("Base statistiques IA : " + DB_STATS.toAbsolutePath());
            return;
        }

        Files.deleteIfExists(DB_STATS);
        Files.deleteIfExists(DB_HEURISTIQUES);
        tests.genererRapportStatistique();
        tests.genererPartiesHeuristiques();

        System.out.println("Base statistiques IA : " + DB_STATS.toAbsolutePath());
        System.out.println("Base demos heuristiques IA : " + DB_HEURISTIQUES.toAbsolutePath());
    }

    private Duel[] duels() {
        return new Duel[] {
                new Duel(NiveauxIA.FACILE, NiveauxIA.MOYEN),
                new Duel(NiveauxIA.FACILE, NiveauxIA.DIFFICILE),
                new Duel(NiveauxIA.MOYEN, NiveauxIA.DIFFICILE)
        };
    }

    private String[] nomsScenarios() {
        return new String[] {
                "01_meme_repartition_meme_joueur",
                "02_meme_repartition_joueur_alterne",
                "03_repartition_differente_meme_joueur",
                "04_repartition_differente_joueur_alterne"
        };
    }

    private ModeSeed modeSeedPourScenario(int index) {
        return index < 2 ? ModeSeed.MEME_REPARTITION : ModeSeed.REPARTITION_DIFFERENTE;
    }

    private ModeDepart modeDepartPourScenario(int index) {
        return (index == 0 || index == 2) ? ModeDepart.MEME_JOUEUR : ModeDepart.JOUEUR_ALTERNE;
    }

    private void genererRapportStatistique() throws SQLException {
        DatabaseManager db = new DatabaseManager(DB_STATS);
        db.ouvrirConnexion();
        try {
            initialiserTablesRapport(db.getConnexion());

            Duel[] duels = duels();
            String[] scenarios = nomsScenarios();
            int duelIndex = 0;
            for (Duel duel : duels) {
                for (int scenarioIndex = 0; scenarioIndex < scenarios.length; scenarioIndex++) {
                    executerScenario(db.getConnexion(), duel, duelIndex, scenarios[scenarioIndex],
                            modeSeedPourScenario(scenarioIndex), modeDepartPourScenario(scenarioIndex));
                }
                duelIndex++;
            }
        } finally {
            db.fermerConnexion();
        }
    }

    private void genererScenarioStatistique(int duelIndex, int scenarioIndex) throws SQLException {
        Duel[] duels = duels();
        String[] scenarios = nomsScenarios();
        if (duelIndex < 0 || duelIndex >= duels.length || scenarioIndex < 0 || scenarioIndex >= scenarios.length) {
            throw new IllegalArgumentException("duelIndex doit être 0..2 et scenarioIndex 0..3");
        }

        DatabaseManager db = new DatabaseManager(DB_STATS);
        db.ouvrirConnexion();
        try {
            initialiserTablesRapport(db.getConnexion());
            executerScenario(db.getConnexion(), duels[duelIndex], duelIndex, scenarios[scenarioIndex],
                    modeSeedPourScenario(scenarioIndex), modeDepartPourScenario(scenarioIndex));
        } finally {
            db.fermerConnexion();
        }
    }

    private void initialiserTablesRapport(Connection connexion) throws SQLException {
        try (Statement stmt = connexion.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS scenarios_ia (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "scenario TEXT," +
                    "niveau_or TEXT," +
                    "niveau_argent TEXT," +
                    "nb_parties INTEGER," +
                    "mode_seed TEXT," +
                    "mode_depart TEXT," +
                    "seed_base INTEGER," +
                    "victoires_or INTEGER," +
                    "victoires_argent INTEGER," +
                    "egalites INTEGER," +
                    "moyenne_tours REAL," +
                    "moyenne_fleurs_or REAL," +
                    "moyenne_fleurs_argent REAL)");

            stmt.execute("CREATE TABLE IF NOT EXISTS parties_ia_detail (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "scenario TEXT," +
                    "numero_partie INTEGER," +
                    "niveau_or TEXT," +
                    "niveau_argent TEXT," +
                    "seed INTEGER," +
                    "joueur_depart TEXT," +
                    "gagnant TEXT," +
                    "tours INTEGER," +
                    "fleurs_or INTEGER," +
                    "fleurs_argent INTEGER)");
        }
    }

    private void executerScenario(Connection connexion, Duel duel, int duelIndex, String scenario,
            ModeSeed modeSeed, ModeDepart modeDepart) throws SQLException {

        int seedBase = 20260523 + duelIndex * 1000 + scenario.hashCode() % 997;
        ResultatScenario total = new ResultatScenario();

        for (int i = 0; i < NB_PARTIES_PAR_SCENARIO; i++) {
            int seed = (modeSeed == ModeSeed.MEME_REPARTITION) ? seedBase : seedBase + i;
            Pion.TypePion depart = (modeDepart == ModeDepart.MEME_JOUEUR || i % 2 == 0)
                    ? Pion.TypePion.OR
                    : Pion.TypePion.ARGENT;

            ResultatPartie resultat = jouerUnePartie(duel.niveauOr, duel.niveauArgent, depart, seed);
            enregistrerDetail(connexion, scenario, i + 1, duel, seed, depart, resultat);
            ajouter(total, resultat);
        }

        enregistrerScenario(connexion, scenario, duel, modeSeed, modeDepart, seedBase, total);
        afficherResume(scenario, duel, total);
    }

    private ResultatPartie jouerUnePartie(NiveauxIA niveauOr, NiveauxIA niveauArgent,
            Pion.TypePion joueurQuiCommence, int seedFleurs) {

        Jeu jeu = new Jeu(800, 800, new Cercle(new Coordonnees(400, 400), 300));
        jeu.setMarge(15);
        jeu.reinitialiserFleurs(seedFleurs);
        jeu.configurerIAContreIA(niveauOr, niveauArgent, joueurQuiCommence);

        IA iaOr = niveauOr.creerIA(0);
        IA iaArgent = niveauArgent.creerIA(1);

        jeu.jouerAvantageIA();

        int tours = 0;
        while (!jeu.isPartieTerminee() && tours < MAX_TOURS) {
            if (jeu.estSecondePhaseDisponible()) {
                jeu.attribuerFleursRestantes();
                break;
            }

            int indexJoueur = jeu.currentPlayerIndex;
            EtatJeu etat = EtatJeu.depuisJeu(jeu, indexJoueur);
            CoupIA coup = (indexJoueur == 0) ? iaOr.choisirCoup(etat) : iaArgent.choisirCoup(etat);
            jeu.jouerCoupIA(coup);
            tours++;
        }

        if (jeu.estSecondePhaseDisponible()) {
            jeu.attribuerFleursRestantes();
        }

        ResultatPartie resultat = new ResultatPartie();
        resultat.gagnant = jeu.getGagnant() == null ? null : jeu.getGagnant().getTypePion();
        resultat.tours = tours;
        resultat.fleursOr = jeu.getJoueurs()[0].getFleursGagnees().size();
        resultat.fleursArgent = jeu.getJoueurs()[1].getFleursGagnees().size();
        return resultat;
    }

    private void ajouter(ResultatScenario total, ResultatPartie resultat) {
        if (resultat.gagnant == null) {
            total.egalites++;
        } else if (resultat.gagnant == Pion.TypePion.OR) {
            total.victoiresOr++;
        } else {
            total.victoiresArgent++;
        }
        total.totalTours += resultat.tours;
        total.totalFleursOr += resultat.fleursOr;
        total.totalFleursArgent += resultat.fleursArgent;
    }

    private void enregistrerDetail(Connection connexion, String scenario, int numeroPartie, Duel duel,
            int seed, Pion.TypePion depart, ResultatPartie resultat) throws SQLException {
        try (PreparedStatement stmt = connexion.prepareStatement(
                "INSERT INTO parties_ia_detail (scenario, numero_partie, niveau_or, niveau_argent, seed," +
                " joueur_depart, gagnant, tours, fleurs_or, fleurs_argent) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            stmt.setString(1, scenario);
            stmt.setInt(2, numeroPartie);
            stmt.setString(3, duel.niveauOr.name());
            stmt.setString(4, duel.niveauArgent.name());
            stmt.setInt(5, seed);
            stmt.setString(6, depart.name());
            stmt.setString(7, resultat.gagnant == null ? "EGALITE" : resultat.gagnant.name());
            stmt.setInt(8, resultat.tours);
            stmt.setInt(9, resultat.fleursOr);
            stmt.setInt(10, resultat.fleursArgent);
            stmt.executeUpdate();
        }
    }

    private void enregistrerScenario(Connection connexion, String scenario, Duel duel, ModeSeed modeSeed,
            ModeDepart modeDepart, int seedBase, ResultatScenario total) throws SQLException {
        try (PreparedStatement stmt = connexion.prepareStatement(
                "INSERT INTO scenarios_ia (scenario, niveau_or, niveau_argent, nb_parties, mode_seed," +
                " mode_depart, seed_base, victoires_or, victoires_argent, egalites, moyenne_tours," +
                " moyenne_fleurs_or, moyenne_fleurs_argent) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            stmt.setString(1, scenario);
            stmt.setString(2, duel.niveauOr.name());
            stmt.setString(3, duel.niveauArgent.name());
            stmt.setInt(4, NB_PARTIES_PAR_SCENARIO);
            stmt.setString(5, modeSeed.name());
            stmt.setString(6, modeDepart.name());
            stmt.setInt(7, seedBase);
            stmt.setInt(8, total.victoiresOr);
            stmt.setInt(9, total.victoiresArgent);
            stmt.setInt(10, total.egalites);
            stmt.setDouble(11, total.totalTours / (double) NB_PARTIES_PAR_SCENARIO);
            stmt.setDouble(12, total.totalFleursOr / (double) NB_PARTIES_PAR_SCENARIO);
            stmt.setDouble(13, total.totalFleursArgent / (double) NB_PARTIES_PAR_SCENARIO);
            stmt.executeUpdate();
        }
    }

    private void afficherResume(String scenario, Duel duel, ResultatScenario total) {
        System.out.println("=== " + scenario + " : " + duel.niveauOr + " (OR) vs "
                + duel.niveauArgent + " (ARGENT) ===");
        System.out.println("Parties          : " + NB_PARTIES_PAR_SCENARIO);
        System.out.println("Victoires OR     : " + total.victoiresOr);
        System.out.println("Victoires ARGENT : " + total.victoiresArgent);
        System.out.println("Egalites         : " + total.egalites);
    }

    private void diagnostiquerNiveauxIA() {
        Duel[] duels = duels();
        for (int duelIndex = 0; duelIndex < duels.length; duelIndex++) {
            Duel duel = duels[duelIndex];
            ResultatScenario total = new ResultatScenario();
            long debut = System.nanoTime();

            for (int i = 0; i < NB_PARTIES_DIAGNOSTIC; i++) {
                Pion.TypePion depart = i % 2 == 0 ? Pion.TypePion.OR : Pion.TypePion.ARGENT;
                ResultatPartie resultat = jouerUnePartie(duel.niveauOr, duel.niveauArgent,
                        depart, 20260524 + duelIndex * 1000 + i);
                ajouter(total, resultat);
            }

            double secondes = (System.nanoTime() - debut) / 1_000_000_000.0;
            System.out.println("=== Diagnostic " + duel.niveauOr + " (OR) vs "
                    + duel.niveauArgent + " (ARGENT) ===");
            System.out.println("Parties          : " + NB_PARTIES_DIAGNOSTIC);
            System.out.println("Victoires OR     : " + total.victoiresOr);
            System.out.println("Victoires ARGENT : " + total.victoiresArgent);
            System.out.println("Egalites         : " + total.egalites);
            System.out.println("Moyenne tours    : " + (total.totalTours / (double) NB_PARTIES_DIAGNOSTIC));
            System.out.println("Duree secondes   : " + String.format(java.util.Locale.US, "%.2f", secondes));
        }
    }

    private void genererPartiesHeuristiques() throws SQLException {
        DatabaseManager db = new DatabaseManager(DB_HEURISTIQUES);
        db.ouvrirConnexion();
        try {
            db.sauvegarderPartie(partieMajorite(), "heuristique_01_OR_complete_majorite_rouge");
            db.sauvegarderPartie(partieBlocage(), "heuristique_02_OR_bloque_majorite_ARGENT_bleue");
            db.sauvegarderPartie(partiePhase2(), "heuristique_03_OR_prepare_majorite_verte_phase2");
            db.sauvegarderPartie(partieCouleurDejaPerdue(), "heuristique_04_OR_ignore_orange_deja_majoritaire_ARGENT");
            db.sauvegarderPartie(partieCouleurDejaGagnee(), "heuristique_05_OR_ignore_violette_deja_majoritaire_OR");
            db.sauvegarderPartie(partieBlocagePhase2(), "heuristique_06_ARGENT_bloque_majorite_phase2_rose");
        } finally {
            db.fermerConnexion();
        }
    }

    private void verifierPartiesHeuristiques() throws SQLException {
        DatabaseManager db = new DatabaseManager(DB_HEURISTIQUES);
        db.ouvrirConnexion();
        try {
            for (int id = 1; id <= NB_PARTIES_HEURISTIQUES; id++) {
                Jeu jeu = nouveauJeuGraphiqueCompatible();
                jeu.setMarge(15);
                db.chargerPartie(jeu, id);

                if (!jeu.isAgainstIA() || !jeu.estTourIA()) {
                    throw new IllegalStateException("Partie heuristique " + id + " : mode IA invalide.");
                }
                if (jeu.getPions().size() != 12) {
                    throw new IllegalStateException("Partie heuristique " + id + " : nombre de pions invalide.");
                }
                if (jeu.getFleurs().isEmpty()) {
                    throw new IllegalStateException("Partie heuristique " + id + " : aucune fleur plateau.");
                }

                System.out.println("OK heuristique " + id
                        + " | joueur=" + jeu.nomJoueurActuelPourAffichage()
                        + " | pions=" + jeu.getPions().size()
                        + " | fleurs_plateau=" + jeu.getFleurs().size()
                        + " | fleurs_OR=" + jeu.getJoueurs()[0].getFleursGagnees().size()
                        + " | fleurs_ARGENT=" + jeu.getJoueurs()[1].getFleursGagnees().size());
            }
        } finally {
            db.fermerConnexion();
        }
    }

    private void inspecterChoixHeuristiques() {
        Jeu[] scenes = {
                partieMajorite(),
                partieBlocage(),
                partiePhase2(),
                partieCouleurDejaPerdue(),
                partieCouleurDejaGagnee(),
                partieBlocagePhase2()
        };

        for (int i = 0; i < scenes.length; i++) {
            Map<String, Integer> choix = new LinkedHashMap<>();
            for (int essai = 0; essai < 20; essai++) {
                Jeu scene = copierScene(i);
                IA ia = scene.currentPlayerIndex == 0
                        ? NiveauxIA.MOYEN.creerIA(0)
                        : NiveauxIA.DIFFICILE.creerIA(1);
                CoupIA coup = ia.choisirCoup(EtatJeu.depuisJeu(scene, scene.currentPlayerIndex));
                String cle = coup == null ? "aucun"
                        : coup.fleur1.getType() + " " + coup.fleur1.getPosition().getX() + ","
                                + coup.fleur1.getPosition().getY()
                                + " -> " + coup.fleur2.getPosition().getX() + ","
                                + coup.fleur2.getPosition().getY();
                choix.put(cle, choix.getOrDefault(cle, 0) + 1);
            }
            System.out.println("Heuristique " + (i + 1) + " : " + choix);
        }
    }

    private Jeu copierScene(int index) {
        switch (index) {
            case 0:
                return partieMajorite();
            case 1:
                return partieBlocage();
            case 2:
                return partiePhase2();
            case 3:
                return partieCouleurDejaPerdue();
            case 4:
                return partieCouleurDejaGagnee();
            case 5:
                return partieBlocagePhase2();
            default:
                throw new IllegalArgumentException("Scene inconnue : " + index);
        }
    }

    private Jeu baseHeuristique(Pion.TypePion joueurCourant) {
        Jeu jeu = nouveauJeuGraphiqueCompatible();
        jeu.setMarge(15);
        jeu.configurerIAContreIA(NiveauxIA.MOYEN, NiveauxIA.DIFFICILE, joueurCourant);
        jeu.getFleurs().clear();
        jeu.getPions().clear();
        jeu.currentPlayerIndex = joueurCourant == Pion.TypePion.OR ? 0 : 1;
        jeu.setAvantageInitialApplique(true);
        jeu.setEnPhaseSelectionAvantage(false);
        return jeu;
    }

    private Jeu nouveauJeuGraphiqueCompatible() {
        return new Jeu(MODEL_WIDTH, MODEL_HEIGHT,
                new Cercle(new Coordonnees(MODEL_WIDTH / 2, MODEL_HEIGHT / 2),
                        Math.min(MODEL_WIDTH, MODEL_HEIGHT) / 2));
    }

    private Jeu partieMajorite() {
        Jeu jeu = baseHeuristique(Pion.TypePion.OR);
        donnerFleurs(jeu, 0, Fleur.TypeFleur.ROUGE, 2);
        ajouterFleurs(jeu,
                fleur(Fleur.TypeFleur.ROUGE, 210, 220), fleur(Fleur.TypeFleur.ROUGE, 350, 220),
                fleur(Fleur.TypeFleur.BLEUE, 220, 330), fleur(Fleur.TypeFleur.BLEUE, 410, 330),
                fleur(Fleur.TypeFleur.JAUNE, 470, 180), fleur(Fleur.TypeFleur.JAUNE, 500, 300),
                fleur(Fleur.TypeFleur.VERTE, 260, 115), fleur(Fleur.TypeFleur.VERTE, 430, 115));
        ajouterPionsLibres(jeu);
        return jeu;
    }

    private Jeu partieBlocage() {
        Jeu jeu = baseHeuristique(Pion.TypePion.OR);
        donnerFleurs(jeu, 1, Fleur.TypeFleur.BLEUE, 3);
        ajouterFleurs(jeu,
                fleur(Fleur.TypeFleur.BLEUE, 210, 250), fleur(Fleur.TypeFleur.BLEUE, 390, 250),
                fleur(Fleur.TypeFleur.ROUGE, 210, 130), fleur(Fleur.TypeFleur.ROUGE, 430, 130),
                fleur(Fleur.TypeFleur.ORANGE, 265, 350),
                fleur(Fleur.TypeFleur.ROSE, 505, 235),
                fleur(Fleur.TypeFleur.JAUNE, 525, 320),
                fleur(Fleur.TypeFleur.VERTE, 455, 340));
        ajouterPionsLibres(jeu);
        return jeu;
    }

    private Jeu partiePhase2() {
        Jeu jeu = baseHeuristique(Pion.TypePion.OR);
        donnerFleur(jeu, 0, Fleur.TypeFleur.VERTE);
        ajouterFleurs(jeu,
                fleur(Fleur.TypeFleur.VERTE, 240, 220), fleur(Fleur.TypeFleur.VERTE, 420, 220),
                fleur(Fleur.TypeFleur.VERTE, 330, 275),
                fleur(Fleur.TypeFleur.ROUGE, 210, 120), fleur(Fleur.TypeFleur.ROUGE, 450, 120),
                fleur(Fleur.TypeFleur.BLEUE, 210, 335), fleur(Fleur.TypeFleur.BLEUE, 455, 335),
                fleur(Fleur.TypeFleur.JAUNE, 500, 250));
        ajouterPionsAvantDernierCoup(jeu, Pion.TypePion.OR);
        ajouterPionsLibres(jeu);
        return jeu;
    }

    private Jeu partieCouleurDejaPerdue() {
        Jeu jeu = baseHeuristique(Pion.TypePion.OR);
        donnerFleurs(jeu, 1, Fleur.TypeFleur.ORANGE, 4);
        donnerFleurs(jeu, 0, Fleur.TypeFleur.ROUGE, 2);
        ajouterFleurs(jeu,
                fleur(Fleur.TypeFleur.ORANGE, 290, 220), fleur(Fleur.TypeFleur.ORANGE, 365, 220),
                fleur(Fleur.TypeFleur.ROUGE, 210, 250), fleur(Fleur.TypeFleur.ROUGE, 430, 250),
                fleur(Fleur.TypeFleur.BLEUE, 220, 130), fleur(Fleur.TypeFleur.BLEUE, 450, 130),
                fleur(Fleur.TypeFleur.JAUNE, 220, 340), fleur(Fleur.TypeFleur.JAUNE, 450, 340));
        ajouterPionsLibres(jeu);
        return jeu;
    }

    private Jeu partieCouleurDejaGagnee() {
        Jeu jeu = baseHeuristique(Pion.TypePion.OR);
        donnerFleurs(jeu, 0, Fleur.TypeFleur.VIOLETTE, 4);
        donnerFleurs(jeu, 0, Fleur.TypeFleur.JAUNE, 2);
        ajouterFleurs(jeu,
                fleur(Fleur.TypeFleur.VIOLETTE, 290, 220), fleur(Fleur.TypeFleur.VIOLETTE, 365, 220),
                fleur(Fleur.TypeFleur.JAUNE, 215, 250), fleur(Fleur.TypeFleur.JAUNE, 430, 250),
                fleur(Fleur.TypeFleur.ROUGE, 215, 130), fleur(Fleur.TypeFleur.ROUGE, 445, 130),
                fleur(Fleur.TypeFleur.BLEUE, 220, 340), fleur(Fleur.TypeFleur.BLEUE, 445, 340));
        ajouterPionsLibres(jeu);
        return jeu;
    }

    private Jeu partieBlocagePhase2() {
        Jeu jeu = baseHeuristique(Pion.TypePion.ARGENT);
        donnerFleurs(jeu, 0, Fleur.TypeFleur.ROSE, 3);
        ajouterFleurs(jeu,
                fleur(Fleur.TypeFleur.ROSE, 240, 220), fleur(Fleur.TypeFleur.ROSE, 420, 220),
                fleur(Fleur.TypeFleur.ROSE, 330, 275),
                fleur(Fleur.TypeFleur.VERTE, 210, 120), fleur(Fleur.TypeFleur.VERTE, 450, 120),
                fleur(Fleur.TypeFleur.BLEUE, 210, 335), fleur(Fleur.TypeFleur.BLEUE, 455, 335),
                fleur(Fleur.TypeFleur.JAUNE, 500, 250));
        ajouterPionsAvantDernierCoup(jeu, Pion.TypePion.ARGENT);
        ajouterPionsLibres(jeu);
        return jeu;
    }

    private Fleur fleur(Fleur.TypeFleur type, int x, int y) {
        return new Fleur(type, new Coordonnees(x, y));
    }

    private Pion pion(Pion.TypePion type, int x, int y) {
        return new Pion(type, new Coordonnees(x, y));
    }

    private void ajouterFleurs(Jeu jeu, Fleur... fleurs) {
        for (Fleur fleur : fleurs) {
            jeu.getFleurs().add(fleur);
        }
    }

    private void ajouterPions(Jeu jeu, Pion... pions) {
        for (Pion pion : pions) {
            jeu.getPions().add(pion);
        }
    }

    private void ajouterPionsLibres(Jeu jeu) {
        for (Pion.TypePion type : Pion.TypePion.values()) {
            int total = 0;
            for (Pion pion : jeu.getPions()) {
                if (pion.getType() == type) {
                    total++;
                }
            }
            while (total < 6) {
                jeu.getPions().add(new Pion(type, null));
                total++;
            }
        }
    }

    private void ajouterPionsAvantDernierCoup(Jeu jeu, Pion.TypePion joueurLibre) {
        Pion.TypePion autreJoueur = joueurLibre == Pion.TypePion.OR ? Pion.TypePion.ARGENT : Pion.TypePion.OR;

        ajouterPions(jeu,
                pion(joueurLibre, 150, 180),
                pion(joueurLibre, 190, 90),
                pion(joueurLibre, 215, 360),
                pion(joueurLibre, 500, 120),
                pion(joueurLibre, 505, 315));

        ajouterPions(jeu,
                pion(autreJoueur, 160, 270),
                pion(autreJoueur, 230, 65),
                pion(autreJoueur, 450, 75),
                pion(autreJoueur, 525, 220),
                pion(autreJoueur, 455, 365),
                pion(autreJoueur, 330, 350));
    }

    private void donnerFleurs(Jeu jeu, int indexJoueur, Fleur.TypeFleur type, int nombre) {
        for (int i = 0; i < nombre; i++) {
            donnerFleur(jeu, indexJoueur, type, i);
        }
    }

    private void donnerFleur(Jeu jeu, int indexJoueur, Fleur.TypeFleur type) {
        donnerFleur(jeu, indexJoueur, type, jeu.getJoueurs()[indexJoueur].getFleursGagnees().size());
    }

    private void donnerFleur(Jeu jeu, int indexJoueur, Fleur.TypeFleur type, int decalage) {
        jeu.getJoueurs()[indexJoueur].ajouterFleur(
                new Fleur(type, new Coordonnees(80 + decalage * 12, 80 + indexJoueur * 20)));
    }
}