package view;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.Timer;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import java.util.List;

import model.*;
import controller.EcouteurDeSouris;
import view.animations.Animation;
import view.animations.AnimationContext;
import view.animations.AnimationState;
import view.component.DragLayer;
import view.component.Score;
import view.manager.GameEndManager;
import view.manager.ImageManager;
import view.manager.NetworkController;
import view.manager.UIUpdater;
import view.render.FlowerRenderer;
import view.render.GameRenderer;
import view.render.MoveRenderer;
import view.render.OverlayRenderer;

public class JeuGraphique extends JComponent {

    // -------------------------- ATTRIBUTS ------------------------------------- //

    private final JFrame frame;
    public Jeu jeu;

    private final int modelWidth = 660;
    private final int modelHeight = 440;

    private Score scoreArgent;
    private Score scoreOr;

    private final UIUpdater uiUpdater;
    private final ImageManager imageManager;
    private final GameEndManager gameEndManager;

    private final GameRenderer gameRenderer;
    private final OverlayRenderer overlayRenderer;
    private final MoveRenderer moveRenderer;
    private final FlowerRenderer flowerRenderer;

    private EcouteurDeSouris mouse;
    private PionGraphique pileArgent, pileOr;

    private final AnimationContext animCtx = new AnimationContext();
    private final Animation animation;

    private final CoordonneeConverter converter;

    private double facteurVitesse = 1.0;
    private boolean afficherFeedforward = false;

    // Cache des paires 

    // Mode réseau
    private NetworkController gestionnaireReseau; // ServeurReseau ou ClientReseau


    private Fleur suggestedFlower = null;
    private java.util.List<Fleur> otherSuggestedFleurs = new java.util.ArrayList<>();

    public void setAfficherSuggestions(boolean afficher) {
        jeu.setAfficherSuggestions(afficher);
    }


    public void setPhase2Panel(
        javax.swing.JComponent panel) {

        uiUpdater.setPhase2Panel(panel);
    }
    // -------------------------- CONSTRUCTEURS --------------------------------- //

    public JeuGraphique(JFrame frame) {
        this(frame, null, null, null, null, null);
    }

    public JeuGraphique(
            JFrame frame,
            Score scoreArgent,
            Score scoreOr,
            JLabel instructionLabel,
            JLabel statusLabel,
            Runnable quitterAction) {

        // ---------------- ATTRIBUTS ---------------- //

        this.frame = frame;

        this.scoreArgent = scoreArgent;

        this.scoreOr = scoreOr;

        // ---------------- IMAGES ---------------- //

        imageManager = new ImageManager();

        imageManager.initImages();

        // ---------------- JEU ---------------- //

        this.jeu = new Jeu(
                modelWidth,
                modelHeight,
                new Cercle(
                        new Coordonnees(
                                modelWidth / 2,
                                modelHeight / 2),
                        Math.min(
                                modelWidth,
                                modelHeight) / 2));

        // ---------------- CONVERTER ---------------- //

        this.converter =
                new CoordonneeConverter(
                        jeu.getCercleDeJeu(),
                        modelWidth,
                        modelHeight);
    

        // ---------------- ANIMATION ---------------- //

        this.animation =
                new Animation(
                        this,
                        animCtx);

        // ---------------- UI UPDATER ---------------- //

        uiUpdater = new UIUpdater(
                jeu,
                scoreArgent,
                scoreOr,
                instructionLabel,
                statusLabel,
                pileArgent,
                pileOr,
                this::repaint);

        // ---------------- RENDERERS ---------------- //

        gameRenderer = new GameRenderer(
                jeu,
                imageManager);

        overlayRenderer = new OverlayRenderer(
                jeu,
                this,
                animCtx,
                converter);
        
        moveRenderer = new MoveRenderer(
                jeu,
                this);

        flowerRenderer = new FlowerRenderer(
                jeu,
                this,
                imageManager);
        
        // ---------------- UI ---------------- //
        
        initUI();

        // ---------------- RÉSEAU ---------------- //

        this.gestionnaireReseau =
                new NetworkController(this);

        gameEndManager = new GameEndManager(
                jeu,
                frame,
                gestionnaireReseau,
                quitterAction,
                this::refreshScoresEtTourIA,
                this::repaint,
                () -> animCtx.reset());


        // ---------------- PANEL ---------------- //

        setBackground(Color.WHITE);

        setOpaque(true);

        setFocusable(true);

        new Timer(
                33,
                e -> repaint()).start();

        // ---------------- LISTENERS ---------------- //

        initListeners();
    }



    // -------------------------- GETTERS --------------------------------------- //

    /**
     * Retourne la partie.
     *
     * @return partie courante.
     */
    public Jeu getJeu() {
        return jeu;
    }

    public JFrame getFrame() {
        return frame;
    }

    public Animation getAnimation() {
        return animation;
    }

    /**
     * Retourne le contexte animation.
     *
     * @return contexte animation.
     */
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

