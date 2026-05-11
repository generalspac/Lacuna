package model.IA;

// Niveau d'IA difficile
public class IADifficile implements IA {
    // attribut minimax pour calculer le meilleur coup a choisir
    private final Minimax minimax;
    public IADifficile(int indexIA){
        minimax = new Minimax(new EvaluateurJeu(true), 6, indexIA);

    }
    // calcule le meilleur coup a choisir

    public CoupIA choisirCoup(EtatJeu etat){
        return minimax.meilleurCoup(etat);
    }
}
