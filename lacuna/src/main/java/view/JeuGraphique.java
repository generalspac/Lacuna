package view;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.imageio.ImageIO;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.util.List;
import java.util.ArrayList;
import global.Configuration;
import model.*;
import model.IA.CoupIA;
import model.IA.EtatJeu;
import model.IA.GenerateurCoup;
import network.CoupReseau;
import network.ServeurReseau;
import network.ClientReseau;
import controller.EcouteurDeSouris;
import view.animations.Animation;
import view.animations.AnimationContext;
import view.animations.AnimationState;
import javax.swing.Timer;

public class JeuGraphique extends JComponent {

    // -------------------------- ATTRIBUTS ------------------------------------- //

    private final JFrame frame;
    public Jeu jeu;

    private final int modelWidth = 660;
    private final int modelHeight = 440;

    private Score scoreArgent;
    private Score scoreOr;
    private JLabel instructionLabel;
    private JLabel statusLabel;

    private Image rougeImg, bleueImg, jauneImg, verteImg, orangeImg, violetteImg, roseImg;
    private Image orImg, argentImg;
    private Image plateauImg;

    private EcouteurDeSouris mouse;
    private PionGraphique pileArgent, pileOr;

    private final AnimationContext animCtx = new AnimationContext();

    private final Animation animation;

    private final CoordonneeConverter converter;

    private double facteurVitesse = 1.0;
    private boolean afficherCoupsPossibles = false;
    private Fleur.TypeFleur filtreCouleurCoups = null;
    private boolean afficherFeedforward = false;

    // Cache des paires 

    // Mode réseau
    private Object gestionnaireReseau = null; // ServeurReseau ou ClientReseau
    private boolean enAttenteAdversaire = false;

    // Panneau phase 2 (slider + skip) — caché en phase 1, affiché quand phase 2
    // disponible
    private javax.swing.JComponent phase2Panel = null;
    private boolean phase2Affichee = false;

    private Fleur suggestedFlower = null;
    private java.util.List<Fleur> otherSuggestedFleurs = new java.util.ArrayList<>();
    private boolean afficherSuggestions = true;
  

    public void setSuggestedFlower(Fleur f) {
        this.suggestedFlower = f;
    }

public void setAfficherSuggestions(boolean afficher) {
    jeu.setAfficherSuggestions(afficher);
}

    public void setOtherSuggestedFleurs(java.util.List<Fleur> fleurs) {
        this.otherSuggestedFleurs = fleurs != null ? fleurs : new java.util.ArrayList<>();
    }

    private final Runnable quitterAction;

    public void setPhase2Panel(javax.swing.JComponent panel) {
        this.phase2Panel = panel;
    }
    // -------------------------- CONSTRUCTEURS --------------------------------- //

    public JeuGraphique(JFrame frame) {
        this(frame, null, null, null, null, null);
    }

    public JeuGraphique(JFrame frame, Score scoreArgent, Score scoreOr,
            JLabel instructionLabel, JLabel statusLabel, Runnable quitterAction) {

        this.frame = frame;
        this.scoreArgent = scoreArgent;
        this.scoreOr = scoreOr;
        this.instructionLabel = instructionLabel;
        this.statusLabel = statusLabel;
        this.quitterAction = quitterAction;

        this.jeu = new Jeu(modelWidth, modelHeight,
                new Cercle(new Coordonnees(modelWidth / 2, modelHeight / 2),
                        Math.min(modelWidth, modelHeight) / 2));

        this.converter = new CoordonneeConverter(jeu.getCercleDeJeu(), modelWidth, modelHeight);
        this.animation = new Animation(this, animCtx);

       
        setBackground(Color.WHITE);
        setOpaque(true);
        setFocusable(true);
        initImages();
        new Timer(33, e -> repaint()).start();

        initListeners();
    }

    // -------------------------- GETTERS --------------------------------------- //

    public Jeu getJeu() {
        return jeu;
    }

    public JFrame getFrame() {
        return frame;
    }

    public Animation getAnimation() {
        return animation;
    }

    public AnimationContext getAnimCtx() {
        return animCtx;
    }

    public double getFacteurVitesse() {
        return facteurVitesse;
    }


    public AnimationState getAnimationState() {
        return animCtx.getState();
    }

    public void setAnimationState(AnimationState s) {
        animCtx.setState(s);
    }

    public int getGenerationAnimation() {
        return animCtx.getGeneration();
    }

    public int increaseGenerationAnimation() {
        return animCtx.nouvelleGeneration();
    }

    public Fleur getFleurPhase2Illustree() {
        return animCtx.getFleurPhase2Illustree();
    }

    public Pion getPionPhase2Illustre() {
        return animCtx.getPionPhase2Illustre();
    }

    public Pion getPionPhase2Compare() {
        return animCtx.getPionPhase2Compare();
    }

    public int getPhase2ComparePulse() {
        return animCtx.getPhase2ComparePulse();
    }

    public void setFleurPhase2Illustree(Fleur f) {
        animCtx.setFleurPhase2Illustree(f);
    }

    public void setPionPhase2Illustre(Pion p) {
        animCtx.setPionPhase2Illustre(p);
    }

    public void setPionPhase2Compare(Pion p) {
        animCtx.setPionPhase2Compare(p);
    }

    public void setPhase2ComparePulse(int v) {
        animCtx.setPhase2ComparePulse(v);
    }

