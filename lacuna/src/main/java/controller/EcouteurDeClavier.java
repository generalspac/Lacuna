package controller;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import global.Configuration;

public class EcouteurDeClavier extends KeyAdapter {
    private ControleurBoutons controleur;

    public  EcouteurDeClavier(ControleurBoutons controleur) {
        this.controleur = controleur;
    }

    

    @Override
    public void keyPressed(KeyEvent e) {

        switch (e.getKeyCode()) {

            case KeyEvent.VK_LEFT:
                Configuration.debugeur("Le bouton  <- a été pressé\n");
                controleur.undo();
                break;

            case KeyEvent.VK_RIGHT:
                Configuration.debugeur("Le bouton -> a été pressé\n");
                controleur.redo();
                break;

            case KeyEvent.VK_R:
                Configuration.debugeur("Le bouton R clavier a été pressé\n");
                controleur.reset();
                break;

            case KeyEvent.VK_G:
                Configuration.debugeur("Le bouton G clavier a été pressé\n");
                controleur.toggleCoupsPossibles(null, false);
                break;

            case KeyEvent.VK_A:
                Configuration.debugeur("Le bouton A clavier a été pressé\n");
                controleur.abandonner();
                break;
        }
    }

}