package controller;

import model.Fleur;
import view.JeuGraphique;

/**
 * --------------------- CONTRÔLEUR BOUTONS -------------------------- //
 *
 * Centralise les actions des boutons
 * de l'interface graphique.
 *
 * Cette classe permet de :
 * - annuler un coup
 * - refaire un coup
 * - recommencer une partie
 * - abandonner une partie
 * - afficher l'aide visuelle
 * - gérer le feedforward
 */
public class ControleurBoutons {

    // --------------------- ATTRIBUTS -------------------------- //

    /**
     * Vue graphique du jeu.
     */
    private JeuGraphique vue;

    // --------------------- CONSTRUCTEUR -------------------------- //

    /**
     * Crée un contrôleur de boutons.
     *
     * @param vue Vue graphique associée
     */
    public ControleurBoutons(JeuGraphique vue) {

        this.vue = vue;
    }

    // --------------------- ANNULATION -------------------------- //

    /**
     * Annule le dernier coup joué.
     */
    public void undo() {

        if (vue.jeu.isEnModeReseau()) return;

        if (vue.jeu.isPartieTerminee()) return;

        vue.annulerAnimationsVisuelles();

        vue.jeu.undo();

        vue.refreshScoresEtTourIA();
    }

    // --------------------- RÉTABLISSEMENT -------------------------- //

    /**
     * Rejoue le dernier coup annulé.
     */
    public void redo() {

        if (vue.jeu.isEnModeReseau()) return;

        if (vue.jeu.isPartieTerminee()) return;

        vue.annulerAnimationsVisuelles();

        vue.jeu.redo();

        vue.refreshScoresEtTourIA();
    }

    // --------------------- RESET -------------------------- //

    /**
     * Réinitialise la partie.
     */
    public void reset() {

        if (vue.jeu.isEnModeReseau()) return;

        vue.annulerAnimationsVisuelles();

        vue.jeu.reset();

        vue.refreshScoresEtTourIA();
    }

    // --------------------- ABANDON -------------------------- //

    /**
     * Fait abandonner la partie
     * au joueur courant.
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

    // --------------------- FEEDFORWARD -------------------------- //

    /**
     * Active ou désactive
     * le feedforward visuel.
     */
    public void feedforward() {

        if (vue.jeu.isEnModeReseau()) return;

        vue.toggleFeedforward();
    }

    // --------------------- GUIDE -------------------------- //

    /**
     * Affiche les coups possibles.
     */
    public void guide() {

        if (vue.jeu.getFleurSelectionnee1() != null) {

            toggleCoupsPossibles(
                    vue.jeu.getFleurSelectionnee1().getType(),
                    false);

        } else {

            toggleCoupsPossibles(null, false);
        }
    }

    // --------------------- GETTER -------------------------- //

    /**
     * Retourne la vue graphique.
     *
     * @return Vue graphique du jeu
     */
    public JeuGraphique getvue() {

        return vue;
    }

    // --------------------- COUPS POSSIBLES -------------------------- //

    /**
     * Affiche ou masque l'aide visuelle
     * des coups possibles.
     *
     * @param couleur Couleur à mettre en évidence
     * @param masquer true pour masquer l'aide
     */
    public void toggleCoupsPossibles(
            Fleur.TypeFleur couleur,
            boolean masquer) {

        if (masquer) {

            if (vue.jeu.getFleurSelectionnee1() != null) {

                couleur = vue.jeu
                        .getFleurSelectionnee1()
                        .getType();

            } else {

                couleur = null;
            }

            vue.afficherCoupsPossibles(null, true);

        } else {

            vue.afficherCoupsPossibles(couleur, false);
        }
    }
}