    public int increasePhase2ComparePulse() {
        return animCtx.incrementerPhase2ComparePulse();
    }

    public Joueur getGagnantAnime() {
        return animCtx.getGagnantAnime();
    }

    public int getWinnerPulse() {
        return animCtx.getWinnerPulse();
    }

    public void setGagnantAnime(Joueur j) {
        animCtx.setGagnantAnime(j);
    }

    public void setWinnerPulse(int v) {
        animCtx.setWinnerPulse(v);
    }

    public int increaseWinnerPulse() {
        return animCtx.incrementerWinnerPulse();
    }

    public void clearPhase2Visuals() {
        animCtx.clearPhase2();
    }

    // -------------------------- ÉTAT ANIMATION -------------------------------- //

    /**
     * Indique si une animation bloque les interactions humaines.
     *
     * @return {@code true} pendant une animation de coup, phase 2 ou victoire.
     */
    public boolean isAnimationEnCours() {
        return animCtx.isEnCours();
    }

    /**
     * Arrête les animations visuelles et nettoie la sélection en cours.
     */
    public void annulerAnimationsVisuelles() {
        animCtx.reset();
        jeu.resetFleursSelectionnee1();
        jeu.resetFleursSelectionnee2();
        repaint();
    }

    // -------------------------- DIVERS ---------------------------------------- //

    public void setFacteurVitesse(double facteur) {
        this.facteurVitesse = facteur;
    }

    public void setSidePiles(PionGraphique left, PionGraphique right) {
        this.pileArgent = left;
        this.pileOr = right;
    }

    public void showWinner(Joueur gagnant) {
        animCtx.setGagnantAnime(gagnant);
        animCtx.setWinnerPulse(0);
        repaint();
    }

    public void resetIASelectionVisuals() {
        animCtx.setSelectionIAVisuelle(false);
        repaint();
    }

    public void afficherGagnantSansAnimation() {
        animCtx.setGagnantAnime(jeu.getGagnant());
        animCtx.setWinnerPulse(18);
        animCtx.setState(AnimationState.AUCUNE);
        repaint();
    }

    /**
     * Termine immédiatement la phase 2 animée.
     */
    public void skipAnimationPhase2() {
        if (animCtx.getState() != AnimationState.PHASE2)
            return;

        jeu.attribuerFleursRestantes();
        final int gen = animCtx.nouvelleGeneration();

        animCtx.clearPhase2();
        jeu.resetFleursSelectionnee1();
        jeu.resetFleursSelectionnee2();
        refreshScores();
        repaint();

        animation.animerGagnant(gen);
    }

    // -------------------------- COORDONNÉES ----------------------------------- //

    /**
     * Convertit une position écran en coordonnées du modèle de jeu.
     *
     * @param x abscisse dans le composant Swing.
     * @param y ordonnée dans le composant Swing.
     * @return position dans le repère logique du plateau.
     */
    public Coordonnees screenToModel(int x, int y) {
        return converter.screenToModel(x, y);
    }

    /**
     * Convertit une position modèle en coordonnées écran.
     *
     * @param x abscisse logique.
     * @param y ordonnée logique.
     * @return position dessinable dans le composant Swing.
     */
    public Coordonnees modelToScreen(int x, int y) {
        return converter.modelToScreen(x, y);
    }

    // -------------------------- SCORE / UI ------------------------------------ //

    /**
     * Met à jour les scores, piles de pions et messages de la partie.
     */
    public void refreshScores() {
        // Si l'utilisateur change la sélection des fleurs pendant que le feedforward est affiché,
        // on le masque automatiquement quand il n'y a plus 2 fleurs sélectionnées connectées.
        if (afficherFeedforward) {
            Fleur f1 = jeu.getFleurSelectionnee1();
            Fleur f2 = jeu.getFleurSelectionnee2();
            boolean ok = (f1 != null && f2 != null && f1.getPosition() != null && f2.getPosition() != null)
                    && jeu.fleursConnectees(f1, f2);
            if (!ok) {
                afficherFeedforward = false;
                invaliderFeedforwardCache();
            }
        }

        if (scoreOr != null)
            scoreOr.updateFromGame(jeu.getJoueurs()[0]);
        if (scoreArgent != null)
            scoreArgent.updateFromGame(jeu.getJoueurs()[1]);
        if (pileArgent != null)
            pileArgent.repaint();
        if (pileOr != null)
            pileOr.repaint();

        if (phase2Panel != null && !phase2Affichee && jeu.estSecondePhaseDisponible()) {
            phase2Affichee = true;
            phase2Panel.setVisible(true);
            phase2Panel.revalidate();
        }

        updateInstructionText();
        updateStatusMessage();

        // Synchroniser les suggestions avec la sélection du modèle
        if (jeu.getFleurSelectionnee1() == null) {
            suggestedFlower = null;
            otherSuggestedFleurs.clear();
        }
    }

    /**
     * Met l'interface à jour puis lance le tour IA si le joueur courant en a
     * besoin.
     */
    public void refreshScoresEtTourIA() {
        refreshScores();
        repaint();

        if (animCtx.isEnCours() || jeu.isPartieTerminee()) {
            return;
        }

        if (animation.lancerAnimationSecondePhase()) {
            enAttenteAdversaire = false; // phase 2 automatique 
            return;
        }

        if (jeu.estTourIA() && jeu.isEnPhaseSelectionAvantage()) {
            animation.animerAvantageIA();
            return;
        }

        jeu.lancerIA(coup -> animation.animerPuisJouerCoupIA(coup), () -> {
            refreshScores();
            repaint();
            if (!jeu.estTourIA()) {
                animation.lancerAnimationSecondePhase();
            }
        });
    }

