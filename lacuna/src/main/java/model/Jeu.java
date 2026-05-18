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



// Classe principale du modèle: garde l'état de la partie
// Elle délègue aussi les règles
public class Jeu implements EtatPlateau {

    // =========================================================
    // ===================== ATTRIBUTS 
    // =====================================
    Cercle cercleDeJeu; // le cercle du jeu
    ArrayList<Pion> pions; // contient tous les pions actuel dans le cercle
    ArrayList<Fleur> fleurs;
    Fleur fleurSelectionnee1; // la fleur que le joueur a touché ( pour le moment on suppose que c'est la
                                      // fleur la plus proche du point de clic )
    Fleur fleurSelectionnee2; // la fleur que le joueur a touché ( pour le moment on suppose que c'est la
                                      // fleur la plus proche du point de clic )
    int nbPions = 12; // nombre de pions
    int nbFleurs = 49; // nombre de fleurs

    private static final double DIST_MIN_FLEURS = ConstantesJeu.DIST_MIN_FLEURS; // distance min entre les fleurs
    private static final double DIAMETRE_FLEUR_MODELE = ConstantesJeu.DIAMETRE_FLEUR_MODELE;
    private static final double DIAMETRE_PION_MODELE = ConstantesJeu.DIAMETRE_PION_MODELE;
    private static final double MARGE_COLLISION = ConstantesJeu.MARGE_COLLISION;
    private static final int NB_JOUEURS = ConstantesJeu.NB_JOUEURS;
    private static final int MAX_PIONS_PAR_JOUEUR = ConstantesJeu.MAX_PIONS_PAR_JOUEUR;
    private static final int NB_COULEURS = ConstantesJeu.NB_COULEURS;


    private static final int NB_POINTS_SEGMENT = ConstantesJeu.NB_POINTS_SEGMENT; // le nombre de points
    private static final double TOLERANCE_DROP_SEGMENT = ConstantesJeu.TOLERANCE_DROP_SEGMENT;
    private static final double MARGE_EXTREMITES_SEGMENT = ConstantesJeu.MARGE_EXTREMITES_SEGMENT_JEU;

    //// Attributs pour les joueurs
    Joueur[] joueurs; // la liste des joueurs
    public int currentPlayerIndex; // 0 pour le joueur 1

    Random random;

    private int WIDTH = 30;
    private int HEIGHT = 30;

    public boolean againstIA = false;

    ///////// pour la marge (afin que les fleurs ne sortent pas du cercle----centre du cerlce)//////////
    ////////// vu qu'on dessine les fleurs avec le format dans view (hauteur *
    ///////// largeur )
    ///////// avec hauteur == largeur, alors la view va nous envoyer la taille qu'on
    ///////// divisera par 2
    ///////// voir dans jeuGraphique, juste après rcalcul de la taille, on fera
    ///////// setMarge(taille/2)

    double marge;

    Historique historique;

    // Avantage du joueur qui commence
    boolean avantageInitialApplique = false;
    boolean enPhaseSelectionAvantage = false;
    String feedbackMessage = "";

    // Fin anticipée de partie (abandon)
    boolean partieAbandonnee = false;
    Joueur gagnantParAbandon = null;


    ///////////////// IA /////////////////
    IA ia;
    boolean iaEnCours = false;
    int generationIA = 0;

    public String getFeedbackMessage() {
        return feedbackMessage;
    }

    public void setFeedbackMessage(String feedbackMessage) {
        this.feedbackMessage = feedbackMessage;
    }

    public boolean isPartieAbandonnee() {
        return partieAbandonnee;
    }

    public boolean isPartieTerminee() {
        return partieAbandonnee || estFinNormalePartie();
    }

    public boolean estFinNormalePartie() {
        return Phase2.tousLesPionsPlaces(pions) && fleurs.isEmpty();
    }

    // =========================================================
    // ===================== CONSTRUCTEUR ======================
    // ===-----------------------------<--------------------------<
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
    // =========-------------------------------------------------

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

    @Override
    public List<Fleur> fleursPlateau() {
        return fleurs;
    }

    @Override
    public List<Pion> pionsPlateau() {
        return pions;
    }

    @Override
    public Cercle cerclePlateau() {
        return cercleDeJeu;
    }

    public void setAvantageInitialApplique(boolean avantageInitialApplique) {
        this.avantageInitialApplique = avantageInitialApplique;
    }

