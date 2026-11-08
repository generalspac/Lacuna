package model;

import java.util.ArrayList;
import java.util.List;

// Cette classe Gère la phase 2 : attribution des fleurs restantes au pion le plus proche
public final class Phase2 {

    private Phase2() {
    }

    public static class AttributionFleur {
        public final Fleur fleur;
        public final Joueur joueur;
        public final Pion pionLePlusProche;
        public final Pion pionCompare;
        public final boolean comparaisonNecessaire;

        public AttributionFleur(Fleur fleur, Joueur joueur, Pion pionLePlusProche) {
            this(fleur, joueur, pionLePlusProche, null, false);
        }

        public AttributionFleur(Fleur fleur, Joueur joueur, Pion pionLePlusProche, Pion pionCompare, boolean comparaisonNecessaire) {
            this.fleur = fleur;
            this.joueur = joueur;
            this.pionLePlusProche = pionLePlusProche;
            this.pionCompare = pionCompare;
            this.comparaisonNecessaire = comparaisonNecessaire;
        }
    }

    public static boolean secondePhase(Jeu jeu) {
        if (tousLesPionsPlaces(jeu.pions)) {
            attribuerFleursRestantes(jeu);// on attribue les fleurs restantes
            return true; // fin du jeu
        }
        return false;
    }

    public static void attribuerFleursRestantes(Jeu jeu) {
        attribuerFleursRestantes(jeu.fleurs, jeu.pions, jeu.joueurs);
    }

    // attribuer les fleurs restantes 
    public static void attribuerFleursRestantes(List<Fleur> fleurs, List<Pion> pions, Joueur[] joueurs) {
        for (AttributionFleur attribution : calculerAttributionsFleursRestantes(fleurs, pions, joueurs)) {
            appliquerAttributionFleur(fleurs, attribution);
        }
    }

    public static ArrayList<AttributionFleur> calculerAttributionsFleursRestantes(List<Fleur> fleurs,
            List<Pion> pions, Joueur[] joueurs) {
        ArrayList<AttributionFleur> attributions = new ArrayList<>();
        ArrayList<Fleur> copie = new ArrayList<>(fleurs);

        for (Fleur f : copie) {
            AttributionFleur attribution = calculerAttributionFleur(f, pions, joueurs);
            if (attribution != null) {
                attributions.add(attribution);
            }
        }
        return attributions;
    }

    private static AttributionFleur calculerAttributionFleur(Fleur fleur, List<Pion> pions, Joueur[] joueurs) {
        Pion meilleur = null;
        Pion secondAdverse = null;
        double meilleureDistance = Double.MAX_VALUE;
        double distanceSecondAdverse = Double.MAX_VALUE;

        for (Pion p : pions) {
            if (p.getPosition() == null) {
                continue;
            }

            double d = Coordonnees.distance(fleur.getPosition(), p.getPosition());
            if (d < meilleureDistance) {
                if (meilleur != null && meilleur.getType() != p.getType()) {
                    secondAdverse = meilleur;
                    distanceSecondAdverse = meilleureDistance;
                }
                meilleureDistance = d;
                meilleur = p;
            } else if (meilleur != null && p.getType() != meilleur.getType() && d < distanceSecondAdverse) {
                secondAdverse = p;
                distanceSecondAdverse = d;
            }
        }

        Joueur joueur = joueurPourPion(joueurs, meilleur);
        if (joueur == null) {
            return null;
        }

        boolean comparaison = secondAdverse != null && (Math.abs(distanceSecondAdverse - meilleureDistance) <= 35
                || distanceSecondAdverse <= meilleureDistance * 1.18);
        return new AttributionFleur(fleur, joueur, meilleur, secondAdverse, comparaison);
    }

    public static void appliquerAttributionFleur(List<Fleur> fleurs, AttributionFleur attribution) {
        if (attribution == null || attribution.fleur == null || attribution.joueur == null) {
            return;
        }
        if (fleurs.remove(attribution.fleur)) {
            attribution.joueur.ajouterFleur(attribution.fleur);
        }
    }

    public static Joueur joueurPourPion(Joueur[] joueurs, Pion pion) {
        if (pion == null) {
            return null;
        }
        for (Joueur j : joueurs) {
            if (j.getTypePion() == pion.getType()) {
                return j;
            }
        }
        return null;
    }

    public static boolean estSecondePhaseDisponible(List<Fleur> fleurs, List<Pion> pions) {
        return tousLesPionsPlaces(pions) && !fleurs.isEmpty();
    }

    public static boolean tousLesPionsPlaces(List<Pion> pions) {
        for (Pion p : pions) {
            // le pion n'a pas de coordonnées, donc il n'est pas placé dans le plateau de jeu
            if (p.getPosition() == null)
                return false;
        }
        return true;
    }
}
