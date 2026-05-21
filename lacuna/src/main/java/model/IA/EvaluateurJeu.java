package model.IA;

import model.Fleur;
import model.Pion;
import model.ReglesPlateau;

/**
 * Note un état de jeu pour guider le minimax.
 */
public class EvaluateurJeu {

    private final boolean utiliserBonus;

    /**
     * Crée un évaluateur plus ou moins complet selon le niveau d'IA.
     *
     * @param utiliserBonus active les bonus d'anticipation et de mobilité.
     */
    public EvaluateurJeu(boolean utiliserBonus) {
        this.utiliserBonus = utiliserBonus;
    }

    /**
     * Calcule le score d'un état pour l'IA donnée.
     *
     * @param etat état à noter.
     * @param indexIA index du joueur contrôlé par l'IA.
     * @return score positif si l'état favorise l'IA, négatif sinon.
     */
    public int evaluer(EtatJeu etat, int indexIA) {

        int indexJoueur = 1 - indexIA;

       int couleursIA;
       int couleursJoueur;
        if (utiliserBonus) {
            couleursIA = compterCouleursMajoritaires(compterFleursAvecProjectionPhase2(etat, indexIA));
            couleursJoueur = compterCouleursMajoritaires(compterFleursAvecProjectionPhase2(etat, indexJoueur));
        } 
        else {
            couleursIA = compterCouleursMajoritaires(etat, indexIA);
            couleursJoueur = compterCouleursMajoritaires(etat, indexJoueur);
        }

        int score = (couleursIA - couleursJoueur) * 1000;

        score += (nombreFleurs(etat, indexIA) - nombreFleurs(etat, indexJoueur)) * 20;
        score += (scoreCouleursProchesMajorite(etat, indexIA) - scoreCouleursProchesMajorite(etat, indexJoueur)) * 10;

        score += scoreMenaces(etat, indexIA);

      if (utiliserBonus) {
        score += bonus(etat, indexIA) - bonus(etat, indexJoueur);
        score += (scorePhase2(etat, indexIA) - scorePhase2(etat, indexJoueur)) * poidsPhase2(etat);
        score += scoreMobilite(etat, indexIA) - scoreMobilite(etat, indexJoueur);
      }

        return score;
    }

    /**
     * Repère les couleurs où un joueur est à une fleur de la majorité.
     *
     * @param etat état analysé.
     * @param indexIA index du joueur IA.
     * @return bonus ou malus de menace.
     */
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

    /**
     * Estime la mobilité grâce au nombre de paires encore accessibles.
     *
     * @param etat état analysé.
     * @param indexJoueur joueur évalué.
     * @return score de mobilité.
     */
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

    /**
     * Compte les couleurs majoritaires en tenant compte de la projection de phase 2.
     *
     * @param etat état analysé.
     * @param index joueur évalué.
     * @return nombre de couleurs majoritaires.
     */
    public int compterCouleursMajoritaires(EtatJeu etat, int index) {
        return compterCouleursMajoritaires(compterFleursAvecProjectionPhase2(etat, index));
    }
    
