package controller;

import view.JeuGraphique;

public class ControleurBoutons {
    private JeuGraphique vue;

    public ControleurBoutons(JeuGraphique vue) {
        this.vue = vue;
    }

    public void undo() {
        vue.jeu.undo();
        vue.refreshScoresEtTourIA();
    }

    public void redo() {
        vue.jeu.redo();
        vue.refreshScoresEtTourIA();
    }

    public void reset() {
        vue.jeu.reset();
        vue.refreshScoresEtTourIA();
    }
}