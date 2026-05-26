package view.manager;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import model.Joueur;
import model.Jeu;

public class GameEndManager {

    private final Jeu jeu;

    private final JFrame frame;

    private final NetworkController gestionnaireReseau;

    private final Runnable quitterAction;

    private final Runnable refreshAction;

    private final Runnable repaintAction;

    private final Runnable resetAnimationAction;

    private boolean finPartieAffichee = false;

    public GameEndManager(
            Jeu jeu,
            JFrame frame,
            NetworkController gestionnaireReseau,
            Runnable quitterAction,
            Runnable refreshAction,
            Runnable repaintAction,
            Runnable resetAnimationAction) {

        this.jeu = jeu;

        this.frame = frame;

        this.gestionnaireReseau = gestionnaireReseau;

        this.quitterAction = quitterAction;

        this.refreshAction = refreshAction;

        this.repaintAction = repaintAction;

        this.resetAnimationAction = resetAnimationAction;
    }

    /**
     * Vérifie la fin de partie.
     *
     * @param animationEnCours animation en cours.
     */
    public void verifierFinDePartie(
            boolean animationEnCours) {

        if (jeu.isPartieTerminee()
                && !finPartieAffichee
                && !animationEnCours) {

            finPartieAffichee = true;

            SwingUtilities.invokeLater(() -> {

                Joueur gagnant =
                        jeu.getGagnant();

                String msg =
                        (gagnant == null)
                                ? "ÉGALITÉ !"
                                : "FÉLICITATIONS "
                                        + jeu.nomJoueurPourAffichage(gagnant)
                                                .toUpperCase()
                                        + ", VOUS AVEZ GAGNÉ !";

                Object[] options = {
                        "Rejouer",
                        "Menu Principal"
                };

                int n =
                        JOptionPane.showOptionDialog(
                                frame,
                                msg + "\n\nQue voulez-vous faire ?",
                                "Fin de partie",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.QUESTION_MESSAGE,
                                null,
                                options,
                                options[0]);

                if (n == JOptionPane.YES_OPTION) {

                    if (jeu.isEnModeReseau()) {

                        gestionnaireReseau.demanderRejouer();

                    } else {

                        finPartieAffichee = false;

                        resetAnimationAction.run();

                        jeu.recommencerPartie();

                        resetAnimationAction.run();

                        refreshAction.run();

                        repaintAction.run();
                    }

                } else {

                    finPartieAffichee = false;

                    frame.getGlassPane().setVisible(false);

                    if (quitterAction != null) {

                        quitterAction.run();
                    }
                }
            });
        }
    }

    /**
     * Réinitialise l'état de fin de partie.
     */
    public void reset() {

        finPartieAffichee = false;
    }
}