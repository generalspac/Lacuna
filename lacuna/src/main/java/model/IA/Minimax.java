package model.IA;

// Algo Minimax
public class Minimax {

    private final EvaluateurJeu evaluateur;
    private final int profondeurMax;
    private final int indexIA;

    public Minimax(EvaluateurJeu eval, int profondeur, int indexIA) {
        this.evaluateur = eval;
        this.profondeurMax = profondeur;
        this.indexIA = indexIA;
    }

    // Retourne le meilleur coup depuis un état via l'algo minimax
    public CoupIA meilleurCoup(EtatJeu etat) {
        return null;

    }

    // algo minimax
    public int minimax(EtatJeu etat){

    }
}