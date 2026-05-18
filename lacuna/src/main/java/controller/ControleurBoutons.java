package controller;

import model.Fleur;
import view.JeuGraphique;

public class ControleurBoutons {
    private JeuGraphique vue;

    public ControleurBoutons(JeuGraphique vue) {
        this.vue = vue;
    }

    public void undo() {
        if (vue.jeu.estTourReseauDistant()) {
            return;
        }
        vue.annulerAnimationsVisuelles();
        vue.jeu.undo();
        vue.envoyerEtatReseauSiActif();
        vue.refreshScoresEtTourIA();
    }

    public void redo() {
        if (vue.jeu.estTourReseauDistant()) {
            return;
        }
        vue.annulerAnimationsVisuelles();
        vue.jeu.redo();
        vue.envoyerEtatReseauSiActif();
        vue.refreshScoresEtTourIA();
    }

    public void reset() {
        vue.annulerAnimationsVisuelles();
        vue.jeu.reset();
        vue.envoyerEtatReseauSiActif();
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
        vue.envoyerEtatReseauSiActif();
        if (vue.jeu.isPartieAbandonnee()) {
            vue.afficherGagnantSansAnimation();
        }
        vue.refreshScores();
        vue.repaint();
    }

    public JeuGraphique getvue() {
        return vue;
    }
    
    //==== Affichage des coups ========
    public void toggleCoupsPossibles(Fleur.TypeFleur couleur, boolean masquer) {
        if (masquer) {
            // Si on masque, on affiche les coups possibles de la fleur sélectionnée (si il y en a une)
            if(vue.jeu.getFleurSelectionnee1() != null) {
                couleur = vue.jeu.getFleurSelectionnee1().getType();
            } else {
                couleur = null; // Sinon on affiche les coups possibles de toutes les fleurs    
            }
            vue.afficherCoupsPossibles(null, true);
        } else {
            vue.afficherCoupsPossibles(couleur, false);
        }
    }
}
