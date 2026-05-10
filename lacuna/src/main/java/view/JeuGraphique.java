package view;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;

import global.Configuration;
import model.*;
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

                    jeu.placerPionDepuisDrag(model, payload);

                    layer.clear();

                    refreshScores();
                    repaint();
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

    private void updateInstructionMessage() {
        if (jeu.isEnPhaseSelectionAvantage()) {
            instructionLabel.setText(
                    jeu.getJoueurActuel().getTypePion() +
                            " : Choisissez une fleur pour votre avantage");
            return;
        }

        if (jeu.getFleurSelectionnee1() == null) {
            instructionLabel.setText("Joueur " + (jeu.getJoueurActuel().getTypePion()) + " : Cliquez sur une fleur pour commencer.");
        } else if (jeu.getFleurSelectionnee2() == null) {
            instructionLabel.setText("Sélectionnez une seconde fleur du même type.");
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
