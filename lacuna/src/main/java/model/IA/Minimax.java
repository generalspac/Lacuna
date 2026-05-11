package model.IA;

import java.util.Comparator;
import java.util.List;

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

    // Retourne le meilleur coup depuis un état avec minimax
    public CoupIA meilleurCoup(EtatJeu etat) {
        List<CoupIA> coups = GenerateurCoup.genererCoups(etat);

        if (coups.isEmpty()) {
            return null;
        }

        trierCoups(etat, coups);

        CoupIA meilleur = null;
        int meilleurScore = Integer.MIN_VALUE;
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;

        for (CoupIA coup : coups) {
            EtatJeu suivant = etat.appliquerCoup(coup);
            int score = minimax(suivant, profondeurMax - 1, alpha, beta);

            if (score > meilleurScore || meilleur == null) {
                meilleurScore = score;
                meilleur = coup;
            }

            alpha = Math.max(alpha, meilleurScore);
        }

        return meilleur;
    }

    // algo minimax
    public int minimax(EtatJeu etat, int profondeur, int alpha, int beta){
        if (profondeur == 0 || etat.estTerminal()) {
            return evaluateur.evaluer(etat, indexIA);
        }

        List<CoupIA> coups = GenerateurCoup.genererCoups(etat);

        if (coups.isEmpty()) {
            return evaluateur.evaluer(etat, indexIA);
        }

        trierCoups(etat, coups);// Trions pour améliorer la performance

        if (etat.joueurCourant == indexIA) {
            int meilleurScore = Integer.MIN_VALUE;

            for (CoupIA coup : coups) {
                EtatJeu suivant = etat.appliquerCoup(coup);
                int score = minimax(suivant, profondeur - 1, alpha, beta);

                meilleurScore = Math.max(meilleurScore, score);
                alpha = Math.max(alpha, meilleurScore);

                if (beta <= alpha) {
                    break;
                }
            }

            return meilleurScore;
        } 
        else {
            int meilleurScore = Integer.MAX_VALUE;

            for (CoupIA coup : coups) {
                EtatJeu suivant = etat.appliquerCoup(coup);
                int score = minimax(suivant, profondeur - 1, alpha, beta);

                meilleurScore = Math.min(meilleurScore, score);
                beta = Math.min(beta, meilleurScore);

                if (beta <= alpha) {
                    break;
                }
            }

            return meilleurScore;
        }
    }

    private void trierCoups(EtatJeu etat, List<CoupIA> coups) {
        coups.sort(Comparator.comparingInt((CoupIA coup) -> {
            EtatJeu suivant = etat.appliquerCoup(coup);
            return evaluateur.evaluer(suivant, indexIA);
        }).reversed());
    }
}
