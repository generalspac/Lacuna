package model.IA;



// Calcule le score heuristique d'un état
public class EvaluateurJeu {

    // Sert à définir si il y aura une utilisation du bonus ou non
    private final boolean utiliserBonus;

    EvaluateurJeu(boolean utiliserBonus){
        utiliserBonus = utiliserBonus;
    }


    public int evaluer(EtatJeu etat, int indexIA) {

        // Index du joueur
        int indexJoueur = 1 - indexIA;

        // Calcule des couleurs majoritaires pour IA et joueur
        int couleursIA    = compterCouleursMajoritaires(etat, indexIA);
        int couleursJoueur = compterCouleursMajoritaires(etat, indexJoueur);

        // Calcul du bonus pour IA et joueur
        int bonusIA    = bonus(etat, indexIA, indexJoueur);
        int bonusJoueur = bonus(etat, indexJoueur, indexIA);

        // Formule de calcul du score
        int score = (couleursIA - couleursJoueur) * 100;

        // Conditions de l'utilisation du bonus
        if (utiliserBonus) {
            score += bonus(etat, indexIA) - bonus(etat, indexAdv);
        }

        return score;

    }

    // Renvoie le nombre de couleurs majoritaires pour un joueur et un état donné
    public int compterCouleursMajoritaires(EtatJeu etat, int index){
        return 0;
    }

    // Renvoie le bonus pour départager deux coups de valeures égales
    public int bonus(EtatJeu etat,int index){
        return 0;
    }

}

// TODO: implémenter bonus et compterCouleursMajoritaires
