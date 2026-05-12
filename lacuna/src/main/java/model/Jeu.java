package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;

import global.Configuration;

import model.IA.IA;
import model.IA.NiveauxIA;
import model.IA.CoupIA;
import model.IA.EtatJeu;
import model.IA.ThreadIA;




public class Jeu {

    // =========================================================
    // ===================== ATTRIBUTS 
    // =====================================
    private Cercle cercleDeJeu; // le cercle du jeu
    private ArrayList<Pion> pions; // contient tous les pions actuel dans le cercle
    private ArrayList<Fleur> fleurs;
    private Fleur fleurSelectionnee1; // la fleur que le joueur a touché ( pour le moment on suppose que c'est la
                                      // fleur la plus proche du point de clic )
    private Fleur fleurSelectionnee2; // la fleur que le joueur a touché ( pour le moment on suppose que c'est la
                                      // fleur la plus proche du point de clic )
    int nbPions = 12; // nombre de pions
    int nbFleurs = 49; // nombre de fleurs

    private static final double DIST_MIN_FLEURS = 30; // distance min entre les fleurs
    private static final int NB_JOUEURS = 2;
    private static final int MAX_PIONS_PAR_JOUEUR = 6;
    private static final int NB_COULEURS = 6;


    private static final int NB_POINTS_SEGMENT = 3; // le nombre de points

    //// Attributs pour les joueurs
    private Joueur[] joueurs; // la liste des joueurs
    public int currentPlayerIndex; // 0 pour le joueur 1

    private Random random;

    private int WIDTH = 30;
    private int HEIGHT = 30;

    public boolean againstIA = false;

    ///////// pour la marge (afin que les fleurs ne sortent pas du cercle)//////////
    ////////// vu qu'on dessine les fleurs avec le format dans view (hauteur *
    ///////// largeur )
    ///////// avec hauteur == largeur, alors la view va nous envoyer la taille qu'on
    ///////// divisera par 2
    ///////// voir dans jeuGraphique, juste après rcalcul de la taille, on fera
    ///////// setMarge(taille/2)

    private double marge;

    private Historique historique;

    // Avantage du joueur qui commence
    private boolean avantageInitialApplique = false;
    private boolean enPhaseSelectionAvantage = false;
    private String feedbackMessage = "";


    ///////////////// IA /////////////////
    private IA ia;
    private boolean iaEnCours = false;
    private int generationIA = 0;

    public String getFeedbackMessage() {
        return feedbackMessage;
    }

    public void setFeedbackMessage(String feedbackMessage) {
        this.feedbackMessage = feedbackMessage;
    }

    // =========================================================
    // ===================== CONSTRUCTEUR ======================
    // =========================================================

    public Jeu(int WIDTH, int HEIGHT, Cercle cercleDeJeu) {

        if (isAgainstIA()) {
            System.out.println("Game created in IA mode");
        } else {
            System.out.println("Game created in MULTI mode");
        }

        this.cercleDeJeu = cercleDeJeu;
        pions = new ArrayList<>();
        fleurs = new ArrayList<>();
        historique = new Historique();

        this.WIDTH = WIDTH;
        this.HEIGHT = HEIGHT;

        random = new Random();

        initPlayers();
        initFleurs();
        initPions();
        demarrerPhaseSelectionAvantage();

        System.out.println("Game created");
    }

    // =========================================================
    // ================= GETTERS ET SETTERS =====================
    // =========================================================

    //////////////////////////////////// GETTERS ET SETTERS METHODES
    //////////////////////////////////// /////////////////////////////////////////
    public void setMarge(double marge) {
        this.marge = marge;
    }

    public Cercle getCercleDeJeu() {
        return cercleDeJeu;
    }

    public ArrayList<Pion> getPions() {
        return pions;
    }

    public ArrayList<Fleur> getFleurs() {
        return fleurs;
    }

    public int getNbPions() {
        return nbPions;
    }

    public int getNbFleurs() {
        return nbFleurs;
    }

    public int getNbJoueurs() {
        return NB_JOUEURS;
    }

    public Joueur[] getJoueurs() {
        return joueurs;
    }

    public Joueur getJoueurActuel() {
        return joueurs[currentPlayerIndex];
    }

    public Historique getHistorique() {
        return historique;
    }

    public void setAvantageInitialApplique(boolean avantageInitialApplique) {
        this.avantageInitialApplique = avantageInitialApplique;
    }

    public void setEnPhaseSelectionAvantage(boolean enPhaseSelectionAvantage) {
        this.enPhaseSelectionAvantage = enPhaseSelectionAvantage;
    }

