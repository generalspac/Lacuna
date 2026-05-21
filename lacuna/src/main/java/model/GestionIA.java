package model;

import java.util.function.Consumer;

import model.IA.CoupIA;
import model.IA.IA;
import model.IA.EtatJeu;
import model.IA.NiveauxIA;
import model.IA.ThreadIA;

/**
 * Regroupe la logique commune des modes avec IA.
 *
 * La classe reste volontairement statique : elle manipule un {@link Jeu}
 * déjà construit, sans porter elle-même l'état de la partie.
 */
public final class GestionIA {

    private GestionIA() {
    }

    /**
     * Active une partie humain contre IA.
     *
     * @param jeu partie à configurer.
     * @param niveau niveau de difficulté de l'IA.
     * @param nomHumain nom affiché pour le joueur humain.
     * @param joueurQuiCommence couleur qui commence, ou {@code null} pour un départ aléatoire.
     */
    public static void activerIA(Jeu jeu, NiveauxIA niveau, String nomHumain, Pion.TypePion joueurQuiCommence) {

        jeu.againstIA = true;
        jeu.iaContreIA = false;
        if (niveau == null) {
            niveau = NiveauxIA.FACILE;
        }
        jeu.ia = niveau.creerIA(1);
        jeu.iaOr = null;
        jeu.iaArgent = null;
        jeu.niveauIA = niveau;
        jeu.niveauIAOr = null;
        jeu.niveauIAArgent = null;
        jeu.nomHumainIA = nomHumain;
        jeu.joueurDepartIA = joueurQuiCommence;
        jeu.joueurDepartIAContreIA = null;
        jeu.partieAbandonnee = false;
        jeu.gagnantParAbandon = null;

        jeu.joueurs = new Joueur[] {
                new JoueurHumain(jeu.nomValide(nomHumain, "Humain"), Pion.TypePion.OR),
                new JoueurIa("IA", Pion.TypePion.ARGENT)
        };
        jeu.choisirJoueurQuiCommence(joueurQuiCommence);
        jeu.iaEnCours = false;
        jeu.setFeedbackMessage("Mode IA activé (" + libelleNiveau(niveau) + "). "
                + jeu.nomJoueurActuelPourAffichage() + " commence.");
    }

    /**
     * Indique si le joueur courant est une IA.
     *
     * @param jeu partie consultée.
     * @return {@code true} si le tour courant appartient à une IA.
     */
    public static boolean estTourIA(Jeu jeu) {

         if (!jeu.againstIA)
            return false;
        return jeu.getJoueurActuel() instanceof JoueurIa;
    }

    /**
     * Retrouve dans le plateau réel une fleur copiée dans un état IA.
     *
     * @param jeu partie courante.
     * @param recherchee fleur recherchée.
     * @return fleur du plateau, ou {@code null} si elle n'existe plus.
     */
    public static Fleur retrouverFleurDuPlateau(Jeu jeu, Fleur recherchee) {
        if (recherchee == null) {
            return null;
        }

        for (Fleur f : jeu.fleurs) {
            if (f.getType() == recherchee.getType() && f.getPosition().equals(recherchee.getPosition())) {
                return f;
            }
        }

        return null;
    }

    /**
     * Place le pion d'un coup IA sans collecter les fleurs.
     *
     * @param jeu partie courante.
     * @param coup coup choisi par l'IA.
     * @return {@code true} si le pion a été placé.
     */
    public static boolean placerPionCoupIA(Jeu jeu, CoupIA coup) {

        if (coup == null) {
            if (!ActionsJeu.placerPionIADeSecours(jeu)) {
                jeu.setFeedbackMessage("L'IA n'a trouvé aucun coup jouable.");
                return false;
            }
            return true;
        }

        if (!estTourIA(jeu)) {
            return false;
        }

        Joueur joueur = jeu.getJoueurActuel();
        Pion pionLibre = jeu.getPionLibre(joueur.getTypePion());

        if (pionLibre == null) {
            jeu.setFeedbackMessage("L'IA n'a plus de pion disponible.");
            return false;
        }

        Fleur f1 = retrouverFleurDuPlateau(jeu, coup.fleur1);
        Fleur f2 = retrouverFleurDuPlateau(jeu, coup.fleur2);

        if (f1 == null || f2 == null || !jeu.fleursConnectees(f1, f2)) {
            jeu.setFeedbackMessage("Le coup IA n'est plus valide : il a été ignoré.");
            return false;
        }

        if (!jeu.placePion(pionLibre, coup.position)) {
            return false;
        }

        jeu.setFeedbackMessage(jeu.nomJoueurPourAffichage(joueur) + " place son pion.");
        return true;
    }

    /**
     * Collecte les fleurs d'un coup IA déjà placé.
     *
     * @param jeu partie courante.
     * @param coup coup choisi par l'IA.
     * @return {@code true} si les fleurs ont été collectées.
     */
    public static boolean collecterFleursCoupIA(Jeu jeu, CoupIA coup) {

        if (coup == null || !estTourIA(jeu)) {
            return false;
        }

        Joueur joueur = jeu.getJoueurActuel();
        Pion pionPlace = null;

        for (Pion p : jeu.pions) {
            if (p.getType() == joueur.getTypePion() && p.getPosition() != null
                    && p.getPosition().equals(coup.position)) {
                pionPlace = p;
                break;
            }
        }

        Fleur f1 = retrouverFleurDuPlateau(jeu, coup.fleur1);
        Fleur f2 = retrouverFleurDuPlateau(jeu, coup.fleur2);

        if (pionPlace == null || f1 == null || f2 == null) {
            jeu.setFeedbackMessage("Le coup IA ne peut pas être finalisé.");
            return false;
        }

        if (!jeu.mangerFleurs(joueur, f1, f2)) {
            jeu.setFeedbackMessage("Le coup IA ne peut pas collecter les fleurs.");
            return false;
        }

        jeu.historique.enregistrerAction(pionPlace, f1, f2, joueur);
        jeu.joueurSuivant();
        jeu.setFeedbackMessage(jeu.nomJoueurPourAffichage(joueur) + " a joué. C'est à "
                + jeu.nomJoueurActuelPourAffichage() + " de jouer.");
        return true;
    }

