package global;

import model.IA.NiveauxIA;
import model.Pion;

/**
 * ---------------------- PARAMÈTRES PARTIE ------------------------ //
 *
 * Contient tous les paramètres nécessaires
 * au lancement d'une partie.
 *
 * Cette classe permet de centraliser :
 * - le mode de jeu
 * - les noms des joueurs
 * - les niveaux des IA
 * - le joueur qui commence
 */
public class ParametresPartie {

    // ---------------------- MODE DE JEU ------------------------ //

    /**
     * Mode de jeu sélectionné.
     *
     * Exemples :
     * - Humain vs Humain
     * - IA Facile
     * - IA Moyenne
     * - IA Difficile
     * - IA vs IA
     * - Réseau
     */
    public String mode;

    // ---------------------- NOMS DES JOUEURS ------------------------ //

    /**
     * Nom du joueur OR.
     */
    public String nomOr;

    /**
     * Nom du joueur ARGENT.
     */
    public String nomArgent;

    /**
     * Nom du joueur humain
     * en mode Joueur vs IA.
     */
    public String nomHumain;

    // ---------------------- NIVEAUX IA ------------------------ //

    /**
     * Niveau de l'IA OR.
     *
     * Utilisé uniquement
     * en mode IA vs IA.
     */
    public NiveauxIA niveauOr;

    /**
     * Niveau de l'IA ARGENT.
     *
     * Utilisé uniquement
     * en mode IA vs IA.
     */
    public NiveauxIA niveauArgent;

    // ---------------------- JOUEUR QUI COMMENCE ------------------------ //

    /**
     * Joueur qui commence la partie.
     *
     * Valeurs possibles :
     * - OR
     * - ARGENT
     * - null si aléatoire
     */
    public Pion.TypePion joueurQuiCommence;

    // ---------------------- CONSTRUCTEUR ------------------------ //

    /**
     * Constructeur vide.
     */
    public ParametresPartie() {
    }

    // ---------------------- FABRIQUE : HUMAIN VS HUMAIN ------------------------ //

    /**
     * Crée des paramètres pour une partie
     * Humain vs Humain.
     *
     * @param nomOr
     * Nom du joueur OR
     *
     * @param nomArgent
     * Nom du joueur ARGENT
     *
     * @param joueurQuiCommence
     * Joueur qui commence
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

    // ---------------------- FABRIQUE : JOUEUR VS IA ------------------------ //

    /**
     * Crée des paramètres pour une partie
     * Joueur vs IA.
     *
     * @param niveau
     * Niveau de l'IA
     *
     * @param nomHumain
     * Nom du joueur humain
     *
     * @param joueurQuiCommence
     * Joueur qui commence
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

    // ---------------------- FABRIQUE : IA VS IA ------------------------ //

    /**
     * Crée des paramètres pour une partie
     * IA vs IA.
     *
     * @param niveauOr
     * Niveau de l'IA OR
     *
     * @param niveauArgent
     * Niveau de l'IA ARGENT
     *
     * @param joueurQuiCommence
     * Joueur qui commence
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

    // ---------------------- FABRIQUE : RÉSEAU ------------------------ //

    /**
     * Crée des paramètres pour une partie réseau.
     *
     * @param nomOr
     * Nom du joueur hôte
     *
     * @param nomArgent
     * Nom du joueur invité
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

    // ---------------------- FABRIQUE : CHARGEMENT ------------------------ //

    /**
     * Crée des paramètres pour le chargement
     * d'une partie sauvegardée.
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

    // ---------------------- GETTERS ------------------------ //

    /**
     * Retourne le joueur qui commence.
     *
     * @return Joueur qui commence
     */
    public Pion.TypePion getJoueurQuiCommence() {
        return joueurQuiCommence;
    }

    /**
     * Retourne le mode de jeu.
     *
     * @return Mode de jeu
     */
    public String getMode() {
        return mode;
    }

    /**
     * Retourne le niveau de l'IA ARGENT.
     *
     * @return Niveau IA ARGENT
     */
    public NiveauxIA getNiveauArgent() {
        return niveauArgent;
    }

    /**
     * Retourne le niveau de l'IA OR.
     *
     * @return Niveau IA OR
     */
    public NiveauxIA getNiveauOr() {
        return niveauOr;
    }

    /**
     * Retourne le nom du joueur ARGENT.
     *
     * @return Nom joueur ARGENT
     */
    public String getNomArgent() {
        return nomArgent;
    }

    /**
     * Retourne le nom du joueur humain.
     *
     * @return Nom joueur humain
     */
    public String getNomHumain() {
        return nomHumain;
    }

    /**
     * Retourne le nom du joueur OR.
     *
     * @return Nom joueur OR
     */
    public String getNomOr() {
        return nomOr;
    }

    // ---------------------- SETTERS ------------------------ //

    /**
     * Définit le joueur qui commence.
     *
     * @param joueurQuiCommence
     * Nouveau joueur de départ
     */
    public void setJoueurQuiCommence(
            Pion.TypePion joueurQuiCommence) {

        this.joueurQuiCommence = joueurQuiCommence;
    }

    /**
     * Définit le mode de jeu.
     *
     * @param mode
     * Nouveau mode de jeu
     */
    public void setMode(String mode) {
        this.mode = mode;
    }

    /**
     * Définit le niveau de l'IA ARGENT.
     *
     * @param niveauArgent
     * Nouveau niveau IA ARGENT
     */
    public void setNiveauArgent(NiveauxIA niveauArgent) {
        this.niveauArgent = niveauArgent;
    }

    /**
     * Définit le niveau de l'IA OR.
     *
     * @param niveauOr
     * Nouveau niveau IA OR
     */
    public void setNiveauOr(NiveauxIA niveauOr) {
        this.niveauOr = niveauOr;
    }

    /**
     * Définit le nom du joueur ARGENT.
     *
     * @param nomArgent
     * Nouveau nom du joueur ARGENT
     */
    public void setNomArgent(String nomArgent) {
        this.nomArgent = nomArgent;
    }

    /**
     * Définit le nom du joueur humain.
     *
     * @param nomHumain
     * Nouveau nom du joueur humain
     */
    public void setNomHumain(String nomHumain) {
        this.nomHumain = nomHumain;
    }

    /**
     * Définit le nom du joueur OR.
     *
     * @param nomOr
     * Nouveau nom du joueur OR
     */
    public void setNomOr(String nomOr) {
        this.nomOr = nomOr;
    }
}