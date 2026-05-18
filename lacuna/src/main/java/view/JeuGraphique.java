package view;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import java.util.List;

import global.Configuration;
import model.*;
import controller.EcouteurDeSouris;
import view.animations.Animation;
import view.animations.AnimationContext;
import view.animations.AnimationState;

public class JeuGraphique extends JComponent {

    // -------------------------- ATTRIBUTS ------------------------------------- //

    private final JFrame frame;
    public Jeu jeu;

    private final int modelWidth  = 660;
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

    // -------------------------- CONSTRUCTEURS --------------------------------- //

    public JeuGraphique(JFrame frame) {
        this(frame, null, null, null, null);
    }

    public JeuGraphique(JFrame frame, Score scoreArgent, Score scoreOr,
                        JLabel instructionLabel, JLabel statusLabel) {

        this.frame = frame;
        this.scoreArgent = scoreArgent;
        this.scoreOr = scoreOr;
        this.instructionLabel = instructionLabel;
        this.statusLabel = statusLabel;

        this.jeu = new Jeu(modelWidth, modelHeight,
                new Cercle(new Coordonnees(modelWidth / 2, modelHeight / 2),
                        Math.min(modelWidth, modelHeight) / 2));

        this.converter = new CoordonneeConverter(jeu.getCercleDeJeu(), modelWidth, modelHeight);
        this.animation = new Animation(this, animCtx);

        setBackground(Color.WHITE);
        setOpaque(true);
        setFocusable(true);

        initImages();
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

    public boolean isAnimationEnCours() {
        return animCtx.isEnCours();
    }

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

    public void skipAnimationPhase2() {
        if (animCtx.getState() == AnimationState.PHASE2) return;

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

    public Coordonnees screenToModel(int x, int y) {
        return converter.screenToModel(x, y);
    }

    public Coordonnees modelToScreen(int x, int y) {
        return converter.modelToScreen(x, y);
    }

    // -------------------------- SCORE / UI ------------------------------------ //

    public void refreshScores() {
        if (scoreOr != null) scoreOr.updateFromGame(jeu.getJoueurs()[0]);
        if (scoreArgent != null) scoreArgent.updateFromGame(jeu.getJoueurs()[1]);
        if (pileArgent != null) pileArgent.repaint();
        if (pileOr != null) pileOr.repaint();

        updateInstructionText();
        updateStatusMessage();
    }

    public void refreshScoresEtTourIA() {
        refreshScores();
        repaint();

        if (animation.lancerAnimationSecondePhase()) return;

        jeu.lancerIA(coup -> animation.animerPuisJouerCoupIA(coup), () -> {
            refreshScores();
            repaint();
            animation.lancerAnimationSecondePhase();
        });
    }

    private void updateInstructionText() {
        if (instructionLabel == null) return;

        if (animCtx.isEnCours()) {
            instructionLabel.setText("Animation en cours… Vous pouvez utiliser Undo, Redo ou Reset.");
            return;
        }

        if (jeu.isIAEnCours()) {
            instructionLabel.setText("IA (" + jeu.getJoueurActuel().getTypePion()
                    + ") réfléchit… Vous pouvez utiliser Undo, Redo ou Reset.");
            return;
        }

        if (jeu.estTourIA()) {
            instructionLabel.setText("Tour de l'IA (" + jeu.getJoueurActuel().getTypePion() + ")…");
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
            instructionLabel.setText(jeu.nomJoueurActuelPourAffichage() + " : sélectionnez une seconde fleur du même type.");
        } else if (jeu.fleursConnectées(f1, f2)) {
            instructionLabel.setText("Fleurs connectées : un pion sera placé en cliquant.");
        } else {
            instructionLabel.setText("Fleurs non connectées : choisissez une autre seconde fleur.");
        }
    }

    private void updateStatusMessage() {
        if (statusLabel == null) return;
        String message = jeu.getFeedbackMessage();
        statusLabel.setText((message == null || message.trim().isEmpty())
                ? "En attente d'une action…" : message);
    }

    // ------------------ AFFICHAGE / MASQUAGE DES COUPS POSSIBLES -------------- //

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

    public Fleur retrouverFleurGraphique(Fleur recherchee) {
        if (recherchee == null) return null;
        for (Fleur f : jeu.getFleurs()) {
            if (f.getType() == recherchee.getType()
                    && f.getPosition().equals(recherchee.getPosition())) {
                return f;
            }
        }
        return null;
    }

    public Image getFlowerImage(Fleur.TypeFleur type) {
        switch (type) {
            case ROUGE: return rougeImg;
            case BLEUE: return bleueImg;
            case JAUNE: return jauneImg;
            case VERTE: return verteImg;
            case ORANGE: return orangeImg;
            case VIOLETTE: return violetteImg;
            case ROSE: return roseImg;
            default: return null;
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
                if (payload == null) return;

                boolean coupJoue = false;
                if (!animCtx.isEnCours() && !jeu.isIAEnCours()
                        && !jeu.estTourIA() && !jeu.isPartieTerminee()) {
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

        int size    = converter.size();
        int offsetX = converter.offsetX();
        int offsetY = converter.offsetY();
        int centerX = converter.centerX();
        int centerY = converter.centerY();
        double rayon    = converter.rayonScreen();
        int    fleurSz  = converter.fleurSize();

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
            if (f == null) continue;
            Image img = getFlowerImage(f.getType());
            if (img == null) continue;
            Coordonnees p = modelToScreen(f.getPosition().x, f.getPosition().y);
            g2.drawImage(img, p.getX() - fleurSz / 2, p.getY() - fleurSz / 2,
                    fleurSz, fleurSz, null);
        }

        drawPossibleMoves(g2, fleurSz);
        drawSelection(g2);
        drawPhase2Illustration(g2);
        drawPions(g2, fleurSz);
        drawWinnerOverlay(g2);
    }

    // -------------------------- SOUS-MÉTHODES DE DESSIN ----------------------- //
    
    private void drawSelection(Graphics2D g2) {
        Fleur f1 = jeu.getFleurSelectionnee1();
        Fleur f2 = jeu.getFleurSelectionnee2();
        boolean deuxFleurs = f1 != null && f2 != null;
        boolean connectees = deuxFleurs && jeu.fleursConnectées(f1, f2);

        g2.setColor(connectees || !deuxFleurs || animCtx.isSelectionIAVisuelle()
                ? Color.YELLOW : Color.RED);
        g2.setStroke(new BasicStroke(3));
        drawSelectionMarker(g2, f1);
        drawSelectionMarker(g2, f2);

        if (deuxFleurs) {
            Coordonnees p1 = modelToScreen(f1.getPosition().x, f1.getPosition().y);
            Coordonnees p2 = modelToScreen(f2.getPosition().x, f2.getPosition().y);
            Stroke old = g2.getStroke();
            if (!connectees && !animCtx.isSelectionIAVisuelle()) {
                g2.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                        1f, new float[]{10f, 8f}, 0f));
            }
            g2.drawLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
            g2.setStroke(old);
        }
    }

    private void drawSelectionMarker(Graphics2D g2, Fleur f) {
        if (f == null || f.getPosition() == null) return;
        Coordonnees p = modelToScreen(f.getPosition().x, f.getPosition().y);
        int sz = Math.max(24, (int) (converter.size() * 0.065));
        g2.drawOval(p.getX() - sz / 2, p.getY() - sz / 2, sz, sz);
    }

    private void drawPhase2Illustration(Graphics2D g2) {
        Fleur fleurIllu = animCtx.getFleurPhase2Illustree();
        Pion  pionIllu  = animCtx.getPionPhase2Illustre();
        if (fleurIllu == null || pionIllu == null
                || fleurIllu.getPosition() == null || pionIllu.getPosition() == null) return;

        Coordonnees fp = modelToScreen(fleurIllu.getPosition().x, fleurIllu.getPosition().y);
        Coordonnees pp = modelToScreen(pionIllu.getPosition().x,  pionIllu.getPosition().y);
        Stroke oldStroke = g2.getStroke();
        Color  oldColor  = g2.getColor();

        Pion pionCompare = animCtx.getPionPhase2Compare();
        boolean compare  = pionCompare != null && pionCompare.getPosition() != null;

        g2.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                1f, new float[]{12f, 8f}, 0f));

