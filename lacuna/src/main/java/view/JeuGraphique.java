package view;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import global.Configuration;
import model.*;
import model.IA.CoupIA;
import java.util.ArrayList;
import java.util.List;
import controller.EcouteurDeSouris;

public class JeuGraphique extends JComponent {

    private JFrame frame;
    public Jeu jeu;

    // === DIMENSIONS MODELE =======
    private int width = 660;
    private int height = 440;

    private Score scoreArgent, scoreOr;
    //private JLabel scoreLabel;
    private JLabel instructionLabel;
    private JLabel statusLabel;

    // Images
    private Image rougeImg, bleueImg, jauneImg, verteImg, orangeImg, violetteImg, roseImg;
    private Image orImg, argentImg;
    private Image plateauImg;

    private EcouteurDeSouris mouse;
    private PionArgent leftPile;
    private PionOr rightPile;
    private boolean animationEnCours = false;
    private boolean phase2Animee = false;
    private int generationAnimation = 0;
    private Fleur fleurPhase2Illustree = null;
    private Pion pionPhase2Illustre = null;
    private Pion pionPhase2Compare = null;
    private int phase2ComparePulse = 0;
    private Joueur gagnantAnime = null;
    private int winnerPulse = 0;
    // facteur de vitesse phase 2 : <1.0 = plus rapide et >1.0 = plus lent
    private double facteurVitesse = 1.0;
    private boolean afficherCoupsPossibles = false;
    private Fleur.TypeFleur filtreCouleurCoups = null;
    private boolean selectionIAVisuelle = false;

    public JeuGraphique(JFrame frame) {
        this(frame, null, null, null, null);
    }
    
    public JeuGraphique(JFrame frame, Score scoreArgent, Score scoreOr, JLabel instructionLabel, JLabel statusLabel) {

        this.frame = frame;
        this.scoreArgent = scoreArgent;
        this.scoreOr = scoreOr;
        //this.scoreLabel = scoreLabel;
        this.instructionLabel = instructionLabel;
        this.statusLabel = statusLabel;

        // === CERCLE MODELE ====
        this.jeu = new Jeu(width, height,
                new Cercle(new Coordonnees(width / 2, height / 2),
                        Math.min(width, height) / 2));

        setBackground(Color.WHITE);
        setOpaque(true);
        setFocusable(true);

        initImages();
        initListeners();
    }

    public void setSidePiles(PionArgent left, PionOr right) {
        this.leftPile = left;
        this.rightPile = right;
    }

    public boolean isAnimationEnCours() {
        return animationEnCours;
    }

    public void annulerAnimationsVisuelles() {
        generationAnimation++;
        animationEnCours = false;
        phase2Animee = false;
        jeu.resetFleursSelectionnee1();
        jeu.resetFleursSelectionnee2();
        fleurPhase2Illustree = null;
        pionPhase2Illustre = null;
        pionPhase2Compare = null;
        phase2ComparePulse = 0;
        gagnantAnime = null;
        winnerPulse = 0;
        selectionIAVisuelle = false;
        repaint();
    }

    public void setFacteurVitesse(double facteur) {
        this.facteurVitesse = facteur;
    }

    public void skipAnimationPhase2() {

        // ne fait rien si on est pas dans la phase 2
        if (!phase2Animee || !animationEnCours) return;

        // appliquer toutes les fleurs restantes 
        jeu.attribuerFleursRestantes();

        // incrementer la génération pour arreter les timer en cours
        final int generation = ++generationAnimation;

        // Réinitialiser l'état d'illustration
        fleurPhase2Illustree = null;
        pionPhase2Illustre = null;
        pionPhase2Compare = null;
        phase2ComparePulse = 0;
        jeu.resetFleursSelectionnee1();
        jeu.resetFleursSelectionnee2();
        refreshScores();
        repaint();

        // on passe directement à l'animation de gagnant
        animerGagnant(generation);
    }

