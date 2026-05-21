package view.animations;

import java.util.ArrayList;

import javax.swing.Timer;

import model.Phase2;
import view.JeuGraphique;

public class Phase2Animator {

    private final JeuGraphique view;
    private final AnimationContext ctx;
    private final Animation animation;
    private final WinnerAnimator winnerAnimator;

    public Phase2Animator(JeuGraphique view,
                          AnimationContext ctx,
                          Animation animation,
                          WinnerAnimator winnerAnimator) {
        this.view             = view;
        this.ctx              = ctx;
        this.animation        = animation;
        this.winnerAnimator   = winnerAnimator;
    }

    // -------------------------- PHASE 2 : ATTRIBUTION ------------------------ //

    /**
     * Lance l'animation de la phase 2 si les conditions sont réunies.
     * @return true si l'animation a été démarrée.
     */
    public boolean lancerAnimationSecondePhase() {
        if (ctx.getState() == AnimationState.PHASE2
                || !view.getJeu().estSecondePhaseDisponible()) {
            return false;
        }

        ArrayList<Phase2.AttributionFleur> attributions = view.getJeu().calculerAttributionsFleursRestantes();
        if (attributions.isEmpty()) return false;

        ctx.setState(AnimationState.PHASE2);
        final int generation = ctx.nouvelleGeneration();

        view.getJeu().setFeedbackMessage("Phase 2 : attribution animée des fleurs restantes.");
        view.refreshScores();
        view.repaint();
        animerAttributionPhase2(attributions, 0, generation);
        return true;
    }

    public void animerAttributionPhase2(
            ArrayList<Phase2.AttributionFleur> attributions,
            int index,
            int generation
    ) {

        if (!ctx.estGenerationValide(generation)) return;

        if (index >= attributions.size()) {
            ctx.clearPhase2();
            winnerAnimator.animerGagnant(generation);
            return;
        }

        Phase2.AttributionFleur attribution = attributions.get(index);

        if (attribution == null
                || attribution.fleur == null
                || attribution.joueur == null
                || !view.getJeu().getFleurs().contains(attribution.fleur)) {

            animerAttributionPhase2(attributions, index + 1, generation);
            return;
        }

        view.getJeu().selectionnerFleursPourAnimation(
                attribution.fleur,
                null
        );
        ctx.setFleurPhase2Illustree(attribution.fleur);
        ctx.setPionPhase2Illustre(attribution.pionLePlusProche);
        ctx.setPionPhase2Compare(
                attribution.comparaisonNecessaire
                        ? attribution.pionCompare
                        : null
        );
        ctx.setPhase2ComparePulse(0);

        if (attribution.comparaisonNecessaire
                && attribution.pionCompare != null) {
            view.getJeu().setFeedbackMessage(
                    "Phase 2 : comparaison des distances avant attribution de la fleur "
                            + attribution.fleur.getType()
                            + "."
            );

            lancerPulseComparaisonPhase2(generation);

        } else {
            view.getJeu().setFeedbackMessage(
                    "Phase 2 : mesure de la distance avant attribution de la fleur "
                            + attribution.fleur.getType()
                            + "."
            );
        }

        view.refreshScores();
        view.repaint();

        double facteur = view.getFacteurVitesse();

        int delaiMesure = (int) (
                (attribution.comparaisonNecessaire
                        && attribution.pionCompare != null)
                        ? 1900 * facteur
                        : 1150 * facteur
        );

        SwingTimers.startSingle(delaiMesure, e -> {

            if (!ctx.estGenerationValide(generation)) return;

            view.getJeu().setFeedbackMessage(
                    "Phase 2 : la fleur "
                            + attribution.fleur.getType()
                            + " est attribuée à "
                            + view.getJeu().nomJoueurPourAffichage(
                                    attribution.joueur
                            )
                            + "."
            );

            view.refreshScores();
            view.repaint();

            animation.animerFleursVersScore(
                    new model.Fleur[]{attribution.fleur},
                    attribution.joueur,
                    () -> {

                        if (!ctx.estGenerationValide(generation)) return;
                        view.getJeu().appliquerAttributionFleur(attribution);

                        ctx.clearPhase2();

                        view.getJeu().resetFleursSelectionnee1();
                        view.getJeu().resetFleursSelectionnee2();

                        view.refreshScores();
                        view.repaint();

                        SwingTimers.startSingle(280, e2 -> {

                            animerAttributionPhase2(
                                    attributions,
                                    index + 1,
                                    generation
                            );
                        });
                    }
            );
        });
    }