        if (compare) {
            Coordonnees pc = modelToScreen(pionCompare.getPosition().x, pionCompare.getPosition().y);
            float pulse = (float) (Math.sin(animCtx.getPhase2ComparePulse() * 0.8) * 0.5 + 0.5);
            int alphaG = 190 + (int) (50 * pulse);
            int alphaC = 115 + (int) (65 * (1.0 - pulse));
            g2.setColor(new Color(255, 255, 255, alphaC));
            g2.drawLine(fp.getX(), fp.getY(), pc.getX(), pc.getY());
            g2.setColor(new Color(255, 215, 0, alphaG));
            g2.drawLine(fp.getX(), fp.getY(), pp.getX(), pp.getY());
            g2.setStroke(new BasicStroke(5));
            g2.setColor(new Color(255, 255, 255, alphaC));
            g2.drawOval(pc.getX() - 24, pc.getY() - 24, 48, 48);
            g2.setColor(new Color(255, 215, 0, alphaG));
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

    private void drawPossibleMoves(Graphics2D g2, int fleurSize) {
        if (!afficherCoupsPossibles) return;

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

        if (f1 != null && f2 != null && jeu.fleursConnectées(f1, f2)) {
            Coordonnees p1 = modelToScreen(f1.getPosition().x, f1.getPosition().y);
            Coordonnees p2 = modelToScreen(f2.getPosition().x, f2.getPosition().y);
            Stroke old   = g2.getStroke();
            Color  oldC  = g2.getColor();
            g2.setColor(new Color(255, 255, 255, 230));
            g2.setStroke(new BasicStroke(4));
            g2.drawLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
            g2.setColor(oldC);
            g2.setStroke(old);
        }
    }

    private void drawPions(Graphics2D g2, int size) {
        for (Pion p : jeu.getPions()) {
            if (p.getPosition() == null) continue;
            Image img = (p.getType() == Pion.TypePion.OR) ? orImg : argentImg;
            Coordonnees pos = modelToScreen(p.getPosition().x, p.getPosition().y);
            g2.drawImage(img, pos.getX() - size / 2, pos.getY() - size / 2,
                    size, size, null);
        }
    }

    private void drawWinnerOverlay(Graphics2D g2) {
        Joueur gagnant = animCtx.getGagnantAnime();
        int    pulse   = animCtx.getWinnerPulse();
        if (gagnant == null && pulse <= 0) return;

        String texte = (gagnant == null)
                ? "ÉGALITÉ !"
                : jeu.nomJoueurPourAffichage(gagnant) + " GAGNE !";
        int alpha = Math.min(210, 70 + pulse * 8);
        int w = Math.min(getWidth() - 60, 420);
        int h = 100;
        int x = (getWidth() - w) / 2;
        int y = 28;

        Color  oldColor  = g2.getColor();
        Font   oldFont   = g2.getFont();
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
}