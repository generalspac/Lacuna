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
import controller.EcouteurDeSouris;

public class JeuGraphique extends JComponent {

    private JFrame frame;
    public Jeu jeu;

    // === DIMENSIONS MODELE =======
    private int width = 660;
    private int height = 440;

    private Score scoreArgent, scoreOr;
    private JLabel scoreLabel;
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

    public JeuGraphique(JFrame frame) {
        this(frame, null, null, null, null, null);
    }
    
    public JeuGraphique(JFrame frame, Score scoreArgent, Score scoreOr,
            JLabel scoreLabel, JLabel instructionLabel, JLabel statusLabel) {

        this.frame = frame;
        this.scoreArgent = scoreArgent;
        this.scoreOr = scoreOr;
        this.scoreLabel = scoreLabel;
        this.instructionLabel = instructionLabel;
        this.statusLabel = statusLabel;

        // === CERCLE MODELE ====
        this.jeu = new Jeu(width, height,
                new Cercle(new Coordonnees(width / 2, height / 2),
                        Math.min(width, height) / 2));

        setBackground(Color.BLACK);
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
        repaint();
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

                Types.TypePion payload = (Types.TypePion) layer.getPayload();

                if (payload != null) {

                    Coordonnees model = screenToModel(e.getX(), e.getY());

                    if (!animationEnCours && !jeu.isIAEnCours() && !jeu.estTourIA()) {
                        jeu.placerPionDepuisDrag(model, payload);
                    }

                    layer.clear();

                    refreshScoresEtTourIA();
                }
            }
        });
    }

    // ====SCORE =======

    public void refreshScores() {
        scoreOr.updateFromGame(jeu.getJoueurs()[0]);
        scoreArgent.updateFromGame(jeu.getJoueurs()[1]);

        scoreLabel.setText(
                jeu.getJoueurs()[1].getScoreTotal() + " - " +
                        jeu.getJoueurs()[0].getScoreTotal());

        if (leftPile != null)
            leftPile.repaint();
        if (rightPile != null)
            rightPile.repaint();

        updateInstructionMessage();
        updateStatusMessage();
    }

    public void refreshScoresEtTourIA() {
        refreshScores();
        repaint();

        if (lancerAnimationSecondePhaseSiBesoin()) {
            return;
        }

        jeu.lancerIA(coup -> animerPuisJouerCoupIA(coup), () -> {
            refreshScores();
            repaint();
            lancerAnimationSecondePhaseSiBesoin();
        });
    }

    private void updateInstructionMessage() {
        if (animationEnCours) {
            instructionLabel.setText("Animation en cours... Vous pouvez utiliser Undo, Redo, Reset ou Load.");
            return;
        }

        if (jeu.isIAEnCours()) {
            instructionLabel.setText("IA (" + jeu.getJoueurActuel().getTypePion() + ") réfléchit... Vous pouvez utiliser Undo, Redo, Reset ou Load.");
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

        animationEnCours = true;
        final int generation = ++generationAnimation;
        jeu.selectionnerFleursPourAnimation(f1, f2);
        jeu.setFeedbackMessage("IA (" + joueurIA.getTypePion() + ") a choisi ses deux fleurs...");
        refreshScores();
        repaint();

        new Timer(700, e -> {
            ((Timer) e.getSource()).stop();
            if (generation != generationAnimation) return;
            animerFleursVersScore(new Fleur[] { f1, f2 }, joueurIA, () -> {
                if (generation != generationAnimation) return;
                jeu.jouerCoupIA(coup);
                jeu.resetFleursSelectionnee1();
                jeu.resetFleursSelectionnee2();
                animationEnCours = false;
                refreshScores();
                repaint();
                lancerAnimationSecondePhaseSiBesoin();
            });
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

    public boolean lancerAnimationSecondePhaseSiBesoin() {
        if (phase2Animee || animationEnCours || !jeu.estSecondePhaseDisponible()) {
            return false;
        }

        ArrayList<Jeu.AttributionFleur> attributions = jeu.calculerAttributionsFleursRestantes();
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

    private void animerAttributionPhase2(ArrayList<Jeu.AttributionFleur> attributions, int index, int generation) {
        if (generation != generationAnimation) return;
        if (index >= attributions.size()) {
            animationEnCours = false;
            jeu.setFeedbackMessage("Phase 2 terminée : toutes les fleurs restantes ont été attribuées.");
            refreshScores();
            repaint();
            return;
        }

        Jeu.AttributionFleur attribution = attributions.get(index);
        if (attribution == null || attribution.fleur == null || attribution.joueur == null
                || !jeu.getFleurs().contains(attribution.fleur)) {
            animerAttributionPhase2(attributions, index + 1, generation);
            return;
        }

        jeu.selectionnerFleursPourAnimation(attribution.fleur, null);
        jeu.setFeedbackMessage("Phase 2 : une fleur " + attribution.fleur.getType()
                + " va à " + jeu.nomJoueurPourAffichage(attribution.joueur) + ".");
        refreshScores();
        repaint();

        new Timer(250, e -> {
            ((Timer) e.getSource()).stop();
            if (generation != generationAnimation) return;
            animerFleursVersScore(new Fleur[] { attribution.fleur }, attribution.joueur, () -> {
                if (generation != generationAnimation) return;
                jeu.appliquerAttributionFleur(attribution);
                jeu.resetFleursSelectionnee1();
                jeu.resetFleursSelectionnee2();
                refreshScores();
                repaint();
                new Timer(120, e2 -> {
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
        JComponent cible = (joueur.getTypePion() == Types.TypePion.ARGENT) ? scoreArgent : scoreOr;
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

        drawSelection(g2);
        drawPions(g2, fleurSize);
    }

    private Image getFlowerImage(Types.TypeFleur type) {
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

        g2.setColor(Color.YELLOW);
        g2.setStroke(new BasicStroke(3));

        drawBox(g2, f1);
        drawBox(g2, f2);

        if (f1 != null && f2 != null && jeu.fleursConnectées(f1, f2)) {
            Coordonnees p1 = modelToScreen(f1.getPosition().x, f1.getPosition().y);
            Coordonnees p2 = modelToScreen(f2.getPosition().x, f2.getPosition().y);

            g2.drawLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
        }
    }

    private void drawBox(Graphics2D g2, Fleur f) {
        if (f == null || f.getPosition() == null)
            return;

        Coordonnees p = modelToScreen(f.getPosition().x, f.getPosition().y);

        g2.drawRoundRect(
                p.getX() - 20,
                p.getY() - 20,
                40, 40,
                15, 15);
    }

    private void drawPions(Graphics2D g2, int size) {

        for (Pion p : jeu.getPions()) {
            if (p.getPosition() == null)
                continue;

            Image img = (p.getType() == Types.TypePion.OR) ? orImg : argentImg;

            Coordonnees pos = modelToScreen(p.getPosition().x, p.getPosition().y);

            g2.drawImage(img,
                    pos.getX() - size / 2,
                    pos.getY() - size / 2,
                    size, size, null);
        }
    }

   
}
