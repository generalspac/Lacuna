package controller;

import model.Fleur;
import view.JeuGraphique;

public class ControleurBoutons {
    private JeuGraphique vue;

    public ControleurBoutons(JeuGraphique vue) {
        this.vue = vue;
    }

    public void undo() {
        vue.annulerAnimationsVisuelles();
        vue.jeu.undo();
        vue.refreshScoresEtTourIA();
    }

    public void redo() {
        vue.annulerAnimationsVisuelles();
        vue.jeu.redo();
        vue.refreshScoresEtTourIA();
    }

    public void reset() {
        vue.annulerAnimationsVisuelles();
        vue.jeu.reset();
        vue.refreshScoresEtTourIA();
    }

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
    
    //==== Affichage des coups ========
    public void toggleCoupsPossibles(Fleur.TypeFleur couleur, boolean masquer) {
        if (masquer) {
            vue.afficherCoupsPossibles(null, true);
        } else {
            vue.afficherCoupsPossibles(couleur, false);
        }
    }
}