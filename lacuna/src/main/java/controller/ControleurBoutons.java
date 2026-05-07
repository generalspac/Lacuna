package controller;

import view.JeuGraphique;

public class ControleurBoutons {
    private JeuGraphique vue;

    public ControleurBoutons(JeuGraphique vue) {
        this.vue = vue;
    }

    public void undo() {
        vue.jeu.undo();
        vue.refreshScores();
        vue.repaint();
    }

    public void redo() {
        vue.jeu.redo();
        vue.refreshScores();
        vue.repaint();
    }

    public void reset() {
        vue.jeu.reset();
        vue.refreshScores();
        vue.repaint();
    }
}