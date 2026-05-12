package view;

import global.Configuration;
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
        int availableWidth = w - 10;

       
        int gap = 2;

        
        int boxSizeH = (availableHeight - 6 * gap) / 7;
        int boxSizeW = (availableWidth - 6 * gap) / 7;
        int boxSize = Math.min(boxSizeH, boxSizeW);
        if (boxSize < 1) boxSize = 1; // éviter une taille nulle
        int totalGridW = 7 * boxSize + 6 * gap;
        int startX = (w - totalGridW) / 2;

        Fleur.TypeFleur[] types = Fleur.TypeFleur.values();
        for (int i = 0; i < types.length; i++) {
            Fleur.TypeFleur type = types[i];
            int y = currentY + i * (boxSize + gap);
            int count = flowerCounts.get(type);

            for (int j = 0; j < 7; j++) {
                int x = startX + j * (boxSize + gap);
                Image img = flowerImages.get(type);
                if (img == null)
                    continue;

                if (j < count) {
                    // fleurs gagnées
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
                    g2.drawImage(img, x, y, boxSize, boxSize, null);
                } else {
                    // fleurs non gagnées
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f));
                    g2.drawImage(img, x, y, boxSize, boxSize, null);
                }
            }
        }
    }
}