    // =========================================================
    // ================= GESTION DES JOUEURS ====================
    // =========================================================

    public void joueurSuivant() {
        currentPlayerIndex = (currentPlayerIndex + 1) % NB_JOUEURS;
    }

    public void joueurPrecedent() {
        currentPlayerIndex = (currentPlayerIndex - 1 + NB_JOUEURS) % NB_JOUEURS;
    }

    public boolean isAgainstIA() {
        return againstIA;
    }

    public boolean isAvantageInitialApplique() {
        return avantageInitialApplique;
    }

    public boolean isEnPhaseSelectionAvantage() {
        return enPhaseSelectionAvantage;
    }

    public Pion getPionLibre(Pion.TypePion type) {
        for (Pion p : pions) {
            if (p.getType() == type && p.getPosition() == null) {
                return p;
            }
        }
        return null;
    }

    // Le nombre de pions par type placés dans le cercle
    private int nombreDePionsPlacees(Pion.TypePion type) {
        int compteur = 0;
        for (Pion p : pions) {
            if (p.getType() == type && p.getPosition() != null) {
                compteur++;
            }
        }
        return compteur;
    }

    // si on peut placer un pion d'un certain type(or ou argent) dans le cercle
    private boolean peutPlacerPionDeType(Pion.TypePion type) {
        return nombreDePionsPlacees(type) < MAX_PIONS_PAR_JOUEUR;
    }

    // =========================================================
    // ================= INITIALISATION =========================
    // =========================================================

    void initPlayers() {
        if (isAgainstIA()) {
            joueurs = new Joueur[] {
                    new JoueurHumain("Humain", Pion.TypePion.OR),
                    new JoueurIa("IA", Pion.TypePion.ARGENT)
            };
            currentPlayerIndex = 0;
        } else {
            joueurs = new Joueur[] {
                    new JoueurHumain("Joueur 1", Pion.TypePion.OR),
                    new JoueurHumain("Joueur 2", Pion.TypePion.ARGENT)
            };
            currentPlayerIndex = random.nextInt(getNbJoueurs());
            setFeedbackMessage(feedbackMessage + " Le joueur qui commence est : " + getJoueurActuel().getNom() + " (" + getJoueurActuel().getTypePion() + ").") ;
        }
    }

    ////////////////// initialisatin des fleurs et
    ////////////////// pions////////////////////////////////////
    public void initFleurs() {
        fleurs.clear();

        for (Fleur.TypeFleur type : Fleur.TypeFleur.values()) {
            for (int i = 0; i < 7; i++) { //CHAQUE COULEUR À 6 PIONS    
                Coordonnees pos = genererPositionAleatoire(fleurs);
                fleurs.add(new Fleur(type, pos));
            }
        }
    }

    public void initPions() {
        pions.clear();

        for (Pion.TypePion type : Pion.TypePion.values()) {
            for (int i = 0; i < MAX_PIONS_PAR_JOUEUR; i++) {
                Coordonnees pos = null;
                pions.add(new Pion(type, pos));
            }
        }
    }

    private void demarrerPhaseSelectionAvantage() {
        enPhaseSelectionAvantage = true;
        //statusLabel.setText("Phase d'avantage initial: " + getJoueurActuel().getNom() + " doit choisir une fleur");
        setFeedbackMessage("Phase d'avantage initial: " + getJoueurActuel().getTypePion() + " doit choisir une fleur");
        //System.out.println("Phase d'avantage initial: " + getJoueurActuel().getNom() + " doit choisir une fleur");
    }

    // Génération de position aleatoire pour les fleurs
    private Coordonnees genererPositionAleatoire(ArrayList<Fleur> fleurs) {
        Coordonnees pos;

        do {
            double angle = Math.random() * 2 * Math.PI;
            double margeFleur = marge; // pour ne pas déborder
            double r = Math.sqrt(Math.random()) * (cercleDeJeu.getRayon() - margeFleur);

            double x = cercleDeJeu.getCentre().getX() + r * Math.cos(angle);
            double y = cercleDeJeu.getCentre().getY() + r * Math.sin(angle);
            pos = new Coordonnees((int) x, (int) y);

        } while (!positionValide(pos, fleurs));

        return pos;
    }

    // Si la position pour mettre les fleurs est valide
    private boolean positionValide(Coordonnees pos, ArrayList<Fleur> fleurs) {
        for (Fleur f : fleurs) {
            if (Coordonnees.distance(pos, f.getPosition()) < DIST_MIN_FLEURS) {
                return false;
            }
        }
        return true;
    }

