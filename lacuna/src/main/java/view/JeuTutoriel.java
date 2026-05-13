package view;

import javax.swing.*;
import model.*;
import java.awt.*;

public class JeuTutoriel extends JeuGraphique {

    private int etape = 0;
    private TutorialOverlay overlay;
    private Runnable quitterAction;

    public JeuTutoriel(JFrame frame, TutorialOverlay overlay, Runnable quitterAction) {
        super(frame);
        this.quitterAction = quitterAction;
        this.overlay = overlay;

        PionArgent leftPile = new PionArgent(frame, jeu);
        PionOr rightPile = new PionOr(frame, jeu);

        setSidePiles(leftPile, rightPile);

        initialiserTutoriel();
    }

    private void initialiserTutoriel() {
        jeu.activerIA(null);
        jeu.setEnPhaseSelectionAvantage(false);
        jeu.setAvantageInitialApplique(true);
        jeu.getFleurs().clear();

        jeu.getFleurs().add(new Fleur(Fleur.TypeFleur.ROUGE, new Coordonnees(250, 200)));
        jeu.getFleurs().add(new Fleur(Fleur.TypeFleur.ROUGE, new Coordonnees(400, 200)));
        jeu.getFleurs().add(new Fleur(Fleur.TypeFleur.BLEUE, new Coordonnees(320, 320)));

        repaint();
    }

    public void startTutorial() {
        etape = 0;
        afficherMessageEtape();
    }

    private void afficherMessageAvecDelai(int delaiMs) {
        javax.swing.Timer timer = new javax.swing.Timer(delaiMs, e -> {
            afficherMessageEtape();
        });

        timer.setRepeats(false);
        timer.start();
    }

    private void afficherMessageEtape() {
        if (overlay == null) return;

        switch (etape) {

            case 0:
                overlay.showMessage(
                        "Etape 1 : Selection",
                        "Cliquez sur la premiere fleur rouge.",
                        null,
                        null,
                        () -> passerEtape(),
                        () -> arreterTutoriel()
                );

                overlay.setSkipButtonVisible(true);
                overlay.setNextButtonText("Continuer");
                break;

            case 1:
                overlay.showMessage(
                        "Etape 2 : Paire",
                        "Cliquez sur la deuxieme fleur rouge.",
                        null,
                        null,
                        () -> passerEtape(),
                        () -> arreterTutoriel()
                );

                overlay.setSkipButtonVisible(true);
                break;

            case 2:
                overlay.showMessage(
                        "Etape 3 : Placement",
                        "Cliquez entre les deux fleurs rouges.",
                        null,
                        null,
                        () -> passerEtape(),
                        () -> arreterTutoriel()
                );

                overlay.setSkipButtonVisible(true);
                break;

            case 3:
                overlay.showMessage(
                        "Bravo !",
                        "Tutoriel terminé.",
                        () -> {
                            arreterTutoriel();
                        },
                        null,
                        null,
                        () -> {
                            arreterTutoriel();
                        }
                );

                overlay.setNextButtonText("Retourner au menu");
                overlay.setSkipButtonVisible(false);
                break;
        }
    }

    private void passerEtape() {
        etape++;
        afficherMessageAvecDelai(300);
    }

    private void arreterTutoriel() {
        if (overlay != null) {
            overlay.setVisible(false);
        }
        if (quitterAction != null) {
            quitterAction.run();
        }
    }

    public void quitterTutoriel() {
        arreterTutoriel();
    }

    @Override
    public void refreshScores() {
        super.refreshScores();
        validerProgression();
    }

    @Override
    public void refreshScoresEtTourIA() {
        super.refreshScoresEtTourIA();
        validerProgression();
    }

    private void validerProgression() {
        switch (etape) {

            case 0:
                if (jeu.getFleurSelectionnee1() != null
                        && jeu.getFleurSelectionnee1().getType() == Fleur.TypeFleur.ROUGE) {
                    etape = 1;
                    afficherMessageAvecDelai(300);
                }
                break;

            case 1:
                if (jeu.getFleurSelectionnee2() != null
                        && jeu.getFleurSelectionnee2().getType() == Fleur.TypeFleur.ROUGE) {
                    etape = 2;
                    afficherMessageAvecDelai(300);
                }
                break;

            case 2:
                if (!jeu.getPions().isEmpty()) {
                    etape = 3;
                    afficherMessageAvecDelai(300);
                }
                break;

            case 3:
                break;
        }
    }
}