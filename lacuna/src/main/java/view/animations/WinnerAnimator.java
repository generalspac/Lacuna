package view.animations;

import javax.swing.Timer;

import model.Joueur;
import view.JeuGraphique;

/**
 * ---------------------- ANIMATION GAGNANT ------------------------ //
 *
 * Gère l'animation de fin de partie affichant
 * le joueur gagnant avec un effet visuel pulsé.
 */
public class WinnerAnimator {

    /**
     * Vue graphique principale.
     */
    private final JeuGraphique view;

    /**
     * Contexte partagé des animations.
     */
    private final AnimationContext ctx;

    // ---------------------- CONSTRUCTEUR ------------------------ //

    /**
     * Crée le gestionnaire d'animation du gagnant.
     *
     * @param view Vue graphique principale
     * @param ctx  Contexte partagé des animations
     */
    public WinnerAnimator(JeuGraphique view, AnimationContext ctx) {
        this.view = view;
        this.ctx  = ctx;
    }

    // ---------------------- ANIMATION : GAGNANT ------------------------ //

    /**
     * Lance l'animation de victoire du joueur gagnant.
     *
     * L'animation applique un effet pulsé temporaire
     * avant de remettre le système d'animation à l'état neutre.
     *
     * @param generation Génération d'animation actuelle
     */
    public void animerGagnant(int generation) {

        if (!ctx.estGenerationValide(generation)) {
            return;
        }

        Joueur gagnant = view.getJeu().getGagnant();

        ctx.setGagnantAnime(gagnant);
        ctx.setWinnerPulse(0);

        String nom = (gagnant == null)
                ? "égalité"
                : view.getJeu().nomJoueurPourAffichage(gagnant);

        view.getJeu().setFeedbackMessage(
                "Partie terminée : " + nom + " gagne !"
        );

        view.refreshScores();

        view.repaint();

        SwingTimers.startRepeating(120, e -> {

            if (!ctx.estGenerationValide(generation)) {

                ((Timer) e.getSource()).stop();

                return;
            }

            ctx.incrementerWinnerPulse();

            view.repaint();

            if (ctx.getWinnerPulse() >= 26) {

                ((Timer) e.getSource()).stop();

                ctx.setState(AnimationState.AUCUNE);

                view.refreshScores();

                view.repaint();
            }
        });
    }
}