package model.IA;

import java.util.function.Consumer;

/**
 * Exécute le calcul d'un coup IA hors du thread graphique.
 */
public class ThreadIA extends Thread {

    private final IA ia;
    private final EtatJeu etat;
    private final Consumer<CoupIA> actionQuandCoupTrouve;

    /**
     * Prépare le calcul d'un coup.
     *
     * @param ia IA qui choisira le coup.
     * @param etat copie de l'état du jeu.
     * @param actionQuandCoupTrouve callback appelé avec le coup trouvé.
     */
    public ThreadIA(IA ia, EtatJeu etat, Consumer<CoupIA> actionQuandCoupTrouve) {
        this.ia = ia;
        this.etat = etat;
        this.actionQuandCoupTrouve = actionQuandCoupTrouve;
    }

    /**
     * Lance le calcul et renvoie {@code null} au callback en cas d'erreur.
     */
    @Override
    public void run() {
        try {
            CoupIA coup = ia.choisirCoup(etat);

            if (actionQuandCoupTrouve != null) {
                actionQuandCoupTrouve.accept(coup);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (actionQuandCoupTrouve != null) {
                actionQuandCoupTrouve.accept(null);
            }
        }
    }
}
