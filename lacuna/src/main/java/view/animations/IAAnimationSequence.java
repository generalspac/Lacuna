package view.animations;

import model.*;
import model.IA.CoupIA;

public class IAAnimationSequence {

    private final AnimationContext context;

    public IAAnimationSequence(AnimationContext context) {
        this.context = context;
    }

    public void play(CoupIA coup, Runnable onComplete) {

        if (coup == null) {
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }

        Jeu jeu = context.getJeu();

        Fleur f1 = context.findDisplayedFlower(coup.fleur1);
        Fleur f2 = context.findDisplayedFlower(coup.fleur2);

        if (f1 == null || f2 == null) {
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }

        Joueur joueurIA = jeu.getJoueurActuel();

        context.setAnimationState(AnimationState.COUP_IA);

        context.setFeedbackMessage(
                "IA (" + joueurIA.getTypePion() + ") prépare son coup..."
        );

        context.refreshScores();
        context.repaintBoard();

        final int generation = context.getAnimationGeneration();

        SwingTimers.after(700, () -> {

            if (generation != context.getAnimationGeneration()) {
                return;
            }

            jeu.selectionnerFleursPourAnimation(f1, f2);

            context.setFeedbackMessage(
                    "IA (" + joueurIA.getTypePion() + ") sélectionne ses fleurs..."
            );

            context.refreshScores();
            context.repaintBoard();

            SwingTimers.after(900, () -> {

                if (generation != context.getAnimationGeneration()) {
                    return;
                }

                if (!jeu.placerPionCoupIA(coup)) {

                    context.resetFlowerSelections();
                    context.setAnimationState(AnimationState.AUCUNE);

                    if (onComplete != null) {
                        onComplete.run();
                    }

                    return;
                }

                context.refreshScores();
                context.repaintBoard();

                SwingTimers.after(700, () -> {

                    if (generation != context.getAnimationGeneration()) {
                        return;
                    }

                    context.animateFlowersToScore(
                            new Fleur[]{f1, f2},
                            joueurIA,
                            () -> {

                                jeu.collecterFleursCoupIA(coup);

                                context.resetFlowerSelections();
                                context.setAnimationState(AnimationState.AUCUNE);

                                context.refreshScores();
                                context.repaintBoard();

                                if (onComplete != null) {
                                    onComplete.run();
                                }
                            }
                    );
                });
            });
        });
    }
}