    public Image getFlowerImage(Fleur.TypeFleur type) {
        return flowerRenderer.getFlowerImage(type);
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

    public void setSuggestedFlower(Fleur f) {
        flowerRenderer.setSuggestedFlower(f);
    }

    public void setOtherSuggestedFleurs(List<Fleur> fleurs) {
        flowerRenderer.setOtherSuggestedFleurs(fleurs);
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
        if (frame != null && frame.getGlassPane() instanceof DragLayer) {
            ((DragLayer) frame.getGlassPane()).cancelAnimations();
        }
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

        uiUpdater.refreshScores();

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
        return gestionnaireReseau.isEnAttenteAdversaire();
    }

    // Configure le mode réseau : relie le gestionnaire (ServeurReseau ou
    // ClientReseau) au jeu
    /**
     * Configure le mode réseau.
     *
     * @param gestionnaire gestionnaire réseau.
     * @param typeLocal type local.
     */
    public void installerReseau(Object gestionnaire, Pion.TypePion typeLocal) {
        gestionnaireReseau.installerReseau(gestionnaire, typeLocal);
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

    public Point pointScorePourJoueur(Joueur joueur, int decalage, DragLayer layer) {
        JComponent cible = (joueur.getTypePion() == Pion.TypePion.ARGENT) ? scoreArgent : scoreOr;
        int x = cible.getWidth() / 2 + (decalage * 22) - 11;
        int y = Math.max(55, cible.getHeight() / 2);
        return SwingUtilities.convertPoint(cible, new Point(x, y), layer);
    }

    // -------------------------- INITIALISATION -------------------------------- //

    /**
     * Initialise les composants de l'interface.
     */
    private void initUI() {

        // ---------------- PILES ---------------- //

        pileOr = new PionGraphique(
                frame,
                jeu,
                Pion.TypePion.OR);

        pileArgent = new PionGraphique(
                frame,
                jeu,
                Pion.TypePion.ARGENT);
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
                        && !jeu.estTourIA() && !gestionnaireReseau.isEnAttenteAdversaire()
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

        // ---------------- DIMENSIONS ---------------- //

        converter.setDimensions(
                getWidth(),
                getHeight());

        int size = converter.size();

        int offsetX = converter.offsetX();

        int offsetY = converter.offsetY();

        int fleurSz = converter.fleurSize();

        jeu.setMarge(fleurSz / 2.0);

        // ---------------- FOND ---------------- //

        g2.setColor(getBackground());

        g2.fillRect(
                0,
                0,
                getWidth(),
                getHeight());

        gameRenderer.drawBoard(
                g2,
                offsetX,
                offsetY,
                size);

        // ---------------- FLEURS ---------------- //

        gameRenderer.drawFlowers(
                g2,
                this::modelToScreen,
                fleurSz);

        // ---------------- COUPS ---------------- //

        moveRenderer.drawPossibleMoves(
                g2,
                fleurSz);

        if (afficherFeedforward) {
            moveRenderer.afficheFeedforward(g2);
        }

        overlayRenderer.drawSelection(g2);

        // ---------------- SUGGESTIONS ---------------- //

        if (jeu.isAfficherSuggestions()) {

            flowerRenderer.drawAnimatedCercle(
                    g2,
                    getHighlightedFlower(),
                    Color.GREEN);

            for (Fleur f : otherSuggestedFleurs) {

                flowerRenderer.drawAnimatedCercle(
                        g2,
                        f,
                        Color.WHITE);
            }

        } else if (this instanceof JeuTutoriel) {

            flowerRenderer.drawAnimatedCercle(
                    g2,
                    getHighlightedFlower(),
                    Color.GREEN);
        }

        // ---------------- PHASE 2 ---------------- //

        overlayRenderer.drawPhase2Illustration(g2);

        // ---------------- PIONS ---------------- //
        gameRenderer.drawPions(
                g2,
                this::modelToScreen,
                fleurSz);

        // ---------------- RÉSEAU ---------------- //

        if (gestionnaireReseau.isEnAttenteAdversaire()) {

            overlayRenderer.drawAttenteAdversaireOverlay(g2);
        }

        // ---------------- FIN ---------------- //

        overlayRenderer.drawWinnerOverlay(g2);

        verifierFinDePartie();
    }
    // -------------------------- SOUS-MÉTHODES DE DESSIN ----------------------- //

    /**
     * Vérifie la fin de partie.
     */
    private void verifierFinDePartie() {

        gameEndManager.verifierFinDePartie(
                animCtx.isEnCours());
    }
    // =================== FEEDFORWARD (cache) ===================

    public void toggleFeedforward() {
        afficherFeedforward = !afficherFeedforward;
        if (!afficherFeedforward) {
            moveRenderer.invaliderFeedforwardCache();
        } else {
            // Recalcul au prochain paint (ou au prochain draw) une seule fois
            moveRenderer.invaliderFeedforwardCache();
        }
        repaint();
    }

    public void afficherCoupsPossibles(Fleur.TypeFleur couleur, boolean masquer) {
        moveRenderer.afficherCoupsPossibles(couleur, masquer);
    }
}
