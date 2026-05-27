package view;

import javax.swing.*;
import java.awt.*;

import model.*;
import view.component.PionGraphique;
import view.component.RoundedButton;

/**
 * \class JeuTutoriel
 * \brief Version tutorielle du jeu avec progression guidée.
 *
 * \details Cette classe hérite de JeuGraphique et impose un déroulement
 *          pas-à-pas pour apprendre les mécaniques du jeu :
 *          - sélection de fleurs
 *          - validation de paires
 *          - placement de pion
 *          - gestion des erreurs
 */
public class JeuTutoriel extends JeuGraphique {

    // =========================
    // ETAPES DU TUTORIEL
    // =========================

    /**
     * Étape : sélection première fleur.
     */
    private static final int ETAPE_SELECTION_1 = 0;

    /**
     * Étape : sélection deuxième fleur.
     */
    private static final int ETAPE_SELECTION_2 = 1;

    /**
     * Étape : placement du pion.
     */
    private static final int ETAPE_PLACEMENT = 2;

    /**
     * Étape : fin du tutoriel.
     */
    private static final int ETAPE_FIN = 3;

    /**
     * Étape : erreur de couleur.
     */
    private static final int ETAPE_ERREUR_COULEUR = 4;

    /**
     * Étape : chemin bloqué.
     */
    private static final int ETAPE_ERREUR_BLOQUE = 5;

    /**
     * Étape actuelle du tutoriel.
     */
    private int etape = ETAPE_SELECTION_1;

    /**
     * Overlay d’affichage des messages tutoriels.
     */
    private final TutorialOverlay overlay;

    /**
     * Action de sortie du tutoriel.
     */
    private final Runnable quitterAction;

    /**
     * Empêche les appels multiples de messages.
     */
    private boolean messageEnCours = false;

    /**
     * Constructeur du tutoriel.
     *
     * @param frame fenêtre principale
     * @param overlay overlay d’instructions
     * @param quitterAction action de sortie
     */
    public JeuTutoriel(
            JFrame frame,
            TutorialOverlay overlay,
            Runnable quitterAction) {

        super(frame, null, null, null, null, quitterAction);

        this.overlay = overlay;

        this.quitterAction = quitterAction;

        setBackground(Color.BLACK);

        // =========================
        // BOUTON QUITTER PERMANENT
        // =========================

        setLayout(null);

        RoundedButton btnQuitter = new RoundedButton("Quitter le tutoriel");

        btnQuitter.setBounds(20, 20, 200, 45);
        btnQuitter.setBackground(new Color(180, 40, 40));
        btnQuitter.setForeground(Color.WHITE);
        btnQuitter.setFont(new Font("SansSerif", Font.BOLD, 16));

        btnQuitter.addActionListener(e -> arreterTutoriel());

        add(btnQuitter);

        // =========================
        // PILES LATERALES
        // =========================

        PionGraphique leftPile =
                new PionGraphique(frame, jeu, Pion.TypePion.ARGENT);

        PionGraphique rightPile =
                new PionGraphique(frame, jeu, Pion.TypePion.OR);

        setSidePiles(leftPile, rightPile);

        initialiserTutoriel();
    }

    // =========================================================
    // INITIALISATION
    // =========================================================

    /**
     * Initialise le plateau du tutoriel.
     */
    private void initialiserTutoriel() {

        jeu.activerIA(null);
        jeu.setEnPhaseSelectionAvantage(false);
        jeu.setAvantageInitialApplique(true);

        jeu.getFleurs().clear();

        jeu.getFleurs().add(new Fleur(Fleur.TypeFleur.ROUGE, new Coordonnees(250, 200)));
        jeu.getFleurs().add(new Fleur(Fleur.TypeFleur.BLEUE, new Coordonnees(300, 200)));
        jeu.getFleurs().add(new Fleur(Fleur.TypeFleur.ROUGE, new Coordonnees(450, 200)));
        jeu.getFleurs().add(new Fleur(Fleur.TypeFleur.ROUGE, new Coordonnees(450, 130)));
        jeu.getFleurs().add(new Fleur(Fleur.TypeFleur.BLEUE, new Coordonnees(320, 320)));

        repaint();
    }

    // =========================================================
    // DEMARRAGE
    // =========================================================

    /**
     * Lance le tutoriel.
     */
    public void startTutorial() {

        etape = ETAPE_SELECTION_1;

        afficherMessageEtape();
    }

    /**
     * Retourne la fleur à mettre en évidence selon l’étape.
     *
     * @return fleur highlightée
     */
    @Override
    protected Fleur getHighlightedFlower() {

        switch (etape) {

            case ETAPE_SELECTION_1:
                return jeu.getFleurs().get(0);

            case ETAPE_SELECTION_2:
                return jeu.getFleurs().get(3);

            default:
                return null;
        }
    }

    // =========================================================
    // AFFICHAGE DELAY
    // =========================================================

    /**
     * Affiche un message après un délai.
     *
     * @param delaiMs délai en millisecondes
     */
    private void afficherMessageAvecDelai(int delaiMs) {

        if (messageEnCours)
            return;

        messageEnCours = true;

        Timer timer = new Timer(delaiMs, e -> {

            messageEnCours = false;

            afficherMessageEtape();
        });

        timer.setRepeats(false);
        timer.start();
    }

    // =========================================================
    // OVERLAY
    // =========================================================

