/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import view.JeuGraphique;
import model.Coordonnees;
import model.Jeu;

public class EcouteurDeSouris extends MouseAdapter {
    private Jeu game;
    private JeuGraphique jeuGraphique;

    public EcouteurDeSouris(JeuGraphique jeuGraphique) {
        this.game = jeuGraphique.jeu;
        this.jeuGraphique = jeuGraphique;
    }

    @Override
    public void mousePressed(MouseEvent e) {

        if (jeuGraphique.isAnimationEnCours() || game.isPartieAbandonnee()) {
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

        if (jeuGraphique.lancerAnimationSecondePhaseSiBesoin()) {
            return;
        }

        if (!game.getCercleDeJeu().contientPoint(modelPos)) {
            return;
        }

        if (game.fleursConnectées(game.getFleurSelectionnee1(), game.getFleurSelectionnee2())) {

            if (game.jouerCoupAvecFleursSelectionnees(modelPos)) {

                jeuGraphique.refreshScoresEtTourIA();
            } else {
                jeuGraphique.refreshScores();
                jeuGraphique.repaint();
            }

            return;
        }

        if (game.toucherFleur(modelPos)) {

            jeuGraphique.refreshScores();
            jeuGraphique.repaint();
            return;
        }

        if (!game.existeCoupConnecte() && game.placerPionLibreSiAucunCoup(modelPos)) {
            jeuGraphique.refreshScoresEtTourIA();
        }

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }
}
