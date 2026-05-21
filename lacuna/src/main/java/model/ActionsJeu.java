package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Regroupe les actions concrètes du plateau.
 *
 * Cette classe contient surtout le parcours d'un tour humain : sélectionner les
 * fleurs, vérifier la ligne jouable, placer le pion et donner les fleurs au
 * joueur.
 */
public final class ActionsJeu {

    private ActionsJeu() {
    }

    /**
     * Joue automatiquement au milieu de la paire de fleurs déjà sélectionnée.
     *
     * @param jeu partie courante.
     * @return {@code true} si le coup a été joué.
     */
    public static boolean jouerCoupAvecFleursSelectionnees(Jeu jeu) {
        Coordonnees posReference = null;
        if (jeu.fleurSelectionnee1 != null && jeu.fleurSelectionnee2 != null) {
            posReference = Coordonnees.milieu(jeu.fleurSelectionnee1.getPosition(), jeu.fleurSelectionnee2.getPosition());
        }
        return jouerCoupAvecFleursSelectionnees(jeu, posReference);
    }

    /**
     * Joue avec les deux fleurs sélectionnées en plaçant le pion au point libre
     * le plus proche du clic.
     *
     * @param jeu partie courante.
     * @param clic position modèle du clic joueur.
     * @return {@code true} si le pion est placé et les fleurs collectées.
     */
    public static boolean jouerCoupAvecFleursSelectionnees(Jeu jeu, Coordonnees clic) {

        if (jeu.partieAbandonnee) {
            return false;
        }

        if (!fleursConnectees(jeu, jeu.fleurSelectionnee1, jeu.fleurSelectionnee2)) {
            return false;
        }

        Coordonnees pos = positionSegmentLaPlusProcheDuClic(
                jeu,
                jeu.fleurSelectionnee1.getPosition(),
                jeu.fleurSelectionnee2.getPosition(),
                clic);

        if (pos == null) {
            jeu.resetFleursSelectionnee1();
            jeu.resetFleursSelectionnee2();
            jeu.setFeedbackMessage("Aucune zone libre sur ce segment : choisissez une autre paire de fleurs.");
            return false;
        }

        Joueur joueurActuel = jeu.getJoueurActuel();
        Pion.TypePion pionType = joueurActuel.getTypePion();
        Pion pionLibre = jeu.getPionLibre(pionType);

        if (pionLibre == null) {
            jeu.setFeedbackMessage("Aucun pion libre disponible pour " + jeu.nomJoueurActuelPourAffichage() + ".");
            return false;
        }

        jeu.jouerCoup(joueurActuel, pionLibre, pos, jeu.fleurSelectionnee1, jeu.fleurSelectionnee2);

        if (pionLibre.getPosition() != null) {
            jeu.resetFleursSelectionnee1();
            jeu.resetFleursSelectionnee2();
            return true;
         }

        return false;
    }

    /**
     * Trouve la meilleure position libre sur le segment.
     *
     * @param jeu partie courante.
     * @param A première extrémité du segment.
     * @param B seconde extrémité du segment.
     * @param clic point visé par le joueur.
     * @return position jouable la plus proche, ou {@code null} si le segment est plein.
     */
    public static Coordonnees positionSegmentLaPlusProcheDuClic(Jeu jeu, Coordonnees A, Coordonnees B, Coordonnees clic) {
        List<Coordonnees> points = pointsDuSegment(jeu, A, B);
        if (points.isEmpty()) {
            return null;
        }

        Coordonnees reference = (clic != null) ? clic : Coordonnees.milieu(A, B);
        Coordonnees meilleure = null;
        double meilleureDistance = Double.MAX_VALUE;

        for (Coordonnees point : points) {
            double d = Coordonnees.distance(reference, point);
            if (d < meilleureDistance) {
                meilleureDistance = d;
                meilleure = point;
            }
        }

        return meilleure;
    }

