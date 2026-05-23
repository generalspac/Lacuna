package view.animations;

import java.awt.Image;
import java.awt.Point;

import javax.swing.SwingUtilities;

import model.Coordonnees;
import model.Fleur;
import model.Joueur;
import model.IA.CoupIA;
import view.DragLayer;
import view.JeuGraphique;

/**
 * Orchestre toutes les animations visuelles du jeu.
 */
public class Animation {

    private final JeuGraphique view;
    private final AnimationContext ctx;

    private final WinnerAnimator winnerAnimator;
    private final Phase2Animator phase2Animator;
    private final IAAnimator iaAnimator;

    public Animation(JeuGraphique view, AnimationContext ctx) {
        this.view = view;
        this.ctx = ctx;

        this.winnerAnimator = new WinnerAnimator(view, ctx);
        this.phase2Animator = new Phase2Animator(view, ctx, this, winnerAnimator);
        this.iaAnimator = new IAAnimator(view, ctx, this, phase2Animator);
    }

    // -------------------------- ACCESSEURS ----------------------------------- //

    public AnimationState getState() {
        return ctx.getState();
    }

    public void setState(AnimationState s) {
        ctx.setState(s);
    }

    // -------------------------- PHASE 2 -------------------------------------- //

    public boolean lancerAnimationSecondePhase() {
        return phase2Animator.lancerAnimationSecondePhase();
    }

    // -------------------------- IA ------------------------------------------- //

    /**
     * Anime le coup choisi par l'IA, puis laisse la vue décider si un autre tour IA doit suivre.
     *
     * @param coup coup calculé par l'IA, ou {@code null} si elle doit jouer le coup de secours.
     */
    public void animerPuisJouerCoupIA(CoupIA coup) {
        iaAnimator.animerPuisJouerCoupIA(coup);
    }

    public void animerAvantageIA() {
        iaAnimator.animerAvantageIA();
    }

    // -------------------------- ANIMATION : GAGNANT -------------------------- //

    public void animerGagnant(int generation) {
        winnerAnimator.animerGagnant(generation);
    }

    // -------------------------- ANIMATION : FLEURS → SCORE ------------------- //

    public void animerFleursVersScore(Fleur[] fleursAnimees, Joueur joueur, Runnable fin) {

        if (fleursAnimees == null || fleursAnimees.length == 0) {
            if (fin != null) {
                fin.run();
            }
            return;
        }

        DragLayer layer = (DragLayer) view.getFrame().getGlassPane();

        final int[] restantes = {0};

        for (int i = 0; i < fleursAnimees.length; i++) {

            Fleur f = fleursAnimees[i];

            if (f == null || f.getPosition() == null) {
                continue;
            }

            Image img = view.getFlowerImage(f.getType());

            Coordonnees screen =
                    view.modelToScreen(f.getPosition().x, f.getPosition().y);

            Point start = SwingUtilities.convertPoint(
                    view,
                    new Point(screen.getX(), screen.getY()),
                    layer
            );

            Point end = view.pointScorePourJoueur(joueur, i, layer);

            restantes[0]++;

            layer.animateImage(img, start, end, 750, 34, () -> {

                restantes[0]--;

                if (restantes[0] <= 0 && fin != null) {
                    fin.run();
                }
            });
        }

        if (restantes[0] == 0 && fin != null) {
            fin.run();
        }
    }
}