    public boolean isEnAttenteAdversaire() {
        return enAttenteAdversaire;
    }

    // Configure le mode réseau : relie le gestionnaire (ServeurReseau ou
    // ClientReseau) au jeu
    public void installerReseau(Object gestionnaire, Pion.TypePion typeLocal) {
        this.gestionnaireReseau = gestionnaire;
        int indexLocal = (typeLocal == Pion.TypePion.OR) ? 0 : 1;

        jeu.setEnModeReseau(true);
        jeu.setIndexJoueurLocal(indexLocal);

        // Callback d'envoi : appelé par Jeu quand le joueur local joue un coup
        jeu.setEcouteurCoupEnvoye(coup -> {
            try {
                if (gestionnaire instanceof ServeurReseau) {
                    ((ServeurReseau) gestionnaire).envoyerCoup(coup);
                } else if (gestionnaire instanceof ClientReseau) {
                    ((ClientReseau) gestionnaire).envoyerCoup(coup);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            // On attend uniquement après un COUP (l'AVANTAGE ne change pas le tour)
            enAttenteAdversaire = "COUP".equals(coup.typeMessage);
            refreshScores(); // met à jour les grilles de fleurs collectées
            repaint();
        });

        // callback de réception : appelé quand l'adversaire joue un coup
        java.util.function.Consumer<CoupReseau> reception = coup -> SwingUtilities.invokeLater(() -> {
            if ("COUP".equals(coup.typeMessage)) {
                // Montrer les fleurs choisies par l'adversaire pendant 700ms avant d'appliquer
                Fleur f1 = jeu.trouverFleurSurPlateau(
                        model.Fleur.TypeFleur.valueOf(coup.typeFleur1), coup.fleur1X, coup.fleur1Y);
                Fleur f2 = jeu.trouverFleurSurPlateau(
                        model.Fleur.TypeFleur.valueOf(coup.typeFleur2), coup.fleur2X, coup.fleur2Y);
                jeu.setFleurSelectionnee1(f1);
                jeu.setFleurSelectionnee2(f2);
                repaint();
                javax.swing.Timer timer = new javax.swing.Timer(700, e -> {
                    jeu.setFleurSelectionnee1(null);
                    jeu.setFleurSelectionnee2(null);
                    jeu.appliquerCoupReseau(coup);
                    enAttenteAdversaire = false;
                    refreshScoresEtTourIA();
                    if (jeu.isPartieAbandonnee()) afficherGagnantSansAnimation();
                    repaint();
                });
                timer.setRepeats(false);
                timer.start();
            } else {
                // AVANTAGE ou ABANDON : application immédiate
                jeu.appliquerCoupReseau(coup);
                if (!"AVANTAGE".equals(coup.typeMessage)) {
                    enAttenteAdversaire = false;
                }
                refreshScoresEtTourIA();
                if (jeu.isPartieAbandonnee()) afficherGagnantSansAnimation();
                repaint();
            }
        });

        if (gestionnaire instanceof ServeurReseau) {
            ((ServeurReseau) gestionnaire).setEcouteur(reception);
        } else if (gestionnaire instanceof ClientReseau) {
            ((ClientReseau) gestionnaire).setEcouteur(reception);
        }

        // Si l'adversaire commence, on attend son coup d'entrée de jeu
        enAttenteAdversaire = (jeu.currentPlayerIndex != indexLocal);

        refreshScores();
        repaint();
    }

    private void updateInstructionText() {
        if (instructionLabel == null)
            return;

        if (animCtx.isEnCours()) {
            instructionLabel.setText("Animation en cours… Vous pouvez utiliser Undo, Redo ou Reset.");
            return;
        }

        if (jeu.isIAEnCours()) {
            instructionLabel.setText(jeu.nomJoueurActuelPourAffichage()
                    + " réfléchit… Vous pouvez utiliser Undo, Redo ou Reset.");
            return;
        }

        if (jeu.estTourIA()) {
            instructionLabel.setText("Tour de " + jeu.nomJoueurActuelPourAffichage() + "…");
            return;
        }

        if (jeu.isEnModeReseau() && enAttenteAdversaire) {
            instructionLabel.setText("Tour du joueur distant (" + jeu.getJoueurActuel().getTypePion() + ")…");
            return;
        }

        if (jeu.isEnPhaseSelectionAvantage()) {
            instructionLabel.setText(jeu.nomJoueurActuelPourAffichage()
                    + " : choisissez une fleur pour votre avantage");
            return;
        }

        Fleur f1 = jeu.getFleurSelectionnee1();
        Fleur f2 = jeu.getFleurSelectionnee2(); 

        if (f1 == null) {
            instructionLabel.setText(jeu.nomJoueurActuelPourAffichage() + " : cliquez sur une fleur pour commencer.");
        } else if (f2 == null) {
            instructionLabel
                    .setText(jeu.nomJoueurActuelPourAffichage() + " : sélectionnez une seconde fleur du même type.");
        } else if (jeu.fleursConnectees(f1, f2)) {
            instructionLabel.setText("Fleurs connectees : un pion sera place en cliquant.");
        } else {
            instructionLabel.setText("Fleurs non Connectee : choisissez une autre seconde fleur.");
        }
    }

    private void updateStatusMessage() {
        if (statusLabel == null)
            return;
        String message = jeu.getFeedbackMessage();
        statusLabel.setText((message == null || message.trim().isEmpty())
                ? "En attente d'une action…"
                : message);
    }

    // ------------------ AFFICHAGE / MASQUAGE DES COUPS POSSIBLES -------------- //

    /**
     * Active ou masque l'affichage des coups possibles pour guider le joueur.
     *
     * @param couleur couleur filtrée, ou {@code null} pour toutes les couleurs.
     * @param masquer {@code true} pour cacher l'aide.
     */
    public void afficherCoupsPossibles(Fleur.TypeFleur couleur, boolean masquer) {
        if (masquer) {
            afficherCoupsPossibles = false;
            filtreCouleurCoups = null;
            jeu.setFeedbackMessage("Affichage des coups possibles masqué.");
        } else {
            if (afficherCoupsPossibles && filtreCouleurCoups == couleur) {
                afficherCoupsPossibles = false;
                filtreCouleurCoups = null;
                jeu.setFeedbackMessage("Affichage des coups possibles masqué.");
            } else {
                afficherCoupsPossibles = true;
                filtreCouleurCoups = couleur;
                String label = (couleur == null) ? "toutes les couleurs" : couleur.name().toLowerCase();
                jeu.setFeedbackMessage("Affichage des coups possibles pour " + label + ".");
            }
        }
        repaint();
        updateStatusMessage();
    }

    // -------------------------- UTILITAIRES ----------------------------------- //

    /**
     * Retrouve dans le plateau réel une fleur issue d'une copie.
     *
     * @param recherchee fleur recherchée.
     * @return fleur du plateau, ou {@code null} si elle a déjà été collectée.
     */
    public Fleur retrouverFleurGraphique(Fleur recherchee) {
        if (recherchee == null)
            return null;
        for (Fleur f : jeu.getFleurs()) {
            if (f.getType() == recherchee.getType()
                    && f.getPosition().equals(recherchee.getPosition())) {
                return f;
            }
        }
        return null;
    }

    protected Fleur getHighlightedFlower() {
        return suggestedFlower;
    }

    public Image getFlowerImage(Fleur.TypeFleur type) {
        switch (type) {
            case ROUGE:
                return rougeImg;
            case BLEUE:
                return bleueImg;
            case JAUNE:
                return jauneImg;
            case VERTE:
                return verteImg;
            case ORANGE:
                return orangeImg;
            case VIOLETTE:
                return violetteImg;
            case ROSE:
                return roseImg;
            default:
                return null;
        }
    }

    public Point pointScorePourJoueur(Joueur joueur, int decalage, DragLayer layer) {
        JComponent cible = (joueur.getTypePion() == Pion.TypePion.ARGENT) ? scoreArgent : scoreOr;
        int x = cible.getWidth() / 2 + (decalage * 22) - 11;
        int y = Math.max(55, cible.getHeight() / 2);
        return SwingUtilities.convertPoint(cible, new Point(x, y), layer);
    }

    // -------------------------- INITIALISATION -------------------------------- //

    private void initImages() {
        try {
            rougeImg = load("fleur_rouge.png");
            bleueImg = load("fleur_bleue.png");
            jauneImg = load("fleur_jaune.png");
            verteImg = load("fleur_verte.png");
            orangeImg = load("fleur_orange.png");
            violetteImg = load("fleur_violette.png");
            roseImg = load("fleur_rose.png");
            orImg = load("pion_or.png");
            argentImg = load("pion_argent.png");
            plateauImg = load("plateau.png");
        } catch (Exception e) {
            Configuration.debugeurErreur("Erreur chargement images : " + e.getMessage());
            System.exit(3);
        }
    }

    private Image load(String name) throws Exception {
        return ImageIO.read(Configuration.ouvre(name));
    }

    private void initListeners() {
        mouse = new EcouteurDeSouris(this);
        addMouseListener(mouse);
        addMouseMotionListener(mouse);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                DragLayer layer = (DragLayer) frame.getGlassPane();
                Pion.TypePion payload = (Pion.TypePion) layer.getPayload();
                if (payload == null)
                    return;

                boolean coupJoue = false;
                if (!animCtx.isEnCours() && !jeu.isIAEnCours()
                        && !jeu.estTourIA() && !enAttenteAdversaire
                        && !jeu.isPartieTerminee()) {
                    Coordonnees model = screenToModel(e.getX(), e.getY());
                    coupJoue = jeu.placerPionDepuisDrag(model, payload);
                }

                layer.clear();

                if (coupJoue) {
                    refreshScoresEtTourIA();
                } else {
                    refreshScores();
                    repaint();
                }
            }
        });
    }

    // -------------------------- RENDU ----------------------------------------- //

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // Met à jour les dimensions du converter à chaque repaint
        converter.setDimensions(getWidth(), getHeight());

        int size = converter.size();
        int offsetX = converter.offsetX();
        int offsetY = converter.offsetY();
        int centerX = converter.centerX();
        int centerY = converter.centerY();
        double rayon = converter.rayonScreen();
        int fleurSz = converter.fleurSize();

        jeu.setMarge(fleurSz / 2.0);

        // Fond
        g2.setColor(getBackground());
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.drawImage(plateauImg, offsetX, offsetY, size, size, null);

        // Cercle de jeu
        g2.setColor(Color.RED);
        int d = (int) (2 * rayon);
        g2.drawOval(centerX - (int) rayon, centerY - (int) rayon, d, d);

        // Fleurs
        for (Fleur f : jeu.getFleurs()) {
            if (f == null)
                continue;
            Image img = getFlowerImage(f.getType());
            if (img == null)
                continue;
            Coordonnees p = modelToScreen(f.getPosition().x, f.getPosition().y);
            g2.drawImage(img, p.getX() - fleurSz / 2, p.getY() - fleurSz / 2,
                    fleurSz, fleurSz, null);
        }

        drawPossibleMoves(g2, fleurSz);
        if (afficherFeedforward) {
             afficheFeedforward(g2);
        }
        drawSelection(g2);

       

        // Dessin des fleurs suggérées / tutoriel
        if (jeu.isAfficherSuggestions()) {
            drawAnimatedCercle(g2, getHighlightedFlower(), Color.GREEN);
            for (Fleur f : otherSuggestedFleurs) {
                drawAnimatedCercle(g2, f, Color.WHITE);
            }
        } else if (this instanceof JeuTutoriel) {
            drawAnimatedCercle(g2, getHighlightedFlower(), Color.GREEN);
        }

        drawPhase2Illustration(g2);
        drawPions(g2, fleurSz);
        if (enAttenteAdversaire)
            drawAttenteAdversaireOverlay(g2);
        drawWinnerOverlay(g2);

        verifierFinDePartie();
    }
    // -------------------------- SOUS-MÉTHODES DE DESSIN ----------------------- //

    private boolean finPartieAffichee = false;

    private void verifierFinDePartie() {
        if (jeu.isPartieTerminee() && !finPartieAffichee && !animCtx.isEnCours()) {
            finPartieAffichee = true;
            SwingUtilities.invokeLater(() -> {
                Joueur gagnant = jeu.getGagnant();
                String msg = (gagnant == null) ? "ÉGALITÉ !"
                        : "FÉLICITATIONS " + jeu.nomJoueurPourAffichage(gagnant).toUpperCase() + ", VOUS AVEZ GAGNÉ !";

                Object[] options = { "Rejouer", "Menu Principal" };
                int n = JOptionPane.showOptionDialog(frame,
                        msg + "\n\nQue voulez-vous faire ?",
                        "Fin de partie",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        options,
                        options[0]);

                if (n == JOptionPane.YES_OPTION) {
                    finPartieAffichee = false;
                    // Nettoyer l'état visuel de fin de partie avant de relancer.
                    // Sinon l'overlay "gagnant" peut rester affiché après recommencer.
                    animCtx.reset();
                    jeu.recommencerPartie();
                    animCtx.reset();
                    refreshScoresEtTourIA();
                } else {
                    // Menu Principal
                    finPartieAffichee = false;
                    frame.getGlassPane().setVisible(false);
                    if (quitterAction != null) {
                        quitterAction.run();
                    }
                }
            });
        }
    }

    /**
     * Dessine la sélection courante du joueur humain.
     *
     * @param g2 contexte graphique Swing.
     */
    private void drawSelection(Graphics2D g2) {
        Fleur f1 = jeu.getFleurSelectionnee1();
        Fleur f2 = jeu.getFleurSelectionnee2();
        boolean deuxFleurs = f1 != null && f2 != null;
        boolean connectees = deuxFleurs && jeu.fleursConnectees(f1, f2);

        g2.setColor(connectees || !deuxFleurs || animCtx.isSelectionIAVisuelle()
                ? Color.YELLOW
                : Color.RED);
        g2.setStroke(new BasicStroke(3));
        drawSelectionMarker(g2, f1);
        drawSelectionMarker(g2, f2);

        if (deuxFleurs) {
            Coordonnees p1 = modelToScreen(f1.getPosition().x, f1.getPosition().y);
            Coordonnees p2 = modelToScreen(f2.getPosition().x, f2.getPosition().y);
            Stroke old = g2.getStroke();
            if (!connectees && !animCtx.isSelectionIAVisuelle()) {
                g2.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                        1f, new float[] { 10f, 8f }, 0f));
            }
            g2.drawLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
            g2.setStroke(old);
        }
    }

    /**
     * Dessine le marqueur autour d'une fleur sélectionnée.
     *
     * @param g2 contexte graphique Swing.
     * @param f  fleur à entourer.
     */
    private void drawSelectionMarker(Graphics2D g2, Fleur f) {
        if (f == null || f.getPosition() == null)
            return;
        Coordonnees p = modelToScreen(f.getPosition().x, f.getPosition().y);
        int sz = Math.max(24, (int) (converter.size() * 0.065));
        g2.drawOval(p.getX() - sz / 2, p.getY() - sz / 2, sz, sz);
    }

    protected void drawAnimatedCercle(Graphics2D g2, Fleur f, Color color) {

        if (f == null || f.getPosition() == null)
            return;

        Coordonnees p = modelToScreen(
                f.getPosition().x,
                f.getPosition().y);

        int maxSz = Math.max(30, (int) (converter.size() * 0.08));

        // Pulsation fluide basée sur le temps
        double time = System.currentTimeMillis() / 1000.0;
        float alpha = (float) (Math.sin(time * 6.0) * 0.4 + 0.6); // Oscille entre 0.2 et 1.0
        alpha = Math.max(0.1f, Math.min(1.0f, alpha));

        Stroke oldStroke = g2.getStroke();
        Color oldColor = g2.getColor();

        g2.setStroke(new BasicStroke(
                4,
                BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND));

        // Application de l'alpha à la couleur
        Color pulseColor = new Color(
                color.getRed(),
                color.getGreen(),
                color.getBlue(),
                (int) (alpha * 255));

        g2.setColor(pulseColor);

        g2.drawOval(
                p.getX() - maxSz / 2,
                p.getY() - maxSz / 2,
                maxSz,
                maxSz);

        g2.setStroke(oldStroke);
        g2.setColor(oldColor);
    }

    /**
     * Dessine les aides visuelles de la phase 2 avec des marqueurs discrets.
     *
     * @param g2 contexte graphique Swing.
     */
    private void drawPhase2Illustration(Graphics2D g2) {
        Fleur fleurIllu = animCtx.getFleurPhase2Illustree();
        Pion pionIllu = animCtx.getPionPhase2Illustre();
        if (fleurIllu == null || pionIllu == null
                || fleurIllu.getPosition() == null || pionIllu.getPosition() == null)
            return;

        Coordonnees fp = modelToScreen(fleurIllu.getPosition().x, fleurIllu.getPosition().y);
        Coordonnees pp = modelToScreen(pionIllu.getPosition().x, pionIllu.getPosition().y);
        Stroke oldStroke = g2.getStroke();
        Color oldColor = g2.getColor();

        Pion pionCompare = animCtx.getPionPhase2Compare();
        boolean compare = pionCompare != null && pionCompare.getPosition() != null;

        int rayonFleur = Math.max(13, (int) (converter.fleurSize() * 0.55));
        int rayonPion = Math.max(15, (int) (converter.fleurSize() * 0.62));
        int rayonCompare = Math.max(13, (int) (converter.fleurSize() * 0.55));

        g2.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                1f, new float[] { 12f, 8f }, 0f));

        if (compare) {
            Coordonnees pc = modelToScreen(pionCompare.getPosition().x, pionCompare.getPosition().y);
            float pulse = (float) (Math.sin(animCtx.getPhase2ComparePulse() * 0.8) * 0.5 + 0.5);
            int alphaG = 190 + (int) (50 * pulse);
            int alphaC = 115 + (int) (65 * (1.0 - pulse));
            g2.setColor(new Color(255, 255, 255, alphaC));
            g2.drawLine(fp.getX(), fp.getY(), pc.getX(), pc.getY());
            g2.setColor(new Color(255, 215, 0, alphaG));
            g2.drawLine(fp.getX(), fp.getY(), pp.getX(), pp.getY());
            g2.setStroke(new BasicStroke(3));
            g2.setColor(new Color(255, 255, 255, alphaC));
            g2.drawOval(pc.getX() - rayonCompare, pc.getY() - rayonCompare,
                    rayonCompare * 2, rayonCompare * 2);
            g2.setColor(new Color(255, 215, 0, alphaG));
            g2.drawOval(pp.getX() - rayonPion, pp.getY() - rayonPion,
                    rayonPion * 2, rayonPion * 2);
        } else {
            g2.setColor(new Color(255, 215, 0, 230));
            g2.drawLine(fp.getX(), fp.getY(), pp.getX(), pp.getY());
            g2.setStroke(new BasicStroke(3));
            g2.drawOval(pp.getX() - rayonPion, pp.getY() - rayonPion,
                    rayonPion * 2, rayonPion * 2);
        }

        g2.setColor(new Color(255, 215, 0, 230));
        g2.setStroke(new BasicStroke(3));
        g2.drawOval(fp.getX() - rayonFleur, fp.getY() - rayonFleur,
                rayonFleur * 2, rayonFleur * 2);
        g2.setStroke(oldStroke);
        g2.setColor(oldColor);
    }

    private void drawPossibleMoves(Graphics2D g2, int fleurSize) {
        if (!afficherCoupsPossibles)
            return;

        if (jeu.estTourIA()) {
            drawAICandidateMoves(g2);
            return;
        }

        Fleur f1 = jeu.getFleurSelectionnee1();
        Fleur f2 = jeu.getFleurSelectionnee2();
        List<Fleur[]> paires = jeu.pairesConnectees(filtreCouleurCoups);

        g2.setColor(new Color(0, 255, 255, 170));
        g2.setStroke(new BasicStroke(2));
        for (Fleur[] paire : paires) {
            Coordonnees p1 = modelToScreen(paire[0].getPosition().x, paire[0].getPosition().y);
            Coordonnees p2 = modelToScreen(paire[1].getPosition().x, paire[1].getPosition().y);
            g2.drawLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
        }

        if (f1 != null && f2 != null && jeu.fleursConnectees(f1, f2)) {
            Coordonnees p1 = modelToScreen(f1.getPosition().x, f1.getPosition().y);
            Coordonnees p2 = modelToScreen(f2.getPosition().x, f2.getPosition().y);
            Stroke old = g2.getStroke();
            Color oldC = g2.getColor();
            g2.setColor(new Color(255, 255, 255, 230));
            g2.setStroke(new BasicStroke(4));
            g2.drawLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
            g2.setColor(oldC);
            g2.setStroke(old);
        }
    }

    /**
     * Dessine les coups que le générateur IA considère comme candidats.
     *
     * @param g2 contexte graphique Swing.
     */
    private void drawAICandidateMoves(Graphics2D g2) {
        List<CoupIA> coups = GenerateurCoup.genererCoups(EtatJeu.depuisJeu(jeu, jeu.currentPlayerIndex));

        Stroke oldStroke = g2.getStroke();
        Color oldColor = g2.getColor();
        g2.setStroke(new BasicStroke(2));

        for (CoupIA coup : coups) {
            if (coup == null || coup.fleur1 == null || coup.fleur2 == null || coup.position == null) {
                continue;
            }
            if (filtreCouleurCoups != null && coup.fleur1.getType() != filtreCouleurCoups) {
                continue;
            }

            Coordonnees p1 = modelToScreen(coup.fleur1.getPosition().x, coup.fleur1.getPosition().y);
            Coordonnees p2 = modelToScreen(coup.fleur2.getPosition().x, coup.fleur2.getPosition().y);
            Coordonnees pion = modelToScreen(coup.position.x, coup.position.y);

            g2.setColor(new Color(0, 255, 255, 145));
            g2.drawLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
            g2.setColor(new Color(255, 255, 255, 210));
            g2.fillOval(pion.getX() - 4, pion.getY() - 4, 8, 8);
        }

        g2.setStroke(oldStroke);
        g2.setColor(oldColor);
    }

    private void drawPions(Graphics2D g2, int size) {
        for (Pion p : jeu.getPions()) {
            if (p.getPosition() == null)
                continue;
            Image img = (p.getType() == Pion.TypePion.OR) ? orImg : argentImg;
            Coordonnees pos = modelToScreen(p.getPosition().x, p.getPosition().y);
            g2.drawImage(img, pos.getX() - size / 2, pos.getY() - size / 2,
                    size, size, null);
        }
    }

    // Overlay semi-transparent affiché pendant qu'on attend le coup de l'adversaire
    private void drawAttenteAdversaireOverlay(Graphics2D g2) {
        Color oldColor = g2.getColor();
        Font oldFont = g2.getFont();

        g2.setColor(new Color(0, 0, 0, 130));
        g2.fillRect(0, 0, getWidth(), getHeight());

        String texte = "En attente de l'adversaire…";
        g2.setFont(oldFont.deriveFont(Font.BOLD, 26f));
        g2.setColor(Color.WHITE);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(texte, (getWidth() - fm.stringWidth(texte)) / 2, getHeight() / 2);

        g2.setColor(oldColor);
        g2.setFont(oldFont);
    }

    private void drawWinnerOverlay(Graphics2D g2) {
        Joueur gagnant = animCtx.getGagnantAnime();
        int pulse = animCtx.getWinnerPulse();
        if (gagnant == null && pulse <= 0)
            return;

        String texte = (gagnant == null)
                ? "ÉGALITÉ !"
                : jeu.nomJoueurPourAffichage(gagnant) + " GAGNE !";
        int alpha = Math.min(210, 70 + pulse * 8);
        int w = Math.min(getWidth() - 60, 420);
        int h = 100;
        int x = (getWidth() - w) / 2;
        int y = 28;

        Color oldColor = g2.getColor();
        Font oldFont = g2.getFont();
        Stroke oldStroke = g2.getStroke();

        g2.setColor(new Color(0, 0, 0, alpha));
        g2.fillRoundRect(x, y, w, h, 28, 28);
        g2.setColor(new Color(255, 215, 0, 230));
        g2.setStroke(new BasicStroke(4));
        g2.drawRoundRect(x, y, w, h, 28, 28);

        float taille = 30f + (float) (Math.sin(pulse * 0.7) * 4.0);
        g2.setFont(oldFont.deriveFont(Font.BOLD, taille));
        FontMetrics fm = g2.getFontMetrics();
        g2.setColor(Color.WHITE);
        g2.drawString(texte,
                x + (w - fm.stringWidth(texte)) / 2,
                y + (h + fm.getAscent()) / 2 - 10);

        g2.setFont(oldFont.deriveFont(Font.BOLD, 14f));
        String detail = "Couleurs gagnées : " + jeu.getJoueurs()[0].nbCouleursGagnees()
                + " - " + jeu.getJoueurs()[1].nbCouleursGagnees();
        FontMetrics fm2 = g2.getFontMetrics();
        g2.drawString(detail,
                x + (w - fm2.stringWidth(detail)) / 2,
                y + h - 18);

        g2.setColor(oldColor);
        g2.setFont(oldFont);
        g2.setStroke(oldStroke);
    }

    // =================== FEEDFORWARD (cache) ===================

    private boolean feedforwardCacheValide = false;
    private final List<Fleur[]> feedforwardPairesNouvelles = new ArrayList<>();

    private String feedforwardSelectionSignature() {
        Fleur a = jeu.getFleurSelectionnee1();
        Fleur b = jeu.getFleurSelectionnee2();
        if (a == null || b == null || a.getPosition() == null || b.getPosition() == null) {
            return "";
        }
        String s1 = a.getType() + "@" + a.getPosition().x + "," + a.getPosition().y;
        String s2 = b.getType() + "@" + b.getPosition().x + "," + b.getPosition().y;
        return (s1.compareTo(s2) <= 0) ? (s1 + "|" + s2) : (s2 + "|" + s1);
    }

    public void invaliderFeedforwardCache() {
        feedforwardCacheValide = false;
        feedforwardPairesNouvelles.clear();
    }

    public void toggleFeedforward() {
        afficherFeedforward = !afficherFeedforward;
        if (!afficherFeedforward) {
            invaliderFeedforwardCache();
        } else {
            // Recalcul au prochain paint (ou au prochain draw) une seule fois
            invaliderFeedforwardCache();
        }
        repaint();
    }


    private boolean memeFleur(Fleur a, Fleur b) {
    if (a == null || b == null) return false;
    return a.getType() == b.getType() && a.getPosition().equals(b.getPosition());
    }

    private void recalculerFeedforward() {
        // Ne calcule que si les 2 fleurs sont sélectionnées et connectées
        Fleur s1 = jeu.getFleurSelectionnee1();
        Fleur s2 = jeu.getFleurSelectionnee2();
        if (s1 == null || s2 == null || s1.getPosition() == null || s2.getPosition() == null) {
            invaliderFeedforwardCache();
            feedforwardCacheValide = true;
            return;
        }
        if (!jeu.fleursConnectees(s1, s2)) {
            invaliderFeedforwardCache();
            feedforwardCacheValide = true;
            return;
        }

        // (Re)calcule les nouvelles connexions après le "manger" du coup.
        feedforwardPairesNouvelles.clear();

        EtatJeu etatAvant = EtatJeu.depuisJeu(jeu, jeu.currentPlayerIndex);

        java.util.function.Function<Fleur[], String> signature = (paire) -> {
            if (paire == null || paire.length < 2 || paire[0] == null || paire[1] == null
                    || paire[0].getPosition() == null || paire[1].getPosition() == null) {
                return "";
            }
            Fleur a = paire[0];
            Fleur b = paire[1];
            String sA = a.getType() + "@" + a.getPosition().x + "," + a.getPosition().y;
            String sB = b.getType() + "@" + b.getPosition().x + "," + b.getPosition().y;
            return (sA.compareTo(sB) <= 0) ? (sA + "|" + sB) : (sB + "|" + sA);
        };

        List<Fleur[]> anciennesPaires = new ArrayList<>();
        for (int i = 0; i < etatAvant.fleursRestantes.size(); i++) {
            for (int j = i + 1; j < etatAvant.fleursRestantes.size(); j++) {
                Fleur f1 = etatAvant.fleursRestantes.get(i);
                Fleur f2 = etatAvant.fleursRestantes.get(j);
                if (f1.getType() == f2.getType()
                        && model.ReglesPlateau.segmentLibre(etatAvant, f1.getPosition(), f2.getPosition(), model.ConstantesJeu.TOLERANCE_SEGMENT)) {
                    anciennesPaires.add(new Fleur[] { f1, f2 });
                }
            }
        }

        CoupIA coup = new CoupIA(
                etatAvant.trouverFleur(jeu.getFleurSelectionnee1()),
                etatAvant.trouverFleur(jeu.getFleurSelectionnee2()),
                Coordonnees.milieu(jeu.getFleurSelectionnee1().getPosition(), jeu.getFleurSelectionnee2().getPosition()));
        EtatJeu etatApres = etatAvant.appliquerCoup(coup);

        List<Fleur[]> nouvellesPaires = new ArrayList<>();
        for (int i = 0; i < etatApres.fleursRestantes.size(); i++) {
            for (int j = i + 1; j < etatApres.fleursRestantes.size(); j++) {
                Fleur f1 = etatApres.fleursRestantes.get(i);
                Fleur f2 = etatApres.fleursRestantes.get(j);
                if (f1.getType() == f2.getType()
                        && model.ReglesPlateau.segmentLibre(etatApres, f1.getPosition(), f2.getPosition(), model.ConstantesJeu.TOLERANCE_SEGMENT)) {
                    nouvellesPaires.add(new Fleur[] { f1, f2 });
                }
            }
        }

        java.util.Set<String> anciennesSignatures = new java.util.HashSet<>();
        for (Fleur[] pa : anciennesPaires) {
            anciennesSignatures.add(signature.apply(pa));
        }

        for (Fleur[] pa : nouvellesPaires) {
            String sig = signature.apply(pa);
            if (!anciennesSignatures.contains(sig)) {
                feedforwardPairesNouvelles.add(pa);
            }
        }

        feedforwardCacheValide = true;
    }

    public void afficheFeedforward(Graphics2D g2) {
    
    if (!feedforwardCacheValide) {
        recalculerFeedforward();
    }
        if (jeu.getFleurSelectionnee1() != null && jeu.getFleurSelectionnee2() != null) {
            g2.setColor(new Color(255, 0, 0, 150));
            g2.setStroke(new BasicStroke(3));

            for (Fleur[] paire : feedforwardPairesNouvelles) {
                if ((memeFleur(paire[0], jeu.getFleurSelectionnee1()) && memeFleur(paire[1], jeu.getFleurSelectionnee2())) ||
                        (memeFleur(paire[1], jeu.getFleurSelectionnee1()) && memeFleur(paire[0], jeu.getFleurSelectionnee2()))) {
                    continue;
                }
                Coordonnees p1 = modelToScreen(paire[0].getPosition().x, paire[0].getPosition().y);
                Coordonnees p2 = modelToScreen(paire[1].getPosition().x, paire[1].getPosition().y);
                g2.drawLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
            }
        }

    }
}