    public void setEnPhaseSelectionAvantage(boolean enPhaseSelectionAvantage) {
        this.enPhaseSelectionAvantage = enPhaseSelectionAvantage;
    }

    // ====//////////////////////////////////////////////====
    // ================= GESTION DES JOUEURS ====================
    // =========================================================

    public void joueurSuivant() {
        currentPlayerIndex = (currentPlayerIndex + 1) % NB_JOUEURS;
    }

    public void joueurPrecedent() {
        currentPlayerIndex = (currentPlayerIndex - 1 + NB_JOUEURS) % NB_JOUEURS;
    }


    // Configuration des jeoueurs qui commencent
    // On donne à chacun son nom
    public void configurerHumainVsHumain(String nomOr, String nomArgent, Pion.TypePion joueurQuiCommence) {
        againstIA = false;
        ia = null;
        iaEnCours = false;
        partieAbandonnee = false;
        gagnantParAbandon = null;
        joueurs = new Joueur[] {
                new JoueurHumain(nomValide(nomOr, "OR"), Pion.TypePion.OR),
                new JoueurHumain(nomValide(nomArgent, "ARGENT"), Pion.TypePion.ARGENT)
        };
        choisirJoueurQuiCommence(joueurQuiCommence);
        setFeedbackMessage("Le joueur qui commence est : " + nomJoueurActuelPourAffichage() + ".");
    }

    // Replace les noms par défaut si le champ est vide
    // En fait ici s'il y 'a pas de nom on laisse les noms parDefaut du jeu'
    String nomValide(String nom, String nomParDefaut) {
        if (nom == null || nom.trim().isEmpty()) {
            return nomParDefaut;
        }
        return nom.trim();
    }

    // Choix de l'index du joueur qui commence
    void choisirJoueurQuiCommence(Pion.TypePion type) {
        if (type == null) {
            currentPlayerIndex = random.nextInt(getNbJoueurs());
            return;
        }
        for (int i = 0; i < joueurs.length; i++) {
            if (joueurs[i].getTypePion() == type) {
                currentPlayerIndex = i;
                return;
            }
        }
        currentPlayerIndex = 0;
    }

    // Abandon de la partie
    public void abandonnerPartie() {
        if (partieAbandonnee) {
            setFeedbackMessage("La partie est déjà terminée.");
            return;
        }
        if (estFinNormalePartie()) {
            setFeedbackMessage("La partie est déjà terminée : l'abandon n'est plus possible.");
            return;
        }
        annulerIAEnCours();
        partieAbandonnee = true;
        enPhaseSelectionAvantage = false;
        resetFleursSelectionnee1();
        resetFleursSelectionnee2();
        gagnantParAbandon = joueurs[(currentPlayerIndex + 1) % NB_JOUEURS];
        setFeedbackMessage(nomJoueurActuelPourAffichage() + " abandonne. "
                + nomJoueurPourAffichage(gagnantParAbandon) + " gagne par abandon.");
    }