    /*
    public void animerAttributionPhase2(ArrayList<Phase2.AttributionFleur> attributions,
                                        int index, int generation) {
        if (!ctx.estGenerationValide(generation)) return;

        if (index >= attributions.size()) {
            ctx.clearPhase2();
            winnerAnimator.animerGagnant(generation);
            return;
        }

        Phase2.AttributionFleur attribution = attributions.get(index);
        if (attribution == null || attribution.fleur == null || attribution.joueur == null
                || !view.getJeu().getFleurs().contains(attribution.fleur)) {
            animerAttributionPhase2(attributions, index + 1, generation);
            return;
        }

        view.getJeu().selectionnerFleursPourAnimation(attribution.fleur, null);
        ctx.setFleurPhase2Illustree(attribution.fleur);
        ctx.setPionPhase2Illustre(attribution.pionLePlusProche);
        ctx.setPionPhase2Compare(attribution.comparaisonNecessaire ? attribution.pionCompare : null);
        ctx.setPhase2ComparePulse(0);

        if (attribution.comparaisonNecessaire && attribution.pionCompare != null) {
            view.getJeu().setFeedbackMessage("Phase 2 : comparaison des distances avant attribution de la fleur "
                    + attribution.fleur.getType() + ".");
            lancerPulseComparaisonPhase2(generation);
        } else {
            view.getJeu().setFeedbackMessage("Phase 2 : mesure de la distance avant attribution de la fleur "
                    + attribution.fleur.getType() + ".");
        }
        view.refreshScores();
        view.repaint();

        double facteur     = view.getFacteurVitesse();
        int    delaiMesure = (int) ((attribution.comparaisonNecessaire && attribution.pionCompare != null)
                ? 1900 * facteur : 1150 * facteur);

        new Timer(delaiMesure, e -> {
            ((Timer) e.getSource()).stop();
            if (!ctx.estGenerationValide(generation)) return;

            view.getJeu().setFeedbackMessage("Phase 2 : la fleur " + attribution.fleur.getType()
                    + " est attribuée à " + view.getJeu().nomJoueurPourAffichage(attribution.joueur) + ".");
            view.refreshScores();
            view.repaint();

            animation.animerFleursVersScore(new Fleur[]{attribution.fleur}, attribution.joueur, () -> {
                if (!ctx.estGenerationValide(generation)) return;
                view.getJeu().appliquerAttributionFleur(attribution);
                ctx.clearPhase2();
                view.getJeu().resetFleursSelectionnee1();
                view.getJeu().resetFleursSelectionnee2();
                view.refreshScores();
                view.repaint();

                new Timer(280, e2 -> {
                    ((Timer) e2.getSource()).stop();
                    animerAttributionPhase2(attributions, index + 1, generation);
                }).start();
                
            });
        }).start();
    }

     */

    // -------------------------- PULSE COMPARAISON PHASE 2 -------------------- //

    private void lancerPulseComparaisonPhase2(int generation) {
        SwingTimers.startRepeating(110, e -> {

            if (!ctx.estGenerationValide(generation)
                    || ctx.getPionPhase2Compare() == null) {

                ((Timer) e.getSource()).stop();
                return;
            }

            ctx.incrementerPhase2ComparePulse();

            view.repaint();

            if(ctx.getPhase2ComparePulse() >= 18) {
                ((Timer) e.getSource()).stop();
            }
        });
    }
}