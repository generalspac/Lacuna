package main;

import javax.swing.SwingUtilities;

import global.Configuration;
import view.InterfaceGraphique;
import view.InterfaceTextuelle;

/**
 * --------------------- LACUNA -------------------------- //
 *
 * Point d'entrée principal du jeu Lacuna.
 *
 * Cette classe permet :
 * - de choisir le mode de jeu (graphique ou textuel)
 * - d'initialiser l'interface correspondante
 * - de lancer l'application
 */
public class Lacuna {

    /**
     * --------------------- POINT D'ENTRÉE -------------------------- //
     *
     * Méthode principale du programme.
     *
     * Arguments possibles :
     * - "graphique" : lance l'interface graphique
     * - "textuel" : lance l'interface textuelle
     *
     * Si aucun argument n'est fourni,
     * le mode défini dans {@link Configuration} est utilisé.
     *
     * @param args Arguments de lancement
     */
    public static void main(String[] args) {

        if(args.length == 1)
            switch (args[0]) {

                case "graphique":
                    Configuration.mode = Configuration.Mode.GRAPHIQUE;
                    break;

                case "textuel":
                    Configuration.mode = Configuration.Mode.TEXTUEL;
                    break;

                default:
                    break;
            }

        switch (Configuration.mode) {

            case TEXTUEL:

                InterfaceTextuelle inter = new InterfaceTextuelle();

                inter.lancer();

                break;

            /**
             * Swing s'exécute dans un thread séparé.
             *
             * En aucun cas il ne faut accéder directement
             * aux composants graphiques depuis le thread principal.
             *
             * Swing fournit la méthode invokeLater()
             * pour exécuter le Runnable dans le thread graphique.
             */
            default:

                SwingUtilities.invokeLater(new InterfaceGraphique());

                break;
        }
    }
}