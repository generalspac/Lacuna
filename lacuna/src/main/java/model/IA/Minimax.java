package model.IA;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

// Algo Minimax
public class Minimax {

    private final EvaluateurJeu evaluateur;
    private final int profondeurMax;
    private final int indexIA;
    private final Random random = new Random();

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

        // Trions pour maximiser l'elagage alpha____beta
        trierCoups(etat, coups);

        List<CoupIA> meilleursCoups = new ArrayList<>();
        int meilleurScore = Integer.MIN_VALUE;
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;

        for (CoupIA coup : coups) {
            EtatJeu suivant = etat.appliquerCoup(coup);
            int score = minimax(suivant, profondeurMax - 1, alpha, beta);
            if (score > meilleurScore) {
                meilleurScore = score;
                meilleursCoups.clear();
                meilleursCoups.add(coup);
            } 
            else if (score == meilleurScore) {
                meilleursCoups.add(coup);
            }

            alpha = Math.max(alpha, meilleurScore);
        }

        return meilleursCoups.get(random.nextInt(meilleursCoups.size()));
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

        if (profondeur > 2) {
            trierCoups(etat, coups);
        }

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

    // Trier les coups
    private void trierCoups(EtatJeu etat, List<CoupIA> coups) {
        List<CoupNote> coupsNotes = new ArrayList<>(coups.size());

        for (CoupIA coup : coups) {
            EtatJeu suivant = etat.appliquerCoup(coup);
            int score = evaluateur.evaluer(suivant, indexIA);
            coupsNotes.add(new CoupNote(coup, score));
        }

        coupsNotes.sort(Comparator.comparingInt((CoupNote coupNote) -> coupNote.score).reversed());

        coups.clear();
        for (CoupNote coupNote : coupsNotes) {
            coups.add(coupNote.coup);
        }
    }

    private static class CoupNote {
        private final CoupIA coup;
        private final int score;

        private CoupNote(CoupIA coup, int score) {
            this.coup = coup;
            this.score = score;
        }
    }
}
