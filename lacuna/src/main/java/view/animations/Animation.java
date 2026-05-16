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

public class Animation {
    /*
        lancerPulseComparaisonPhase2()
    */

    private AnimationState state;
    private final JeuGraphique view;

    public Animation(JeuGraphique view) {
        this.view = view;
        state = AnimationState.AUCUNE;
    }

    public AnimationState getState() {
        return state;
    }
    
    public void setState(AnimationState state) {
        this.state = state;
    }
    


    public boolean lancerAnimationSecondePhase() {
        /*
            if (view.phase2Animee || animationEnCours || !view.getJeu().estSecondePhaseDisponible()) {
            return false;
        }
        */

        AnimationState state = view.getAnimationState();
        if (state == AnimationState.PHASE2 || !view.getJeu().estSecondePhaseDisponible()) {
            return false;
        }
        

        ArrayList<Phase2.AttributionFleur> attributions = view.getJeu().calculerAttributionsFleursRestantes();
        if (attributions.isEmpty()) {
            return false;
        }

        // phase2Animee = true;
        // animationEnCours = true;
        view.setAnimationState(AnimationState.PHASE2);

        final int generation = view.increaseGenerationAnimation();

        view.getJeu().setFeedbackMessage("Phase 2 : attribution animée des fleurs restantes.");
        view.refreshScores();
        view.repaint();
        animerAttributionPhase2(attributions, 0, generation);
        return true;
    }