    // =========================================================
    // ================= LOGIQUE DU JEU =========================
    // =========================================================

    public void appliquerAvantageAvecFleur(Fleur fleurChoisie) {
        if (enPhaseSelectionAvantage && !avantageInitialApplique && fleurs.contains(fleurChoisie)) {
            // Donner cette fleur au joueur qui commence
            Joueur joueurCommence = getJoueurActuel();
            joueurCommence.ajouterFleur(fleurChoisie);
            fleurs.remove(fleurChoisie);
            historique.enregistrerActionAvantage(fleurChoisie, joueurCommence);
            avantageInitialApplique = true;
            enPhaseSelectionAvantage = false;

            System.out.println(
                    "Avantage initial: " + joueurCommence.getNom() + " choisit une fleur " + fleurChoisie.getType());
        }
    }

    //////////////// FONCTIONS POUR LES REGLES DU JEU ///////////////
    ///////////// les 3 premieres fonctions sont les principales pour le
    //////////////// jeu///////////

    // l'avantage du premier joueur au tout debut
    public void avantagePremierJoueur() {
        Joueur joueur = getJoueurActuel();

        if (fleurs.isEmpty())
            return; // on va verifier la liste des fleurs

        Fleur f = fleurs.get(random.nextInt(fleurs.size()));
        joueur.ajouterFleur(f);
        fleurs.remove(f);
    }

    // pour placer un pion dans le cercle de jeu
    public boolean placePion(Pion pion, Coordonnees pos) {

        if (!peutPlacerPionDeType(pion.getType())) {
            setFeedbackMessage("Impossible : plus de pions de type " + pion.getType() + " disponibles.");
            return false;
        }

        if (!cercleDeJeu.contientPoint(pos)) {
            setFeedbackMessage("Impossible : le pion doit être placé à l'intérieur du cercle.");
            return false;
        }

        if (!positionLibrePourPion(pos)) {
            setFeedbackMessage("Impossible : la position est déjà occupée ou trop proche d'un autre pion.");
            return false;
        }

        pion.setPosition(pos);
        setFeedbackMessage("Pion " + pion.getType() + " placé avec succès.");
        return true;
    }

    // pour jouer un coup (controller appelle ça )
    // c'est la premiere phase du jeu
    public void jouerCoup(Joueur joueur, Pion pion, Coordonnees pos, Fleur f1, Fleur f2) {

        // si tous les pions sont placés
        //pas besoin de  cette fonction ici car jouer coup est pour la premiere
        //phase du jeu
        /*if (tousLesPionsPlaces()) {
            attribuerFleursRestantes();// on attribue les fleurs restantes
            return; // fin du jeu
        }*/


        if (!placePion(pion, pos))
            return;

        if (!mangerFleurs(joueur, f1, f2)) {
            pion.setPosition(null); // On remet la position à null (car si on a passé la premiere condition, la
                                    // position n'est pas null)
            return;
        }

        // Gestion de la stack
        historique.enregistrerAction(pion, f1, f2, joueur);




        joueurSuivant();// Passons au joueur suivant

    }


    public boolean jouerCoupAvecFleursSelectionnees() {
        Coordonnees posReference = null;
        if (fleurSelectionnee1 != null && fleurSelectionnee2 != null) {
            posReference = Coordonnees.milieu(fleurSelectionnee1.getPosition(), fleurSelectionnee2.getPosition());
        }
        return jouerCoupAvecFleursSelectionnees(posReference);
    }

    public boolean jouerCoupAvecFleursSelectionnees(Coordonnees clic) {

        if (!fleursConnectées(fleurSelectionnee1, fleurSelectionnee2)) {
            return false;
        }

        Coordonnees pos = positionSegmentLaPlusProcheDuClic(
                fleurSelectionnee1.getPosition(),
                fleurSelectionnee2.getPosition(),
                clic);

        if (pos == null) {
            setFeedbackMessage("Aucune zone libre sur ce segment : choisissez une autre paire de fleurs.");
            return false;
        }

        Joueur joueurActuel = getJoueurActuel();
        Pion.TypePion pionType = joueurActuel.getTypePion();
        Pion pionLibre = getPionLibre(pionType);

        if (pionLibre == null) {
            setFeedbackMessage("Aucun pion libre disponible pour " + nomJoueurActuelPourAffichage() + ".");
            return false;
        }

        jouerCoup(joueurActuel, pionLibre, pos, fleurSelectionnee1, fleurSelectionnee2);

        if (pionLibre.getPosition() != null) {
            resetFleursSelectionnee1();
            resetFleursSelectionnee2();
            return true;
         }

        return false;
    }

