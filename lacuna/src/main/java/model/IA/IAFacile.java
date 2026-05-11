package model.IA;

// Niveau d'IA facile
public class IAFacile implements IA{

    // attribut minimax pour calculer le meilleur coup a choisir
    private final Minimax minimax;
    public IAFacile(int indexIA){
        minimax = new Minimax(new EvaluateurJeu(false), 1, indexIA);

    }
    // calcule le meilleur coup a choisir
    public CoupIA choisirCoup(EtatJeu etat){
        return minimax.meilleurCoup(etat);
    }
}
