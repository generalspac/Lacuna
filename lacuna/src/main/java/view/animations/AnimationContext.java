package view.animations;

import model.*;

public interface AnimationContext {

    Jeu getJeu();

    void refreshScores();

    void repaintBoard();

    void setFeedbackMessage(String text);

    void resetFlowerSelections();

    void setAnimationState(AnimationState state);

    AnimationState getAnimationState();

    void onAnimationFinished();

    Fleur findDisplayedFlower(Fleur fleur);

    void animateFlowersToScore(
            Fleur[] fleurs,
            Joueur joueur,
            Runnable onDone
    );

    void startPhase2Animation();

    void startWinnerAnimation();

    boolean isAnimationCancelled();

    int getAnimationGeneration();

    double getAnimationSpeedFactor();
}