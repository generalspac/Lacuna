package model.IA;
import model.Minimax;

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

// Niveau d'IA moyen
public class IAMoyen implements IA {
    // attribut minimax pour calculer le meilleur coup a choisir
    private final Minimax minimax;
    public IAMoyen(int indexIA){
        minimax = new Minimax(new EvaluateurJeu(false), 3, indexIA);
    }
    // calcule le meilleur coup a choisir
    public CoupIA choisirCoup(EtatJeu etat){
        return minimax.meilleurCoup(etat);

    }

}

// Niveau d'IA difficile
public class IADifficile implements IA {
    // attribut minimax pour calculer le meilleur coup a choisir
    private final Minimax minimax;
    public IADifficile(int indexIA){
        minimax = new Minimax(new EvaluateurJeu(true), 5, indexIA);

    }
    // calcule le meilleur coup a choisir

    public CoupIA choisirCoup(EtatJeu etat){
        return minimax.meilleurCoup(etat);
    }
}