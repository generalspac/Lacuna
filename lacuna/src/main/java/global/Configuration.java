package global;

import java.io.InputStream;

/**
 * ---------------------- CONFIGURATION ------------------------ //
 *
 * Classe utilitaire globale contenant :
 * - les constantes du projet
 * - les méthodes de chargement des ressources
 * - les outils de débogage
 * - le mode d'affichage du jeu
 *
 * Cette classe est statique et ne doit jamais être instanciée.
 */
public final class Configuration {

    // ---------------------- CONSTANTES ------------------------ //

    /**
     * Chemin racine des ressources images.
     */
    private static final String PATH = "/images/";

    /**
     * Active l'affichage des messages de debug.
     */
    private static boolean debug = true;

    /**
     * Active l'affichage des messages d'erreur.
     */
    private static boolean debugError = true;

    // ---------------------- MODE DE JEU ------------------------ //

    /**
     * Modes d'affichage disponibles.
     */
    public enum Mode {

        /**
         * Mode graphique Swing.
         */
        GRAPHIQUE,

        /**
         * Mode console.
         */
        TEXTUEL
    }

    /**
     * Mode actuel du jeu.
     */
    public static Mode mode = Mode.GRAPHIQUE;

    // ---------------------- CHARGEMENT RESSOURCES ------------------------ //

    /**
     * Ouvre une ressource située dans le dossier images.
     *
     * @param path
     * Chemin relatif du fichier à ouvrir
     *
     * @return Flux du fichier chargé
     */
    public static InputStream ouvre(String path) {

        InputStream in =
            Configuration.class.getResourceAsStream(
                PATH + path
            );

        if (in == null) {

            debugeurErreur(
                "Impossible de trouver : " + PATH + path
            );

            System.exit(2);
        }

        return in;
    }

    /**
     * Ouvre une ressource depuis un dossier spécifique.
     *
     * @param path
     * Dossier contenant la ressource
     *
     * @param name
     * Nom du fichier
     *
     * @return Flux du fichier chargé
     */
    public static InputStream ouvre(String path, String name) {

        InputStream in =
            Configuration.class.getResourceAsStream(
                path + "/" + name
            );

        if (in == null) {

            debugeurErreur(
                "ERREUR : impossible de trouver le fichier "
                + path + "/" + name
            );

            System.exit(2);
        }

        return in;
    }

    // ---------------------- DEBUG ------------------------ //

    /**
     * Configure le niveau d'affichage des messages.
     *
     * Options disponibles :
     * - 0 : aucun affichage
     * - 1 : erreurs uniquement
     * - autre : tout afficher
     *
     * @param option
     * Niveau de debug souhaité
     */
    public static void setModeAffichage(int option) {

        switch (option) {

            case 0:

                debug = false;
                break;

            case 1:

                debug = false;
                debugError = true;
                break;

            default:

                debug = true;
                break;
        }
    }

    /**
     * Affiche un message de debug standard.
     *
     * @param message
     * Message à afficher
     *
     * @param args
     * Arguments de formatage
     */
    public static void debugeur(String message, Object... args) {

        if (debug) {
            System.out.printf(message, args);
        }
    }

    /**
     * Affiche un message d'erreur.
     *
     * @param message
     * Message d'erreur
     *
     * @param args
     * Arguments de formatage
     */
    public static void debugeurErreur(String message, Object... args) {

        if (debug || debugError) {
            System.err.printf(message, args);
        }
    }
}