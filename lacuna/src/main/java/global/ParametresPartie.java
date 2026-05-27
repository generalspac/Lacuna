package global;

import model.IA.NiveauxIA;
import model.Pion;

/**
 * --------------------- PARAMÈTRES PARTIE -------------------------- //
 *
 * Contient tous les paramètres nécessaires au lancement d'une partie.
 * Cette classe évite d'avoir trop de variables dispersées dans
 * InterfaceGraphique.
 */
public class ParametresPartie {

    // --------------------- MODE DE JEU -------------------------- //

    /**
     * Mode de jeu sélectionné.
     *
     * Exemples :
     * - "Humain vs Humain"
     * - "IA Facile"
     * - "IA Moyenne"
     * - "IA Difficile"
     * - "IA vs IA"
     * - "Réseau"
     */
    public String mode;

    // --------------------- NOMS DES JOUEURS -------------------------- //

    /**
     * Nom du joueur OR.
     */
    public String nomOr;

    /**
     * Nom du joueur ARGENT.
     */
    public String nomArgent;

    /**
     * Nom du joueur humain en mode Joueur vs IA.
     */
    public String nomHumain;

    // --------------------- NIVEAUX IA -------------------------- //

    /**
     * Niveau de l'IA OR.
     * Utilisé uniquement en mode IA vs IA.
     */
    public NiveauxIA niveauOr;

    /**
     * Niveau de l'IA ARGENT.
     * Utilisé uniquement en mode IA vs IA.
     */
    public NiveauxIA niveauArgent;

    // --------------------- JOUEUR QUI COMMENCE -------------------------- //

    /**
     * Joueur qui commence la partie.
     *
     * Peut être :
     * - OR
     * - ARGENT
     * - null si aléatoire
     */
    public Pion.TypePion joueurQuiCommence;

    // --------------------- CONSTRUCTEUR VIDE -------------------------- //

    public ParametresPartie() {
    }

    // --------------------- FABRIQUE : HUMAIN VS HUMAIN -------------------------- //

    /**
     * Crée des paramètres pour une partie Humain vs Humain.
     *
     * @param nomOr                Nom du joueur OR
     * @param nomArgent            Nom du joueur ARGENT
     * @param joueurQuiCommence    Joueur qui commence
     *
     * @return Paramètres configurés
     */
    public static ParametresPartie creerHumainVsHumain(
            String nomOr,
            String nomArgent,
            Pion.TypePion joueurQuiCommence) {

        ParametresPartie p = new ParametresPartie();

        p.mode = "Humain vs Humain";
        p.nomOr = nomOr;
        p.nomArgent = nomArgent;
        p.joueurQuiCommence = joueurQuiCommence;

        return p;
    }

    // --------------------- FABRIQUE : JOUEUR VS IA -------------------------- //

    /**
     * Crée des paramètres pour une partie Joueur vs IA.
     *
     * @param niveau               Niveau de l'IA
     * @param nomHumain            Nom du joueur humain
     * @param joueurQuiCommence    Joueur qui commence
     *
     * @return Paramètres configurés
     */
    public static ParametresPartie creerJeuIA(
            NiveauxIA niveau,
            String nomHumain,
            Pion.TypePion joueurQuiCommence) {

        ParametresPartie p = new ParametresPartie();

        p.nomHumain = nomHumain;
        p.joueurQuiCommence = joueurQuiCommence;

        switch (niveau) {

            case FACILE:
                p.mode = "IA Facile";
                break;

            case MOYEN:
                p.mode = "IA Moyenne";
                break;

            case DIFFICILE:
                p.mode = "IA Difficile";
                break;
        }

        return p;
    }

    // --------------------- FABRIQUE : IA VS IA -------------------------- //

    /**
     * Crée des paramètres pour une partie IA vs IA.
     *
     * @param niveauOr             Niveau de l'IA OR
     * @param niveauArgent         Niveau de l'IA ARGENT
     * @param joueurQuiCommence    Joueur qui commence
     *
     * @return Paramètres configurés
     */
    public static ParametresPartie creerIAvsIA(
            NiveauxIA niveauOr,
            NiveauxIA niveauArgent,
            Pion.TypePion joueurQuiCommence) {

        ParametresPartie p = new ParametresPartie();

        p.mode = "IA vs IA";
        p.niveauOr = niveauOr;
        p.niveauArgent = niveauArgent;
        p.joueurQuiCommence = joueurQuiCommence;

        return p;
    }

    // --------------------- FABRIQUE : RÉSEAU -------------------------- //

    /**
     * Crée des paramètres pour une partie réseau.
     *
     * @param nomOr        Nom du joueur hôte
     * @param nomArgent    Nom du joueur invité
     *
     * @return Paramètres configurés
     */
    public static ParametresPartie creerReseau(
            String nomOr,
            String nomArgent) {

        ParametresPartie p = new ParametresPartie();

        p.mode = "Réseau";
        p.nomOr = nomOr;
        p.nomArgent = nomArgent;

        return p;
    }

    // --------------------- FABRIQUE : CHARGEMENT -------------------------- //

    /**
     * Crée des paramètres pour le chargement
     * d'une partie depuis la base de données.
     *
     * @return Paramètres configurés
     */
    public static ParametresPartie creerChargement() {

        ParametresPartie p = new ParametresPartie();

        p.mode = "Humain vs Humain";
        p.nomOr = "";
        p.nomArgent = "";
        p.nomHumain = "";
        p.joueurQuiCommence = null;

        return p;
    }


    /*
    // --------------------- GETTERS/SETTERS -------------------------- //
    public Pion.TypePion getJoueurQuiCommence() {
        return joueurQuiCommence;
    }

    public String getMode() {
        return mode;
    }

    public NiveauxIA getNiveauArgent() {
        return niveauArgent;
    }

    public NiveauxIA getNiveauOr() {
        return niveauOr;
    }

    public String getNomArgent() {
        return nomArgent;
    }

    public String getNomHumain() {
        return nomHumain;
    }

    public String getNomOr() {
        return nomOr;
    }

    public void setJoueurQuiCommence(Pion.TypePion joueurQuiCommence) {
        this.joueurQuiCommence = joueurQuiCommence;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public void setNiveauArgent(NiveauxIA niveauArgent) {
        this.niveauArgent = niveauArgent;
    }

    public void setNiveauOr(NiveauxIA niveauOr) {
        this.niveauOr = niveauOr;
    }

    public void setNomArgent(String nomArgent) {
        this.nomArgent = nomArgent;
    }

    public void setNomHumain(String nomHumain) {
        this.nomHumain = nomHumain;
    }

    public void setNomOr(String nomOr) {
        this.nomOr = nomOr;
    }

    */
}