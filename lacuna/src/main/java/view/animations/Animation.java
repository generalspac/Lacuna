package view.animations;

import java.awt.Image;
import java.awt.Point;

import javax.swing.SwingUtilities;

import model.Coordonnees;
import model.Fleur;
import model.Joueur;
import model.IA.CoupIA;
import view.JeuGraphique;
import view.component.DragLayer;

/**
 * --------------------- GESTIONNAIRE D'ANIMATIONS -------------------------- //
 *
 * Orchestre toutes les animations visuelles du jeu.
 *
 * Cette classe centralise :
 * - les animations IA
 * - les animations de victoire
 * - les animations de transition de phase
 * - les animations des fleurs vers le score
 */
public class Animation {

    // --------------------- ATTRIBUTS -------------------------- //

    /**
     * Vue principale du jeu.
     */
    private final JeuGraphique view;

    /**
     * Contexte partagé entre les animations.
     */
    private final AnimationContext ctx;

    /**
     * Gestionnaire des animations de victoire.
     */
    private final WinnerAnimator winnerAnimator;

    /**
     * Gestionnaire des animations
     * de seconde phase.
     */
    private final Phase2Animator phase2Animator;

    /**
     * Gestionnaire des animations IA.
     */
    private final IAAnimator iaAnimator;

    // --------------------- CONSTRUCTEUR -------------------------- //

    /**
     * Initialise le gestionnaire d’animations.
     *
     * @param view Vue principale du jeu
     * @param ctx  Contexte partagé des animations
     */
    public Animation(JeuGraphique view, AnimationContext ctx) {

        this.view = view;
        this.ctx = ctx;

        this.winnerAnimator = new WinnerAnimator(view, ctx);

        this.phase2Animator =
                new Phase2Animator(view, ctx, this, winnerAnimator);

        this.iaAnimator =
                new IAAnimator(view, ctx, this, phase2Animator);
    }

    // --------------------- ÉTAT DES ANIMATIONS -------------------------- //

    /**
     * Retourne l’état actuel des animations.
     *
     * @return État courant
     */
    public AnimationState getState() {
        return ctx.getState();
    }

    /**
     * Modifie l’état actuel des animations.
     *
     * @param s Nouvel état
     */
    public void setState(AnimationState s) {
        ctx.setState(s);
    }

    // --------------------- PHASE 2 -------------------------- //

    /**
     * Lance l’animation de transition
     * vers la seconde phase.
     *
     * @return true si l’animation démarre
     */
    public boolean lancerAnimationSecondePhase() {
        return phase2Animator.lancerAnimationSecondePhase();
    }

    // --------------------- IA -------------------------- //

    /**
     * Anime le coup choisi par l’IA.
     *
     * Une fois l’animation terminée,
     * la vue peut décider si un autre
     * tour IA doit être joué.
     *
     * @param coup Coup calculé par l’IA
     *             ou null pour un coup de secours
     */
    public void animerPuisJouerCoupIA(CoupIA coup) {
        iaAnimator.animerPuisJouerCoupIA(coup);
    }

    /**
     * Anime le placement automatique
     * d’un avantage IA.
     */
    public void animerAvantageIA() {
        iaAnimator.animerAvantageIA();
    }

    // --------------------- GAGNANT -------------------------- //

    /**
     * Lance l’animation de victoire.
     *
     * @param generation Génération actuelle
     *                   de l’animation
     */
    public void animerGagnant(int generation) {
        winnerAnimator.animerGagnant(generation);
    }

    // --------------------- FLEURS → SCORE -------------------------- //

    /**
     * Anime des fleurs vers la zone de score
     * d’un joueur.
     *
     * @param fleursAnimees Fleurs à animer
     * @param joueur        Joueur recevant les points
     * @param fin           Callback exécuté à la fin
     *                      de toutes les animations
     */
    public void animerFleursVersScore(
            Fleur[] fleursAnimees,
            Joueur joueur,
            Runnable fin) {

        if (fleursAnimees == null || fleursAnimees.length == 0) {

            if (fin != null) {
                fin.run();
            }

            return;
        }

        DragLayer layer =
                (DragLayer) view.getFrame().getGlassPane();

        final int[] restantes = {0};

        for (int i = 0; i < fleursAnimees.length; i++) {

            Fleur f = fleursAnimees[i];

            if (f == null || f.getPosition() == null) {
                continue;
            }

            Image img = view.getFlowerImage(f.getType());

            Coordonnees screen =
                    view.modelToScreen(
                            f.getPosition().x,
                            f.getPosition().y
                    );

            Point start = SwingUtilities.convertPoint(
                    view,
                    new Point(screen.getX(), screen.getY()),
                    layer
            );

            Point end =
                    view.pointScorePourJoueur(joueur, i, layer);

            restantes[0]++;

            layer.animateImage(
                    img,
                    start,
                    end,
                    750,
                    34,
                    () -> {

                        restantes[0]--;

                        if (restantes[0] <= 0 && fin != null) {
                            fin.run();
                        }
                    }
            );
        }

        if (restantes[0] == 0 && fin != null) {
            fin.run();
        }
    }
}