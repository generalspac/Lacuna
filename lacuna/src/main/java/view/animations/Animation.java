package view.animations;

import java.awt.*;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.util.ArrayList;

import model.Coordonnees;
import model.Fleur;
import model.Joueur;
import model.Phase2;
import model.IA.CoupIA;
import view.DragLayer;
import view.JeuGraphique;

/**
 * Orchestre toutes les animations visuelles du jeu.
 *
 * Cette classe ne stocke plus aucun état mutable d'animation : tout est
 * délégué à {@link AnimationContext}. Elle ne fait que lire/écrire ce contexte
 * et programmer des Timers Swing.
 *
 * Le mécanisme d'annulation repose sur la « génération » :
 * chaque nouveau Timer capture la génération courante au démarrage et
 * s'arrête immédiatement si la génération a changé (annulation ou reset).
 */
public class Animation {

    private final JeuGraphique  view;
    private final AnimationContext ctx;

    public Animation(JeuGraphique view, AnimationContext ctx) {
        this.view = view;
        this.ctx  = ctx;
    }

    // -------------------------- ACCESSEURS ----------------------------------- //

    public AnimationState getState()                  { return ctx.getState(); }
    public void           setState(AnimationState s)  { ctx.setState(s); }

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

    public void animerAttributionPhase2(ArrayList<Phase2.AttributionFleur> attributions,
                                        int index, int generation) {
        if (!ctx.estGenerationValide(generation)) return;

        if (index >= attributions.size()) {
            ctx.clearPhase2();
            animerGagnant(generation);
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

            animerFleursVersScore(new Fleur[]{attribution.fleur}, attribution.joueur, () -> {
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

    // -------------------------- ANIMATION : FLEURS → SCORE ------------------- //

    public void animerFleursVersScore(Fleur[] fleursAnimees, Joueur joueur, Runnable fin) {
        if (fleursAnimees == null || fleursAnimees.length == 0) {
            if (fin != null) fin.run();
            return;
        }

        DragLayer layer = (DragLayer) view.getFrame().getGlassPane();
        final int[] restantes = {0};

        for (int i = 0; i < fleursAnimees.length; i++) {
            Fleur f = fleursAnimees[i];
            if (f == null || f.getPosition() == null) continue;

            Image img = view.getFlowerImage(f.getType());
            Coordonnees screen = view.modelToScreen(f.getPosition().x, f.getPosition().y);
            Point start = SwingUtilities.convertPoint(view,
                    new Point(screen.getX(), screen.getY()), layer);
            Point end = view.pointScorePourJoueur(joueur, i, layer);

            restantes[0]++;
            layer.animateImage(img, start, end, 750, 34, () -> {
                restantes[0]--;
                if (restantes[0] <= 0 && fin != null) fin.run();
            });
        }

        if (restantes[0] == 0 && fin != null) fin.run();
    }

    // -------------------------- ANIMATION : GAGNANT -------------------------- //

    public void animerGagnant(int generation) {
        if (!ctx.estGenerationValide(generation)) return;

        Joueur gagnant = view.getJeu().getGagnant();
        ctx.setGagnantAnime(gagnant);
        ctx.setWinnerPulse(0);

        String nom = (gagnant == null) ? "égalité"
                : view.getJeu().nomJoueurPourAffichage(gagnant);
        view.getJeu().setFeedbackMessage("Partie terminée : " + nom + " gagne !");
        view.refreshScores();
        view.repaint();

        Timer timer = new Timer(120, null);
        timer.addActionListener(e -> {
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
        timer.start();
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

        new Timer(700, attente -> {
            ((Timer) attente.getSource()).stop();
            if (!ctx.estGenerationValide(generation)) return;

            view.getJeu().selectionnerFleursPourAnimation(f1, f2);
            view.getJeu().setFeedbackMessage("IA (" + joueurIA.getTypePion() + ") sélectionne ses deux fleurs...");
            view.refreshScores();
            view.repaint();

            new Timer(950, e -> {
                ((Timer) e.getSource()).stop();
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

                new Timer(900, e2 -> {
                    ((Timer) e2.getSource()).stop();
                    if (!ctx.estGenerationValide(generation)) return;

                    view.getJeu().setFeedbackMessage("IA (" + joueurIA.getTypePion() + ") collecte ses fleurs...");
                    animerFleursVersScore(new Fleur[]{f1Animee, f2Animee}, joueurIA, () -> {
                        if (!ctx.estGenerationValide(generation)) return;
                        view.getJeu().collecterFleursCoupIA(coup);
                        ctx.setState(AnimationState.AUCUNE);
                        ctx.setSelectionIAVisuelle(false);
                        view.getJeu().resetFleursSelectionnee1();
                        view.getJeu().resetFleursSelectionnee2();
                        view.refreshScores();
                        view.repaint();
                        lancerAnimationSecondePhase();
                    });
                }).start();
            }).start();
        }).start();
    }

    // -------------------------- PULSE COMPARAISON PHASE 2 -------------------- //

    private void lancerPulseComparaisonPhase2(int generation) {
        Timer timer = new Timer(110, null);
        timer.addActionListener(e -> {
            if (!ctx.estGenerationValide(generation) || ctx.getPionPhase2Compare() == null) {
                ((Timer) e.getSource()).stop();
                return;
            }
            ctx.incrementerPhase2ComparePulse();
            view.repaint();
            if (ctx.getPhase2ComparePulse() >= 18) {
                ((Timer) e.getSource()).stop();
            }
        });
        timer.start();
    }
}