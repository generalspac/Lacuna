/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import view.JeuGraphique;
import model.*;
import model.Jeu.ActionJeu;

public class EcouteurDeSouris extends MouseAdapter {
    private Jeu game;
    private JeuGraphique jeuGraphique;

    public EcouteurDeSouris(JeuGraphique jeuGraphique) {
        this.game = jeuGraphique.jeu;
        this.jeuGraphique = jeuGraphique;
    }
    
    @Override
    public void mousePressed(MouseEvent e) {

        int x = e.getX();
        int y = e.getY();

        Coordonnees modelPos = jeuGraphique.screenToModel(x, y);

        // appeler la deuxieme phase du jeu(elle ne s'execute que si tous les pions sont placés)
        // et celà est assurer dans la fonction dans le modèle déjà

        if( game.secondePhase() ){
            System.out.println("seconde phase"); // Affichage à remplacer par la méthode d'affichage dans config
            jeuGraphique.refreshScores();

            jeuGraphique.repaint();

            System.out.println("Fin de la seconde phase");
            return ; // Fin du jeu
        }


        // Vérifie si clic dans le cercle
        if (!game.getCercleDeJeu().contientPoint(modelPos)) {
            System.out.println("Clic en dehors du cercle modèle");
            return;
        }

       // Si deux fleurs sélectionnées et connectées
        if (game.fleursConnectées( game.getFleurSelectionnee1(), game.getFleurSelectionnee2())) {
            
            if (game.jouerCoupAvecFleursSelectionnees()) {
                jeuGraphique.refreshScores();
                jeuGraphique.repaint();
            } else {
                System.out.println("Placement impossible : limite de pions ou position invalide.");
            }
            return;
        }

        // Sélection de fleur
        if (game.toucherFleur(modelPos)) {
            System.out.println("Clic sur une fleur !");
            jeuGraphique.refreshScores();
            jeuGraphique.repaint();
            return;
        }
    }

    
    @Override
    public void mouseReleased(MouseEvent e) {
      
    }
}