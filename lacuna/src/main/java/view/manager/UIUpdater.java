package view.manager;


import javax.swing.JLabel;
import javax.swing.JPanel;

import model.Fleur;
import global.Configuration;
import model.Jeu;
import view.component.PionGraphique;
import view.component.Score;

/**
 * Gestionnaire des mises à jour de l'interface utilisateur.
 */
public class UIUpdater {

    // ---------------- ATTRIBUTS ---------------- //

    private final Jeu jeu;

    private final Score scoreArgent;

    private final Score scoreOr;

    private final JLabel instructionLabel;

    private final JLabel statusLabel;

    private final PionGraphique pileArgent;

    private final PionGraphique pileOr;


    private final Runnable refreshCallback;

    private javax.swing.JComponent phase2Panel;
    
    private boolean phase2Affichee = false;

    // ---------------- CONSTRUCTEUR ---------------- //

    /**
     * Construit le gestionnaire UI.
     *
     * @param jeu partie courante.
     * @param scoreArgent score argent.
     * @param scoreOr score or.
     * @param instructionLabel label instructions.
     * @param statusLabel label status.
     * @param pileArgent pile argent.
     * @param pileOr pile or.
     * @param refreshCallback callback repaint.
     */
    public UIUpdater(
            Jeu jeu,
            Score scoreArgent,
            Score scoreOr,
            JLabel instructionLabel,
            JLabel statusLabel,
            PionGraphique pileArgent,
            PionGraphique pileOr,
            Runnable refreshCallback) {

        this.jeu = jeu;

        this.scoreArgent = scoreArgent;
        this.scoreOr = scoreOr;

        this.instructionLabel = instructionLabel;
        this.statusLabel = statusLabel;

        this.pileArgent = pileArgent;
        this.pileOr = pileOr;

        this.refreshCallback = refreshCallback;
    }

    /**
     * Définit le panel phase 2.
     *
     * @param phase2Panel panel phase 2.
     */
    public void setPhase2Panel(
            javax.swing.JComponent phase2Panel) {

        this.phase2Panel = phase2Panel;
    }


    // ---------------- REFRESH ---------------- //

    /**
     * Met à jour toute l'interface.
     */
    public void refreshScores() {

        if (scoreOr != null) {
            scoreOr.updateFromGame(jeu.getJoueurs()[0]);
        }

        if (scoreArgent != null) {
            scoreArgent.updateFromGame(jeu.getJoueurs()[1]);
        }

        if (pileArgent != null) {
            pileArgent.repaint();
        }

        if (pileOr != null) {
            pileOr.repaint();
        }

        if (phase2Panel != null
                && !phase2Affichee
                && jeu.estSecondePhaseDisponible()) {

            phase2Affichee = true;

            phase2Panel.setVisible(true);

            phase2Panel.revalidate();
        }

        updateInstructionText();

        updateStatusMessage();

        if (refreshCallback != null) {
            refreshCallback.run();
        }
    }

    // ---------------- INSTRUCTIONS ---------------- //

    /**
     * Met à jour le texte d'instruction.
     */
    public void updateInstructionText() {

        if (instructionLabel == null) {
            return;
        }

        if (jeu.isIAEnCours()) {

            instructionLabel.setText(
                    jeu.nomJoueurActuelPourAffichage()
                            + " réfléchit…");

            return;
        }

        if (jeu.estTourIA()) {

            instructionLabel.setText(
                    "Tour de "
                            + jeu.nomJoueurActuelPourAffichage()
                            + "…");

            return;
        }

        if (jeu.isEnPhaseSelectionAvantage()) {

            instructionLabel.setText(
                    jeu.nomJoueurActuelPourAffichage()
                            + " : choisissez une fleur pour votre avantage");

            return;
        }

        Fleur f1 = jeu.getFleurSelectionnee1();

        Fleur f2 = jeu.getFleurSelectionnee2();

        if (f1 == null) {

            instructionLabel.setText(
                    jeu.nomJoueurActuelPourAffichage()
                            + " : cliquez sur une fleur pour commencer.");

        } else if (f2 == null) {

            instructionLabel.setText(
                    jeu.nomJoueurActuelPourAffichage()
                            + " : sélectionnez une seconde fleur du même type.");

        } else if (jeu.fleursConnectees(f1, f2)) {

            instructionLabel.setText(
                    "Fleurs connectees : un pion sera place en cliquant.");

        } else {

            instructionLabel.setText(
                    "Fleurs non Connectee : choisissez une autre seconde fleur.");
        }
    }

    // ---------------- STATUS ---------------- //

    /**
     * Met à jour le message de status.
     */
    public void updateStatusMessage() {

        if (statusLabel == null) {
            return;
        }

        String message = jeu.getFeedbackMessage();

        statusLabel.setText(
                (message == null || message.trim().isEmpty())
                        ? "En attente d'une action…"
                        : message);
    }

    // ---------------- PHASE 2 ---------------- //

    /**
     * Réinitialise l'état phase 2.
     */
    public void resetPhase2() {

        phase2Affichee = false;

        if (phase2Panel != null) {

            phase2Panel.setVisible(false);
        } else {

            Configuration.debugeur("UIUpdater.resetPhase2: phase2Panel est null, impossible de le cacher");
        }
    }
}