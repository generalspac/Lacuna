package controller;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import view.JeuGraphique;

import model.ConstantesJeu;
import model.Coordonnees;
import model.Fleur;
import model.Jeu;

/**
 * --------------------- ÉCOUTEUR SOURIS -------------------------- //
 *
 * Gère les interactions souris
 * avec le plateau graphique.
 *
 * Cette classe traduit les clics
 * du joueur en actions de jeu.
 */
public class EcouteurDeSouris extends MouseAdapter {

    // --------------------- ATTRIBUTS -------------------------- //

    /**
     * Modèle du jeu.
     */
    private Jeu game;

    /**
     * Vue graphique associée.
     */
    private JeuGraphique jeuGraphique;

    /**
     * Fleur suggérée par le feedforward.
     */
    Fleur meilleureFleur2;

    // --------------------- CONSTRUCTEUR -------------------------- //

    /**
     * Crée un écouteur souris.
     *
     * @param jeuGraphique Vue graphique associée
     */
    public EcouteurDeSouris(
            JeuGraphique jeuGraphique) {

        this.game = jeuGraphique.jeu;

        this.jeuGraphique = jeuGraphique;
    }

    // --------------------- GESTION CLIC -------------------------- //

    /**
     * Gère le clic souris principal.
     *
     * @param e Événement souris
     */
    @Override
    public void mousePressed(MouseEvent e) {

        if (jeuGraphique.isAnimationEnCours()
                || game.isPartieTerminee()) {

            return;
        }

        if (jeuGraphique.isEnAttenteAdversaire()) {

            return;
        }

        if (game.isIAEnCours()) {

            return;
        }

        if (game.estTourIA()) {

            return;
        }

        int x = e.getX();

        int y = e.getY();

        Coordonnees modelPos =
                jeuGraphique.screenToModel(x, y);

        if (jeuGraphique.getAnimation()
                .lancerAnimationSecondePhase()) {

            return;
        }

        if (!game.getCercleDeJeu().contientPoint(modelPos)) {

            if (game.toucherFleur(modelPos)) {

                rafraichirApresClicFleur();
            }

            return;
        }

        if (doitPrioriserPlacementSurSegment(modelPos)) {

            if (game.jouerCoupAvecFleursSelectionnees(modelPos)) {

                jeuGraphique.setSuggestedFlower(null);

                jeuGraphique.setOtherSuggestedFleurs(null);

                jeuGraphique.refreshScoresEtTourIA();

            } else {

                jeuGraphique.refreshScores();

                jeuGraphique.repaint();
            }

            return;
        }

        if (game.toucherFleur(modelPos)) {

            rafraichirApresClicFleur();

            return;
        }

        if (game.fleursConnectees(
                game.getFleurSelectionnee1(),
                game.getFleurSelectionnee2())) {

            if (game.jouerCoupAvecFleursSelectionnees(modelPos)) {

                jeuGraphique.setSuggestedFlower(null);

                jeuGraphique.setOtherSuggestedFleurs(null);

                jeuGraphique.refreshScoresEtTourIA();

            } else {

                jeuGraphique.refreshScores();

                jeuGraphique.repaint();
            }

            return;
        }

        if (!game.existeCoupConnecte()
                && game.placerPionLibreSiAucunCoup(modelPos)) {

            jeuGraphique.setSuggestedFlower(null);

            jeuGraphique.setOtherSuggestedFleurs(null);

            jeuGraphique.refreshScoresEtTourIA();

            return;
        }

        if (!game.getCercleDeJeu().contientPoint(modelPos)) {

            return;
        }

        if (!game.existeCoupConnecte()
                && game.placerPionLibreSiAucunCoup(modelPos)) {

            jeuGraphique.refreshScoresEtTourIA();
        }
    }

    // --------------------- RELEASE SOURIS -------------------------- //

    /**
     * Gère le relâchement du clic souris.
     *
     * @param e Événement souris
     */
    @Override
    public void mouseReleased(MouseEvent e) {

    }

    // --------------------- RAFRAÎCHISSEMENT -------------------------- //

    /**
     * Met à jour l'affichage après
     * la sélection d'une fleur.
     */
    private void rafraichirApresClicFleur() {

        if (game.getFleurSelectionnee1() != null
                && game.getFleurSelectionnee2() == null) {

            meilleureFleur2 =
                    game.choisirDeuxiemeMeilleurFleur(
                            game.getFleurSelectionnee1());

            jeuGraphique.setSuggestedFlower(meilleureFleur2);

            java.util.List<Fleur> autres =
                    game.recupererAutresFleursMemeType(
                            game.getFleurSelectionnee1(),
                            meilleureFleur2);

            jeuGraphique.setOtherSuggestedFleurs(autres);

        } else {

            jeuGraphique.setSuggestedFlower(null);

            jeuGraphique.setOtherSuggestedFleurs(null);
        }

        jeuGraphique.refreshScores();

        jeuGraphique.repaint();
    }

    // --------------------- PRIORITÉ SEGMENT -------------------------- //

    /**
     * Vérifie si le placement du pion
     * doit être prioritaire sur un segment.
     *
     * @param modelPos Position du clic
     *
     * @return true si le placement est prioritaire
     */
    private boolean doitPrioriserPlacementSurSegment(
            Coordonnees modelPos) {

        Fleur f1 = game.getFleurSelectionnee1();

        Fleur f2 = game.getFleurSelectionnee2();

        if (!game.fleursConnectees(f1, f2)) {

            return false;
        }

        if (!Coordonnees.surSegment(
                f1.getPosition(),
                f2.getPosition(),
                modelPos,
                ConstantesJeu.TOLERANCE_DROP_SEGMENT)) {

            return false;
        }

        return !clicDansCorpsDuneFleur(modelPos);
    }

    // --------------------- CLIC FLEUR -------------------------- //

    /**
     * Vérifie si le clic se situe
     * dans le corps d'une fleur.
     *
     * @param modelPos Position du clic
     *
     * @return true si une fleur est touchée
     */
    private boolean clicDansCorpsDuneFleur(
            Coordonnees modelPos) {

        double rayonFleur =
                ConstantesJeu.DIAMETRE_FLEUR_MODELE / 2.0;

        for (Fleur fleur : game.getFleurs()) {

            if (Coordonnees.distance(
                    modelPos,
                    fleur.getPosition()) <= rayonFleur) {

                return true;
            }
        }

        return false;
    }
}