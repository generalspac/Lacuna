package view.animations;

import model.*;

import java.util.ArrayList;

public class Phase2Animator {

    private final AnimationContext context;

    public Phase2Animator(AnimationContext context) {
        this.context = context;
    }

    public void play(
            ArrayList<Phase2.AttributionFleur> attributions,
            Runnable onComplete
    ) {

        playNext(attributions, 0, onComplete);
    }

    private void playNext(
            ArrayList<Phase2.AttributionFleur> attributions,
            int index,
            Runnable onComplete
    ) {

        if (index >= attributions.size()) {

            context.setAnimationState(AnimationState.AUCUNE);

            if (onComplete != null) {
                onComplete.run();
            }

            return;
        }

        Phase2.AttributionFleur attribution = attributions.get(index);

        if (attribution == null) {
            playNext(attributions, index + 1, onComplete);
            return;
        }

        context.setAnimationState(AnimationState.PHASE2);

        context.setFeedbackMessage(
                "Attribution de la fleur "
                        + attribution.fleur.getType()
        );

        context.refreshScores();
        context.repaintBoard();

        int delay = (int)(1200 * context.getAnimationSpeedFactor());

        SwingTimers.after(delay, () -> {

            context.animateFlowersToScore(
                    new Fleur[]{attribution.fleur},
                    attribution.joueur,
                    () -> {

                        context.getJeu()
                                .appliquerAttributionFleur(attribution);

                        context.refreshScores();
                        context.repaintBoard();

                        playNext(attributions, index + 1, onComplete);
                    }
            );
        });
    }
}