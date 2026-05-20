package model.IA;

// Niveau d'IA moyen
public class IAMoyen implements IA {
    // attribut minimax pour calculer le meilleur coup a choisir
    private final Minimax minimax;
    public IAMoyen(int indexIA){
        minimax = new Minimax(new EvaluateurJeu(true), 4, indexIA);
    }
    // calcule le meilleur coup a choisir
    public CoupIA choisirCoup(EtatJeu etat){
        return minimax.meilleurCoup(etat);

    }

}
