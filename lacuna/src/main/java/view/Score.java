package view;

import global.Configuration;
import model.ConstantesJeu;
import model.Fleur;
import model.Joueur;
import model.Pion;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.util.*;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Polygon;

public class Score extends JComponent {
    private Image pawnImage;
    private Map<Fleur.TypeFleur, Image> flowerImages;
    private Map<Fleur.TypeFleur, Integer> flowerCounts;
    private Pion.TypePion playerType;

    public Score(Pion.TypePion type) {
        this.playerType = type;
        this.flowerImages = new HashMap<>();
        this.flowerCounts = new HashMap<>();
        setOpaque(false);

        loadImages();

        for (Fleur.TypeFleur t : Fleur.TypeFleur.values()) {
            flowerCounts.put(t, 0);
        }
    }

    private void loadImages() {
        try {
            String pawnFile = (playerType == Pion.TypePion.OR) ? "pion_or.png" : "pion_argent.png";
            pawnImage = ImageIO.read(Configuration.ouvre(pawnFile));

            flowerImages.put(Fleur.TypeFleur.ROUGE, ImageIO.read(Configuration.ouvre("fleur_rouge.png")));
            flowerImages.put(Fleur.TypeFleur.BLEUE, ImageIO.read(Configuration.ouvre("fleur_bleue.png")));
            flowerImages.put(Fleur.TypeFleur.JAUNE, ImageIO.read(Configuration.ouvre("fleur_jaune.png")));
            flowerImages.put(Fleur.TypeFleur.VERTE, ImageIO.read(Configuration.ouvre("fleur_verte.png")));
            flowerImages.put(Fleur.TypeFleur.ORANGE, ImageIO.read(Configuration.ouvre("fleur_orange.png")));
            flowerImages.put(Fleur.TypeFleur.VIOLETTE, ImageIO.read(Configuration.ouvre("fleur_violette.png")));
            flowerImages.put(Fleur.TypeFleur.ROSE, ImageIO.read(Configuration.ouvre("fleur_rose.png")));
        } catch (Exception e) {
            Configuration.debugeurErreur("Erreur chargement images Score : " + e.getMessage());
        }
    }

    public void setFlowerCount(Fleur.TypeFleur type, int count) {
        flowerCounts.put(type, count);
        repaint();
    }

    public void updateScore(Map<Fleur.TypeFleur, Integer> newCounts) {
        for (Fleur.TypeFleur t : Fleur.TypeFleur.values()) {
            flowerCounts.put(t, newCounts.getOrDefault(t, 0));
        }
        repaint();
    }

    private void dessinerEtoile(
            Graphics2D g2,
            int centerX,
            int centerY,
            int radius) {

        int branches = 5;

        Polygon star = new Polygon();

        for (int i = 0; i < branches * 2; i++) {

            double angle = Math.PI / branches * i;

            int r = (i % 2 == 0)
                    ? radius
                    : radius / 2;

            int x = (int) (centerX + Math.cos(angle - Math.PI / 2) * r);

            int y = (int) (centerY + Math.sin(angle - Math.PI / 2) * r);

            star.addPoint(x, y);
        }

        g2.setColor(new Color(255, 215, 0));

        g2.fillPolygon(star);

        g2.setColor(Color.WHITE);

        g2.drawPolygon(star);
    }