    /**
     * Gère le clic d'un joueur humain sur une fleur.
     *
     * @param jeu partie courante.
     * @param pos position du clic dans le repère du modèle.
     * @return {@code true} si une fleur a été touchée ou désélectionnée.
     */
    public static boolean toucherFleur(Jeu jeu, Coordonnees pos) {
        if (jeu.partieAbandonnee) {
            return false;
        }
        for (Fleur f : jeu.fleurs) {

            if (Coordonnees.distance(pos, f.getPosition()) < 10) {

                // Phase de sélection d'avantage initial
                if (jeu.enPhaseSelectionAvantage) {
                    jeu.appliquerAvantageAvecFleur(f);
                    jeu.setFeedbackMessage("Avantage initial : " + jeu.getJoueurActuel().getNom() + " a choisi une fleur " + f.getType() + ".");
                    return true;
                }

                if (f == jeu.fleurSelectionnee1) {
                    jeu.fleurSelectionnee1 = null;
                    jeu.setFeedbackMessage("Sélection annulée pour la première fleur.");
                    return true;
                }

                if (f == jeu.fleurSelectionnee2) {
                    jeu.fleurSelectionnee2 = null;
                    jeu.setFeedbackMessage("Sélection annulée pour la seconde fleur.");
                    return true;
                }

                if (jeu.fleurSelectionnee1 == null) {
                    jeu.fleurSelectionnee1 = f;
                    jeu.setFeedbackMessage("Première fleur sélectionnée : " + f.getType() + ".");
                    return true;
                }

                if (jeu.fleurSelectionnee2 == null) {
                    jeu.fleurSelectionnee2 = f;
                    jeu.setFeedbackMessage("Seconde fleur sélectionnée : " + f.getType() + ".");
                    return true;
                }

                jeu.fleurSelectionnee1 = f;
                jeu.fleurSelectionnee2 = null;
                jeu.setFeedbackMessage("Nouvelle première fleur sélectionnée : " + f.getType() + ". Choisissez une seconde fleur.");
                return true;
            }
        }
        jeu.setFeedbackMessage("Aucune fleur touchée. Cliquez sur une fleur visible dans le cercle.");
        return false;
    }

    /**
     * Vérifie que le segment entre deux points n'est pas bloqué.
     *
     * @param jeu partie courante.
     * @param A première extrémité.
     * @param B seconde extrémité.
     * @return {@code true} si la ligne peut servir pour un coup.
     */
    public static boolean segmentLibre(Jeu jeu, Coordonnees A, Coordonnees B) {
        return ReglesPlateau.segmentLibre(jeu, A, B, ConstantesJeu.TOLERANCE_SEGMENT);
    }

    /**
     * Vérifie que deux fleurs forment une paire jouable.
     *
     * @param jeu partie courante.
     * @param f1 première fleur.
     * @param f2 seconde fleur.
     * @return {@code true} si les fleurs ont la même couleur et un segment libre.
     */
    public static boolean fleursConnectees(Jeu jeu, Fleur f1, Fleur f2) {

        if (f1 == null || f2 == null)
            return false;

        if (f1.getType() != f2.getType())
            return false;

        Coordonnees A = f1.getPosition();
        Coordonnees B = f2.getPosition();

        return segmentLibre(jeu, A, B);
    }

    /**
     * Calcule les emplacements où un pion peut être posé sur un segment.
     *
     * @param jeu partie courante.
     * @param A première extrémité.
     * @param B seconde extrémité.
     * @return positions disponibles entre les deux fleurs.
     */
    public static List<Coordonnees> pointsDuSegment(Jeu jeu, Coordonnees A, Coordonnees B) {
        return ReglesPlateau.pointsDuSegment(jeu, A, B, ConstantesJeu.NB_POINTS_SEGMENT,
                ConstantesJeu.DIAMETRE_PION_MODELE + ConstantesJeu.MARGE_COLLISION,
                ConstantesJeu.MARGE_EXTREMITES_SEGMENT_JEU);
    }

    /**
     * Indique si une position évite les zones réservées autour des fleurs.
     *
     * @param A première fleur du segment.
     * @param B seconde fleur du segment.
     * @param pos position testée.
     * @return {@code true} si la position n'est pas trop proche des fleurs.
     */
    public static boolean horsZoneFleursExtremites(Coordonnees A, Coordonnees B, Coordonnees pos) {
        return ReglesPlateau.horsZoneFleursExtremites(A, B, pos, ConstantesJeu.MARGE_EXTREMITES_SEGMENT_JEU);
    }

    /**
     * Liste toutes les positions jouables pour les fleurs encore présentes.
     *
     * @param jeu partie courante.
     * @return positions disponibles pour un coup humain.
     */
    public static List<Coordonnees> coupsPossibles(Jeu jeu) {

        List<Coordonnees> coups = new ArrayList<>();

        for (int i = 0; i < jeu.fleurs.size(); i++) {

            for (int j = i + 1; j < jeu.fleurs.size(); j++) {

                Fleur f1 = jeu.fleurs.get(i);
                Fleur f2 = jeu.fleurs.get(j);

                if (fleursConnectees(jeu, f1, f2)) {

                    List<Coordonnees> points = pointsDuSegment(jeu, f1.getPosition(),f2.getPosition());

                    coups.addAll(points);
                }
            }
        }

        return coups;
    }

