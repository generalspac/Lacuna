package model.IA;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import model.ConstantesJeu;

/**
 * Recherche le meilleur coup avec minimax et élagage alpha-bêta.
 */
public class Minimax {

    private final EvaluateurJeu evaluateur;
    private final int profondeurMax;
    private final int indexIA;
    private final Random random = new Random();

    /**
     * Crée un moteur minimax pour un niveau d'IA.
     *
     * @param eval évaluateur qui note les états.
     * @param profondeur profondeur maximale de recherche.
     * @param indexIA index du joueur contrôlé par cette IA.
     */
    public Minimax(EvaluateurJeu eval, int profondeur, int indexIA) {
        this.evaluateur = eval;
        this.profondeurMax = profondeur;
        this.indexIA = indexIA;
    }

    /**
     * Cherche le meilleur coup depuis l'état courant.
     *
     * @param etat état analysé.
     * @return meilleur coup trouvé, ou {@code null} si aucun coup n'est jouable.
     */
    public CoupIA meilleurCoup(EtatJeu etat) {
        List<CoupIA> coups = GenerateurCoup.genererCoups(etat);

        if (coups.isEmpty()) {
            return null;
        }

        trierCoups(etat, coups, true);

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

    /**
     * Évalue récursivement les états suivants.
     *
     * @param etat état à évaluer.
     * @param profondeur profondeur restante.
     * @param alpha meilleure valeur déjà garantie au maximiseur.
     * @param beta meilleure valeur déjà garantie au minimiseur.
     * @return score estimé de l'état.
     */
    public int minimax(EtatJeu etat, int profondeur, int alpha, int beta) {
        if (profondeur == 0 || etat.estTerminal()) {
            return evaluateur.evaluer(etat, indexIA);
        }

        List<CoupIA> coups = GenerateurCoup.genererCoups(etat);

        if (coups.isEmpty()) {
            return evaluateur.evaluer(etat, indexIA);
        }

        if (profondeur > 2) {
            trierCoups(etat, coups, etat.joueurCourant == indexIA);
        }
        limiterCoups(coups, profondeur);

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

    /**
     * Trie les coups pour rendre l'élagage plus efficace.
     *
     * @param etat état de départ.
     * @param coups coups à trier sur place.
     * @param maximiser {@code true} si le joueur courant cherche le meilleur score.
     */
    private void trierCoups(EtatJeu etat, List<CoupIA> coups, boolean maximiser) {
        List<CoupNote> coupsNotes = new ArrayList<>(coups.size());

        for (CoupIA coup : coups) {
            EtatJeu suivant = etat.appliquerCoup(coup);
            int score = evaluateur.evaluer(suivant, indexIA);
            coupsNotes.add(new CoupNote(coup, score));
        }

        Comparator<CoupNote> comparateur = Comparator.comparingInt(coupNote -> coupNote.score);
        coupsNotes.sort(maximiser ? comparateur.reversed() : comparateur);

        coups.clear();
        for (CoupNote coupNote : coupsNotes) {
            coups.add(coupNote.coup);
        }
    }

    /**
     * Limite le nombre de coups explorés selon la profondeur restante.
     *
     * @param coups liste de coups modifiée sur place.
     * @param profondeur profondeur restante.
     */
    private void limiterCoups(List<CoupIA> coups, int profondeur) {
        int limite = profondeur > 2
                ? ConstantesJeu.MAX_COUPS_MINIMAX_PROFONDEUR_HAUTE
                : ConstantesJeu.MAX_COUPS_MINIMAX_PROFONDEUR_BASSE;

        if (coups.size() > limite) {
            coups.subList(limite, coups.size()).clear();
        }
    }

    /**
     * Associe un coup à sa note pour le tri interne.
     */
    private static class CoupNote {
        private final CoupIA coup;
        private final int score;

        private CoupNote(CoupIA coup, int score) {
            this.coup = coup;
            this.score = score;
        }
    }
}