    // Avantage de l'ia
    // A REVOIR REFACTORING (UTILISATION DE LA FONCTION D'AVANTAGE POUR HUMAIN)
    public void jouerAvantageIA() {
        if (!enPhaseSelectionAvantage || !estTourIA() || fleurs.isEmpty()) {
            return;
        }
        Fleur fleurChoisie = fleurs.get(random.nextInt(fleurs.size()));
        appliquerAvantageAvecFleur(fleurChoisie);
        setFeedbackMessage("IA (" + getJoueurActuel().getTypePion() + ") choisit une fleur " + fleurChoisie.getType() + " pour l'avantage initial.");
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

    // =================/////////////////========================
    // ================= INITIALISATION =========================
    // =================//////////////////////////===============

    void initPlayers() {
        partieAbandonnee = false;
        gagnantParAbandon = null;
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
        int tentatives = 0; // compteur de tentatives pour éviter une boucle infinie

        do {
            double angle = Math.random() * 2 * Math.PI;
            double margeFleur = marge; // pour ne pas déborder
            double r = Math.sqrt(Math.random()) * (cercleDeJeu.getRayon() - margeFleur);

            double x = cercleDeJeu.getCentre().getX() + r * Math.cos(angle);
            double y = cercleDeJeu.getCentre().getY() + r * Math.sin(angle);
            pos = new Coordonnees((int) x, (int) y);

            tentatives++;
        } while (!positionValide(pos, fleurs) && tentatives++ < 1000);

        return pos;
    }

    // Si la position pour mettre les fleurs est valide
    private boolean positionValide(Coordonnees pos, ArrayList<Fleur> fleurs) {
        return !fleurTropProcheDuneAutre(pos, fleurs);
    }

    // Vérifie si deux objets dessinés se superposent
    private boolean objetsSeSuperposent(Coordonnees a, Coordonnees b, double distanceMin) {
        return ReglesPlateau.objetsSeSuperposent(a, b, distanceMin);
    }

    // Vérifieddd si une fleur générée est trop proche d'une autre fleur
    private boolean fleurTropProcheDuneAutre(Coordonnees pos, ArrayList<Fleur> fleurs) {
        return ReglesPlateau.fleurTropProcheDuneAutre(pos, fleurs, DIAMETRE_FLEUR_MODELE + MARGE_COLLISION);
    }

    // Vérifie si un pion placé est trop proche d'un autre pion
    private boolean pionTropProcheDunAutre(Coordonnees pos) {
        return ReglesPlateau.pionTropProcheDunAutre(pos, pions, DIAMETRE_PION_MODELE + MARGE_COLLISION);
    }

    // =========================================================
    // ================= LOGIQUE DU JEU////////////////////////// 

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

        if (partieAbandonnee) {
            setFeedbackMessage("La partie est terminée : un joueur a abandonné.");
            return false;
        }

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
        return ActionsJeu.jouerCoupAvecFleursSelectionnees(this);
    }

    public boolean jouerCoupAvecFleursSelectionnees(Coordonnees clic) {
        return ActionsJeu.jouerCoupAvecFleursSelectionnees(this, clic);
    }

    public Coordonnees positionSegmentLaPlusProcheDuClic(Coordonnees A, Coordonnees B, Coordonnees clic) {
        return ActionsJeu.positionSegmentLaPlusProcheDuClic(this, A, B, clic);
    }

    //fonction pour la seconde phase du jeu (controller appelle ça aussi )
    public boolean secondePhase(){
        return Phase2.secondePhase(this);
    }

    /* ============= fonctions auxiliares ================= */

    // on verifie si la position où on place le pion est libre
    // A REVOIR
    private boolean positionLibrePourPion(Coordonnees pos) {
        return !pionTropProcheDunAutre(pos);
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
        Phase2.attribuerFleursRestantes(this);
    }

    public ArrayList<Phase2.AttributionFleur> calculerAttributionsFleursRestantes() {
        return Phase2.calculerAttributionsFleursRestantes(fleurs, pions, joueurs);
    }

    public void appliquerAttributionFleur(Phase2.AttributionFleur attribution) {
        Phase2.appliquerAttributionFleur(fleurs, attribution);
    }

    public boolean estSecondePhaseDisponible() {
        return Phase2.estSecondePhaseDisponible(fleurs, pions);
    }

    public Pion pionLePlusProche(Fleur fleur) {
        return ReglesPlateau.pionLePlusProche(this, fleur);
    }

    public Joueur joueurPourPion(Pion pion) {
        return Phase2.joueurPourPion(joueurs, pion);
    }

    public Joueur getGagnant() {
        return ScoreJeu.getGagnant(joueurs, gagnantParAbandon);
    }

    // =========================================================
    // =============== SELECTION DES FLEURS / SEGMENTS ==========
    // =========================================================

    public boolean toucherFleur(Coordonnees pos) {
        return ActionsJeu.toucherFleur(this, pos);
    }

    private boolean segmentLibre(Coordonnees A, Coordonnees B) {
        return ActionsJeu.segmentLibre(this, A, B);
    }