    /**
     * Liste les positions jouables pour une couleur donnée.
     *
     * @param jeu partie courante.
     * @param type couleur filtrée, ou {@code null} pour toutes les couleurs.
     * @return positions disponibles.
     */
    public static List<Coordonnees> coupsPossibles(Jeu jeu, Fleur.TypeFleur type) {
        if (type == null) {
            return coupsPossibles(jeu);
        }

        List<Coordonnees> coups = new ArrayList<>();

        for (int i = 0; i < jeu.fleurs.size(); i++) {
            Fleur f1 = jeu.fleurs.get(i);
            if (f1.getType() != type) {
                continue;
            }
            for (int j = i + 1; j < jeu.fleurs.size(); j++) {
                Fleur f2 = jeu.fleurs.get(j);
                if (f2.getType() != type) {
                    continue;
                }
                if (fleursConnectees(jeu, f1, f2)) {
                    coups.addAll(pointsDuSegment(jeu, f1.getPosition(), f2.getPosition()));
                }
            }
        }

        return coups;
    }

    /**
     * Liste les paires de fleurs qui peuvent encore être jouées.
     *
     * @param jeu partie courante.
     * @param type couleur filtrée, ou {@code null} pour toutes les couleurs.
     * @return paires connectées restantes.
     */
    public static List<Fleur[]> pairesConnectees(Jeu jeu, Fleur.TypeFleur type) {
        List<Fleur[]> paires = new ArrayList<>();
        if (type == null) {
            for (int i = 0; i < jeu.fleurs.size(); i++) {
                for (int j = i + 1; j < jeu.fleurs.size(); j++) {
                    Fleur f1 = jeu.fleurs.get(i);
                    Fleur f2 = jeu.fleurs.get(j);
                    if (fleursConnectees(jeu, f1, f2)) {
                        paires.add(new Fleur[]{f1, f2});
                    }
                }
            }
        } else {
            for (int i = 0; i < jeu.fleurs.size(); i++) {
                Fleur f1 = jeu.fleurs.get(i);
                if (f1.getType() != type) continue;
                for (int j = i + 1; j < jeu.fleurs.size(); j++) {
                    Fleur f2 = jeu.fleurs.get(j);
                    if (f2.getType() != type) continue;
                    if (fleursConnectees(jeu, f1, f2)) {
                        paires.add(new Fleur[]{f1, f2});
                    }
                }
            }
        }
        return paires;
    }

    /**
     * Indique s'il reste au moins une paire de fleurs jouable.
     *
     * @param jeu partie courante.
     * @return {@code true} si le joueur doit encore jouer une paire.
     */
    public static boolean existeCoupConnecte(Jeu jeu) {
        return !coupsPossibles(jeu).isEmpty();
    }

    /**
     * Autorise un placement libre quand aucune paire de fleurs n'est jouable
     *  dans la phase1.
     *
     * @param jeu partie courante.
     * @param pos position visée par le joueur.
     * @return {@code true} si le pion libre a été placé.
     */
    public static boolean placerPionLibreSiAucunCoup(Jeu jeu, Coordonnees pos) {
        if (jeu.partieAbandonnee || jeu.estFinNormalePartie()) {
            jeu.setFeedbackMessage("La partie est terminée.");
            return false;
        }

        if (existeCoupConnecte(jeu)) {
            jeu.setFeedbackMessage("Il reste au moins une paire de fleurs connectée : il faut jouer une paire avant de placer librement.");
            return false;
        }

        Joueur joueurActuel = jeu.getJoueurActuel();
        Pion pionLibre = jeu.getPionLibre(joueurActuel.getTypePion());
        if (pionLibre == null) {
            jeu.setFeedbackMessage("Aucun pion libre disponible pour " + jeu.nomJoueurActuelPourAffichage() + ".");
            return false;
        }

        if (!jeu.placePion(pionLibre, pos)) {
            return false;
        }

        jeu.historique.enregistrerAction(pionLibre, null, null, joueurActuel);
        jeu.joueurSuivant();
        jeu.resetFleursSelectionnee1();
        jeu.resetFleursSelectionnee2();
        jeu.setFeedbackMessage(jeu.nomJoueurPourAffichage(joueurActuel) + " place un pion sans collecter : aucune paire connectée n'était disponible.");
        return true;
    }

    /**
     * Trouve une position libre utilisable en secours.
     *
     * @param jeu partie courante.
     * @param type couleur du pion à placer.
     * @return position libre, ou {@code null} si le plateau est saturé.
     */
    public static Coordonnees positionLibreDeSecours(Jeu jeu, Pion.TypePion type) {
        Coordonnees centre = jeu.cercleDeJeu.getCentre();
        double rayonMax = jeu.cercleDeJeu.getRayon() - Math.max(10, jeu.marge);

        for (int r = 0; r <= 12; r++) {
            double rayon = rayonMax * r / 12.0;
            int nbAngles = (r == 0) ? 1 : 24;
            for (int a = 0; a < nbAngles; a++) {
                double angle = (2 * Math.PI * a) / nbAngles;
                Coordonnees pos = new Coordonnees(
                        (int) Math.round(centre.getX() + rayon * Math.cos(angle)),
                        (int) Math.round(centre.getY() + rayon * Math.sin(angle)));
                if (jeu.cercleDeJeu.contientPoint(pos) && ReglesPlateau.positionLibrePourPion(pos, jeu.pions,
                        ConstantesJeu.DIAMETRE_PION_MODELE + ConstantesJeu.MARGE_COLLISION)) {
                    return pos;
                }
            }
        }
        return null;
    }

