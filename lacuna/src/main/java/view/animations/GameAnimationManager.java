package view.animations;

import model.IA.CoupIA;
import model.Phase2;

import java.util.ArrayList;

public class GameAnimationManager {

    private final IAAnimationSequence iaAnimation;
    private final Phase2Animator phase2Animator;
    private final WinnerAnimator winnerAnimator;

    public GameAnimationManager(AnimationContext context) {

        this.iaAnimation = new IAAnimationSequence(context);

        this.phase2Animator = new Phase2Animator(context);

        this.winnerAnimator = new WinnerAnimator(context);
    }

    public void playIaMove(
            CoupIA coup,
            Runnable onComplete
    ) {

        iaAnimation.play(coup, onComplete);
    }

    public void playPhase2(
            ArrayList<Phase2.AttributionFleur> attributions,
            Runnable onComplete
    ) {

        phase2Animator.play(attributions, onComplete);
    }

    public void playWinner(
            Runnable onComplete
    ) {

        winnerAnimator.play(onComplete);
    }
}