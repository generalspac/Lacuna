package model.IA;

import model.Fleur;
import model.Pion;
import model.ReglesPlateau;

// Calcule le score heuristique d'un état
public class EvaluateurJeu {

    // Sert à définir si il y aura une utilisation du bonus ou non
    private final boolean utiliserBonus;

    public EvaluateurJeu(boolean utiliserBonus){
        this.utiliserBonus = utiliserBonus;
    }

    public int evaluer(EtatJeu etat, int indexIA) {

        int indexJoueur = 1 - indexIA;

        int couleursIA = compterCouleursMajoritaires(etat, indexIA);
        int couleursJoueur = compterCouleursMajoritaires(etat, indexJoueur);

        int score = (couleursIA - couleursJoueur) * 1000;

        score += (nombreFleurs(etat, indexIA) - nombreFleurs(etat, indexJoueur)) * 20;
        score += (scoreCouleursProchesMajorite(etat, indexIA)
                - scoreCouleursProchesMajorite(etat, indexJoueur)) * 10;

        score += scoreMenaces(etat, indexIA);

      if (utiliserBonus) {
        score += bonus(etat, indexIA) - bonus(etat, indexJoueur);
        score += scorePhase2(etat, indexIA) - scorePhase2(etat, indexJoueur);
          score += scoreMobilite(etat, indexIA) - scoreMobilite(etat, indexJoueur);
      }

        return score;
    }

    // Ajuste le score si l'adversaire est à 1 coup d'une majorité
    public int scoreMenaces(EtatJeu etat, int indexIA) {
        int indexJoueur = 1 - indexIA;
        int score = 0;

        for (Fleur.TypeFleur type : Fleur.TypeFleur.values()) {
            int nbAdversaire = nombreFleursDeCouleur(etat, indexJoueur, type);
            int nbIA  = nombreFleursDeCouleur(etat, indexIA,type);


            if (nbAdversaire == 3) {
                score -= 500;
            }


            if (nbIA == 3) {
                score += 300;
            }
        }

        return score;
    }

    // Compte le nombre de pairs accesibles pour un joueur donné
    public int scoreMobilite(EtatJeu etat, int indexJoueur) {
        int nombre = 0;
        int taille = etat.fleursRestantes.size();

        for (int i = 0; i < taille; i++) {
            for (int j = i + 1; j < taille; j++) {
                Fleur f1 = etat.fleursRestantes.get(i);
                Fleur f2 = etat.fleursRestantes.get(j);

                if (f1.getType() == f2.getType()) {
                    nombre++;
                }
            }
        }
        return nombre * 3;
    }

    public int compterCouleursMajoritaires(EtatJeu etat, int index){
        int compteur = 0;

        for (Fleur.TypeFleur type : Fleur.TypeFleur.values()) {
            if (nombreFleursDeCouleur(etat, index, type) >= 4) {
                compteur++;
            }
        }

        return compteur;
    }
    
    public int bonus(EtatJeu etat,int index){
        int score = 0;

        for (Fleur.TypeFleur type : Fleur.TypeFleur.values()) {
            int nb = nombreFleursDeCouleur(etat, index, type);

            if (nb == 3) {
                score += 40;
            } else if (nb == 2) {
                score += 15;
            } else if (nb >= 4) {
                score += 60;
            }
        }

        return score;
    }

   
    // Anticipation de la phase 2
    private int scorePhase2(EtatJeu etat, int index) {
        int score = 0;

        for (Fleur fleur : etat.fleursRestantes) {
            Pion pionProche = pionLePlusProche(etat, fleur);

            if (pionProche != null && pionProche.getType() == etat.typesPionsJoueurs[index]) {
                score += 8;
            }
        }

        return score;
    }

    private int scoreCouleursProchesMajorite(EtatJeu etat, int index) {
        int score = 0;

        for (Fleur.TypeFleur type : Fleur.TypeFleur.values()) {
            int nb = nombreFleursDeCouleur(etat, index, type);

            if (nb > 0 && nb < 4) {
                score += nb;
            }
        }

        return score;
    }

    private int nombreFleurs(EtatJeu etat, int index) {
        return etat.fleursParJoueur[index].size();
    }

    private int nombreFleursDeCouleur(EtatJeu etat, int index, Fleur.TypeFleur type) {
        int compteur = 0;

        for (Fleur f : etat.fleursParJoueur[index]) {
            if (f.getType() == type) {
                compteur++;
            }
        }
        return compteur;
    }


    private Pion pionLePlusProche(EtatJeu etat, Fleur fleur) {
        return ReglesPlateau.pionLePlusProche(etat, fleur);
    }
}