    /**
     * Donne un bonus aux couleurs proches d'une majorité.
     *
     * @param etat état analysé.
     * @param index joueur évalué.
     * @return score de bonus.
     */
    public int bonus(EtatJeu etat, int index) {
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

    /**
     * Anticipe les fleurs qui pourraient être gagnées en phase 2.
     *
     * @param etat état analysé.
     * @param index joueur évalué.
     * @return score de projection de phase 2.
     */
    private int scorePhase2(EtatJeu etat, int index) {
        int score = 0;

        for (Fleur fleur : etat.fleursRestantes) {
            Pion pionProche = pionLePlusProche(etat, fleur);

            if (pionProche != null && pionProche.getType() == etat.typesPionsJoueurs[index]) {
                score += 8 + valeurCouleurPhase2(etat, index, fleur.getType());
            }
        }

        return score;
    }

    /**
     * Augmente le poids de la phase 2 quand la fin de partie approche.
     *
     * @param etat état analysé.
     * @return multiplicateur appliqué à l'anticipation de phase 2.
     */
    private int poidsPhase2(EtatJeu etat) {
        int pionsPlaces = 0;
        for (Pion p : etat.pions) {
            if (p.getPosition() != null) {
                pionsPlaces++;
            }
        }

        int poids = 1 + pionsPlaces / 3;
        if (etat.fleursRestantes.size() <= 18) {
            poids += 2;
        }
        if (etat.fleursRestantes.size() <= 10) {
            poids += 3;
        }
        return poids;
    }

    /**
     * Donne une valeur à une couleur selon son intérêt en phase 2.
     *
     * @param etat état analysé.
     * @param index joueur évalué.
     * @param type couleur étudiée.
     * @return bonus associé à cette couleur.
     */
    private int valeurCouleurPhase2(EtatJeu etat, int index, Fleur.TypeFleur type) {
        int nb = nombreFleursDeCouleur(etat, index, type);
        if (nb == 3) {
            return 45;
        }
        if (nb == 2) {
            return 18;
        }
        if (nb >= 4) {
            return 10;
        }
        return 0;
    }

    /**
     * Valorise les couleurs déjà entamées mais pas encore majoritaires.
     *
     * @param etat état analysé.
     * @param index joueur évalué.
     * @return score de progression.
     */
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

    /**
     * Compte les fleurs déjà collectées par un joueur.
     *
     * @param etat état analysé.
     * @param index joueur évalué.
     * @return nombre de fleurs collectées.
     */
    private int nombreFleurs(EtatJeu etat, int index) {
        return etat.fleursParJoueur[index].size();
    }

    /**
     * Compte les fleurs d'une couleur collectées par un joueur.
     *
     * @param etat état analysé.
     * @param index joueur évalué.
     * @param type couleur recherchée.
     * @return nombre de fleurs dans cette couleur.
     */
    private int nombreFleursDeCouleur(EtatJeu etat, int index, Fleur.TypeFleur type) {
        int compteur = 0;

        for (Fleur f : etat.fleursParJoueur[index]) {
            if (f.getType() == type) {
                compteur++;
            }
        }
        return compteur;
    }

    /**
     * Ajoute aux fleurs gagnées celles que le joueur récupérerait en phase 2.
     *
     * @param etat état analysé.
     * @param index joueur évalué.
     * @return compteurs par couleur.
     */
    private int[] compterFleursAvecProjectionPhase2(EtatJeu etat, int index) {
        int[] compteurs = new int[Fleur.TypeFleur.values().length];

        for (Fleur f : etat.fleursParJoueur[index]) {
            compteurs[f.getType().ordinal()]++;
        }

        for (Fleur fleur : etat.fleursRestantes) {
            Pion pionProche = pionLePlusProche(etat, fleur);
            if (pionProche != null && pionProche.getType() == etat.typesPionsJoueurs[index]) {
                compteurs[fleur.getType().ordinal()]++;
            }
        }

        return compteurs;
    }

    /**
     * Compte les couleurs qui atteignent la majorité.
     *
     * @param compteurs nombre de fleurs par couleur.
     * @return nombre de couleurs avec au moins quatre fleurs.
     */
    private int compterCouleursMajoritaires(int[] compteurs) {
        int compteur = 0;
        for (int nb : compteurs) {
            if (nb >= 4) {
                compteur++;
            }
        }
        return compteur;
    }


    /**
     * Délègue la recherche du pion le plus proche aux règles du plateau.
     *
     * @param etat état analysé.
     * @param fleur fleur étudiée.
     * @return pion le plus proche, ou {@code null} s'il n'y en a aucun.
     */
    private Pion pionLePlusProche(EtatJeu etat, Fleur fleur) {
        return ReglesPlateau.pionLePlusProche(etat, fleur);
    }
}
