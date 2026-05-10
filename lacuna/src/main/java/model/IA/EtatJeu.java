package model.IA;

import java.util.List;
import model.Fleur;
import model.Pion;

// Copie l'état du plateau pour l'algo minimax


public class EtatJeu {

    // Fleurs encore sur le plateau
    public List<Fleur> fleursRestantes;

    // Pions déjà placés sur le plateau
    public List<Pion> pions;

    // Fleurs collectées par chaque joueur
    public List<Fleur>[] fleursParJoueur;

    // Index du joueur courant ( 0 ou 1 )
    public int joueurCourant;


}