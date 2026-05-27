package view.animations;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import model.Fleur;
import model.Joueur;
import model.IA.CoupIA;
import view.JeuGraphique;

/**
 * --------------------- ANIMATEUR IA -------------------------- //
 *
 * Gère toutes les animations liées
 * aux actions de l’intelligence artificielle.
 *
 * Cette classe orchestre :
 * - les animations de coups IA
 * - les animations d’avantage initial
 * - les transitions visuelles associées
 */
public class IAAnimator {

    // --------------------- ATTRIBUTS -------------------------- //

    /**
     * Vue principale du jeu.
     */
    private final JeuGraphique view;

    /**
     * Contexte partagé des animations.
     */
    private final AnimationContext ctx;

    /**
     * Gestionnaire principal des animations.
     */
    private final Animation animation;

    /**
     * Gestionnaire des animations
     * de seconde phase.
     */
    private final Phase2Animator phase2Animator;

    // --------------------- CONSTRUCTEUR -------------------------- //

    /**
     * Initialise l’animateur IA.
     *
     * @param view             Vue principale du jeu
     * @param ctx              Contexte des animations
     * @param animation        Gestionnaire principal
     * @param phase2Animator   Animateur de phase 2
     */
    public IAAnimator(
            JeuGraphique view,
            AnimationContext ctx,
            Animation animation,
            Phase2Animator phase2Animator) {

        this.view = view;
        this.ctx = ctx;
        this.animation = animation;
        this.phase2Animator = phase2Animator;
    }

    // --------------------- ANIMATION : AVANTAGE IA -------------------------- //

    /**
     * Anime le choix d’avantage initial
     * effectué par l’IA.
     */
    public void animerAvantageIA() {

        List<Fleur> fleurs = view.getJeu().getFleurs();

        if (!view.getJeu().isEnPhaseSelectionAvantage()
                || !view.getJeu().estTourIA()
                || fleurs.isEmpty()) {

            return;
        }

        Fleur fleur =
                fleurs.get(
                        ThreadLocalRandom.current()
                                .nextInt(fleurs.size())
                );

        Fleur fleurAnimee = Fleur.copieFleur(fleur);

        Joueur joueurIA =
                view.getJeu().getJoueurActuel();

        ctx.setState(AnimationState.COUP_IA);

        ctx.setSelectionIAVisuelle(true);

        final int generation =
                ctx.nouvelleGeneration();

        view.getJeu()
                .selectionnerFleursPourAnimation(fleur, null);

        view.getJeu().setFeedbackMessage(
                "IA (" + joueurIA.getTypePion()
                        + ") choisit son avantage initial..."
        );

        view.refreshScores();

        view.repaint();

        SwingTimers.startSingle(900, attente -> {

            if (!ctx.estGenerationValide(generation)) {
                return;
            }

            animation.animerFleursVersScore(
                    new Fleur[]{fleurAnimee},
                    joueurIA,
                    () -> {

                        if (!ctx.estGenerationValide(generation)) {
                            return;
                        }

                        view.getJeu()
                                .appliquerAvantageAvecFleur(fleur);

                        view.getJeu().setFeedbackMessage(
                                "IA (" + joueurIA.getTypePion()
                                        + ") collecte une fleur "
                                        + fleur.getType()
                                        + " pour l'avantage initial."
                        );

                        ctx.setState(AnimationState.AUCUNE);

                        ctx.setSelectionIAVisuelle(false);

                        view.getJeu().resetFleursSelectionnee1();

                        view.getJeu().resetFleursSelectionnee2();

                        view.refreshScoresEtTourIA();
                    }
            );
        });
    }

    // --------------------- ANIMATION : COUP IA -------------------------- //

    /**
     * Anime un coup joué par l’IA.
     *
     * @param coup Coup calculé par l’IA
     */
    public void animerPuisJouerCoupIA(CoupIA coup) {

        if (coup == null) {

            view.getJeu().jouerCoupIA(null);

            return;
        }

        Fleur f1 =
                view.retrouverFleurGraphique(coup.fleur1);

        Fleur f2 =
                view.retrouverFleurGraphique(coup.fleur2);

        Joueur joueurIA =
                view.getJeu().getJoueurActuel();

        if (f1 == null || f2 == null) {

            view.getJeu().jouerCoupIA(coup);

            return;
        }

        Fleur f1Animee = Fleur.copieFleur(f1);

        Fleur f2Animee = Fleur.copieFleur(f2);

        ctx.setState(AnimationState.COUP_IA);

        ctx.setSelectionIAVisuelle(true);

        final int generation =
                ctx.nouvelleGeneration();

        view.getJeu().setFeedbackMessage(
                "IA (" + joueurIA.getTypePion()
                        + ") prépare son coup..."
        );

        view.refreshScores();

        view.repaint();

        SwingTimers.startSingle(700, attente -> {

            if (!ctx.estGenerationValide(generation)) {
                return;
            }

            view.getJeu()
                    .selectionnerFleursPourAnimation(f1, f2);

            view.getJeu().setFeedbackMessage(
                    "IA (" + joueurIA.getTypePion()
                            + ") sélectionne ses deux fleurs..."
            );

            view.refreshScores();

            view.repaint();

            SwingTimers.startSingle(950, e -> {

                if (!ctx.estGenerationValide(generation)) {
                    return;
                }

                view.getJeu().setFeedbackMessage(
                        "IA (" + joueurIA.getTypePion()
                                + ") place son pion..."
                );

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

                    if (!ctx.estGenerationValide(generation)) {
                        return;
                    }

                    view.getJeu().setFeedbackMessage(
                            "IA (" + joueurIA.getTypePion()
                                    + ") collecte ses fleurs..."
                    );

                    animation.animerFleursVersScore(
                            new Fleur[]{f1Animee, f2Animee},
                            joueurIA,
                            () -> {

                                if (!ctx.estGenerationValide(generation)) {
                                    return;
                                }

                                view.getJeu()
                                        .collecterFleursCoupIA(coup);

                                ctx.setState(AnimationState.AUCUNE);

                                ctx.setSelectionIAVisuelle(false);

                                view.getJeu().resetFleursSelectionnee1();

                                view.getJeu().resetFleursSelectionnee2();

                                view.refreshScoresEtTourIA();
                            }
                    );
                });
            });
        });
    }
}