package controller;

import model.Types;
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
    
    // ======= AFFICHAGE COUPS POSSIBLES ========
    public void toggleCoupsPossibles(Types.TypeFleur couleur, boolean masquer) {
        if (masquer) {
            vue.afficherCoupsPossibles(null, true);
        } else {
            vue.afficherCoupsPossibles(couleur, false);
        }
    }
}