    /**
     * Affiche le message correspondant à l’étape actuelle.
     */
    private void afficherMessageEtape() {

        if (overlay == null)
            return;

        overlay.setBackButtonVisible(false);

        switch (etape) {

            case ETAPE_SELECTION_1:
                overlay.showMessage(
                        "Étape 1 : Sélection",
                        "Cliquez sur la <span style='color:#FF3C3C; font-weight:bold;'> fleur rouge qui clignote </span>.",
                        null,
                        null,
                        () -> passerEtape(),
                        () -> arreterTutoriel());
                overlay.setSkipButtonVisible(true);
                overlay.setNextButtonText("Suivant");
                break;

            case ETAPE_SELECTION_2:
                overlay.showMessage(
                        "Étape 2 : La paire",
                        "Sélectionnez maintenant la <span style='color:#FF3C3C; font-weight:bold;'>deuxième fleur rouge</span>.",
                        null,
                        null,
                        () -> passerEtape(),
                        () -> arreterTutoriel());
                overlay.setSkipButtonVisible(true);
                break;

            case ETAPE_PLACEMENT:
                overlay.showMessage(
                        "Étape 3 : Placement",
                        "Cliquez <span style='color:#FFB400; font-weight:bold;'>entre les deux fleurs</span> pour placer votre pion.",
                        null,
                        null,
                        () -> passerEtape(),
                        () -> arreterTutoriel());
                overlay.setSkipButtonVisible(true);
                break;

            case ETAPE_FIN:
                overlay.showMessage(
                        "Félicitations !",
                        "Vous avez appris les bases de "
                                + "<span style='color:#8C00FF; font-weight:bold;'>LACUNA</span>.<br><br>"
                                + "Le tutoriel est terminé.",
                        () -> arreterTutoriel(),
                        null,
                        null,
                        () -> arreterTutoriel());

                overlay.setNextButtonText("Terminer");
                overlay.setSkipButtonVisible(false);
                break;

            case ETAPE_ERREUR_COULEUR:
                overlay.showMessage(
                        "Oups !",
                        "Les deux fleurs doivent être "
                                + "<span style='color:#0078FF; font-weight:bold;'>de la même couleur</span>.<br><br>"
                                + "Réessayez avec les fleurs rouges.",
                        () -> {
                            etape = ETAPE_SELECTION_1;
                            afficherMessageEtape();
                        },
                        null,
                        null,
                        () -> arreterTutoriel());

                overlay.setNextButtonText("Réessayer");
                overlay.setSkipButtonVisible(false);
                break;

            case ETAPE_ERREUR_BLOQUE:
                overlay.showMessage(
                        "Chemin bloqué",
                        "Une fleur bloque la ligne entre les deux fleurs.<br><br>"
                                + "Choisissez la fleur rouge tout à droite.",
                        () -> {
                            etape = ETAPE_SELECTION_2;
                            afficherMessageEtape();
                        },
                        null,
                        null,
                        () -> arreterTutoriel());

                overlay.setNextButtonText("Réessayer");
                overlay.setSkipButtonVisible(false);
                break;
        }
    }

    // =========================================================
    // PASSAGE ETAPE
    // =========================================================

    /**
     * Passe à l’étape suivante du tutoriel.
     */
    private void passerEtape() {

        switch (etape) {

            case ETAPE_SELECTION_1:
                jeu.setFleurSelectionnee1(jeu.getFleurs().get(0));
                break;

            case ETAPE_SELECTION_2:
                jeu.setFleurSelectionnee2(jeu.getFleurs().get(3));
                break;

            case ETAPE_PLACEMENT:
                jeu.jouerCoupAvecFleursSelectionnees();
                break;
        }

        etape++;

        afficherMessageAvecDelai(250);
    }

    // =========================================================
    // QUITTER
    // =========================================================

    /**
     * Quitte le tutoriel.
     */
    private void arreterTutoriel() {

        if (overlay != null)
            overlay.setVisible(false);

        if (quitterAction != null)
            quitterAction.run();
    }

    public void quitterTutoriel() {
        arreterTutoriel();
    }

    // =========================================================
    // REFRESH
    // =========================================================

    @Override
    public void refreshScores() {
        super.refreshScores();
        validerProgression();
    }

    @Override
    public void refreshScoresEtTourIA() {
        refreshScores();
        repaint();
        validerProgression();
    }

    // =========================================================
    // VALIDATION
    // =========================================================

    /**
     * Valide la progression du tutoriel selon les actions du joueur.
     */
    private void validerProgression() {

        switch (etape) {

            case ETAPE_SELECTION_1:

                if (jeu.getFleurSelectionnee1() != null
                        && jeu.getFleurSelectionnee1().getType() == Fleur.TypeFleur.ROUGE) {

                    etape = ETAPE_SELECTION_2;
                    afficherMessageAvecDelai(300);
                }

                break;

            case ETAPE_SELECTION_2:

                Fleur f1 = jeu.getFleurSelectionnee1();
                Fleur f2 = jeu.getFleurSelectionnee2();

                if (f1 != null && f2 != null) {

                    if (f2.getType() != Fleur.TypeFleur.ROUGE) {

                        etape = ETAPE_ERREUR_COULEUR;
                        repaint();
                        afficherMessageAvecDelai(300);
                        return;
                    }

                    if (!jeu.fleursConnectees(f1, f2)) {

                        etape = ETAPE_ERREUR_BLOQUE;
                        repaint();
                        afficherMessageAvecDelai(700);
                        return;
                    }

                    etape = ETAPE_PLACEMENT;
                    afficherMessageAvecDelai(300);
                }

                break;

            case ETAPE_PLACEMENT:

                boolean pionPlace = false;

                for (Pion p : jeu.getPions()) {
                    if (p.getPosition() != null) {
                        pionPlace = true;
                        break;
                    }
                }

                if (pionPlace) {
                    etape = ETAPE_FIN;
                    afficherMessageAvecDelai(300);
                }

                break;
        }
    }
}