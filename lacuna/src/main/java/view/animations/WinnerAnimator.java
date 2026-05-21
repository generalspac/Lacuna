package view.animations;

import javax.swing.Timer;

import model.Joueur;
import view.JeuGraphique;

public class WinnerAnimator {

    private final JeuGraphique view;
    private final AnimationContext ctx;

    public WinnerAnimator(JeuGraphique view, AnimationContext ctx) {
        this.view = view;
        this.ctx  = ctx;
    }

    // -------------------------- ANIMATION : GAGNANT -------------------------- //

    public void animerGagnant(int generation) {
        if (!ctx.estGenerationValide(generation)) return;

        Joueur gagnant = view.getJeu().getGagnant();
        ctx.setGagnantAnime(gagnant);
        ctx.setWinnerPulse(0);

        String nom = (gagnant == null) ? "égalité"
                : view.getJeu().nomJoueurPourAffichage(gagnant);
        view.getJeu().setFeedbackMessage("Partie terminée : " + nom + " gagne !");
        view.refreshScores();
        view.repaint();

        /*
        Timer timer = new Timer(120, null);
        timer.addActionListener(e -> {
            if (!ctx.estGenerationValide(generation)) {
                ((Timer) e.getSource()).stop();
                return;
            }
            ctx.incrementerWinnerPulse();
            view.repaint();
            if (ctx.getWinnerPulse() >= 26) {
                ((Timer) e.getSource()).stop();
                ctx.setState(AnimationState.AUCUNE);
                view.refreshScores();
                view.repaint();
            }
        });
        timer.start();
        */

        SwingTimers.startRepeating(120, e -> {

            if (!ctx.estGenerationValide(generation)) {
                ((Timer) e.getSource()).stop();
                return;
            }

            ctx.incrementerWinnerPulse();

            view.repaint();

            if (ctx.getWinnerPulse() >= 26) {

                ((Timer) e.getSource()).stop();

                ctx.setState(AnimationState.AUCUNE);

                view.refreshScores();
                view.repaint();
            }
        });
    }
}