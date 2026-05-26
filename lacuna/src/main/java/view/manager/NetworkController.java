package view.manager;

import javax.swing.SwingUtilities;
import javax.swing.Timer;

import model.Fleur;
import model.Jeu;
import model.Pion;
import network.ClientReseau;
import network.CoupReseau;
import network.ServeurReseau;
import view.JeuGraphique;

/**
 * Gestionnaire du mode réseau.
 */
public class NetworkController {

    // ---------------- ATTRIBUTS ---------------- //

    private final JeuGraphique jeuGraphique;

    private final Jeu jeu;

    private Object gestionnaireReseau;

    private boolean enAttenteAdversaire = false;

    private boolean rejouerDemandeLocal = false;

    private CoupReseau rejouerParamsEnAttente = null;

    // ---------------- CONSTRUCTEUR ---------------- //

    /**
     * Construit le gestionnaire réseau.
     *
     * @param jeuGraphique composant graphique.
     */
    public NetworkController(JeuGraphique jeuGraphique) {

        this.jeuGraphique = jeuGraphique;

        this.jeu = jeuGraphique.getJeu();
    }

    // ---------------- GETTERS ---------------- //

    /**
     * Indique si le joueur attend l'adversaire.
     *
     * @return {@code true} si attente.
     */
    public boolean isEnAttenteAdversaire() {

        return enAttenteAdversaire;
    }

    // ---------------- INSTALLATION ---------------- //

    /**
     * Configure le mode réseau.
     *
     * @param gestionnaire gestionnaire réseau.
     * @param typeLocal type local.
     */
    public void installerReseau(
            Object gestionnaire,
            Pion.TypePion typeLocal) {

        this.gestionnaireReseau = gestionnaire;

        int indexLocal =
                (typeLocal == Pion.TypePion.OR) ? 0 : 1;

        jeu.setEnModeReseau(true);

        jeu.setIndexJoueurLocal(indexLocal);

        jeu.setEcouteurCoupEnvoye(coup -> {

            envoyerMessageReseau(coup);

            enAttenteAdversaire =
                    "COUP".equals(coup.typeMessage);

            jeuGraphique.refreshScores();

            jeuGraphique.repaint();
        });

        java.util.function.Consumer<CoupReseau> reception =
                coup -> SwingUtilities.invokeLater(() -> {

                    if ("COUP".equals(coup.typeMessage)) {

                        Fleur f1 =
                                jeu.trouverFleurSurPlateau(
                                        model.Fleur.TypeFleur.valueOf(
                                                coup.typeFleur1),
                                        coup.fleur1X,
                                        coup.fleur1Y);

                        Fleur f2 =
                                jeu.trouverFleurSurPlateau(
                                        model.Fleur.TypeFleur.valueOf(
                                                coup.typeFleur2),
                                        coup.fleur2X,
                                        coup.fleur2Y);

                        jeu.setFleurSelectionnee1(f1);

                        jeu.setFleurSelectionnee2(f2);

                        jeuGraphique.repaint();

                        Timer timer =
                                new Timer(700, e -> {

                                    jeu.setFleurSelectionnee1(null);

                                    jeu.setFleurSelectionnee2(null);

                                    jeu.appliquerCoupReseau(coup);

                                    enAttenteAdversaire = false;

                                    jeuGraphique.refreshScoresEtTourIA();

                                    if (jeu.isPartieAbandonnee()) {

                                        jeuGraphique.afficherGagnantSansAnimation();
                                    }

                                    jeuGraphique.repaint();
                                });

                        timer.setRepeats(false);

                        timer.start();

                    } else if ("REJOUER".equals(coup.typeMessage)) {

                        if (rejouerDemandeLocal) {

                            demarrerNouvellePartieReseau(
                                    coup.fleur1X,
                                    coup.hoteCommence);

                        } else {

                            rejouerParamsEnAttente = coup;
                        }

                    } else {

                        jeu.appliquerCoupReseau(coup);

                        if (!"AVANTAGE".equals(coup.typeMessage)) {

                            enAttenteAdversaire = false;
                        }

                        jeuGraphique.refreshScoresEtTourIA();

                        if (jeu.isPartieAbandonnee()) {

                            jeuGraphique.afficherGagnantSansAnimation();
                        }

                        jeuGraphique.repaint();
                    }
                });

        if (gestionnaire instanceof ServeurReseau) {

            ((ServeurReseau) gestionnaire)
                    .setEcouteur(reception);

        } else if (gestionnaire instanceof ClientReseau) {

            ((ClientReseau) gestionnaire)
                    .setEcouteur(reception);
        }

        enAttenteAdversaire =
                (jeu.currentPlayerIndex != indexLocal);

        jeuGraphique.refreshScores();

        jeuGraphique.repaint();
    }

    // ---------------- ENVOI ---------------- //

    /**
     * Envoie un message réseau.
     *
     * @param coup coup réseau.
     */
    private void envoyerMessageReseau(CoupReseau coup) {

        try {

            if (gestionnaireReseau instanceof ServeurReseau) {

                ((ServeurReseau) gestionnaireReseau)
                        .envoyerCoup(coup);

            } else if (gestionnaireReseau instanceof ClientReseau) {

                ((ClientReseau) gestionnaireReseau)
                        .envoyerCoup(coup);
            }

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    // ---------------- RECOMMENCER ---------------- //

    /**
     * Redémarre une partie réseau.
     *
     * @param graine graine partie.
     * @param hoteCommence ordre de jeu.
     */
    private void demarrerNouvellePartieReseau(
            int graine,
            boolean hoteCommence) {

        rejouerDemandeLocal = false;

        rejouerParamsEnAttente = null;

        jeuGraphique.getAnimCtx().reset();

        jeu.recommencerPartieReseau(
                graine,
                hoteCommence);

        jeuGraphique.getAnimCtx().reset();

        enAttenteAdversaire =
                (jeu.currentPlayerIndex
                        != jeu.getIndexJoueurLocal());

        jeuGraphique.refreshScoresEtTourIA();

        jeuGraphique.repaint();
    }

    /**
     * Lance une demande de recommencer.
     */
    public void demanderRejouer() {

        rejouerDemandeLocal = true;

        if (jeu.getIndexJoueurLocal() == 0) {

            int graine =
                    new java.util.Random().nextInt();

            boolean hoteCommence =
                    new java.util.Random().nextBoolean();

            CoupReseau rejouer =
                    new CoupReseau();

            rejouer.typeMessage = "REJOUER";

            rejouer.fleur1X = graine;

            rejouer.hoteCommence = hoteCommence;

            envoyerMessageReseau(rejouer);

            demarrerNouvellePartieReseau(
                    graine,
                    hoteCommence);

        } else {

            if (rejouerParamsEnAttente != null) {

                CoupReseau params =
                        rejouerParamsEnAttente;

                rejouerParamsEnAttente = null;

                demarrerNouvellePartieReseau(
                        params.fleur1X,
                        params.hoteCommence);
            }
        }
    }
}