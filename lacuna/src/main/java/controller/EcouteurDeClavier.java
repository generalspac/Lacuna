package controller;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JComponent;

/**
 * --------------------- ÉCOUTEUR CLAVIER -------------------------- //
 *
 * Gère les raccourcis clavier
 * de l'interface graphique.
 */
public class EcouteurDeClavier extends KeyAdapter {

    // --------------------- ATTRIBUTS -------------------------- //

    /**
     * Contrôleur des boutons.
     */
    private final ControleurBoutons controleur;

    // --------------------- CONSTRUCTEUR -------------------------- //

    /**
     * Crée un écouteur clavier.
     *
     * @param controleur Contrôleur associé
     */
    public EcouteurDeClavier(
            ControleurBoutons controleur) {

        this.controleur = controleur;
    }

    // --------------------- INSTALLATION -------------------------- //

    /**
     * Installe l'écouteur clavier
     * sur un composant graphique.
     *
     * L'installation est récursive :
     * tous les composants enfants
     * reçoivent également l'écouteur.
     *
     * @param composant Composant cible
     */
    public void installerSur(Component composant) {

        if (composant == null) {

            return;
        }

        composant.addKeyListener(this);

        if (composant instanceof JComponent) {

            composant.setFocusable(true);
        }

        if (composant instanceof Container) {

            for (Component enfant :
                    ((Container) composant).getComponents()) {

                installerSur(enfant);
            }
        }
    }

    // --------------------- GESTION CLAVIER -------------------------- //

    /**
     * Gère les touches du clavier.
     *
     * Raccourcis :
     * - Flèche gauche : undo
     * - Flèche droite : redo
     * - R : reset
     * - G : guide
     * - A : abandon
     * - F : feedforward
     *
     * @param e Événement clavier
     */
    @Override
    public void keyPressed(KeyEvent e) {

        switch (e.getKeyCode()) {

            case KeyEvent.VK_LEFT:

                controleur.undo();

                e.consume();

                break;

            case KeyEvent.VK_RIGHT:

                controleur.redo();

                e.consume();

                break;

            case KeyEvent.VK_R:

                controleur.reset();

                e.consume();

                break;

            case KeyEvent.VK_G:

                controleur.guide();

                e.consume();

                break;

            case KeyEvent.VK_A:

                controleur.abandonner();

                e.consume();

                break;

            case KeyEvent.VK_F:

                controleur.feedforward();

                e.consume();

                break;

            default:
                break;
        }
    }
}