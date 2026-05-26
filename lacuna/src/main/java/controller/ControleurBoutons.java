package controller;

import model.Fleur;
import view.JeuGraphique;

/**
 * Centralise les actions des boutons de la partie graphique.
 */
public class ControleurBoutons {
    private JeuGraphique vue;

    /**
     * Crée un contrôleur pour une vue de jeu.
     *
     * @param vue , ici, on a un constructeur.
     */
    public ControleurBoutons(JeuGraphique vue) {
        this.vue = vue;
    }

    /**
     * Annule le dernier coup local puis rafraîchit l'affichage.
     */
    public void undo() {
        if (vue.jeu.isEnModeReseau()) return;
        if (vue.jeu.isPartieTerminee()) return;
        vue.annulerAnimationsVisuelles();
        vue.jeu.undo();
        vue.refreshScoresEtTourIA();
    }


    /**
     * Rejoue le dernier coup annulé puis rafraîchit l'affichage.
     */
    public void redo() {
        if (vue.jeu.isEnModeReseau()) return;
        if (vue.jeu.isPartieTerminee()) return;
        vue.annulerAnimationsVisuelles();
        vue.jeu.redo();
        vue.refreshScoresEtTourIA();
    }


    /**
     * Recommence la partie courante depuis son état initial.
     */
    public void reset() {
        if (vue.jeu.isEnModeReseau()) return;
        vue.annulerAnimationsVisuelles();
        vue.jeu.reset();
        vue.refreshScoresEtTourIA();
    }

    /**
     * Termine la partie en donnant la victoire à l'autre joueur.
     */
    public void abandonner() {
        if (vue.jeu.isPartieTerminee()) {
            vue.jeu.abandonnerPartie();
            vue.refreshScores();
            vue.repaint();
            return;
        }
        vue.annulerAnimationsVisuelles();
        vue.jeu.abandonnerPartie();
        if (vue.jeu.isPartieAbandonnee()) {
            vue.afficherGagnantSansAnimation();
        }
        vue.refreshScores();
        vue.repaint();
    }

        /**
        * Affiche un feedforward pour la partie en cours.
        */
    public void feedforward() {
        if (vue.jeu.isEnModeReseau()) return;

        vue.toggleFeedforward();
}

    /**
     * Affiche les coups possibles via le raccourci clavier.
     */
    public void guide() {
        if (vue.jeu.getFleurSelectionnee1() != null) {
            toggleCoupsPossibles(vue.jeu.getFleurSelectionnee1().getType(), false);
        } else {
            toggleCoupsPossibles(null, false);
        }
    }


    /**
     * Renvoie la vue pilotée par ce contrôleur.
     *
     * @return vue de jeu graphique.
     */
    public JeuGraphique getvue() {
        return vue;
    }
    
    /**
     * Affiche ou masque l'aide visuelle des coups possibles.
     *
     * @param couleur couleur à mettre en évidence, ou {@code null} pour toutes.
     * @param masquer {@code true} pour cacher l'aide.
     */
    public void toggleCoupsPossibles(Fleur.TypeFleur couleur, boolean masquer) {
        if (masquer) {
            if(vue.jeu.getFleurSelectionnee1() != null) {
                couleur = vue.jeu.getFleurSelectionnee1().getType();
            } else {
                couleur = null;
            }
            vue.afficherCoupsPossibles(null, true);
        } else {
            vue.afficherCoupsPossibles(couleur, false);
        }
    }
}