    /**
     * Joue un coup IA complet sans animation.
     *
     * @param jeu partie courante.
     * @param coup coup choisi par l'IA, ou {@code null} pour utiliser le secours.
     */
    public static void jouerCoupIA(Jeu jeu, CoupIA coup) {
        if (placerPionCoupIA(jeu, coup) && coup != null) {
            collecterFleursCoupIA(jeu, coup);
        }
    }

    /**
     * Lance un tour IA avec l'application standard du coup.
     *
     * @param jeu partie courante.
     * @param callbackFin action appelée après le calcul.
     */
    public static void lancerIA(Jeu jeu, Runnable callbackFin) {
        lancerIA(jeu, coup -> jouerCoupIA(jeu, coup), callbackFin);
    }

    /**
     * Lance le calcul du tour IA dans un thread séparé.
     *
     * @param jeu partie courante.
     * @param actionQuandCoupPret action appelée sur l'EDT avec le coup choisi.
     * @param callbackFin action appelée après la fin du calcul IA.
     */
    public static void lancerIA(Jeu jeu, Consumer<CoupIA> actionQuandCoupPret, Runnable callbackFin) {

        IA iaCourante = iaPourJoueurCourant(jeu);
        if (!jeu.againstIA || iaCourante == null || jeu.iaEnCours || jeu.isPartieTerminee())
            return;

        if (!estTourIA(jeu)) return;

        jeu.iaEnCours = true;
        final int generationLancee = ++jeu.generationIA;
        jeu.setFeedbackMessage("Tour de " + jeu.nomJoueurActuelPourAffichage() + " : l'IA réfléchit...");

        EtatJeu etat = EtatJeu.depuisJeu(jeu, jeu.currentPlayerIndex);

        ThreadIA thread = new ThreadIA(iaCourante, etat, coup -> {
            javax.swing.SwingUtilities.invokeLater(() -> {
                if (generationLancee != jeu.generationIA) {
                    return;
                }

                try {
                    if (actionQuandCoupPret != null) {
                        actionQuandCoupPret.accept(coup);
                    } else {
                        jouerCoupIA(jeu, coup);
                    }
                } finally {
                    jeu.iaEnCours = false;
                    if (callbackFin != null) {
                        callbackFin.run();
                    }
                }
            });
        });

        thread.start();
    }

    /**
     * Configure une partie IA contre IA avec deux niveaux indépendants.
     *
     * @param jeu partie à configurer.
     * @param niveauOr niveau de l'IA OR.
     * @param niveauArgent niveau de l'IA ARGENT.
     * @param joueurQuiCommence couleur qui commence, ou {@code null} pour un départ aléatoire.
     */
    public static void configurerIAContreIA(Jeu jeu, NiveauxIA niveauOr, NiveauxIA niveauArgent,
            Pion.TypePion joueurQuiCommence) {
        if (niveauOr == null) {
            niveauOr = NiveauxIA.FACILE;
        }
        if (niveauArgent == null) {
            niveauArgent = NiveauxIA.FACILE;
        }

        jeu.againstIA = true;
        jeu.iaContreIA = true;
        jeu.ia = null;
        jeu.iaOr = niveauOr.creerIA(0);
        jeu.iaArgent = niveauArgent.creerIA(1);
        jeu.niveauIA = null;
        jeu.niveauIAOr = niveauOr;
        jeu.niveauIAArgent = niveauArgent;
        jeu.nomHumainIA = null;
        jeu.joueurDepartIA = null;
        jeu.joueurDepartIAContreIA = joueurQuiCommence;
        jeu.iaEnCours = false;
        jeu.partieAbandonnee = false;
        jeu.gagnantParAbandon = null;

        jeu.joueurs = new Joueur[] {
                new JoueurIa("IA " + libelleNiveau(niveauOr), Pion.TypePion.OR),
                new JoueurIa("IA " + libelleNiveau(niveauArgent), Pion.TypePion.ARGENT)
        };

        jeu.choisirJoueurQuiCommence(joueurQuiCommence);

        jeu.setFeedbackMessage("IA contre IA : " + libelleNiveau(niveauOr) + " (OR) contre "
                + libelleNiveau(niveauArgent) + " (ARGENT). "
                + jeu.nomJoueurActuelPourAffichage() + " commence.");
    }

    /**
     * Sélectionne l'objet IA qui correspond au joueur courant.
     *
     * @param jeu partie courante.
     * @return IA à utiliser pour ce tour, ou {@code null} si aucune n'est configurée.
     */
    private static IA iaPourJoueurCourant(Jeu jeu) {
        if (jeu.ia != null) {
            return jeu.ia;
        }
        return jeu.currentPlayerIndex == 0 ? jeu.iaOr : jeu.iaArgent;
    }

    /**
     * Transforme le nom technique du niveau en texte lisible dans l'interface.
     *
     * @param niveau niveau d'IA à afficher.
     * @return nom humain du niveau.
     */
    private static String libelleNiveau(NiveauxIA niveau) {
        if (niveau == null) {
            return "Facile";
        }
        switch (niveau) {
            case MOYEN:
                return "Moyenne";
            case DIFFICILE:
                return "Difficile";
            case FACILE:
            default:
                return "Facile";
        }
    }
}
