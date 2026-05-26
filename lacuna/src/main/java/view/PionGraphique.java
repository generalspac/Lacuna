package view;

import global.Configuration;
import model.Jeu;
import model.Pion;
import view.component.DragLayer;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;

public class PionGraphique extends JComponent {

    private Image imagePion;
    private Image imagePionFloue;
    private final int maxPions = 6;

    private final java.util.List<Rectangle> zonesPions = new java.util.ArrayList<>();

    private boolean dragging = false;
    private int draggedIndex = -1;

    private final JFrame frame;
    private final Jeu jeu;
    private final Pion.TypePion typePion;

    public PionGraphique(JFrame frame, Jeu jeu, Pion.TypePion typePion) {
        this.frame = frame;
        this.jeu = jeu;
        this.typePion = typePion;

        try {
            String imageName = (typePion == Pion.TypePion.OR)
                    ? "pion_or.png"
                    : "pion_argent.png";

            imagePion = ImageIO.read(Configuration.ouvre(imageName));
            imagePionFloue = flouterImage(imagePion);

        } catch (Exception e) {
            Configuration.debugeurErreur(
                    "Erreur lors du chargement des images : " + e.getMessage());
            System.exit(3);
        }

        setFocusable(true);
        setOpaque(false);

        // Timer pour rafraîchir l'affichage (notamment le flou quand le tour change)
        new Timer(100, e -> repaint()).start();

        addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {

                if (jeu.isPartieTerminee()
                        || jeu.isIAEnCours()
                        || jeu.estTourIA()
                        || jeu.getJoueurActuel().getTypePion() != typePion) {
                    return;
                }

                DragLayer dl = getDragLayer();
                if (dl == null)
                    return;

                for (int i = 0; i < zonesPions.size(); i++) {

                    Rectangle r = zonesPions.get(i);

                    if (r.contains(e.getPoint())) {

                        Point screen = SwingUtilities.convertPoint(
                                PionGraphique.this,
                                e.getPoint(),
                                frame);

                        dl.startDrag(
                                imagePion,
                                typePion,
                                screen);

                        dragging = true;
                        draggedIndex = i;

                        repaint();
                        return;
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                dragging = false;
                draggedIndex = -1;
                repaint();
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {

            @Override
            public void mouseDragged(MouseEvent e) {

                if (!dragging)
                    return;

                DragLayer dl = getDragLayer();

                if (dl == null)
                    return;

                Point screen = SwingUtilities.convertPoint(
                        PionGraphique.this,
                        e.getPoint(),
                        frame);

                dl.updateMouse(screen);
            }
        });
    }

    private Image flouterImage(Image img) {
        if (img == null) return null;
        int w = img.getWidth(null);
        int h = img.getHeight(null);
        if (w <= 0 || h <= 0) return img;

        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = bi.createGraphics();
        g2.drawImage(img, 0, 0, null);
        g2.dispose();

        float[] matrix = new float[9];
        for (int i = 0; i < 9; i++) matrix[i] = 1.0f / 9.0f;
        Kernel kernel = new Kernel(3, 3, matrix);
        ConvolveOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
        return op.filter(bi, null);
    }

    private DragLayer getDragLayer() {

        Component gp = frame.getGlassPane();

        if (gp instanceof DragLayer) {
            return (DragLayer) gp;
        }

        return null;
    }

    @Override
    protected void paintComponent(Graphics g) {

        super.paintComponent(g);

        // fond
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
        Graphics2D g2 = (Graphics2D) g;

        if (imagePion == null)
            return;

        int placed = 0;

        for (Pion p : jeu.getPions()) {

            if (p.getType() == typePion && p.getPosition() != null) {
                placed++;
            }
        }

        int remaining = maxPions - placed;

        int width = getWidth();
        int height = getHeight();

        int gap = 10;

        int maxPossibleSizeByHeight = (height - (maxPions + 1) * gap) / maxPions;

        int maxPossibleSizeByWidth = (int) (width * 0.35);

        int size = Math.min(
                maxPossibleSizeByHeight,
                maxPossibleSizeByWidth);

        if (size <= 0)
            size = 10;

        int totalHeight = maxPions * size + (maxPions - 1) * gap;

        int startY = (height - totalHeight) / 2;

        // OR à gauche / ARGENT à droite
        int startX = (typePion == Pion.TypePion.OR) ? 10 : width - size - 10; 

        zonesPions.clear();

        for (int i = 0; i < remaining; i++) {

            int x = startX;
            int y = startY + i * (size + gap);

            Rectangle r = new Rectangle(x, y, size, size);

            zonesPions.add(r);

            if (dragging && i == draggedIndex)
                continue;

            if (jeu.getJoueurActuel().getTypePion() == typePion) {
                g.drawImage(imagePion, x, y, size, size, null);
            } else {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
                g.drawImage(imagePionFloue, x, y, size, size, null);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            }
        }
    }
}