    /////////////// INIT ///////////////////////

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

                if (payload != null) {

                    Coordonnees model = screenToModel(e.getX(), e.getY());

                    boolean coupJoue = false;
                    if (!animationEnCours && !jeu.isIAEnCours() && !jeu.estTourIA() && !jeu.isPartieTerminee()) {
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
            }
        });
    }

    // ====SCORE =======

    public void refreshScores() {
        if (scoreOr != null) scoreOr.updateFromGame(jeu.getJoueurs()[0]);
        if (scoreArgent != null) scoreArgent.updateFromGame(jeu.getJoueurs()[1]);

        //scoreLabel.setText(
        //        jeu.getJoueurs()[1].getScoreTotal() + " - " +
         //               jeu.getJoueurs()[0].getScoreTotal());

        if (leftPile != null)
            leftPile.repaint();
        if (rightPile != null)
            rightPile.repaint();

        updateInstructionText();
        updateStatusMessage();
    }

    public void refreshScoresEtTourIA() {
        refreshScores();
        repaint();

        if (lancerAnimationSecondePhase()) {
            return;
        }

        jeu.lancerIA(coup -> animerPuisJouerCoupIA(coup), () -> {
            refreshScores();
            repaint();
            lancerAnimationSecondePhase();
        });
    }

    private void updateInstructionText() {
        if (instructionLabel == null) return;
        
        if (animationEnCours) {
            instructionLabel.setText("Animation en cours... Vous pouvez utiliser Undo, Redo ou Reset.");
            return;
        }

        if (jeu.isIAEnCours()) {
            instructionLabel.setText("IA (" + jeu.getJoueurActuel().getTypePion() + ") réfléchit... Vous pouvez utiliser Undo, Redo ou Reset.");
            return;
        }

        if (jeu.estTourIA()) {
            instructionLabel.setText("Tour de l'IA (" + jeu.getJoueurActuel().getTypePion() + ")...");
            return;
        }

        if (jeu.isEnPhaseSelectionAvantage()) {
            instructionLabel.setText(
                    jeu.nomJoueurActuelPourAffichage() +
                            " : choisissez une fleur pour votre avantage");
            return;
        }

        if (jeu.getFleurSelectionnee1() == null) {
            instructionLabel.setText(jeu.nomJoueurActuelPourAffichage() + " : cliquez sur une fleur pour commencer.");
        } else if (jeu.getFleurSelectionnee2() == null) {
            instructionLabel.setText(jeu.nomJoueurActuelPourAffichage() + " : sélectionnez une seconde fleur du même type.");
        } else if (jeu.fleursConnectées(jeu.getFleurSelectionnee1(), jeu.getFleurSelectionnee2())) {
            instructionLabel.setText("Fleurs connectées : un pion sera placé en cliquant.");
        } else {
            instructionLabel.setText("Fleurs non connectées : choisissez une autre seconde fleur.");
        }
    }

    private void updateStatusMessage() {
        if (statusLabel != null) {
            String message = jeu.getFeedbackMessage();
            if (message == null || message.trim().isEmpty()) {
                statusLabel.setText("En attente d'une action...");
            } else {
                statusLabel.setText(message);
            }
        }
    }

    // ======= AFFICHAGE COUPS POSSIBLES ========
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
                String couleurTexte = (couleur == null) ? "toutes les couleurs" : couleur.name().toLowerCase();
                jeu.setFeedbackMessage("Affichage des coups possibles pour " + couleurTexte + ".");
            }
        }

        repaint();
        updateStatusMessage();
    }

    // ======= DESSIN DES COUPS POSSIBLES ========
    private void drawPossibleMoves(Graphics2D g2, int fleurSize) {
        if (!afficherCoupsPossibles) {
            return;
        }

        Fleur f1 = jeu.getFleurSelectionnee1();
        Fleur f2 = jeu.getFleurSelectionnee2();

        List<Fleur[]> paires = jeu.pairesConnectees(filtreCouleurCoups);
        g2.setColor(new Color(0, 255, 255, 170));
        g2.setStroke(new BasicStroke(2));

        // ====== Dessiner des lignes entre les paires de fleurs connectées ======
        for (Fleur[] paire : paires) {
            Coordonnees p1 = modelToScreen(paire[0].getPosition().x, paire[0].getPosition().y);
            Coordonnees p2 = modelToScreen(paire[1].getPosition().x, paire[1].getPosition().y);
            g2.drawLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
        }
        // ====== Dessiner une ligne plus épaisse entre les fleurs sélectionnées si elles sont connectées ======
        if (f1 != null && f2 != null && jeu.fleursConnectées(f1, f2)) {
            Coordonnees p1 = modelToScreen(f1.getPosition().x, f1.getPosition().y);
            Coordonnees p2 = modelToScreen(f2.getPosition().x, f2.getPosition().y);
            Stroke oldStroke = g2.getStroke();
            Color oldColor = g2.getColor();
            g2.setColor(new Color(255, 255, 255, 230));
            g2.setStroke(new BasicStroke(4));
            g2.drawLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
            g2.setColor(oldColor);
            g2.setStroke(oldStroke);
        }
    }

    // ===== ANIMATIONS IA + PHASE 2 ======

    private void animerPuisJouerCoupIA(CoupIA coup) {
        if (coup == null) {
            jeu.jouerCoupIA(null);
            return;
        }

        Fleur f1 = retrouverFleurGraphique(coup.fleur1);
        Fleur f2 = retrouverFleurGraphique(coup.fleur2);
        Joueur joueurIA = jeu.getJoueurActuel();

        if (f1 == null || f2 == null) {
            jeu.jouerCoupIA(coup);
            return;
        }

        Fleur f1Animee = Fleur.copieFleur(f1);
        Fleur f2Animee = Fleur.copieFleur(f2);

        animationEnCours = true;
        selectionIAVisuelle = true;
        final int generation = ++generationAnimation;
        jeu.setFeedbackMessage("IA (" + joueurIA.getTypePion() + ") prépare son coup...");
        refreshScores();
        repaint();

        new Timer(700, attente -> {
            ((Timer) attente.getSource()).stop();
            if (generation != generationAnimation) return;
            jeu.selectionnerFleursPourAnimation(f1, f2);
            jeu.setFeedbackMessage("IA (" + joueurIA.getTypePion() + ") sélectionne ses deux fleurs...");
            refreshScores();
            repaint();

            new Timer(950, e -> {
                ((Timer) e.getSource()).stop();
                if (generation != generationAnimation) return;
                jeu.setFeedbackMessage("IA (" + joueurIA.getTypePion() + ") place son pion...");
                if (!jeu.placerPionCoupIA(coup)) {
                    selectionIAVisuelle = false;
                    jeu.resetFleursSelectionnee1();
                    jeu.resetFleursSelectionnee2();
                    animationEnCours = false;
                    refreshScoresEtTourIA();
                    return;
                }
                refreshScores();
                repaint();

                new Timer(900, e2 -> {
                    ((Timer) e2.getSource()).stop();
                    if (generation != generationAnimation) return;
                    jeu.setFeedbackMessage("IA (" + joueurIA.getTypePion() + ") collecte ses fleurs...");
                    animerFleursVersScore(new Fleur[] { f1Animee, f2Animee }, joueurIA, () -> {
                        if (generation != generationAnimation) return;
                        jeu.collecterFleursCoupIA(coup);
                        selectionIAVisuelle = false;
                        jeu.resetFleursSelectionnee1();
                        jeu.resetFleursSelectionnee2();
                        animationEnCours = false;
                        refreshScores();
                        repaint();
                        lancerAnimationSecondePhase();
                    });
                }).start();
            }).start();
        }).start();
    }

    private Fleur retrouverFleurGraphique(Fleur recherchee) {
        if (recherchee == null) return null;
        for (Fleur f : jeu.getFleurs()) {
            if (f.getType() == recherchee.getType()
                    && f.getPosition().equals(recherchee.getPosition())) {
                return f;
            }
        }
        return null;
    }

    public boolean lancerAnimationSecondePhase() {
        if (phase2Animee || animationEnCours || !jeu.estSecondePhaseDisponible()) {
            return false;
        }

        ArrayList<Phase2.AttributionFleur> attributions = jeu.calculerAttributionsFleursRestantes();
        if (attributions.isEmpty()) {
            return false;
        }

        phase2Animee = true;
        animationEnCours = true;
        final int generation = ++generationAnimation;
        jeu.setFeedbackMessage("Phase 2 : attribution animée des fleurs restantes.");
        refreshScores();
        repaint();
        animerAttributionPhase2(attributions, 0, generation);
        return true;
    }

    private void animerAttributionPhase2(ArrayList<Phase2.AttributionFleur> attributions, int index, int generation) {
        if (generation != generationAnimation) return;
        if (index >= attributions.size()) {
            fleurPhase2Illustree = null;
            pionPhase2Illustre = null;
            pionPhase2Compare = null;
            phase2ComparePulse = 0;
            animerGagnant(generation);
            return;
        }

        Phase2.AttributionFleur attribution = attributions.get(index);
        if (attribution == null || attribution.fleur == null || attribution.joueur == null
                || !jeu.getFleurs().contains(attribution.fleur)) {
            animerAttributionPhase2(attributions, index + 1, generation);
            return;
        }

        jeu.selectionnerFleursPourAnimation(attribution.fleur, null);
        fleurPhase2Illustree = attribution.fleur;
        pionPhase2Illustre = attribution.pionLePlusProche;
        pionPhase2Compare = attribution.comparaisonNecessaire ? attribution.pionCompare : null;
        phase2ComparePulse = 0;
        if (attribution.comparaisonNecessaire && attribution.pionCompare != null) {
            jeu.setFeedbackMessage("Phase 2 : comparaison des distances avant attribution de la fleur " + attribution.fleur.getType() + ".");
            lancerPulseComparaisonPhase2(generation);
        } else {
            jeu.setFeedbackMessage("Phase 2 : mesure de la distance avant attribution de la fleur " + attribution.fleur.getType() + ".");
        }
        refreshScores();
        repaint();

        // Délai avant attribution ajusté selon le facteur de vitesse du slider
        int delaiMesure = (int) ((attribution.comparaisonNecessaire && attribution.pionCompare != null) ? 1900 * facteurVitesse : 1150 * facteurVitesse);
        new Timer(delaiMesure, e -> {
            ((Timer) e.getSource()).stop();
            if (generation != generationAnimation) return;
            jeu.setFeedbackMessage("Phase 2 : la fleur " + attribution.fleur.getType()
                    + " est attribuée à " + jeu.nomJoueurPourAffichage(attribution.joueur) + ".");
            refreshScores();
            repaint();
            animerFleursVersScore(new Fleur[] { attribution.fleur }, attribution.joueur, () -> {
                if (generation != generationAnimation) return;
                jeu.appliquerAttributionFleur(attribution);
                fleurPhase2Illustree = null;
                pionPhase2Illustre = null;
                pionPhase2Compare = null;
                phase2ComparePulse = 0;
                jeu.resetFleursSelectionnee1();
                jeu.resetFleursSelectionnee2();
                refreshScores();
                repaint();
                new Timer(280, e2 -> {
                    ((Timer) e2.getSource()).stop();
                    animerAttributionPhase2(attributions, index + 1, generation);
                }).start();
            });
        }).start();
    }

    private void animerFleursVersScore(Fleur[] fleursAnimees, Joueur joueur, Runnable fin) {
        if (fleursAnimees == null || fleursAnimees.length == 0) {
            if (fin != null) fin.run();
            return;
        }

        DragLayer layer = (DragLayer) frame.getGlassPane();
        final int[] restantes = { 0 };

        for (int i = 0; i < fleursAnimees.length; i++) {
            Fleur f = fleursAnimees[i];
            if (f == null || f.getPosition() == null) continue;

            Image img = getFlowerImage(f.getType());
            Coordonnees screen = modelToScreen(f.getPosition().x, f.getPosition().y);
            Point start = SwingUtilities.convertPoint(this,
                    new Point(screen.getX(), screen.getY()), layer);
            Point end = pointScorePourJoueur(joueur, i, layer);

            restantes[0]++;
            layer.animateImage(img, start, end, 750, 34, () -> {
                restantes[0]--;
                if (restantes[0] <= 0 && fin != null) {
                    fin.run();
                }
            });
        }

        if (restantes[0] == 0 && fin != null) {
            fin.run();
        }
    }

    private Point pointScorePourJoueur(Joueur joueur, int decalage, DragLayer layer) {
        JComponent cible = (joueur.getTypePion() == Pion.TypePion.ARGENT) ? scoreArgent : scoreOr;
        int x = cible.getWidth() / 2 + (decalage * 22) - 11;
        int y = Math.max(55, cible.getHeight() / 2);
        return SwingUtilities.convertPoint(cible, new Point(x, y), layer);
    }

    // ===== COORDONNÉES ======

    public Coordonnees screenToModel(int x, int y) {

        int size = Math.min(getWidth(), getHeight());
        int offsetX = (getWidth() - size) / 2;
        int offsetY = (getHeight() - size) / 2;

        int centerX = offsetX + size / 2;
        int centerY = offsetY + size / 2;

        double rayonScreen = size * 0.4;

        double relX = (x - centerX) / rayonScreen;
        double relY = (y - centerY) / rayonScreen;

        double rayonModel = jeu.getCercleDeJeu().getRayon();
        Coordonnees centre = jeu.getCercleDeJeu().getCentre();

        return new Coordonnees(
                (int) (centre.getX() + relX * rayonModel),
                (int) (centre.getY() + relY * rayonModel)
        );
    }

    public Coordonnees modelToScreen(int x, int y) {

        int size = Math.min(getWidth(), getHeight());
        int offsetX = (getWidth() - size) / 2;
        int offsetY = (getHeight() - size) / 2;

        int centerX = offsetX + size / 2;
        int centerY = offsetY + size / 2;

        double rayonScreen = size * 0.4;

        double rayonModel = jeu.getCercleDeJeu().getRayon();
        Coordonnees centre = jeu.getCercleDeJeu().getCentre();

        double relX = (x - centre.getX()) / rayonModel;
        double relY = (y - centre.getY()) / rayonModel;

        return new Coordonnees(
                (int) (centerX + relX * rayonScreen),
                (int) (centerY + relY * rayonScreen)
                );
    }
    // =====:///////= RENDu /////////////=================

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;

        int size = Math.min(getWidth(), getHeight());

        int offsetX = (getWidth() - size) / 2;
        int offsetY = (getHeight() - size) / 2;
        
        int centerX = offsetX + size / 2;
        int centerY = offsetY + size / 2;

        double rayon = size * 0.4;
        int fleurSize = (int) (size * 0.05);

        jeu.setMarge(fleurSize / 2.0);

        // Fond
        g2.setColor(getBackground());
        g2.fillRect(0, 0, getWidth(), getHeight());

         g2.drawImage(plateauImg, offsetX, offsetY, size, size, null);

        // Cercle
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

            g2.drawImage(img,
                    p.getX() - fleurSize / 2,
                    p.getY() - fleurSize / 2,
                    fleurSize, fleurSize, null);
        }

        drawPossibleMoves(g2, fleurSize);
        drawSelection(g2);
        drawPhase2Illustration(g2);
        drawPions(g2, fleurSize);
        drawWinnerOverlay(g2);
    }

    private Image getFlowerImage(Fleur.TypeFleur type) {
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

    private void drawSelection(Graphics2D g2) {

        Fleur f1 = jeu.getFleurSelectionnee1();
        Fleur f2 = jeu.getFleurSelectionnee2();

        boolean deuxFleurs = f1 != null && f2 != null;
        boolean connectees = deuxFleurs && jeu.fleursConnectées(f1, f2);

        g2.setColor(connectees || !deuxFleurs || selectionIAVisuelle ? Color.YELLOW : Color.RED);
        g2.setStroke(new BasicStroke(3));

        drawSelectionMarker(g2, f1);
        drawSelectionMarker(g2, f2);

        if (deuxFleurs) {
            Coordonnees p1 = modelToScreen(f1.getPosition().x, f1.getPosition().y);
            Coordonnees p2 = modelToScreen(f2.getPosition().x, f2.getPosition().y);

            Stroke oldStroke = g2.getStroke();
            if (!connectees && !selectionIAVisuelle) {
                g2.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1f, new float[] { 10f, 8f }, 0f));
            }
            g2.drawLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
            g2.setStroke(oldStroke);
        }
    }

    private void drawSelectionMarker(Graphics2D g2, Fleur f) {
        if (f == null || f.getPosition() == null)
            return;

        Coordonnees p = modelToScreen(f.getPosition().x, f.getPosition().y);
        int size = Math.min(getWidth(), getHeight());
        int markerSize = Math.max(24, (int) (size * 0.065));

        g2.drawOval(
                p.getX() - markerSize / 2,
                p.getY() - markerSize / 2,
                markerSize, markerSize);
    }


    private void lancerPulseComparaisonPhase2(int generation) {
        Timer timer = new Timer(110, null);
        timer.addActionListener(e -> {
            if (generation != generationAnimation || pionPhase2Compare == null) {
                ((Timer) e.getSource()).stop();
                return;
            }
            phase2ComparePulse++;
            repaint();
            if (phase2ComparePulse >= 18) {
                ((Timer) e.getSource()).stop();
            }
        });
        timer.start();
    }

    private void drawPhase2Illustration(Graphics2D g2) {
        if (fleurPhase2Illustree == null || pionPhase2Illustre == null
                || fleurPhase2Illustree.getPosition() == null || pionPhase2Illustre.getPosition() == null) {
            return;
        }

        Coordonnees fp = modelToScreen(fleurPhase2Illustree.getPosition().x, fleurPhase2Illustree.getPosition().y);
        Coordonnees pp = modelToScreen(pionPhase2Illustre.getPosition().x, pionPhase2Illustre.getPosition().y);

        Stroke oldStroke = g2.getStroke();
        Color oldColor = g2.getColor();

        boolean compare = pionPhase2Compare != null && pionPhase2Compare.getPosition() != null;

        g2.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1f, new float[] { 12f, 8f }, 0f));

        if (compare) {
            Coordonnees pc = modelToScreen(pionPhase2Compare.getPosition().x, pionPhase2Compare.getPosition().y);
            float pulse = (float) (Math.sin(phase2ComparePulse * 0.8) * 0.5 + 0.5);
            int alphaGagnant = 190 + (int) (50 * pulse);
            int alphaCompare = 115 + (int) (65 * (1.0 - pulse));

            g2.setColor(new Color(255, 255, 255, alphaCompare));
            g2.drawLine(fp.getX(), fp.getY(), pc.getX(), pc.getY());
            g2.setColor(new Color(255, 215, 0, alphaGagnant));
            g2.drawLine(fp.getX(), fp.getY(), pp.getX(), pp.getY());

            g2.setStroke(new BasicStroke(5));
            g2.setColor(new Color(255, 255, 255, alphaCompare));
            g2.drawOval(pc.getX() - 24, pc.getY() - 24, 48, 48);
            g2.setColor(new Color(255, 215, 0, alphaGagnant));
            g2.drawOval(pp.getX() - 28, pp.getY() - 28, 56, 56);
        } else {
            g2.setColor(new Color(255, 215, 0, 230));
            g2.drawLine(fp.getX(), fp.getY(), pp.getX(), pp.getY());
            g2.setStroke(new BasicStroke(5));
            g2.drawOval(pp.getX() - 26, pp.getY() - 26, 52, 52);
        }

        g2.setColor(new Color(255, 215, 0, 230));
        g2.setStroke(new BasicStroke(5));
        g2.drawOval(fp.getX() - 24, fp.getY() - 24, 48, 48);

        g2.setStroke(oldStroke);
        g2.setColor(oldColor);
    }

    private void animerGagnant(int generation) {
        if (generation != generationAnimation) return;
        gagnantAnime = jeu.getGagnant();
        winnerPulse = 0;
        String nom = (gagnantAnime == null) ? "égalité" : jeu.nomJoueurPourAffichage(gagnantAnime);
        jeu.setFeedbackMessage("Partie terminée : " + nom + " gagne !");
        refreshScores();
        repaint();

        Timer timer = new Timer(120, null);
        timer.addActionListener(e -> {
            if (generation != generationAnimation) {
                ((Timer) e.getSource()).stop();
                return;
            }
            winnerPulse++;
            repaint();
            if (winnerPulse >= 26) {
                ((Timer) e.getSource()).stop();
                animationEnCours = false;
                refreshScores();
                repaint();
            }
        });
        timer.start();
    }

    public void afficherGagnantSansAnimation() {
        // Affiche directement le gagnant quand la partie finit sans animation
        ////A revoir si on veut changer 
        gagnantAnime = jeu.getGagnant();
        winnerPulse = 18;
        animationEnCours = false;
        repaint();
    }

    private void drawWinnerOverlay(Graphics2D g2) {
        if (gagnantAnime == null && winnerPulse <= 0) {
            return;
        }

        String texte = (gagnantAnime == null)
                ? "ÉGALITÉ !"
                : jeu.nomJoueurPourAffichage(gagnantAnime) + " GAGNE !";
        int alpha = Math.min(210, 70 + winnerPulse * 8);
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

        float taille = 30f + (float) (Math.sin(winnerPulse * 0.7) * 4.0);
        g2.setFont(oldFont.deriveFont(Font.BOLD, taille));
        FontMetrics fm = g2.getFontMetrics();
        int tx = x + (w - fm.stringWidth(texte)) / 2;
        int ty = y + (h + fm.getAscent()) / 2 - 10;
        g2.setColor(Color.WHITE);
        g2.drawString(texte, tx, ty);

        g2.setFont(oldFont.deriveFont(Font.BOLD, 14f));
        String detail = "Couleurs gagnées : " + jeu.getJoueurs()[0].nbCouleursGagnees()
                + " - " + jeu.getJoueurs()[1].nbCouleursGagnees();
        FontMetrics fm2 = g2.getFontMetrics();
        g2.drawString(detail, x + (w - fm2.stringWidth(detail)) / 2, y + h - 18);

        g2.setColor(oldColor);
        g2.setFont(oldFont);
        g2.setStroke(oldStroke);
    }

    private void drawPions(Graphics2D g2, int size) {

        for (Pion p : jeu.getPions()) {
            if (p.getPosition() == null)
                continue;

            Image img = (p.getType() == Pion.TypePion.OR) ? orImg : argentImg;

            Coordonnees pos = modelToScreen(p.getPosition().x, p.getPosition().y);

            g2.drawImage(img,
                    pos.getX() - size / 2,
                    pos.getY() - size / 2,
                    size, size, null);
        }
    }

   
}
