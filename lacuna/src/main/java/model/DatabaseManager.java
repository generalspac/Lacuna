package model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import model.IA.NiveauxIA;

/*
Gère la connexion à la base de données sqlite du jeu
*/
public class DatabaseManager {

    private static final String NOM_DOSSIER_DONNEES = ".lacuna";
    private static final String NOM_BASE_DONNEES = "lacuna.db";

    private Connection connexion;

    /* ouvre la connexion à la base de données sqlite
     crée le fichier lacuna.db s'il n'existe pas encore
     */
    public void ouvrirConnexion() throws SQLException {
        // chargement explicite du driver SQLite nécessaire hors environnement Maven
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver SQLite introuvable : " + e.getMessage());
        }
        connexion = DriverManager.getConnection(urlBaseDonnees());
        initialiserBase();
    }

    private String urlBaseDonnees() throws SQLException {
        String dossierUtilisateur = System.getProperty("user.home");
        Path dossierDonnees = (dossierUtilisateur == null || dossierUtilisateur.isBlank())
                ? Paths.get(NOM_DOSSIER_DONNEES)
                : Paths.get(dossierUtilisateur, NOM_DOSSIER_DONNEES);

        try {
            Files.createDirectories(dossierDonnees);
        } catch (Exception e) {
            throw new SQLException("Impossible de créer le dossier de sauvegarde : " + e.getMessage(), e);
        }

        return "jdbc:sqlite:" + dossierDonnees.resolve(NOM_BASE_DONNEES).toAbsolutePath();
    }

    /* crée les tables nécessaires au jeu si elles n'existent pas encore */
    public void initialiserBase() throws SQLException {
        Statement stmt = connexion.createStatement();

        // table principale d'une partie
        stmt.execute(
            "CREATE TABLE IF NOT EXISTS parties (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "nom TEXT," +
            "date TEXT," +
            "joueur_actuel INTEGER," +
            "avantage_applique INTEGER," +
            "en_phase_selection_avantage INTEGER," +
            "against_ia INTEGER DEFAULT 0," +
            "mode_jeu TEXT DEFAULT 'HUMAIN_VS_HUMAIN'," +
            "niveau_ia TEXT," +
            "niveau_ia_or TEXT," +
            "niveau_ia_argent TEXT," +
            "nom_humain_ia TEXT," +
            "type_humain_ia TEXT," +
            "depart_ia TEXT," +
            "depart_ia_vs_ia TEXT)"
        );
        // migration pour les bases existantes créées avant l'ajout de cette colonne
        try { stmt.execute("ALTER TABLE parties ADD COLUMN against_ia INTEGER DEFAULT 0"); } catch (SQLException ignored) {}
        try { stmt.execute("ALTER TABLE parties ADD COLUMN mode_jeu TEXT DEFAULT 'HUMAIN_VS_HUMAIN'"); } catch (SQLException ignored) {}
        try { stmt.execute("ALTER TABLE parties ADD COLUMN niveau_ia TEXT"); } catch (SQLException ignored) {}
        try { stmt.execute("ALTER TABLE parties ADD COLUMN niveau_ia_or TEXT"); } catch (SQLException ignored) {}
        try { stmt.execute("ALTER TABLE parties ADD COLUMN niveau_ia_argent TEXT"); } catch (SQLException ignored) {}
        try { stmt.execute("ALTER TABLE parties ADD COLUMN nom_humain_ia TEXT"); } catch (SQLException ignored) {}
        try { stmt.execute("ALTER TABLE parties ADD COLUMN type_humain_ia TEXT"); } catch (SQLException ignored) {}
        try { stmt.execute("ALTER TABLE parties ADD COLUMN depart_ia TEXT"); } catch (SQLException ignored) {}
        try { stmt.execute("ALTER TABLE parties ADD COLUMN depart_ia_vs_ia TEXT"); } catch (SQLException ignored) {}

        // pions sur le plateau (position nullable si le pion n'est pas encore placé)
        stmt.execute(
            "CREATE TABLE IF NOT EXISTS pions (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "id_partie INTEGER," +
            "type TEXT," +
            "position_x INTEGER," +
            "position_y INTEGER)"
        );

        // fleurs posées sur le plateau
        stmt.execute(
            "CREATE TABLE IF NOT EXISTS fleurs_plateau (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "id_partie INTEGER," +
            "type_fleur TEXT," +
            "position_x INTEGER," +
            "position_y INTEGER)"
        );

        // fleurs collectées par un joueur (index_joueur : 0 ou 1, correspond à currentPlayerIndex)
        stmt.execute(
            "CREATE TABLE IF NOT EXISTS fleurs_collectees (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "id_partie INTEGER," +
            "index_joueur INTEGER," +
            "type_fleur TEXT," +
            "position_x INTEGER," +
            "position_y INTEGER)"
        );

        stmt.execute(
            "CREATE TABLE IF NOT EXISTS resultats_ia (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "date TEXT," +
            "niveauOr TEXT," +
            "niveauArgent TEXT," +
            "nbParties INTEGER," +
            "victoiresOr INTEGER," +
            "victoiresArgent INTEGER," +
            "egalites INTEGER)"
        );
        try { stmt.execute("ALTER TABLE resultats_ia ADD COLUMN date TEXT"); } catch (SQLException ignored) {}
        try { stmt.execute("ALTER TABLE resultats_ia ADD COLUMN nbParties INTEGER"); } catch (SQLException ignored) {}

        stmt.close();
    }

    /* sauvegarde l'état complet d'une partie dans les 4 tables */
    public void sauvegarderPartie(Jeu jeu, String nom) throws SQLException {
        String date = new java.util.Date().toString();

        // insertion dans parties et récupération de l'id généré
        PreparedStatement stmtPartie = connexion.prepareStatement(
            "INSERT INTO parties (nom, date, joueur_actuel, avantage_applique, en_phase_selection_avantage, against_ia," +
            " mode_jeu, niveau_ia, niveau_ia_or, niveau_ia_argent, nom_humain_ia, type_humain_ia, depart_ia, depart_ia_vs_ia)" +
            " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            Statement.RETURN_GENERATED_KEYS
        );
        stmtPartie.setString(1, nom);
        stmtPartie.setString(2, date);
        stmtPartie.setInt(3, jeu.currentPlayerIndex);
        stmtPartie.setInt(4, jeu.isAvantageInitialApplique() ? 1 : 0);
        stmtPartie.setInt(5, jeu.isEnPhaseSelectionAvantage() ? 1 : 0);
        stmtPartie.setInt(6, jeu.isAgainstIA() ? 1 : 0);
        stmtPartie.setString(7, modeJeu(jeu));
        stmtPartie.setString(8, nomNiveau(jeu.niveauIA));
        stmtPartie.setString(9, nomNiveau(jeu.niveauIAOr));
        stmtPartie.setString(10, nomNiveau(jeu.niveauIAArgent));
        stmtPartie.setString(11, jeu.nomHumainIA);
        stmtPartie.setString(12, nomTypePion(jeu.typeHumainIA));
        stmtPartie.setString(13, nomTypePion(jeu.joueurDepartIA));
        stmtPartie.setString(14, nomTypePion(jeu.joueurDepartIAContreIA));
        stmtPartie.executeUpdate();

        // next() positionne le curseur sur la première ligne avant toute lecture
        ResultSet cles = stmtPartie.getGeneratedKeys();
        if (!cles.next()) {
            throw new SQLException("Impossible de récupérer l'id généré pour la partie.");
        }
        int idPartie = cles.getInt(1);
        cles.close();
        stmtPartie.close();

        // insertion des pions (position NULL si le pion n'est pas encore placé)
        PreparedStatement stmtPion = connexion.prepareStatement(
            "INSERT INTO pions (id_partie, type, position_x, position_y) VALUES (?, ?, ?, ?)"
        );
        for (Pion p : jeu.getPions()) {
            stmtPion.setInt(1, idPartie);
            stmtPion.setString(2, p.getType().name());
            if (p.getPosition() == null) {
                stmtPion.setNull(3, java.sql.Types.INTEGER);
                stmtPion.setNull(4, java.sql.Types.INTEGER);
            } else {
                stmtPion.setInt(3, p.getPosition().getX());
                stmtPion.setInt(4, p.getPosition().getY());
            }
            stmtPion.addBatch();
        }
        stmtPion.executeBatch();
        stmtPion.close();

        // insertion des fleurs encore présentes sur le plateau
        PreparedStatement stmtFleur = connexion.prepareStatement(
            "INSERT INTO fleurs_plateau (id_partie, type_fleur, position_x, position_y) VALUES (?, ?, ?, ?)"
        );
        for (Fleur f : jeu.getFleurs()) {
            stmtFleur.setInt(1, idPartie);
            stmtFleur.setString(2, f.getType().name());
            stmtFleur.setInt(3, f.getPosition().getX());
            stmtFleur.setInt(4, f.getPosition().getY());
            stmtFleur.addBatch();
        }
        stmtFleur.executeBatch();
        stmtFleur.close();

        // insertion des fleurs collectées par chaque joueur (index 0 ou 1)
        PreparedStatement stmtCollectee = connexion.prepareStatement(
            "INSERT INTO fleurs_collectees (id_partie, index_joueur, type_fleur, position_x, position_y)" +
            " VALUES (?, ?, ?, ?, ?)"
        );
        Joueur[] joueurs = jeu.getJoueurs();
        for (int i = 0; i < joueurs.length; i++) {
            for (Fleur f : joueurs[i].getFleursGagnees()) {
                stmtCollectee.setInt(1, idPartie);
                stmtCollectee.setInt(2, i);
                stmtCollectee.setString(3, f.getType().name());
                stmtCollectee.setInt(4, f.getPosition().getX());
                stmtCollectee.setInt(5, f.getPosition().getY());
                stmtCollectee.addBatch();
            }
        }
        stmtCollectee.executeBatch();
        stmtCollectee.close();
    }

    /* recharge l'état complet d'une partie depuis les 4 tables pour un id donné */
    public void chargerPartie(Jeu jeu, int idPartie) throws SQLException {
        // restauration des données principales de la partie
        PreparedStatement stmtPartie = connexion.prepareStatement(
            "SELECT joueur_actuel, avantage_applique, en_phase_selection_avantage, against_ia," +
            " mode_jeu, niveau_ia, niveau_ia_or, niveau_ia_argent, nom_humain_ia, type_humain_ia, depart_ia, depart_ia_vs_ia" +
            " FROM parties WHERE id = ?"
        );
        stmtPartie.setInt(1, idPartie);
        ResultSet rsPartie = stmtPartie.executeQuery();
        if (rsPartie.next()) {
            String mode = rsPartie.getString("mode_jeu");
            if ("IA_VS_IA".equals(mode)) {
                jeu.configurerIAContreIA(
                        niveauDepuisTexte(rsPartie.getString("niveau_ia_or")),
                        niveauDepuisTexte(rsPartie.getString("niveau_ia_argent")),
                        typeDepuisTexte(rsPartie.getString("depart_ia_vs_ia")));
            } else if ("HUMAIN_VS_IA".equals(mode) || rsPartie.getInt("against_ia") == 1) {
                GestionIA.activerIA(
                        jeu,
                        niveauDepuisTexte(rsPartie.getString("niveau_ia")),
                        rsPartie.getString("nom_humain_ia"),
                        typeDepuisTexte(rsPartie.getString("depart_ia")),
                        typeDepuisTexte(rsPartie.getString("type_humain_ia")));
            } else {
                jeu.configurerHumainVsHumain("", "", null);
            }
            jeu.currentPlayerIndex = rsPartie.getInt("joueur_actuel");
            boolean avantage = rsPartie.getInt("avantage_applique") == 1;
            boolean phaseSelection = rsPartie.getInt("en_phase_selection_avantage") == 1;

            // avantageInitialApplique et enPhaseSelectionAvantage sont privés sans setter
            // on utilise la réflexion pour les restaurer
            try {
                java.lang.reflect.Field fAvantage = Jeu.class.getDeclaredField("avantageInitialApplique");
                fAvantage.setAccessible(true);
                fAvantage.set(jeu, avantage);

                java.lang.reflect.Field fPhase = Jeu.class.getDeclaredField("enPhaseSelectionAvantage");
                fPhase.setAccessible(true);
                fPhase.set(jeu, phaseSelection);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new SQLException("Impossible de restaurer les champs privés de Jeu : " + e.getMessage());
            }
        }
        rsPartie.close();
        stmtPartie.close();

        // restauration des pions : on vide la liste puis on la reconstruit depuis la base
        jeu.getPions().clear();
        PreparedStatement stmtPion = connexion.prepareStatement(
            "SELECT type, position_x, position_y FROM pions WHERE id_partie = ?"
        );
        stmtPion.setInt(1, idPartie);
        ResultSet rsPion = stmtPion.executeQuery();
        while (rsPion.next()) {
            Pion.TypePion type = Pion.TypePion.valueOf(rsPion.getString("type"));
            // on lit position_x en premier, wasNull() détecte si la valeur est NULL en base
            int px = rsPion.getInt("position_x");
            boolean positionNulle = rsPion.wasNull();
            int py = rsPion.getInt("position_y");
            Coordonnees pos = positionNulle ? null : new Coordonnees(px, py);
            jeu.getPions().add(new Pion(type, pos));
        }
        rsPion.close();
        stmtPion.close();
        completerPionsManquants(jeu);

        // restauration des fleurs sur le plateau
        jeu.getFleurs().clear();
        PreparedStatement stmtFleur = connexion.prepareStatement(
            "SELECT type_fleur, position_x, position_y FROM fleurs_plateau WHERE id_partie = ?"
        );
        stmtFleur.setInt(1, idPartie);
        ResultSet rsFleur = stmtFleur.executeQuery();
        while (rsFleur.next()) {
            Fleur.TypeFleur type = Fleur.TypeFleur.valueOf(rsFleur.getString("type_fleur"));
            Coordonnees pos = new Coordonnees(rsFleur.getInt("position_x"), rsFleur.getInt("position_y"));
            jeu.getFleurs().add(new Fleur(type, pos));
        }
        rsFleur.close();
        stmtFleur.close();

        // redistribution des fleurs collectées aux bons joueurs via leur index
        Joueur[] joueurs = jeu.getJoueurs();
        for (Joueur j : joueurs) {
            j.getFleursGagnees().clear();
        }
        PreparedStatement stmtCollectee = connexion.prepareStatement(
            "SELECT index_joueur, type_fleur, position_x, position_y FROM fleurs_collectees WHERE id_partie = ?"
        );
        stmtCollectee.setInt(1, idPartie);
        ResultSet rsCollectee = stmtCollectee.executeQuery();
        while (rsCollectee.next()) {
            int indexJoueur = rsCollectee.getInt("index_joueur");
            Fleur.TypeFleur type = Fleur.TypeFleur.valueOf(rsCollectee.getString("type_fleur"));
            Coordonnees pos = new Coordonnees(rsCollectee.getInt("position_x"), rsCollectee.getInt("position_y"));
            joueurs[indexJoueur].ajouterFleur(new Fleur(type, pos));
        }
        rsCollectee.close();
        stmtCollectee.close();
    }

    private String modeJeu(Jeu jeu) {
        if (jeu.iaContreIA) {
            return "IA_VS_IA";
        }
        if (jeu.isAgainstIA()) {
            return "HUMAIN_VS_IA";
        }
        return "HUMAIN_VS_HUMAIN";
    }

    private String nomNiveau(NiveauxIA niveau) {
        return niveau == null ? null : niveau.name();
    }

    private String nomTypePion(Pion.TypePion type) {
        return type == null ? null : type.name();
    }

    private NiveauxIA niveauDepuisTexte(String texte) {
        if (texte == null || texte.trim().isEmpty()) {
            return NiveauxIA.FACILE;
        }
        return NiveauxIA.valueOf(texte);
    }

    private Pion.TypePion typeDepuisTexte(String texte) {
        if (texte == null || texte.trim().isEmpty()) {
            return null;
        }
        return Pion.TypePion.valueOf(texte);
    }

    private void completerPionsManquants(Jeu jeu) {
        for (Pion.TypePion type : Pion.TypePion.values()) {
            int total = 0;
            for (Pion pion : jeu.getPions()) {
                if (pion.getType() == type) {
                    total++;
                }
            }
            while (total < ConstantesJeu.MAX_PIONS_PAR_JOUEUR) {
                jeu.getPions().add(new Pion(type, null));
                total++;
            }
        }
    }

    /* retourne la liste des parties sauvegardées avec leur id, nom et date */
    public List<String> listerParties() throws SQLException {
        List<String> parties = new ArrayList<>();
        Statement stmt = connexion.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT id, nom, date FROM parties ORDER BY id DESC");
        while (rs.next()) {
            parties.add(rs.getInt("id") + " - " + rs.getString("nom") + " (" + rs.getString("date") + ")");
        }
        rs.close();
        stmt.close();
        return parties;
    }

    /* Ferme la connexion à la base de données si elle est ouverte */
    public void fermerConnexion() throws SQLException {
        if (connexion != null && !connexion.isClosed()) {
            connexion.close();
        }
    }

    public Connection getConnexion() {
        return connexion;
    }

    // sauvegarde les résultat d'un duel ai vs ai dans la table resultats_ia de la DB 
    public void sauvegarderResultatsIA(String niveauOr, String niveauArgent, int victoiresOr,
            int victoiresArgent, int egalites) throws SQLException {
        sauvegarderResultatsIA(niveauOr, niveauArgent, victoiresOr + victoiresArgent + egalites,
                victoiresOr, victoiresArgent, egalites);
    }

    public void sauvegarderResultatsIA(String niveauOr, String niveauArgent, int nbParties,
            int victoiresOr, int victoiresArgent, int egalites) throws SQLException {

        PreparedStatement stmt = connexion.prepareStatement(
            "INSERT INTO resultats_ia (date, niveauOr, niveauArgent, nbParties, victoiresOr, victoiresArgent, egalites)" +
            " VALUES (?, ?, ?, ?, ?, ?, ?)"
        );

        stmt.setString(1, new java.util.Date().toString());
        stmt.setString(2, niveauOr);
        stmt.setString(3, niveauArgent);
        stmt.setInt(4, nbParties);
        stmt.setInt(5, victoiresOr);
        stmt.setInt(6, victoiresArgent);
        stmt.setInt(7, egalites);

        stmt.executeUpdate();
        stmt.close();
    }
}