    public void updateFromGame(Joueur joueur) {

        Map<Fleur.TypeFleur, Integer> data = new HashMap<>();

        for (Fleur.TypeFleur t : Fleur.TypeFleur.values()) {
            data.put(t, 0);
        }

        List<Fleur> fleurs = joueur.getFleursGagnees();
        if (fleurs == null)
            return;

        for (Fleur f : fleurs) {
            data.put(f.getType(), data.get(f.getType()) + 1);
        }

        flowerCounts.clear();
        flowerCounts.putAll(data);

        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();

        java.awt.Rectangle vis = getVisibleRect();
        int visH = (vis.height > 0) ? vis.height : getHeight();
        int visY = (vis.height > 0) ? vis.y : 0;

        // Pion en haut de la zone visible
        int pawnSize = Math.min(w, visH / 6);
        int startY = visY + 5;
        if (pawnImage != null) {
            g2.drawImage(pawnImage, (w - pawnSize) / 2, startY, pawnSize, pawnSize, null);
        }

        // Grille de fleurs (7 types x 7 fleurs)
        int currentY = startY + pawnSize + 15;
        int availableHeight = (visY + visH) - currentY - 10;

        // On réserve de la place pour l'étoile sur le côté
        int starSpace = 30;
        int availableWidth = w - 10 - starSpace;

        int gap = 2;

        int nbFleursParCouleur = ConstantesJeu.NB_FLEURS_PAR_COULEUR;
        int nbGaps = nbFleursParCouleur - 1;
        int boxSizeH = (availableHeight - nbGaps * gap) / nbFleursParCouleur;
        int boxSizeW = (availableWidth - nbGaps * gap) / nbFleursParCouleur;
        int boxSize = Math.min(boxSizeH, boxSizeW);
        if (boxSize < 1)
            boxSize = 1; // éviter une taille nulle
        int totalGridW = nbFleursParCouleur * boxSize + nbGaps * gap;

        // On décale la grille pour laisser de la place à l'étoile selon le joueur
        int startX;
        if (playerType == Pion.TypePion.ARGENT) {
            startX = 5; // Place pour l'étoile à droite
        } else {
            startX = w - totalGridW - 5; // Place pour l'étoile à gauche
        }

        Fleur.TypeFleur[] types = Fleur.TypeFleur.values();

        for (int i = 0; i < types.length; i++) {

            Fleur.TypeFleur type = types[i];

            int y = currentY + i * (boxSize + gap);

            int count = flowerCounts.get(type);

            Image img = flowerImages.get(type);

            if (img == null)
                continue;

            for (int j = 0; j < nbFleursParCouleur; j++) {

                // =========================
                // POSITION SYMÉTRIQUE
                // =========================
                int displayIndex;

                if (playerType == Pion.TypePion.ARGENT) {
                    displayIndex = j;
                } else {
                    displayIndex = nbGaps - j;
                }

                int x = startX + displayIndex * (boxSize + gap);

                // =========================
                // FLEURS GAGNÉES
                // =========================
                boolean active = j < count;

                if (active) {
                    g2.setComposite(
                            AlphaComposite.getInstance(
                                    AlphaComposite.SRC_OVER,
                                    1.0f));
                } else {
                    g2.setComposite(
                            AlphaComposite.getInstance(
                                    AlphaComposite.SRC_OVER,
                                    0.12f));
                }

                g2.drawImage(img, x, y, boxSize, boxSize, null);

                // Dessiner l'étoile seulement une fois par ligne, à la fin de la grille
                if (count >= 4 && j == 6) {

                    int starX;

                    if (playerType == Pion.TypePion.ARGENT) {
                        // étoile à droite de la dernière fleur (qui est à l'index 6)
                        starX = x + boxSize + gap + (starSpace / 2);
                    } else {
                        // étoile à gauche de la première fleur (qui est à l'index 0, donc j=6 pour OR)
                        // Pour OR, j=6 est l'index d'affichage 0
                        int x0 = startX;
                        starX = x0 - (starSpace / 2);
                    }

                    int starY = y + boxSize / 2;
                    g2.setComposite(
                            AlphaComposite.getInstance(
                                    AlphaComposite.SRC_OVER,
                                    1.0f));

                    dessinerEtoile(
                            g2,
                            starX,
                            starY,
                            boxSize / 2);
                }
                g2.setComposite(
                        AlphaComposite.getInstance(
                                AlphaComposite.SRC_OVER,
                                1.0f));
            }
        }
    }
}
