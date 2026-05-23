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
import network.CoupReseau;

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
    int nbFleurs = ConstantesJeu.NB_FLEURS; // nombre de fleurs

    private static final double DIST_MIN_FLEURS = ConstantesJeu.DIST_MIN_FLEURS; // distance min entre les fleurs
    private static final double DIAMETRE_FLEUR_MODELE = ConstantesJeu.DIAMETRE_FLEUR_MODELE;
    private static final double DIAMETRE_PION_MODELE = ConstantesJeu.DIAMETRE_PION_MODELE;
    private static final double MARGE_COLLISION = ConstantesJeu.MARGE_COLLISION;
    private static final int NB_JOUEURS = ConstantesJeu.NB_JOUEURS;
    private static final int MAX_PIONS_PAR_JOUEUR = ConstantesJeu.MAX_PIONS_PAR_JOUEUR;
    private static final int NB_COULEURS = ConstantesJeu.NB_COULEURS;
    private static final int NB_FLEURS_PAR_COULEUR = ConstantesJeu.NB_FLEURS_PAR_COULEUR;

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
    private boolean afficherSuggestions = false;

    ///////// pour la marge (afin que les fleurs ne sortent pas du cercle----centre
    ///////// du cerlce)//////////
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
    IA iaOr;
    IA iaArgent;
    NiveauxIA niveauIA;
    NiveauxIA niveauIAOr;
    NiveauxIA niveauIAArgent;
    String nomHumainIA;
    Pion.TypePion typeHumainIA;
    Pion.TypePion joueurDepartIA;
    Pion.TypePion joueurDepartIAContreIA;
    boolean iaContreIA = false;
    boolean iaEnCours = false;
    int generationIA = 0;

    ///////////////// RÉSEAU /////////////////
    private boolean enModeReseau = false;
    private int indexJoueurLocal = 0; // 0 = OR (hôte), 1 = ARGENT (invité)
    private Consumer<CoupReseau> ecouteurCoupEnvoye = null;
    private boolean applicationCoupDistant = false; // évite la réémission en boucle

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
    }

    public Jeu(Jeu autre) {

        this.cercleDeJeu = autre.cercleDeJeu;

        // ===== COPIE DES PIONS =====
        this.pions = new ArrayList<>();
        for (Pion p : autre.pions) {
            this.pions.add(
                    new Pion(
                            p.getType(),
                            p.getPosition() == null ? null
                                    : new Coordonnees(p.getPosition().getX(), p.getPosition().getY())));
        }

        // ===== COPIE DES FLEURS =====
        this.fleurs = new ArrayList<>();
        for (Fleur f : autre.fleurs) {
            Coordonnees pos = f.getPosition();

            this.fleurs.add(
                    new Fleur(
                            f.getType(),
                            new Coordonnees(pos.x, pos.y)));
        }

        // ===== RECALAGE DES FLEURS SÉLECTIONNÉES =====
        this.fleurSelectionnee1 = null;
        this.fleurSelectionnee2 = null;

        if (autre.fleurSelectionnee1 != null) {
            for (Fleur f : this.fleurs) {
                if (f.getPosition().x == autre.fleurSelectionnee1.getPosition().x &&
                        f.getPosition().y == autre.fleurSelectionnee1.getPosition().y) {
                    this.fleurSelectionnee1 = f;
                    break;
                }
            }
        }

        if (autre.fleurSelectionnee2 != null) {
            for (Fleur f : this.fleurs) {
                if (f.getPosition().x == autre.fleurSelectionnee2.getPosition().x &&
                        f.getPosition().y == autre.fleurSelectionnee2.getPosition().y) {
                    this.fleurSelectionnee2 = f;
                    break;
                }
            }
        }

        // ===== AUTRES DONNÉES =====
        this.nbPions = autre.nbPions;
        this.nbFleurs = autre.nbFleurs;

        this.joueurs = new Joueur[autre.joueurs.length];
        for (int i = 0; i < autre.joueurs.length; i++) {
            this.joueurs[i] = new JoueurHumain(
                    autre.joueurs[i].getNom(),
                    autre.joueurs[i].getTypePion());
            this.joueurs[i].getFleursGagnees()
                    .addAll(autre.joueurs[i].getFleursGagnees());
        }

        this.currentPlayerIndex = autre.currentPlayerIndex;

        this.random = new Random();

        this.avantageInitialApplique = autre.avantageInitialApplique;
        this.enPhaseSelectionAvantage = autre.enPhaseSelectionAvantage;
        this.feedbackMessage = autre.feedbackMessage;

        this.partieAbandonnee = autre.partieAbandonnee;
        this.gagnantParAbandon = autre.gagnantParAbandon;

        this.ia = autre.ia;
        this.iaOr = autre.iaOr;
        this.iaArgent = autre.iaArgent;

        this.niveauIA = autre.niveauIA;
        this.niveauIAOr = autre.niveauIAOr;
        this.niveauIAArgent = autre.niveauIAArgent;

        this.nomHumainIA = autre.nomHumainIA;
        this.typeHumainIA = autre.typeHumainIA;

        this.joueurDepartIA = autre.joueurDepartIA;
        this.joueurDepartIAContreIA = autre.joueurDepartIAContreIA;

        this.iaContreIA = autre.iaContreIA;
        this.iaEnCours = autre.iaEnCours;

        this.enModeReseau = autre.enModeReseau;
        this.indexJoueurLocal = autre.indexJoueurLocal;

        this.ecouteurCoupEnvoye = autre.ecouteurCoupEnvoye;
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

    // =========================================================
    // ================= GETTERS / SETTERS RÉSEAU ==============
    // =========================================================

    public boolean isEnModeReseau() {
        return enModeReseau;
    }

    public void setEnModeReseau(boolean enModeReseau) {
        this.enModeReseau = enModeReseau;
    }

    public int getIndexJoueurLocal() {
        return indexJoueurLocal;
    }

    public void setAfficherSuggestions(boolean afficher) {
        this.afficherSuggestions = afficher;
    }

    public boolean isAfficherSuggestions() {
        return afficherSuggestions;
    }

    public void setIndexJoueurLocal(int indexJoueurLocal) {
        this.indexJoueurLocal = indexJoueurLocal;
    }

    public void setEcouteurCoupEnvoye(Consumer<CoupReseau> ecouteur) {
        this.ecouteurCoupEnvoye = ecouteur;
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
        iaContreIA = false;
        ia = null;
        iaOr = null;
        iaArgent = null;
        niveauIA = null;
        niveauIAOr = null;
        niveauIAArgent = null;
        nomHumainIA = null;
        typeHumainIA = null;
        joueurDepartIA = null;
        joueurDepartIAContreIA = null;
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

        // Signaler l'abandon à l'adversaire en mode réseau
        if (enModeReseau && !applicationCoupDistant && ecouteurCoupEnvoye != null) {
            CoupReseau coup = new CoupReseau();
            coup.typeMessage = "ABANDON";
            ecouteurCoupEnvoye.accept(coup);
        }
    }

    /**
     * Choisit automatiquement la fleur d'avantage initial pour une IA.
     */
    public void jouerAvantageIA() {
        if (!enPhaseSelectionAvantage || !estTourIA() || fleurs.isEmpty()) {
            return;
        }
        Fleur fleurChoisie = fleurs.get(random.nextInt(fleurs.size()));
        appliquerAvantageAvecFleur(fleurChoisie);
        setFeedbackMessage(nomJoueurActuelPourAffichage() + " choisit une fleur "
                + fleurChoisie.getType() + " pour l'avantage initial.");
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
            setFeedbackMessage(feedbackMessage + " Le joueur qui commence est : " + getJoueurActuel().getNom() + " ("
                    + getJoueurActuel().getTypePion() + ").");
        }
    }

    ////////////////// initialisatin des fleurs et
    ////////////////// pions////////////////////////////////////
    public void initFleurs() {
        fleurs.clear();

        for (Fleur.TypeFleur type : Fleur.TypeFleur.values()) {
            for (int i = 0; i < NB_FLEURS_PAR_COULEUR; i++) {
                Coordonnees pos = genererPositionAleatoire(fleurs);
                fleurs.add(new Fleur(type, pos));
            }
        }
    }

    // Réinitialise le plateau de fleurs avec une graine partagée (mode réseau)
    public void reinitialiserFleurs(int graine) {
        random = new Random(graine);
        initFleurs();
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
        setFeedbackMessage("Phase d'avantage initial : "
                + nomJoueurActuelPourAffichage() + " doit choisir une fleur.");
    }

    // Génération de position aleatoire pour les fleurs
    private Coordonnees genererPositionAleatoire(ArrayList<Fleur> fleurs) {
        Coordonnees pos;
        int tentatives = 0; // compteur de tentatives pour éviter une boucle infinie

        do {
            double angle = random.nextDouble() * 2 * Math.PI;
            double margeFleur = marge; // pour ne pas déborder
            double r = Math.sqrt(random.nextDouble()) * (cercleDeJeu.getRayon() - margeFleur);

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

            // Émettre l'avantage réseau si c'est le joueur local qui vient de jouer
            if (enModeReseau && !applicationCoupDistant && currentPlayerIndex == indexJoueurLocal
                    && ecouteurCoupEnvoye != null) {
                CoupReseau coup = new CoupReseau();
                coup.typeMessage = "AVANTAGE";
                coup.pionNull = true;
                coup.typeFleur1 = fleurChoisie.getType().name();
                coup.fleur1X = fleurChoisie.getPosition().getX();
                coup.fleur1Y = fleurChoisie.getPosition().getY();
                coup.fleur2Null = true;
                ecouteurCoupEnvoye.accept(coup);
            }

        }
    }

    //////////////// FONCTIONS POUR LES REGLES DU JEU ///////////////
    ///////////// les 3 premieres fonctions sont les principales pour le
    //////////////// jeu///////////

    /**
     * Donne une fleur aléatoire au joueur qui commence.
     */
    public void avantagePremierJoueur() {
        Joueur joueur = getJoueurActuel();

        if (fleurs.isEmpty())
            return;

        Fleur f = fleurs.get(random.nextInt(fleurs.size()));
        joueur.ajouterFleur(f);
        fleurs.remove(f);
    }

    /**
     * Place un pion sur le plateau après les vérifications communes.
     *
     * @param pion pion libre à placer.
     * @param pos  position modèle souhaitée.
     * @return {@code true} si le pion a bien été placé.
     */
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

    /**
     * Applique un coup complet de phase 1.
     *
     * Le coup place le pion, collecte les deux fleurs, l'enregistre dans
     * l'historique et passe au joueur suivant.
     *
     * @param joueur joueur qui joue.
     * @param pion   pion libre du joueur.
     * @param pos    position validée pour le pion.
     * @param f1     première fleur collectée.
     * @param f2     seconde fleur collectée.
     */
    public void jouerCoup(Joueur joueur, Pion pion, Coordonnees pos, Fleur f1, Fleur f2) {

        if (!placePion(pion, pos))
            return;

        if (!mangerFleurs(joueur, f1, f2)) {
            pion.setPosition(null);
            return;
        }

        historique.enregistrerAction(pion, f1, f2, joueur);

        if (enModeReseau && !applicationCoupDistant && currentPlayerIndex == indexJoueurLocal
                && ecouteurCoupEnvoye != null) {
            CoupReseau coup = new CoupReseau();
            coup.typeMessage = "COUP";
            coup.typePion = pion.getType().name();
            coup.pionX = pos.getX();
            coup.pionY = pos.getY();
            coup.typeFleur1 = f1.getType().name();
            coup.fleur1X = f1.getPosition().getX();
            coup.fleur1Y = f1.getPosition().getY();
            coup.typeFleur2 = f2.getType().name();
            coup.fleur2X = f2.getPosition().getX();
            coup.fleur2Y = f2.getPosition().getY();
            ecouteurCoupEnvoye.accept(coup);
        }

        joueurSuivant();

    }

    /**
     * Joue le coup correspondant aux deux fleurs actuellement sélectionnées.
     *
     * @return {@code true} si le coup a été joué.
     */
    public boolean jouerCoupAvecFleursSelectionnees() {
        return ActionsJeu.jouerCoupAvecFleursSelectionnees(this);
    }

    /**
     * Joue avec les fleurs sélectionnées en utilisant le clic comme préférence de
     * placement.
     *
     * @param clic position modèle du clic.
     * @return {@code true} si le coup a été joué.
     */
    public boolean jouerCoupAvecFleursSelectionnees(Coordonnees clic) {
        return ActionsJeu.jouerCoupAvecFleursSelectionnees(this, clic);
    }

    /**
     * Trouve l'emplacement libre du segment le plus proche du clic.
     *
     * @param A    première extrémité du segment.
     * @param B    seconde extrémité du segment.
     * @param clic position modèle visée par le joueur.
     * @return position jouable, ou {@code null} si aucune n'existe.
     */
    public Coordonnees positionSegmentLaPlusProcheDuClic(Coordonnees A, Coordonnees B, Coordonnees clic) {
        return ActionsJeu.positionSegmentLaPlusProcheDuClic(this, A, B, clic);
    }

    /**
     * Lance la seconde phase si ses conditions sont réunies.
     *
     * @return {@code true} si une attribution a été effectuée.
     */
    public boolean secondePhase() {
        return Phase2.secondePhase(this);
    }

    /* ============= fonctions auxiliares ================= */

    /**
     * Vérifie qu'un pion peut occuper cette position sans toucher un autre pion.
     *
     * @param pos position modèle testée.
     * @return {@code true} si la position est libre.
     */
    private boolean positionLibrePourPion(Coordonnees pos) {
        return !pionTropProcheDunAutre(pos);
    }

    /**
     * Donne deux fleurs au joueur et les retire du plateau.
     *
     * @param joueur joueur qui reçoit les fleurs.
     * @param f1     première fleur.
     * @param f2     seconde fleur.
     * @return {@code true} si les fleurs étaient valides et présentes.
     */
    public boolean mangerFleurs(Joueur joueur, Fleur f1, Fleur f2) {
        if (f1.getType() != f2.getType())
            return false;
        if (!fleurs.contains(f1) || !fleurs.contains(f2))
            return false;

        joueur.ajouterFleur(f1);
        joueur.ajouterFleur(f2);
        fleurs.remove(f1);
        fleurs.remove(f2);

        return true;
    }

    /**
     * Attribue toutes les fleurs restantes au pion le plus proche.
     */
    public void attribuerFleursRestantes() {
        Phase2.attribuerFleursRestantes(this);
    }

    /**
     * Prépare les attributions de phase 2 sans modifier le plateau.
     *
     * @return liste des fleurs et joueurs concernés par la phase 2.
     */
    public ArrayList<Phase2.AttributionFleur> calculerAttributionsFleursRestantes() {
        return Phase2.calculerAttributionsFleursRestantes(fleurs, pions, joueurs);
    }

    /**
     * Applique une seule attribution de phase 2.
     *
     * @param attribution attribution calculée à appliquer.
     */
    public void appliquerAttributionFleur(Phase2.AttributionFleur attribution) {
        Phase2.appliquerAttributionFleur(fleurs, attribution);
    }

    /**
     * Indique si la partie peut passer en phase 2.
     *
     * @return {@code true} quand tous les pions sont posés et qu'il reste des
     *         fleurs.
     */
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

    /**
     * Traite la sélection d'une fleur par un clic humain.
     *
     * @param pos position modèle du clic.
     * @return {@code true} si la sélection a changé.
     */
    public boolean toucherFleur(Coordonnees pos) {
        return ActionsJeu.toucherFleur(this, pos);
    }

    private boolean segmentLibre(Coordonnees A, Coordonnees B) {
        return ActionsJeu.segmentLibre(this, A, B);
    }

    /**
     * Vérifie que deux fleurs peuvent être jouées ensemble.
     *
     * @param f1 première fleur.
     * @param f2 seconde fleur.
     * @return {@code true} si elles ont la même couleur et une ligne libre.
     */
    public boolean fleursConnectees(Fleur f1, Fleur f2) {
        return ActionsJeu.fleursConnectees(this, f1, f2);
    }

    public Coordonnees milieu(Coordonnees A, Coordonnees B) {
        return Coordonnees.milieu(A, B);
    }

    public Fleur getFleurSelectionnee1() {
        return fleurSelectionnee1;
    }

    public void setFleurSelectionnee1(Fleur fleur) {
        this.fleurSelectionnee1 = fleur;
    }

    public void setFleurSelectionnee2(Fleur fleur) {
        this.fleurSelectionnee2 = fleur;
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

    public void recommencerPartie() {
        if (isEnModeReseau())
            return;
        initPlayers();
        initFleurs();
        initPions();
        historique.clear();
        avantageInitialApplique = false;
        enPhaseSelectionAvantage = false;
        partieAbandonnee = false;
        gagnantParAbandon = null;
    }

    /**
     * Sélection visuelle de deux fleurs pour une animation.
     *
     * @param f1 première fleur à afficher comme sélectionnée.
     * @param f2 seconde fleur à afficher comme sélectionnée.
     */
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

    /**
     * Liste toutes les positions encore jouables.
     *
     * @return positions où un pion humain peut être placé.
     */
    public List<Coordonnees> coupsPossibles() {
        return ActionsJeu.coupsPossibles(this);
    }

    /**
     * Liste les positions jouables pour une couleur de fleur.
     *
     * @param type couleur filtrée, ou {@code null} pour toutes les couleurs.
     * @return positions jouables.
     */
    public List<Coordonnees> coupsPossibles(Fleur.TypeFleur type) {
        return ActionsJeu.coupsPossibles(this, type);
    }

    // fonction pour recuperer une fleur en donnant juste ses coordonnees
    private Fleur recupererFleur(Coordonnees position) {
        if (position == null) {
            return null;
        }

        for (Fleur fleur : fleurs) {
            Coordonnees positionFleur = fleur.getPosition();
            if (positionFleur != null && positionFleur.equals(position)) {
                return fleur;
            }
        }

        return null;
    }

    public List<Fleur> recupererAutresFleursMemeType(Fleur base, Fleur aIgnorer) {
        List<Fleur> result = new ArrayList<>();
        if (base == null)
            return result;

        for (Fleur f : fleurs) {
            if (f.getType() == base.getType() && !memeFleur(f, base) && !memeFleur(f, aIgnorer)
                    && fleursConnectees(base, f)) {
                result.add(f);
            }
        }
        return result;
    }

    // Permet de choisir la 2nd meilleure fleur, pour mieux avantager
    // le joueur
    public Fleur choisirDeuxiemeMeilleurFleur(Fleur fleurSelectionnee1) {
        if (fleurSelectionnee1 == null || fleurSelectionnee1.getPosition() == null) {
            return null;
        }

        Fleur premiereFleur = recupererFleur(fleurSelectionnee1.getPosition());
        if (premiereFleur == null) {
            return null;
        }

        Fleur meilleureFleur = null;
        int meilleurScore = Integer.MIN_VALUE;

        for (Fleur[] paire : pairesConnectees(premiereFleur.getType())) {
            Fleur candidate = autreFleurDeLaPaire(paire, premiereFleur);
            if (candidate == null) {
                continue;
            }

            int score = scoreDeuxiemeFleur(premiereFleur, candidate);
            if (score > meilleurScore) {
                meilleurScore = score;
                meilleureFleur = candidate;
            }
        }

        return meilleureFleur;
    }

    private Fleur autreFleurDeLaPaire(Fleur[] paire, Fleur fleur) {
        if (paire == null || paire.length < 2) {
            return null;
        }
        if (memeFleur(paire[0], fleur)) {
            return paire[1];
        }
        if (memeFleur(paire[1], fleur)) {
            return paire[0];
        }
        return null;
    }

    private int scoreDeuxiemeFleur(Fleur premiereFleur, Fleur deuxiemeFleur) {
        int joueur = currentPlayerIndex;
        int adversaire = (currentPlayerIndex + 1) % NB_JOUEURS;
        Fleur.TypeFleur type = premiereFleur.getType();

        int score = 0;
        score += progressionMajorite(joueurs[joueur].nbFleursDeCouleur(type)) * 90;
        score += progressionMajorite(joueurs[adversaire].nbFleursDeCouleur(type)) * 70;
        score += (int) Math.max(0,
                180 - Coordonnees.distance(premiereFleur.getPosition(), deuxiemeFleur.getPosition()));
        score += scorePhase2Fleur(joueur, premiereFleur);
        score += scorePhase2Fleur(joueur, deuxiemeFleur);
        score += meilleurePositionSurPaire(premiereFleur, deuxiemeFleur);

        return score;
    }

    private int progressionMajorite(int fleursDejaGagnees) {
        int apresCoup = fleursDejaGagnees + 2;
        if (apresCoup >= 4) {
            return 8;
        }
        if (apresCoup == 3) {
            return 4;
        }
        if (apresCoup == 2) {
            return 1;
        }
        return 0;
    }

    private int scorePhase2Fleur(int indexJoueur, Fleur fleur) {
        Pion pionProche = ReglesPlateau.pionLePlusProche(this, fleur);
        if (pionProche == null) {
            return 0;
        }
        if (pionProche.getType() == joueurs[indexJoueur].getTypePion()) {
            return -25;
        }
        return 35;
    }

    private int meilleurePositionSurPaire(Fleur premiereFleur, Fleur deuxiemeFleur) {
        List<Coordonnees> positions = pointsDuSegment(premiereFleur.getPosition(), deuxiemeFleur.getPosition());
        if (positions.isEmpty()) {
            return 0;
        }

        int meilleurScore = Integer.MIN_VALUE;
        for (Coordonnees position : positions) {
            meilleurScore = Math.max(meilleurScore, scorePositionPion(position, premiereFleur, deuxiemeFleur));
        }
        return meilleurScore;
    }

    private int scorePositionPion(Coordonnees position, Fleur premiereFleur, Fleur deuxiemeFleur) {
        int score = 0;
        Coordonnees milieu = Coordonnees.milieu(premiereFleur.getPosition(), deuxiemeFleur.getPosition());
        score -= (int) Coordonnees.distance(position, milieu);

        Pion.TypePion typeJoueur = getJoueurActuel().getTypePion();
        for (Fleur fleur : fleurs) {
            if (memeFleur(fleur, premiereFleur) || memeFleur(fleur, deuxiemeFleur)) {
                continue;
            }

            double distanceNouveauPion = Coordonnees.distance(position, fleur.getPosition());
            Pion pionProche = ReglesPlateau.pionLePlusProche(this, fleur);
            if (pionProche == null
                    || distanceNouveauPion < Coordonnees.distance(pionProche.getPosition(), fleur.getPosition())) {
                score += 12;
            } else if (pionProche.getType() == typeJoueur) {
                score += 3;
            }
        }

        return score;
    }

    private boolean memeFleur(Fleur a, Fleur b) {
        return a != null && b != null
                && a.getType() == b.getType()
                && a.getPosition() != null
                && a.getPosition().equals(b.getPosition());
    }

    /**
     * Liste les paires de fleurs encore connectées.
     *
     * @param type couleur filtrée, ou {@code null} pour toutes les couleurs.
     * @return paires jouables.
     */
    public List<Fleur[]> pairesConnectees(Fleur.TypeFleur type) {
        return ActionsJeu.pairesConnectees(this, type);
    }

    /**
     * Indique s'il existe encore un coup classique avec deux fleurs.
     *
     * @return {@code true} si une paire connectée existe.
     */
    public boolean existeCoupConnecte() {
        return ActionsJeu.existeCoupConnecte(this);
    }

    /**
     * Place un pion libre quand aucune paire de fleurs n'est encore jouable.
     *
     * @param pos position modèle choisie.
     * @return {@code true} si le placement libre a réussi.
     */
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

    /**
     * Applique le dépôt d'un pion depuis la pile graphique.
     *
     * @param pos     position modèle du dépôt.
     * @param payload information portée par le drag, normalement un type de pion.
     * @return {@code true} si le dépôt a joué un coup.
     */
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
        boolean resetIAContreIA = iaContreIA;
        boolean resetHumainContreIA = againstIA && ia != null;
        NiveauxIA niveauIAReset = niveauIA;
        NiveauxIA niveauOrReset = niveauIAOr;
        NiveauxIA niveauArgentReset = niveauIAArgent;
        String nomHumainReset = nomHumainIA;
        Pion.TypePion typeHumainReset = typeHumainIA;
        Pion.TypePion departIAReset = joueurDepartIA;
        Pion.TypePion departIAContreIAReset = joueurDepartIAContreIA;

        partieAbandonnee = false;
        gagnantParAbandon = null;
        avantageInitialApplique = false;
        enPhaseSelectionAvantage = false;
        initFleurs();
        initPions();
        if (resetIAContreIA) {
            GestionIA.configurerIAContreIA(this, niveauOrReset, niveauArgentReset, departIAContreIAReset);
        } else if (resetHumainContreIA) {
            GestionIA.activerIA(this, niveauIAReset, nomHumainReset, departIAReset, typeHumainReset);
        } else {
            initPlayers();
        }
        avantageInitialApplique = false;
        enPhaseSelectionAvantage = false;
        demarrerPhaseSelectionAvantage();
        historique.clear();
        setFeedbackMessage("Partie réinitialisée. " + nomJoueurActuelPourAffichage()
                + " doit choisir une fleur pour l'avantage initial.");
    }

    // =========================================================
    // =================== LOGIQUE RÉSEAU ======================
    // =========================================================

    // Applique un coup reçu depuis l'adversaire distant
    public void appliquerCoupReseau(CoupReseau coup) {
        applicationCoupDistant = true;
        try {
            if ("AVANTAGE".equals(coup.typeMessage)) {
                Fleur f = trouverFleurSurPlateau(Fleur.TypeFleur.valueOf(coup.typeFleur1), coup.fleur1X, coup.fleur1Y);
                if (f != null)
                    appliquerAvantageAvecFleur(f);

            } else if ("COUP".equals(coup.typeMessage)) {
                Pion pion = getPionLibre(Pion.TypePion.valueOf(coup.typePion));
                Coordonnees pos = new Coordonnees(coup.pionX, coup.pionY);
                Fleur f1 = trouverFleurSurPlateau(Fleur.TypeFleur.valueOf(coup.typeFleur1), coup.fleur1X, coup.fleur1Y);
                Fleur f2 = trouverFleurSurPlateau(Fleur.TypeFleur.valueOf(coup.typeFleur2), coup.fleur2X, coup.fleur2Y);
                if (pion != null && f1 != null && f2 != null) {
                    jouerCoup(getJoueurActuel(), pion, pos, f1, f2);
                }

            } else if ("ABANDON".equals(coup.typeMessage)) {
                abandonnerPartie();
            }
        } finally {
            applicationCoupDistant = false;
        }
    }

    // Cherche une fleur sur le plateau par type et coordonnées exactes
    public Fleur trouverFleurSurPlateau(Fleur.TypeFleur type, int x, int y) {
        for (Fleur f : fleurs) {
            if (f.getType() == type && f.getPosition().getX() == x && f.getPosition().getY() == y) {
                return f;
            }
        }
        return null;
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

    /**
     * Active une partie humain contre IA avec les valeurs par défaut.
     *
     * @param niveau niveau de difficulté de l'IA.
     */
    public void activerIA(NiveauxIA niveau) {
        activerIA(niveau, "Humain", null);
    }

    /**
     * Active une partie humain contre IA.
     *
     * @param niveau            niveau de difficulté de l'IA.
     * @param nomHumain         nom affiché pour le joueur humain.
     * @param joueurQuiCommence couleur qui commence, ou {@code null} pour un départ
     *                          aléatoire.
     */
    public void activerIA(NiveauxIA niveau, String nomHumain, Pion.TypePion joueurQuiCommence) {
        GestionIA.activerIA(this, niveau, nomHumain, joueurQuiCommence);
    }

    /**
     * Indique si un calcul d'IA est déjà lancé.
     *
     * @return {@code true} pendant que le thread IA réfléchit.
     */
    public boolean isIAEnCours() {
        return iaEnCours;
    }

    /**
     * Indique si le joueur courant est contrôlé par l'IA.
     *
     * @return {@code true} si le tour courant appartient à une IA.
     */
    public boolean estTourIA() {
        return GestionIA.estTourIA(this);
    }

    private Fleur retrouverFleurDuPlateau(Fleur recherchee) {
        return GestionIA.retrouverFleurDuPlateau(this, recherchee);
    }

    /**
     * Place le pion prévu par un coup IA, sans encore collecter les fleurs.
     *
     * @param coup coup calculé par l'IA.
     * @return {@code true} si le pion a été placé.
     */
    public boolean placerPionCoupIA(CoupIA coup) {
        return GestionIA.placerPionCoupIA(this, coup);
    }

    /**
     * Collecte les fleurs d'un coup IA déjà placé et passe au joueur suivant.
     *
     * @param coup coup calculé par l'IA.
     * @return {@code true} si les fleurs ont été collectées.
     */
    public boolean collecterFleursCoupIA(CoupIA coup) {
        return GestionIA.collecterFleursCoupIA(this, coup);
    }

    /**
     * Joue directement un coup IA complet.
     *
     * @param coup coup calculé par l'IA, ou {@code null} pour utiliser le secours.
     */
    public void jouerCoupIA(CoupIA coup) {
        GestionIA.jouerCoupIA(this, coup);
    }

    /**
     * Invalide le calcul IA en cours et remet le drapeau d'activité à zéro.
     */
    public void annulerIAEnCours() {
        generationIA++;
        iaEnCours = false;
    }

    /**
     * Produit un libellé pour un joueur.
     *
     * @param joueur joueur à afficher.
     * @return nom lisible dans l'interface.
     */
    public String nomJoueurPourAffichage(Joueur joueur) {
        if (joueur instanceof JoueurIa) {
            return joueur.getNom() + " (" + joueur.getTypePion() + ")";
        }
        return joueur.getNom() + " (" + joueur.getTypePion() + ")";
    }

    /**
     * Produit le libellé du joueur qui doit jouer.
     *
     * @return nom lisible du joueur courant.
     */
    public String nomJoueurActuelPourAffichage() {
        return nomJoueurPourAffichage(getJoueurActuel());
    }

    /**
     * Lance le calcul du tour IA avec le comportement standard.
     *
     * @param callbackFin action appelée quand le calcul est terminé.
     */
    public void lancerIA(Runnable callbackFin) {
        GestionIA.lancerIA(this, callbackFin);
    }

    /**
     * Lance le calcul du tour IA et délègue l'application du coup à l'appelant.
     *
     * @param actionQuandCoupPret action appelée avec le coup choisi.
     * @param callbackFin         action appelée après la remise à zéro du calcul
     *                            IA.
     */
    public void lancerIA(Consumer<CoupIA> actionQuandCoupPret, Runnable callbackFin) {
        GestionIA.lancerIA(this, actionQuandCoupPret, callbackFin);
    }

    /**
     * Configure une partie IA contre IA observable depuis l'interface ou les tests.
     *
     * @param niveauOr          niveau de l'IA qui joue les pions OR.
     * @param niveauArgent      niveau de l'IA qui joue les pions ARGENT.
     * @param joueurQuiCommence couleur qui commence, ou {@code null} pour un départ
     *                          aléatoire.
     */
    public void configurerIAContreIA(NiveauxIA niveauOr, NiveauxIA niveauArgent, Pion.TypePion joueurQuiCommence) {
        GestionIA.configurerIAContreIA(this, niveauOr, niveauArgent, joueurQuiCommence);
    }

    /**
     * Simule l'effet d'un coup de phase 1 sur ce plateau (modifie l'état).
     * Utile notamment pour le feedforward graphique.
     */
    public void simulerCoup(Fleur fleur1, Fleur fleur2) {
        ActionsJeu.simulerCoups(this, fleur1, fleur2);
    }

    public Fleur findFleurDansJeu(Jeu jeu, Fleur f) {
        for (Fleur fj : jeu.fleurs) {
            if (fj.getPosition().x == f.getPosition().x &&
                    fj.getPosition().y == f.getPosition().y &&
                    fj.getType() == f.getType()) {
                return fj;
            }
        }
        return null;
    }
}
