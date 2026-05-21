package view.animations;

import javax.swing.Timer;

import model.Fleur;
import model.Joueur;
import model.IA.CoupIA;
import view.JeuGraphique;

public class IAAnimator {

    private final JeuGraphique view;
    private final AnimationContext ctx;
    private final Animation animation;
    private final Phase2Animator phase2Animator;

    public IAAnimator(JeuGraphique view,
                      AnimationContext ctx,
                      Animation animation,
                      Phase2Animator phase2Animator) {
        this.view            = view;
        this.ctx             = ctx;
        this.animation       = animation;
        this.phase2Animator  = phase2Animator;
    }

    // -------------------------- ANIMATION : COUP IA -------------------------- //

    public void animerPuisJouerCoupIA(CoupIA coup) {
        if (coup == null) {
            view.getJeu().jouerCoupIA(null);
            return;
        }

        Fleur f1 = view.retrouverFleurGraphique(coup.fleur1);
        Fleur f2 = view.retrouverFleurGraphique(coup.fleur2);
        Joueur joueurIA = view.getJeu().getJoueurActuel();

        if (f1 == null || f2 == null) {
            view.getJeu().jouerCoupIA(coup);
            return;
        }

        Fleur f1Animee = Fleur.copieFleur(f1);
        Fleur f2Animee = Fleur.copieFleur(f2);

        ctx.setState(AnimationState.COUP_IA);
        ctx.setSelectionIAVisuelle(true);
        final int generation = ctx.nouvelleGeneration();

        view.getJeu().setFeedbackMessage("IA (" + joueurIA.getTypePion() + ") prépare son coup...");
        view.refreshScores();
        view.repaint();

        SwingTimers.startSingle(700, attente -> {
            if (!ctx.estGenerationValide(generation)) return;

            view.getJeu().selectionnerFleursPourAnimation(f1, f2);
            view.getJeu().setFeedbackMessage("IA (" + joueurIA.getTypePion() + ") sélectionne ses deux fleurs...");
            view.refreshScores();
            view.repaint();

            SwingTimers.startSingle(950, e -> {
                if (!ctx.estGenerationValide(generation)) return;

                view.getJeu().setFeedbackMessage("IA (" + joueurIA.getTypePion() + ") place son pion...");
                if (!view.getJeu().placerPionCoupIA(coup)) {
                    ctx.setState(AnimationState.AUCUNE);
                    ctx.setSelectionIAVisuelle(false);
                    view.getJeu().resetFleursSelectionnee1();
                    view.getJeu().resetFleursSelectionnee2();
                    view.refreshScoresEtTourIA();
                    return;
                }
                view.refreshScores();
                view.repaint();

                SwingTimers.startSingle(900, e2 -> {
                    if (!ctx.estGenerationValide(generation)) return;

                    view.getJeu().setFeedbackMessage("IA (" + joueurIA.getTypePion() + ") collecte ses fleurs...");
                    animation.animerFleursVersScore(new Fleur[]{f1Animee, f2Animee}, joueurIA, () -> {
                        if (!ctx.estGenerationValide(generation)) return;
                        view.getJeu().collecterFleursCoupIA(coup);
                        ctx.setState(AnimationState.AUCUNE);
                        ctx.setSelectionIAVisuelle(false);
                        view.getJeu().resetFleursSelectionnee1();
                        view.getJeu().resetFleursSelectionnee2();
                        view.refreshScores();
                        view.repaint();
                        phase2Animator.lancerAnimationSecondePhase();
                    });
                });
            });
        });
    }
}