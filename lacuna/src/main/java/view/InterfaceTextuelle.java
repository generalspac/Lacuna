package view;

import global.Configuration;
import model.Cercle;
import model.Coordonnees;
import model.Jeu;
import model.Pion;

import static model.Pion.TypePion.ARGENT;
import static model.Pion.TypePion.OR;

/**
 * \class InterfaceTextuelle
 * \brief Interface de test en mode console pour le jeu.
 *
 * \details Cette classe permet de lancer une version textuelle
 *          du jeu afin de tester rapidement la logique métier
 *          sans interface graphique.
 */
public class InterfaceTextuelle {

    /**
     * Lance l’interface textuelle et exécute une séquence de test.
     */
    public void lancer() {

        Configuration.debugeur("Interface textuelle lancée !\n");

        Jeu jeu = new Jeu(50, 30, new Cercle(new Coordonnees(25, 15), 15));
        jeu.afficher();

        jeu.placePion(new Pion(OR, new Coordonnees(5, 5)), new Coordonnees(5, 5));
        jeu.placePion(new Pion(ARGENT, new Coordonnees(20, 20)), new Coordonnees(20, 20));
        jeu.afficher();
    }
}