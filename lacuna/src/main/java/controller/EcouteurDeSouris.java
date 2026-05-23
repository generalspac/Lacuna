package controller;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import view.JeuGraphique;
import model.ConstantesJeu;
import model.Coordonnees;
import model.Jeu;

import model.Fleur;

/**
 * Traduit les clics du plateau graphique en actions de jeu humain.
 */
public class EcouteurDeSouris extends MouseAdapter {
    private Jeu game;
    private JeuGraphique jeuGraphique;
    Fleur meilleureFleur2;

    /**
     * Branche l'écouteur sur la vue de jeu.
     *
     * @param jeuGraphique vue qui reçoit les clics du joueur.
     */
    public EcouteurDeSouris(JeuGraphique jeuGraphique) {
        this.game = jeuGraphique.jeu;
        this.jeuGraphique = jeuGraphique;
    }

    /**
     * Gère le clic principal d'un tour humain.
     * 
     *
     * @param e événement souris reçu par Swing.
     */
    @Override
    public void mousePressed(MouseEvent e) {

        if (jeuGraphique.isAnimationEnCours() || game.isPartieTerminee()) {
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

        Coordonnees modelPos = jeuGraphique.screenToModel(x, y);

        if (jeuGraphique.getAnimation().lancerAnimationSecondePhase()) {
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

        if (game.fleursConnectees(game.getFleurSelectionnee1(), game.getFleurSelectionnee2())) {
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

        if (!game.existeCoupConnecte() && game.placerPionLibreSiAucunCoup(modelPos)) {
            jeuGraphique.setSuggestedFlower(null);
            jeuGraphique.setOtherSuggestedFleurs(null);
            jeuGraphique.refreshScoresEtTourIA();
        
            
            return;
        }

        if (!game.getCercleDeJeu().contientPoint(modelPos)) {
            return;
        }

        if (!game.existeCoupConnecte() && game.placerPionLibreSiAucunCoup(modelPos)) {
            jeuGraphique.refreshScoresEtTourIA();
        }

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    private void rafraichirApresClicFleur() {
        if (game.getFleurSelectionnee1() != null && game.getFleurSelectionnee2() == null) {
            meilleureFleur2 = game.choisirDeuxiemeMeilleurFleur(game.getFleurSelectionnee1());
            jeuGraphique.setSuggestedFlower(meilleureFleur2);

            java.util.List<model.Fleur> autres = game.recupererAutresFleursMemeType(
                game.getFleurSelectionnee1(), meilleureFleur2);
            jeuGraphique.setOtherSuggestedFleurs(autres);
        } else {
            jeuGraphique.setSuggestedFlower(null);
            jeuGraphique.setOtherSuggestedFleurs(null);
        }

        jeuGraphique.refreshScores();
        jeuGraphique.repaint();
    }

    private boolean doitPrioriserPlacementSurSegment(Coordonnees modelPos) {
        Fleur f1 = game.getFleurSelectionnee1();
        Fleur f2 = game.getFleurSelectionnee2();

        if (!game.fleursConnectees(f1, f2)) {
            return false;
        }

        if (!Coordonnees.surSegment(f1.getPosition(), f2.getPosition(), modelPos,
                ConstantesJeu.TOLERANCE_DROP_SEGMENT)) {
            return false;
        }

        return !clicDansCorpsDuneFleur(modelPos);
    }

    private boolean clicDansCorpsDuneFleur(Coordonnees modelPos) {
        double rayonFleur = ConstantesJeu.DIAMETRE_FLEUR_MODELE / 2.0;
        for (Fleur fleur : game.getFleurs()) {
            if (Coordonnees.distance(modelPos, fleur.getPosition()) <= rayonFleur) {
                return true;
            }
        }
        return false;
    }
}
