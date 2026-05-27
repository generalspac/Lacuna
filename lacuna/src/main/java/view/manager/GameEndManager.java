package view.manager;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import model.Joueur;
import model.Jeu;

/**
 * \class GameEndManager
 * \brief Gère toute la logique liée à la fin de partie.
 *
 * \details Cette classe s’occupe de :
 *          - l’affichage de la fenêtre de victoire
 *          - la gestion de la relance de partie
 *          - le retour au menu principal
 *          - la coordination avec le mode réseau
 */
public class GameEndManager {

    // --------------------- MODELE / VUE / CONTROLE -------------------------- //

    /**
     * Référence vers le modèle principal du jeu.
     */
    private final Jeu jeu;

    /**
     * Fenêtre principale Swing.
     */
    private final JFrame frame;

    /**
     * Gestionnaire réseau utilisé en mode multijoueur réseau.
     */
    private final NetworkController gestionnaireReseau;

    /**
     * Action exécutée lorsque le joueur quitte la partie.
     */
    private final Runnable quitterAction;

    /**
     * Action de rafraîchissement des composants.
     */
    private final Runnable refreshAction;

    /**
     * Action de repaint global.
     */
    private final Runnable repaintAction;

    /**
     * Action de réinitialisation des animations.
     */
    private final Runnable resetAnimationAction;

    /**
     * Empêche l’affichage multiple de la popup de fin.
     */
    private boolean finPartieAffichee = false;

    // --------------------- CONSTRUCTEUR -------------------------- //

    /**
     * Constructeur principal du GameEndManager.
     */
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

    // --------------------- FIN DE PARTIE -------------------------- //

    /**
     * Vérifie si la partie est terminée
     * et affiche la popup de fin si nécessaire.
     *
     * @param animationEnCours true si une animation est encore active
     */
    public void verifierFinDePartie(
            boolean animationEnCours) {

        // Conditions nécessaires :
        // - partie terminée
        // - popup pas déjà affichée
        // - aucune animation en cours
        if (jeu.isPartieTerminee()
                && !finPartieAffichee
                && !animationEnCours) {

            finPartieAffichee = true;

            // IMPORTANT :
            // toute interaction Swing doit être exécutée
            // dans l’Event Dispatch Thread
            SwingUtilities.invokeLater(() -> {

                // récupération du gagnant
                Joueur gagnant =
                        jeu.getGagnant();

                // message affiché au joueur
                String msg =
                        (gagnant == null)
                                ? "ÉGALITÉ !"
                                : "FÉLICITATIONS "
                                        + jeu.nomJoueurPourAffichage(gagnant)
                                                .toUpperCase()
                                        + ", VOUS AVEZ GAGNÉ !";

                // boutons proposés dans la popup
                Object[] options = {
                        "Rejouer",
                        "Menu Principal"
                };

                // affichage de la fenêtre de fin
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

                
                // REJOUER
                
                if (n == JOptionPane.YES_OPTION) {

                    // mode multijoueur réseau
                    if (jeu.isEnModeReseau()) {

                        // demande de revanche envoyée au joueur distant
                        gestionnaireReseau.demanderRejouer();

                    } else {

                        // reset local classique

                        finPartieAffichee = false;

                        // annule les animations encore actives
                        resetAnimationAction.run();

                        // recrée une nouvelle partie
                        jeu.recommencerPartie();

                        // sécurité supplémentaire :
                        // reset complet des animations
                        resetAnimationAction.run();

                        // rafraîchit l’interface
                        refreshAction.run();

                        repaintAction.run();
                    }

                
                // RETOUR MENU
                
                } else {

                    finPartieAffichee = false;

                    // masque le glass pane utilisé par les animations
                    frame.getGlassPane().setVisible(false);

                    // exécute l’action de sortie
                    if (quitterAction != null) {

                        quitterAction.run();
                    }
                }
            });
        }
    }

    // --------------------- RESET -------------------------- //

    /**
     * Réinitialise l’état du gestionnaire.
     * Utilisé lors d’un redémarrage de partie.
     */
    public void reset() {

        finPartieAffichee = false;
    }
}