    public void animerAttributionPhase2(ArrayList<Phase2.AttributionFleur> attributions, int index, int generation) {
        if (generation != view.getGenerationAnimation()) return;
        if (index >= attributions.size()) {
            /*
            fleurPhase2Illustree = null;
            pionPhase2Illustre = null;
            pionPhase2Compare = null;
            phase2ComparePulse = 0;
            animerGagnant(generation);
             */
            view.setFleurPhase2Illustree(null);
            view.setPionPhase2Illustre(null);
            view.setPionPhase2Compare(null);
            view.setPhase2ComparePulse(0);
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
        /*fleurPhase2Illustree = attribution.fleur;
        pionPhase2Illustre = attribution.pionLePlusProche;
        pionPhase2Compare = attribution.comparaisonNecessaire ? attribution.pionCompare : null;
        phase2ComparePulse = 0;
        */
        view.setFleurPhase2Illustree(attribution.fleur);
        view.setPionPhase2Illustre(attribution.pionLePlusProche);
        view.setPionPhase2Compare(attribution.comparaisonNecessaire ? attribution.pionCompare : null);
        view.setPhase2ComparePulse(0);

        if (attribution.comparaisonNecessaire && attribution.pionCompare != null) {
            view.getJeu().setFeedbackMessage("Phase 2 : comparaison des distances avant attribution de la fleur " + attribution.fleur.getType() + ".");
            lancerPulseComparaisonPhase2(generation);
        } else {
            view.getJeu().setFeedbackMessage("Phase 2 : mesure de la distance avant attribution de la fleur " + attribution.fleur.getType() + ".");
        }
        view.refreshScores();
        view.repaint();

        double facteurVitesse = view.getFacteurVitesse();
        // Délai avant attribution ajusté selon le facteur de vitesse du slider
        int delaiMesure = (int) ((attribution.comparaisonNecessaire && attribution.pionCompare != null) ? 1900 * facteurVitesse : 1150 * facteurVitesse);
        new Timer(delaiMesure, e -> {
            ((Timer) e.getSource()).stop();
            if (generation != view.getGenerationAnimation()) return;
            view.getJeu().setFeedbackMessage("Phase 2 : la fleur " + attribution.fleur.getType()
                    + " est attribuée à " + view.getJeu().nomJoueurPourAffichage(attribution.joueur) + ".");
            view.refreshScores();
            view.repaint();
            animerFleursVersScore(new Fleur[] { attribution.fleur }, attribution.joueur, () -> {
                if (generation != view.getGenerationAnimation()) return;
                view.getJeu().appliquerAttributionFleur(attribution);
                /*fleurPhase2Illustree = null;
                pionPhase2Illustre = null;
                pionPhase2Compare = null;
                phase2ComparePulse = 0;
                */
                view.setFleurPhase2Illustree(null);
                view.setPionPhase2Illustre(null);
                view.setPionPhase2Compare(null);
                view.setPhase2ComparePulse(0);
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

    public void animerFleursVersScore(Fleur[] fleursAnimees, Joueur joueur, Runnable fin) {
        if (fleursAnimees == null || fleursAnimees.length == 0) {
            if (fin != null) fin.run();
            return;
        }

        DragLayer layer = (DragLayer) view.getFrame().getGlassPane();
        final int[] restantes = { 0 };

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
                if (restantes[0] <= 0 && fin != null) {
                    fin.run();
                }
            });
        }

        if (restantes[0] == 0 && fin != null) {
            fin.run();
        }
    }

    public void animerGagnant(int generation) {
        if (generation != view.getGenerationAnimation()) return;
        Joueur gagnantAnime = view.getJeu().getGagnant();
        // gagnantAnime = view.getJeu().getGagnant();
        // winnerPulse = 0;
        view.setGagnantAnime(gagnantAnime);
        view.setWinnerPulse(0);
        String nom = (gagnantAnime == null) ? "égalité" : view.getJeu().nomJoueurPourAffichage(gagnantAnime);
        view.getJeu().setFeedbackMessage("Partie terminée : " + nom + " gagne !");
        view.refreshScores();
        view.repaint();

        Timer timer = new Timer(120, null);
        timer.addActionListener(e -> {
            if (generation != view.getGenerationAnimation()) {
                ((Timer) e.getSource()).stop();
                return;
            }
            // winnerPulse++;
            view.increaseWinnerPulse();
            view.repaint();
            if (view.getWinnerPulse() >= 26) {
                ((Timer) e.getSource()).stop();
                // animationEnCours = false;
                view.setAnimationState(AnimationState.AUCUNE);
                view.refreshScores();
                view.repaint();
            }
        });
        timer.start();
    }

    // ===== ANIMATIONS IA + PHASE 2 ======

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

        // animationEnCours = true;
        // selectionIAVisuelle = true;
        view.setAnimationState(AnimationState.COUP_IA);
        final int generation = view.increaseGenerationAnimation();
        view.getJeu().setFeedbackMessage("IA (" + joueurIA.getTypePion() + ") prépare son coup...");
        view.refreshScores();
        view.repaint();

        new Timer(700, attente -> {
            ((Timer) attente.getSource()).stop();
            if (generation != view.getGenerationAnimation()) return;
            view.getJeu().selectionnerFleursPourAnimation(f1, f2);
            view.getJeu().setFeedbackMessage("IA (" + joueurIA.getTypePion() + ") sélectionne ses deux fleurs...");
            view.refreshScores();
            view.repaint();

            new Timer(950, e -> {
                ((Timer) e.getSource()).stop();
                if (generation != view.getGenerationAnimation()) return;
                view.getJeu().setFeedbackMessage("IA (" + joueurIA.getTypePion() + ") place son pion...");
                if (!view.getJeu().placerPionCoupIA(coup)) {
                    // selectionIAVisuelle = false;
                    view.setAnimationState(AnimationState.AUCUNE);
                    view.getJeu().resetFleursSelectionnee1();
                    view.getJeu().resetFleursSelectionnee2();
                    // animationEnCours = false;
                    view.refreshScoresEtTourIA();
                    return;
                }
                view.refreshScores();
                view.repaint();

                new Timer(900, e2 -> {
                    ((Timer) e2.getSource()).stop();
                    if (generation != view.getGenerationAnimation()) return;
                    view.getJeu().setFeedbackMessage("IA (" + joueurIA.getTypePion() + ") collecte ses fleurs...");
                    animerFleursVersScore(new Fleur[] { f1Animee, f2Animee }, joueurIA, () -> {
                        if (generation != view.getGenerationAnimation()) return;
                        view.getJeu().collecterFleursCoupIA(coup);
                        // selectionIAVisuelle = false;
                        view.setAnimationState(AnimationState.AUCUNE);
                        view.getJeu().resetFleursSelectionnee1();
                        view.getJeu().resetFleursSelectionnee2();
                        // animationEnCours = false;
                        view.refreshScores();
                        view.repaint();
                        lancerAnimationSecondePhase();
                    });
                }).start();
            }).start();
        }).start();
    }

    private void lancerPulseComparaisonPhase2(int generation) {
        Timer timer = new Timer(110, null);
        timer.addActionListener(e -> {
            if (generation != view.getGenerationAnimation() || view.getPionPhase2Compare() == null) {
                ((Timer) e.getSource()).stop();
                return;
            }
            // phase2ComparePulse++;
            view.increasePhase2ComparePulse();
            view.repaint();
            if (view.getPhase2ComparePulse() >= 18) {
                ((Timer) e.getSource()).stop();
            }
        });
        timer.start();
    }
}
