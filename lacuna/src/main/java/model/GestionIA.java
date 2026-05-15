package model;

import java.util.function.Consumer;

import model.IA.CoupIA;
import model.IA.EtatJeu;
import model.IA.NiveauxIA;
import model.IA.ThreadIA;

// Cette classe, Gère le mode IA:configuration,coups calculés et lancement du thread
public final class GestionIA {

    private GestionIA() {
    }

    //Active le mode IA en laissant --- choisir celui qui commence
    public static void activerIA(Jeu jeu, NiveauxIA niveau, String nomHumain, Pion.TypePion joueurQuiCommence) {

        jeu.againstIA = true;
        if (niveau == null) {
            niveau = NiveauxIA.FACILE;
        }
        jeu.ia = niveau.creerIA(1);
        jeu.partieAbandonnee = false;
        jeu.gagnantParAbandon = null;

        jeu.joueurs = new Joueur[] {
                new JoueurHumain(jeu.nomValide(nomHumain, "Humain"), Pion.TypePion.OR),
                new JoueurIa("IA", Pion.TypePion.ARGENT)
        };
        jeu.choisirJoueurQuiCommence(joueurQuiCommence);
        jeu.iaEnCours = false;
        jeu.setFeedbackMessage("Mode IA activé (" + niveau + "). " + jeu.nomJoueurActuelPourAffichage() + " commence.");
    }

    // Savoir si c'est le tour de l'ia
    public static boolean estTourIA(Jeu jeu) {

         if (!jeu.againstIA)
            return false;
        return jeu.getJoueurActuel() instanceof JoueurIa;
    }

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

    // L'AI joue un coup
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

        if (f1 == null || f2 == null || !jeu.fleursConnectées(f1, f2)) {
            jeu.setFeedbackMessage("Le coup IA n'est plus valide : il a été ignoré.");
            return false;
        }

        if (!jeu.placePion(pionLibre, coup.position)) {
            return false;
        }

        jeu.setFeedbackMessage("IA (" + joueur.getTypePion() + ") place son pion.");
        return true;
    }

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
        jeu.setFeedbackMessage("IA (" + joueur.getTypePion() + ") a joué. C'est à " + jeu.nomJoueurActuelPourAffichage() + " de jouer.");
        return true;
    }

    public static void jouerCoupIA(Jeu jeu, CoupIA coup) {
        if (placerPionCoupIA(jeu, coup) && coup != null) {
            collecterFleursCoupIA(jeu, coup);
        }
    }

    // Lancement du thread pour l'ia
    public static void lancerIA(Jeu jeu, Runnable callbackFin) {
        lancerIA(jeu, coup -> jouerCoupIA(jeu, coup), callbackFin);
    }

    public static void lancerIA(Jeu jeu, Consumer<CoupIA> actionQuandCoupPret, Runnable callbackFin) {

        if (!jeu.againstIA || jeu.ia == null || jeu.iaEnCours)
            return;

        if (!estTourIA(jeu)) return;

        jeu.iaEnCours = true;
        final int generationLancee = ++jeu.generationIA;
        jeu.setFeedbackMessage("Tour de l'IA (" + jeu.getJoueurActuel().getTypePion() + ") : elle réfléchit...");

        EtatJeu etat = EtatJeu.depuisJeu(jeu, jeu.currentPlayerIndex);

        ThreadIA thread = new ThreadIA(jeu.ia, etat, coup -> {
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

    public static void configurerIAContreIA(Jeu jeu, NiveauxIA niveauOr, NiveauxIA niveauArgent,
            Pion.TypePion joueurQuiCommence) {
        jeu.againstIA = true;
        jeu.ia = null;
        jeu.iaEnCours = false;
        jeu.partieAbandonnee = false;
        jeu.gagnantParAbandon = null;

        jeu.joueurs = new Joueur[] {new JoueurIa("IA " + niveauOr, Pion.TypePion.OR),
                new JoueurIa("IA " + niveauArgent, Pion.TypePion.ARGENT)};

        jeu.choisirJoueurQuiCommence(joueurQuiCommence);

        jeu.setFeedbackMessage("Test IA contre IA : " + niveauOr + " (OR) contre "
                + niveauArgent + " (ARGENT). "
                + jeu.nomJoueurActuelPourAffichage() + " commence.");
    }
}
