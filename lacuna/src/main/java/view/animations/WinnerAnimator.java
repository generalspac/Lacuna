package view.animations;

public class WinnerAnimator {

    private final AnimationContext context;

    public WinnerAnimator(AnimationContext context) {
        this.context = context;
    }

    public void play(Runnable onComplete) {

        context.setAnimationState(AnimationState.GAGNANT);

        context.setFeedbackMessage("Fin de partie");

        context.repaintBoard();

        SwingTimers.after(2500, () -> {

            context.setAnimationState(AnimationState.AUCUNE);

            if (onComplete != null) {
                onComplete.run();
            }
        });
    }
}