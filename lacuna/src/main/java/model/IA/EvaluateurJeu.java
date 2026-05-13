package model.IA;

import model.Fleur;

// Calcule le score heuristique d'un état
public class EvaluateurJeu {

    // Sert à définir si il y aura une utilisation du bonus ou non
    private final boolean utiliserBonus;

    public EvaluateurJeu(boolean utiliserBonus){
        this.utiliserBonus = utiliserBonus;
    }

    public int evaluer(EtatJeu etat, int indexIA) {

        // Index du joueur
        int indexJoueur = 1 - indexIA;

        // On compte les fleurs par couleur pour les deux joueurs une seule fois
        int[] countsIA = countColors(etat, indexIA);
        int[] countsJoueur = countColors(etat, indexJoueur);

        // Calcule des couleurs majoritaires pour IA et joueur
        int couleursIA = 0;
        int scoreProcheIA = 0;
        int bonusIA = 0;
        
        for (int nb : countsIA) {
            if (nb >= 4) {
                couleursIA++;
                bonusIA += 60;
            } else if (nb > 0) {
                scoreProcheIA += nb;
                if (nb == 3) bonusIA += 40;
                else if (nb == 2) bonusIA += 15;
            }
        }

        int couleursJoueur = 0;
        int scoreProcheJoueur = 0;
        int bonusJoueur = 0;
        
        for (int nb : countsJoueur) {
            if (nb >= 4) {
                couleursJoueur++;
                bonusJoueur += 60;
            } else if (nb > 0) {
                scoreProcheJoueur += nb;
                if (nb == 3) bonusJoueur += 40;
                else if (nb == 2) bonusJoueur += 15;
            }
        }

        // Formule de calcul du score
        int score = (couleursIA - couleursJoueur) * 1000;

        score += (etat.fleursParJoueur[indexIA].size() - etat.fleursParJoueur[indexJoueur].size()) * 20;
        score += (scoreProcheIA - scoreProcheJoueur) * 10;

        // Conditions de l'utilisation du bonus
        if (utiliserBonus) {
            score += bonusIA - bonusJoueur;
        }

        return score;
    }

    private int[] countColors(EtatJeu etat, int index) {
        int[] counts = new int[Fleur.TypeFleur.values().length];
        for (Fleur f : etat.fleursParJoueur[index]) {
            counts[f.getType().ordinal()]++;
        }
        return counts;
    }
}