    public boolean fleursConnectées(Fleur f1, Fleur f2) {
        return ActionsJeu.fleursConnectees(this, f1, f2);
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
    // ==========(Guide) ==========================
    // =========================================================

    public List<Coordonnees> pointsDuSegment(Coordonnees A, Coordonnees B) {
        return ActionsJeu.pointsDuSegment(this, A, B);
    }

    private boolean horsZoneFleursExtremites(Coordonnees A, Coordonnees B, Coordonnees pos) {
        return ActionsJeu.horsZoneFleursExtremites(A, B, pos);
    }

    // Retourne toutes les positions possibles pour jouer
    public List<Coordonnees> coupsPossibles() {
        return ActionsJeu.coupsPossibles(this);
    }

    public List<Coordonnees> coupsPossibles(Fleur.TypeFleur type) {
        return ActionsJeu.coupsPossibles(this, type);
    }

    public List<Fleur[]> pairesConnectees(Fleur.TypeFleur type) {
        return ActionsJeu.pairesConnectees(this, type);
    }

    public boolean existeCoupConnecte() {
        return ActionsJeu.existeCoupConnecte(this);
    }

    public boolean placerPionLibreSiAucunCoup(Coordonnees pos) {
        return ActionsJeu.placerPionLibreSiAucunCoup(this, pos);
    }

    private Coordonnees positionLibreDeSecours(Pion.TypePion type) {
        return ActionsJeu.positionLibreDeSecours(this, type);
    }

    private boolean placerPionIADeSecours() {
        return ActionsJeu.placerPionIADeSecours(this);
    }

    // =========================================================
    // ==================== DRAG AND DROP =======================
    // =========================================================

    public boolean placerPionDepuisDrag(Coordonnees pos, Object payload) {
        return ActionsJeu.placerPionDepuisDrag(this, pos, payload);
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
        return ScoreJeu.getScore(joueur);
    }

    // ======================-------------===================================
    // ====================== HISTORIQUE ========================
    // =======================-------------==================================

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
        partieAbandonnee = false;
        gagnantParAbandon = null;
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
    ///// FONCTION À REVOIR CAR L'HISTORIQUE EST CHANGÉ
    public void sauvegarderPartie(String cheminFichier) {
        historique.sauvegarderPartie(this, cheminFichier);
    }

    // Charge l'état d'une partie depuis un fichier texte et reconstruit le jeu
    public void chargerPartie(String cheminFichier) {
        annulerIAEnCours();
        historique.chargerPartie(this, cheminFichier);
    }




    // ==============-------------------------===========================================
    // ========================= (IA) ==========================
    // =========================================================///////////////////

    // Activation de l'ia
    public void activerIA(NiveauxIA niveau) {
        activerIA(niveau, "Humain", Pion.TypePion.OR);
    }

    //Active le mode IA en laissant --- choisir celui qui commence
    public void activerIA(NiveauxIA niveau, String nomHumain, Pion.TypePion joueurQuiCommence) {
        GestionIA.activerIA(this, niveau, nomHumain, joueurQuiCommence);
    }

    // Savoir si l'ia est en cours
    public boolean isIAEnCours() {
        return iaEnCours;
    }

    // Savoir si c'est le tour de l'ia
    public boolean estTourIA() {
        return GestionIA.estTourIA(this);
    }

    private Fleur retrouverFleurDuPlateau(Fleur recherchee) {
        return GestionIA.retrouverFleurDuPlateau(this, recherchee);
    }

    // L'AI joue un coup
    public boolean placerPionCoupIA(CoupIA coup) {
        return GestionIA.placerPionCoupIA(this, coup);
    }

    public boolean collecterFleursCoupIA(CoupIA coup) {
        return GestionIA.collecterFleursCoupIA(this, coup);
    }

    public void jouerCoupIA(CoupIA coup) {
        GestionIA.jouerCoupIA(this, coup);
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
        return joueur.getNom() + " (" + joueur.getTypePion() + ")";
    }

    public String nomJoueurActuelPourAffichage() {
        return nomJoueurPourAffichage(getJoueurActuel());
    }

    // Lancement du thread pour l'ia
    public void lancerIA(Runnable callbackFin) {
        GestionIA.lancerIA(this, callbackFin);
    }

    public void lancerIA(Consumer<CoupIA> actionQuandCoupPret, Runnable callbackFin) {
        GestionIA.lancerIA(this, actionQuandCoupPret, callbackFin);
    }

    //=============================CONFIGURATION POUR TESTER IA VS IA =================//
    ///////////////////////////////////////////////////////////////////////////////////////////////////////
   public void configurerIAContreIA(NiveauxIA niveauOr, NiveauxIA niveauArgent, Pion.TypePion joueurQuiCommence) {
        GestionIA.configurerIAContreIA(this, niveauOr, niveauArgent, joueurQuiCommence);
    }
}