    public Coordonnees positionSegmentLaPlusProcheDuClic(Coordonnees A, Coordonnees B, Coordonnees clic) {
        List<Coordonnees> points = pointsDuSegment(A, B);
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

    //fonction pour la seconde phase du jeu (controller appelle ça aussi )
    public boolean secondePhase(){

        if (tousLesPionsPlaces()) {
            attribuerFleursRestantes();// on attribue les fleurs restantes
            return true; // fin du jeu
        }
        return false;
    }

    /* ============= fonctions auxiliares ================= */

    // on verifie si la position où on place le pion est libre
    // A REVOIR
    private boolean positionLibrePourPion(Coordonnees pos) {
        // on verifie qu'il y a une distance avec toutes les fleurs
        /*
         * for (Fleur f : fleurs) {
         * if (distance(pos, f.getPosition()) < DIST_MIN_FLEURS) {
         * return false;
         * }
         * }
         */
        // on verifie qu'il y a une distance aussi avec les pions
        for (Pion p : pions) {
            if (p.getPosition() != null && Coordonnees.distance(pos, p.getPosition()) < DIST_MIN_FLEURS) {
                return false;
            }
        }

        return true;
    }

    // pour savoir si le joueur mange les fleurs ( mange et renvoie vrai si oui)
    public boolean mangerFleurs(Joueur joueur, Fleur f1, Fleur f2) {
        // on verifie les type d'abord
        if (f1.getType() != f2.getType())
            return false;
        // s'assrere que les fleurs sont dans le plateau (robustesse)
        if (!fleurs.contains(f1) || !fleurs.contains(f2))
            return false;

        // le joueur mange les 2 fleurs
        joueur.ajouterFleur(f1);
        joueur.ajouterFleur(f2);
        // les fleurs ne font plus partie du plateau
        fleurs.remove(f1);
        fleurs.remove(f2);

        return true;
    }

    // attribuer les fleurs restantes ( après avoir joué , il se peut que le joueur
    // suivant ai 2 fleurs)
    // qui sont proche de lui, car des obstacles ont disparus sur le plateau, si on
    // peut dire comme ça )
    public void attribuerFleursRestantes() {

        for (AttributionFleur attribution : calculerAttributionsFleursRestantes()) {
            appliquerAttributionFleur(attribution);
        }
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

    public ArrayList<AttributionFleur> calculerAttributionsFleursRestantes() {
        ArrayList<AttributionFleur> attributions = new ArrayList<>();
        ArrayList<Fleur> copie = new ArrayList<>(fleurs);

        for (Fleur f : copie) {
            AttributionFleur attribution = calculerAttributionFleur(f);
            if (attribution != null) {
                attributions.add(attribution);
            }
        }
        return attributions;
    }

    private AttributionFleur calculerAttributionFleur(Fleur fleur) {
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

        Joueur joueur = joueurPourPion(meilleur);
        if (joueur == null) {
            return null;
        }

        boolean comparaison = secondAdverse != null
                && (Math.abs(distanceSecondAdverse - meilleureDistance) <= 35
                    || distanceSecondAdverse <= meilleureDistance * 1.18);

        return new AttributionFleur(fleur, joueur, meilleur, secondAdverse, comparaison);
    }

    public void appliquerAttributionFleur(AttributionFleur attribution) {
        if (attribution == null || attribution.fleur == null || attribution.joueur == null) {
            return;
        }
        if (fleurs.remove(attribution.fleur)) {
            attribution.joueur.ajouterFleur(attribution.fleur);
        }
    }

    public boolean estSecondePhaseDisponible() {
        return tousLesPionsPlaces() && !fleurs.isEmpty();
    }

    // le joueur le plus proche
    private Joueur joueurLePlusProche(Fleur fleur) {
        return joueurPourPion(pionLePlusProche(fleur));
    }

    public Pion pionLePlusProche(Fleur fleur) {
        if (fleur == null || fleur.getPosition() == null) {
            return null;
        }

        Pion meilleur = null;
        double min = Double.MAX_VALUE;
        for (Pion p : pions) {
            if (p.getPosition() != null) {
                double d = Coordonnees.distance(fleur.getPosition(), p.getPosition());
                if (d < min) {
                    min = d;
                    meilleur = p;
                }
            }
        }
        return meilleur;
    }

    public Joueur joueurPourPion(Pion pion) {
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

    public Joueur getGagnant() {
        int couleursJ0 = joueurs[0].nbCouleursGagnees();
        int couleursJ1 = joueurs[1].nbCouleursGagnees();
        if (couleursJ0 > couleursJ1) return joueurs[0];
        if (couleursJ1 > couleursJ0) return joueurs[1];

        int fleursJ0 = joueurs[0].getScoreTotal();
        int fleursJ1 = joueurs[1].getScoreTotal();
        if (fleursJ0 > fleursJ1) return joueurs[0];
        if (fleursJ1 > fleursJ0) return joueurs[1];
        return null;
    }

    // verifie si tous les pions sont placés (type argent et or )
    private boolean tousLesPionsPlaces() {
        for (Pion p : pions) {
            // le pion n'a pas de coordonnée, donc il n'est pas placé dans le plateau de jeu
            if (p.getPosition() == null)
                return false;
        }
        return true;
    }

    // =========================================================
    // =============== SELECTION DES FLEURS / SEGMENTS ==========
    // =========================================================

    public boolean toucherFleur(Coordonnees pos) {
        for (Fleur f : fleurs) {

            if (Coordonnees.distance(pos, f.getPosition()) < 10) {

                // Phase de sélection d'avantage initial
                if (enPhaseSelectionAvantage) {
                    appliquerAvantageAvecFleur(f);
                    setFeedbackMessage("Avantage initial : " + getJoueurActuel().getNom() + " a choisi une fleur " + f.getType() + ".");
                    return true;
                }

                if (f == fleurSelectionnee1) {
                    fleurSelectionnee1 = null;
                    setFeedbackMessage("Sélection annulée pour la première fleur.");
                    return true;
                }

                if (f == fleurSelectionnee2) {
                    fleurSelectionnee2 = null;
                    setFeedbackMessage("Sélection annulée pour la seconde fleur.");
                    return true;
                }

                if (fleurSelectionnee1 == null) {
                    fleurSelectionnee1 = f;
                    setFeedbackMessage("Première fleur sélectionnée : " + f.getType() + ".");
                    return true;
                }

                if (fleurSelectionnee2 == null) {
                    fleurSelectionnee2 = f;
                    setFeedbackMessage("Seconde fleur sélectionnée : " + f.getType() + ".");
                    return true;
                }

                fleurSelectionnee1 = f;
                fleurSelectionnee2 = null;
                setFeedbackMessage("Nouvelle première fleur sélectionnée : " + f.getType() + ". Choisissez une seconde fleur.");
                return true;
            }
        }
        setFeedbackMessage("Aucune fleur touchée. Cliquez sur une fleur visible dans le cercle.");
        return false;
    }

    private boolean segmentLibre(Coordonnees A, Coordonnees B) {

        // Vérifier les fleurs comme obstacles
        for (Fleur f : fleurs) {

            Coordonnees C = f.getPosition();

            // ignorer les extrémités (les fleurs sélectionnées)
            if (C.equals(A) || C.equals(B))
                continue;

            if (Coordonnees.surSegment(A, B, C, 12)) {
                return false; // obstacle trouvé
            }
        }

        // Vérifier les pions comme obstacles
        for (Pion p : pions) {
            Coordonnees C = p.getPosition();
            if (C == null)
                continue;

            if (Coordonnees.surSegment(A, B, C, 12)) {
                return false; // un pion bloque le chemin
            }
        }

        return true;
    }

    public boolean fleursConnectées(Fleur f1, Fleur f2) {

        if (f1 == null || f2 == null)
            return false;

        if (f1.getType() != f2.getType())
            return false;

        Coordonnees A = f1.getPosition();
        Coordonnees B = f2.getPosition();

        return segmentLibre(A, B);
    }

    public Coordonnees milieu(Coordonnees A, Coordonnees B) {
        return Coordonnees.milieu(A, B);
    }

    public Fleur getFleurSelectionnee1() {
        return fleurSelectionnee1;
    }

    public Fleur getFleurSelectionnee2() {
        return fleurSelectionnee2;
    }

    public void resetFleursSelectionnee1() {
        fleurSelectionnee1 = null;

    }

    public void resetFleursSelectionnee2() {
        fleurSelectionnee2 = null;

    }

    public void selectionnerFleursPourAnimation(Fleur f1, Fleur f2) {
        fleurSelectionnee1 = f1;
        fleurSelectionnee2 = f2;
    }

    // =========================================================
    // ========================= (Guide) ==========================
    // =========================================================

    public List<Coordonnees> pointsDuSegment(Coordonnees A, Coordonnees B) {

        List<Coordonnees> points = new ArrayList<>();

        for (Coordonnees pos : Coordonnees.pointsDuSegment(A, B, NB_POINTS_SEGMENT)) {

            // Vérification:le pion peut être placé ici?
            if (positionLibrePourPion(pos) && cercleDeJeu.contientPoint(pos)) {
                points.add(pos);
            }
        }

        return points;
    }

    // Retourne toutes les positions possibles pour jouer
    public List<Coordonnees> coupsPossibles() {

        List<Coordonnees> coups = new ArrayList<>();

        for (int i = 0; i < fleurs.size(); i++) {

            for (int j = i + 1; j < fleurs.size(); j++) {

                Fleur f1 = fleurs.get(i);
                Fleur f2 = fleurs.get(j);

                //Même couleur + segment libre
                if (fleursConnectées(f1, f2)) {

                    List<Coordonnees> points = pointsDuSegment(f1.getPosition(),f2.getPosition());

                    coups.addAll(points);
                }
            }
        }

        return coups;
    }

    public boolean existeCoupConnecte() {
        return !coupsPossibles().isEmpty();
    }

    public boolean placerPionLibreSiAucunCoup(Coordonnees pos) {
        if (existeCoupConnecte()) {
            setFeedbackMessage("Il reste au moins une paire de fleurs connectée : il faut jouer une paire avant de placer librement.");
            return false;
        }

        Joueur joueurActuel = getJoueurActuel();
        Pion pionLibre = getPionLibre(joueurActuel.getTypePion());
        if (pionLibre == null) {
            setFeedbackMessage("Aucun pion libre disponible pour " + nomJoueurActuelPourAffichage() + ".");
            return false;
        }

        if (!placePion(pionLibre, pos)) {
            return false;
        }

        historique.enregistrerAction(pionLibre, null, null, joueurActuel);
        joueurSuivant();
        resetFleursSelectionnee1();
        resetFleursSelectionnee2();
        setFeedbackMessage(nomJoueurPourAffichage(joueurActuel) + " place un pion sans collecter : aucune paire connectée n'était disponible.");
        return true;
    }

    private Coordonnees positionLibreDeSecours(Pion.TypePion type) {
        Coordonnees centre = cercleDeJeu.getCentre();
        double rayonMax = cercleDeJeu.getRayon() - Math.max(10, marge);

        for (int r = 0; r <= 12; r++) {
            double rayon = rayonMax * r / 12.0;
            int nbAngles = (r == 0) ? 1 : 24;
            for (int a = 0; a < nbAngles; a++) {
                double angle = (2 * Math.PI * a) / nbAngles;
                Coordonnees pos = new Coordonnees(
                        (int) Math.round(centre.getX() + rayon * Math.cos(angle)),
                        (int) Math.round(centre.getY() + rayon * Math.sin(angle)));
                if (cercleDeJeu.contientPoint(pos) && positionLibrePourPion(pos)) {
                    return pos;
                }
            }
        }
        return null;
    }

    private boolean placerPionIADeSecours() {
        if (!estTourIA()) {
            return false;
        }
        Joueur joueur = getJoueurActuel();
        Pion pionLibre = getPionLibre(joueur.getTypePion());
        if (pionLibre == null) {
            setFeedbackMessage("L'IA n'a plus de pion disponible.");
            return false;
        }
        Coordonnees pos = positionLibreDeSecours(joueur.getTypePion());
        if (pos == null || !placePion(pionLibre, pos)) {
            setFeedbackMessage("L'IA ne trouve aucune position libre pour avancer.");
            return false;
        }
        historique.enregistrerAction(pionLibre, null, null, joueur);
        joueurSuivant();
        setFeedbackMessage("IA (" + joueur.getTypePion() + ") place un pion sans collecter : aucune paire connectée n'était disponible.");
        return true;
    }

    // =========================================================
    // ==================== DRAG AND DROP =======================
    // =========================================================

    public void placerPionDepuisDrag(Coordonnees pos, Object payload) {

        if (payload == null)
            return;

        if (!(payload instanceof Pion.TypePion)) {
            System.out.println("Payload invalide");
            return;
        }

        Pion.TypePion type = (Pion.TypePion) payload;
        Joueur joueurActuel = getJoueurActuel();

        if (joueurActuel.getTypePion() != type) {
            System.out.println("Ce n'est pas le bon joueur !");
            return;
        }

        Fleur[] flowers = trouverFleursPourPion(pos);
        if (flowers == null) {
            System.out.println("Placement invalide : pas entre deux fleurs compatibles !");
            return;
        }

        Pion pion = getPionLibre(type);
        if (pion == null) {
            System.out.println("Aucun pion libre disponible pour ce joueur.");
            return;
        }

        Coordonnees positionSurSegment = positionSegmentLaPlusProcheDuClic(
                flowers[0].getPosition(), flowers[1].getPosition(), pos);
        if (positionSurSegment == null) {
            setFeedbackMessage("Placement invalide : aucune zone libre sur ce segment.");
            return;
        }

        jouerCoup(joueurActuel, pion, positionSurSegment, flowers[0], flowers[1]);

        System.out.println("Pion placé via drag !");
    }

    private Fleur[] trouverFleursPourPion(Coordonnees pos) {
        for (Fleur.TypeFleur typeFleur : Fleur.TypeFleur.values()) {
            List<Fleur> deMemeType = new ArrayList<>();
            for (Fleur f : fleurs) {
                if (f.getType() == typeFleur) {
                    deMemeType.add(f);
                }
            }

            for (int i = 0; i < deMemeType.size(); i++) {
                for (int j = i + 1; j < deMemeType.size(); j++) {
                    Fleur f1 = deMemeType.get(i);
                    Fleur f2 = deMemeType.get(j);

                    if (fleursConnectées(f1, f2)) {
                        Coordonnees candidate = positionSegmentLaPlusProcheDuClic(f1.getPosition(), f2.getPosition(), pos);
                        if (candidate != null && Coordonnees.distance(candidate, pos) <= 22) {
                            return new Fleur[] { f1, f2 };
                        }
                    }
                }
            }
        }
        return null;
    }

    // =========================================================
    // ======================= AFFICHAGE ========================
    // =========================================================

    ////////////////////////////// pour l'affichage
    ////////////////////////////// ////////////////////////////////////////
    public void afficher() {
        Configuration.debugeur("Affichage du jeu : \n#");
        for (int i = 0; i < WIDTH; i++) {
            Configuration.debugeur("-");
        }
        Configuration.debugeur("#\n");

        for (int y = 0; y < HEIGHT; y++) {
            Configuration.debugeur("|");
            for (int x = 0; x < WIDTH; x++) {
                Coordonnees position = new Coordonnees(x, y);
                char symbole = ' ';

                for (Pion pion : pions) {
                    if (pion.getPosition() != null && pion.getPosition().equals(position)) {
                        symbole = pion.typePionToChar();
                        break;
                    }
                }

                if (symbole == ' ') {
                    for (Fleur fleur : fleurs) {
                        if (fleur != null && fleur.getPosition().equals(position)) {
                            symbole = fleur.typeFleurToChar();
                            break;
                        }
                    }
                }

                Configuration.debugeur("%c", symbole);
            }
            Configuration.debugeur("|\n");
        }

        Configuration.debugeur("#");
        for (int i = 0; i < WIDTH; i++) {
            Configuration.debugeur("-");
        }
        Configuration.debugeur("#\n");
    }
//////////////////////////////////////////////////////////////////////

    public Map<Fleur.TypeFleur, Integer> getScore(Joueur joueur) {

        Map<Fleur.TypeFleur, Integer> map = new java.util.HashMap<>();

        for (Fleur.TypeFleur t : Fleur.TypeFleur.values()) {
            map.put(t, 0);
        }

        for (Fleur f : joueur.getFleursGagnees()) {
            map.put(f.getType(), map.get(f.getType()) + 1);
        }

        return map;
    }

    // =========================================================
    // ====================== HISTORIQUE ========================
    // =========================================================

    public void undo() {
        annulerIAEnCours();
        historique.undo(this);
    }

    public void redo() {
        annulerIAEnCours();
        historique.redo(this);
    }

    public void reset() {
        annulerIAEnCours();
        // init le jeu à son état initial
        avantageInitialApplique = false;
        enPhaseSelectionAvantage = false;
        initPlayers();
        initFleurs();
        initPions();
        demarrerPhaseSelectionAvantage();
        historique.clear();
        setFeedbackMessage("Partie réinitialisée. Choisissez une fleur pour l'avantage initial.");
    }

    // Sauvegarde l'état complet de la partie dans un fichier texte
    public void sauvegarderPartie(String cheminFichier) {
        historique.sauvegarderPartie(this, cheminFichier);
    }

    // Charge l'état d'une partie depuis un fichier texte et reconstruit le jeu
    public void chargerPartie(String cheminFichier) {
        annulerIAEnCours();
        historique.chargerPartie(this, cheminFichier);
    }




    // =========================================================
    // ========================= (IA) ==========================
    // =========================================================///////////////////

    // Activation de l'ia
    public void activerIA(NiveauxIA niveau) {

        againstIA = true;
        ia = niveau.creerIA(1);

        joueurs = new Joueur[] {
                new JoueurHumain("Humain", Pion.TypePion.OR),
                new JoueurIa("IA", Pion.TypePion.ARGENT)
        };
        currentPlayerIndex = 0;
        iaEnCours = false;
        setFeedbackMessage("Mode IA activé (" + niveau + "). Humain (OR) commence.");
    }

    // Savoir si l'ia est en cours
    public boolean isIAEnCours() {
        return iaEnCours;
    }

    // Savoir si c'est le tour de l'ia
    public boolean estTourIA() {
        
         if (!againstIA)
            return false;
        return getJoueurActuel() instanceof JoueurIa;
    }

    private Fleur retrouverFleurDuPlateau(Fleur recherchee) {
        if (recherchee == null) {
            return null;
        }

        for (Fleur f : fleurs) {
            if (f.getType() == recherchee.getType()
                    && f.getPosition().equals(recherchee.getPosition())) {
                return f;
            }
        }

        return null;
    }

    // L'AI joue un coup
    public void jouerCoupIA(CoupIA coup) {

        if (coup == null) {
            if (!placerPionIADeSecours()) {
                setFeedbackMessage("L'IA n'a trouvé aucun coup jouable.");
            }
            return;
        }

        if (!estTourIA()) {
            return;
        }

        Joueur joueur = getJoueurActuel();
        Pion pionLibre = getPionLibre(joueur.getTypePion());

        if (pionLibre == null) {
            setFeedbackMessage("L'IA n'a plus de pion disponible.");
            return;
        }

        Fleur f1 = retrouverFleurDuPlateau(coup.fleur1);
        Fleur f2 = retrouverFleurDuPlateau(coup.fleur2);

        if (f1 == null || f2 == null) {
            setFeedbackMessage("Le coup IA n'est plus valide : il a été ignoré.");
            return;
        }

        jouerCoup(joueur, pionLibre, coup.position, f1, f2);

        if (getJoueurActuel() instanceof JoueurIa) {
            setFeedbackMessage("Le coup IA a échoué : aucun changement de tour.");
        } else {
            setFeedbackMessage("IA (" + joueur.getTypePion() + ") a joué. C'est à Humain (" + getJoueurActuel().getTypePion() + ") de jouer.");
        }
    }


    // Annule  
    public void annulerIAEnCours() {
        generationIA++;
        iaEnCours = false;
    }

    public String nomJoueurPourAffichage(Joueur joueur) {
        if (joueur instanceof JoueurIa) {
            return "IA (" + joueur.getTypePion() + ")";
        }
        if (againstIA) {
            return "Humain (" + joueur.getTypePion() + ")";
        }
        return joueur.getNom() + " (" + joueur.getTypePion() + ")";
    }

    public String nomJoueurActuelPourAffichage() {
        return nomJoueurPourAffichage(getJoueurActuel());
    }

    // Lancement du thread pour l'ia
    public void lancerIA(Runnable callbackFin) {
        lancerIA(coup -> jouerCoupIA(coup), callbackFin);
    }

    public void lancerIA(Consumer<CoupIA> actionQuandCoupPret, Runnable callbackFin) {

        if (!againstIA || ia == null || iaEnCours)
            return;
        
        if (!estTourIA()) return;

        iaEnCours = true;
        final int generationLancee = ++generationIA;
        setFeedbackMessage("Tour de l'IA (" + getJoueurActuel().getTypePion() + ") : elle réfléchit...");

        EtatJeu etat = EtatJeu.depuisJeu(this, currentPlayerIndex);

        ThreadIA thread = new ThreadIA(ia, etat, coup -> {
            javax.swing.SwingUtilities.invokeLater(() -> {
                if (generationLancee != generationIA) {
                    return;
                }

                try {
                    if (actionQuandCoupPret != null) {
                        actionQuandCoupPret.accept(coup);
                    } else {
                        jouerCoupIA(coup);
                    }
                } finally {
                    iaEnCours = false;
                    if (callbackFin != null) {
                        callbackFin.run();
                    }
                }
            });
        });

        thread.start();
    }
}
