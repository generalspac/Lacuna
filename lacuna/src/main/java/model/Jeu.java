package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import global.Configuration;

public class Jeu {

    // =========================================================
    // ===================== ATTRIBUTS ==========================
    // =========================================================
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

    public Pion getPionLibre(Types.TypePion type) {
        for (Pion p : pions) {
            if (p.getType() == type && p.getPosition() == null) {
                return p;
            }
        }
        return null;
    }

    // Le nombre de pions par type placés dans le cercle
    private int nombreDePionsPlacees(Types.TypePion type) {
        int compteur = 0;
        for (Pion p : pions) {
            if (p.getType() == type && p.getPosition() != null) {
                compteur++;
            }
        }
        return compteur;
    }

    // si on peut placer un pion d'un certain type(or ou argent) dans le cercle
    private boolean peutPlacerPionDeType(Types.TypePion type) {
        return nombreDePionsPlacees(type) < MAX_PIONS_PAR_JOUEUR;
    }

    // =========================================================
    // ================= INITIALISATION =========================
    // =========================================================

    void initPlayers() {
        if (isAgainstIA()) {
            joueurs = new Joueur[] {
                    new JoueurHumain("HUMAN", Types.TypePion.OR),
                    new JoueurIa("IA", Types.TypePion.ARGENT)
            };
            currentPlayerIndex = 0;
        } else {
            joueurs = new Joueur[] {
                    new JoueurHumain("Joueur 1", Types.TypePion.OR),
                    new JoueurHumain("Joueur 2", Types.TypePion.ARGENT)
            };
            currentPlayerIndex = random.nextInt(getNbJoueurs());
            setFeedbackMessage(feedbackMessage + " Le joueur qui commence est : " + getJoueurActuel().getNom() + " (" + getJoueurActuel().getTypePion() + ").") ;
        }
    }

    ////////////////// initialisatin des fleurs et
    ////////////////// pions////////////////////////////////////
    public void initFleurs() {
        fleurs.clear();

        for (Types.TypeFleur type : Types.TypeFleur.values()) {
            for (int i = 0; i < 7; i++) { //CHAQUE COULEUR À 6 PIONS    
                Coordonnees pos = genererPositionAleatoire(fleurs);
                fleurs.add(new Fleur(type, pos));
            }
        }
    }

    public void initPions() {
        pions.clear();

        for (Types.TypePion type : Types.TypePion.values()) {
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

        if (!fleursConnectées(fleurSelectionnee1, fleurSelectionnee2)) {
            return false;
        }

        Coordonnees pos = Coordonnees.milieu( fleurSelectionnee1.getPosition(), fleurSelectionnee2.getPosition());

        // Sélection du pion du joeur actuel
        Joueur joueurActuel = getJoueurActuel();
        Types.TypePion pionType = joueurActuel.getTypePion();;
        Pion pionLibre = getPionLibre(pionType);

        if (pionLibre == null) {
            System.out.println("Aucun pion libre disponible pour ce joueur.");
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

        ArrayList<Fleur> copie = new ArrayList<>(fleurs);

        for (Fleur f : copie) {
            Joueur j = joueurLePlusProche(f);
            if (j != null) {
                j.ajouterFleur(f);
                fleurs.remove(f);
            }
        }
    }

    // le joueur le plus proche
    private Joueur joueurLePlusProche(Fleur fleur) {

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

        if (meilleur == null)
            return null; // pas de joueur plus proche alors

        for (Joueur j : joueurs) {
            if (j.getTypePion() == meilleur.getType()) {
                return j;
            }
        }

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

    // =========================================================
    // ========================= (Aide IA et Guide) ==========================
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

    // =========================================================
    // ==================== DRAG AND DROP =======================
    // =========================================================

    public void placerPionDepuisDrag(Coordonnees pos, Object payload) {

        if (payload == null)
            return;

        if (!(payload instanceof Types.TypePion)) {
            System.out.println("Payload invalide");
            return;
        }

        Types.TypePion type = (Types.TypePion) payload;
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

        jouerCoup(joueurActuel, pion, pos, flowers[0], flowers[1]);

        System.out.println("Pion placé via drag !");
    }

    private Fleur[] trouverFleursPourPion(Coordonnees pos) {
        for (Types.TypeFleur typeFleur : Types.TypeFleur.values()) {
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

                    if (Coordonnees.estExactementAuMilieu(f1.getPosition(), f2.getPosition(), pos)) {
                        if (fleursConnectées(f1, f2)) {
                            return new Fleur[] { f1, f2 };
                        }
                    }
                }
            }
        }
        return null;
    }


    /*
     * public Fleur getFleurProche(Coordonnees pos, double distanceMax) {
     * Fleur best = null;
     * double min = Double.MAX_VALUE;
     *
     * for (Fleur f : fleurs) {
     * double d = distance(pos, f.getPosition());
     * if (d < min && d < distanceMax) {
     * min = d;
     * best = f;
     * }
     * }
     * return best;
     * }
     *
     * public Fleur getFleurAlignee(Fleur ref) {
     *
     * for (Fleur f : fleurs) {
     *
     * if (f == ref)
     * continue;
     * if (f.getType() != ref.getType())
     * continue;
     *
     * int dx = Math.abs(f.getPosition().getX() - ref.getPosition().getX());
     * int dy = Math.abs(f.getPosition().getY() - ref.getPosition().getY());
     *
     * // horizontal OU vertical
     * if (dx < 15 || dy < 15) {
     * return f;
     * }
     * }
     *
     * return null;
     * }
     */

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

    public Map<Types.TypeFleur, Integer> getScore(Joueur joueur) {

        Map<Types.TypeFleur, Integer> map = new java.util.HashMap<>();

        for (Types.TypeFleur t : Types.TypeFleur.values()) {
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
        historique.undo(this);
    }

    public void redo() {
        historique.redo(this);
    }

    public void reset() {
        // Réinitialiser le jeu à son état initial
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
        historique.chargerPartie(this, cheminFichier);
    }
}
