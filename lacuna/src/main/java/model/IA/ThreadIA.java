package model.IA;

import java.util.function.Consumer;

public class ThreadIA extends Thread {

    private final IA ia;
    private final EtatJeu etat;
    private final Consumer<CoupIA> actionQuandCoupTrouve;

    public ThreadIA (IA ia, EtatJeu etat, Consumer<CoupIA> actionQuandCoupTrouve) {
        this.ia = ia;
        this.etat = etat;
        this.actionQuandCoupTrouve = actionQuandCoupTrouve;
    }

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