    public static boolean placerPionIADeSecours(Jeu jeu) {
        if (!jeu.estTourIA()) {
            return false;
        }
        Joueur joueur = jeu.getJoueurActuel();
        Pion pionLibre = jeu.getPionLibre(joueur.getTypePion());
        if (pionLibre == null) {
            jeu.setFeedbackMessage("L'IA n'a plus de pion disponible.");
            return false;
        }
        Coordonnees pos = positionLibreDeSecours(jeu, joueur.getTypePion());
        if (pos == null || !jeu.placePion(pionLibre, pos)) {
            jeu.setFeedbackMessage("L'IA ne trouve aucune position libre pour avancer.");
            return false;
        }
        jeu.historique.enregistrerAction(pionLibre, null, null, joueur);
        jeu.joueurSuivant();
        jeu.setFeedbackMessage(jeu.nomJoueurPourAffichage(joueur)
                + " place un pion sans collecter : aucune paire connectée n'était disponible.");
        return true;
    }

    /**
     * Valide et applique le dépôt d'un pion par glisser-déposer.
     *
     * @param jeu partie courante.
     * @param pos position modèle où le pion a été relâché.
     * @param payload type de pion transporté par le drag.
     * @return {@code true} si le dépôt a joué un coup.
     */
    public static boolean placerPionDepuisDrag(Jeu jeu, Coordonnees pos, Object payload) {

        if (payload == null) {
            return false;
        }

        if (!(payload instanceof Pion.TypePion)) {
            jeu.setFeedbackMessage("Placement invalide : pion non reconnu.");
            return false;
        }

        if (jeu.partieAbandonnee || jeu.estFinNormalePartie()) {
            jeu.setFeedbackMessage("La partie est terminée.");
            return false;
        }

        Pion.TypePion type = (Pion.TypePion) payload;
        Joueur joueurActuel = jeu.getJoueurActuel();

        if (joueurActuel.getTypePion() != type) {
            jeu.setFeedbackMessage("Ce n'est pas le pion de " + jeu.nomJoueurActuelPourAffichage() + ".");
            return false;
        }

        if (!jeu.cercleDeJeu.contientPoint(pos)) {
            jeu.setFeedbackMessage("Placement invalide : le pion doit rester dans le cercle.");
            return false;
        }

        if (!existeCoupConnecte(jeu)) {
            return placerPionLibreSiAucunCoup(jeu, pos);
        }

        if (jeu.fleurSelectionnee1 == null || jeu.fleurSelectionnee2 == null) {
            jeu.setFeedbackMessage("Placement invalide : sélectionnez d'abord deux fleurs avant de déposer le pion.");
            return false;
        }

        if (jeu.fleurSelectionnee1.getType() != jeu.fleurSelectionnee2.getType()) {
            jeu.setFeedbackMessage("Placement invalide : les deux fleurs sélectionnées ne sont pas de la même couleur.");
            return false;
        }

        Coordonnees a = jeu.fleurSelectionnee1.getPosition();
        Coordonnees b = jeu.fleurSelectionnee2.getPosition();

        if (!segmentLibre(jeu, a, b)) {
            jeu.setFeedbackMessage("Placement invalide : la ligne entre les deux fleurs est obstruée.");
            return false;
        }

        if (!Coordonnees.surSegment(a, b, pos, ConstantesJeu.TOLERANCE_DROP_SEGMENT)) {
            jeu.setFeedbackMessage("Placement invalide : déposez le pion sur la ligne entre les deux fleurs sélectionnées.");
            return false;
        }

        Pion pion = jeu.getPionLibre(type);
        if (pion == null) {
            jeu.setFeedbackMessage("Aucun pion libre disponible pour " + jeu.nomJoueurActuelPourAffichage() + ".");
            return false;
        }

        Coordonnees positionSurSegment = positionSegmentLaPlusProcheDuClic(jeu, a, b, pos);
        if (positionSurSegment == null) {
            jeu.setFeedbackMessage("Placement invalide : aucune zone libre sur ce segment.");
            return false;
        }

        Fleur f1 = jeu.fleurSelectionnee1;
        Fleur f2 = jeu.fleurSelectionnee2;
        jeu.jouerCoup(joueurActuel, pion, positionSurSegment, f1, f2);

        if (pion.getPosition() != null) {
            jeu.resetFleursSelectionnee1();
            jeu.resetFleursSelectionnee2();
            return true;
        }

        return false;
    }
}
