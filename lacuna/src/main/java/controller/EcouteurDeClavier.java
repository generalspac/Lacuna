package controller;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JComponent;

/**
 * Gère les raccourcis clavier de l'écran de partie.
 */
public class EcouteurDeClavier extends KeyAdapter {

    private final ControleurBoutons controleur;

    public EcouteurDeClavier(ControleurBoutons controleur) {
        this.controleur = controleur;
    }


    public void installerSur(Component composant) {
        if (composant == null) {
            return;
        }

        composant.addKeyListener(this);
        if (composant instanceof JComponent) {
            composant.setFocusable(true);
        }

        if (composant instanceof Container) {
            for (Component enfant : ((Container) composant).getComponents()) {
                installerSur(enfant);
            }
        }
    }

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
