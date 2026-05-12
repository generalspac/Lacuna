package view;

import global.Configuration;
import model.Jeu;
import model.Pion;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class PionArgent extends JComponent {

    private Image pionArgent;
    private int maxPions = 6;
    private java.util.List<Rectangle> zonesPions = new java.util.ArrayList<>();
    private boolean dragging = false;
    private DragLayer dragLayer;
    private JFrame frame;
    private Jeu jeu;
    private int draggedIndex = -1;

    public PionArgent(JFrame frame, Jeu jeu) {
        this.frame = frame;
        this.jeu = jeu;
        
        try {
            pionArgent = ImageIO.read(Configuration.ouvre("pion_argent.png"));
        } catch (Exception e) {
            Configuration.debugeurErreur("Erreur lors du chargement des images : " + e.getMessage());
            System.exit(3);
        }
        setFocusable(true);
        setOpaque(false);
        addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                if (jeu.isPartieAbandonnee() || jeu.isIAEnCours() || jeu.estTourIA() || jeu.getJoueurActuel().getTypePion() != Pion.TypePion.ARGENT) {
                    return;
                }

                DragLayer dl = getDragLayer();
                if (dl == null) return;

                for (int i = 0; i < zonesPions.size(); i++) {
                    Rectangle r = zonesPions.get(i);
                    if (r.contains(e.getPoint())) {

                        Point screen = SwingUtilities.convertPoint(
                                PionArgent.this,
                                e.getPoint(),
                                frame);

                        dl.startDrag(
                                pionArgent,
                                Pion.TypePion.ARGENT,
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
                if (dl == null) return;

                Point screen = SwingUtilities.convertPoint(
                        PionArgent.this,
                        e.getPoint(),
                        frame);

                dl.updateMouse(screen);
            }
        });
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

        // fond noir
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());

        if (pionArgent == null)
            return;

        int placed = 0;
        for (Pion p : jeu.getPions()) {
            if (p.getType() == Pion.TypePion.ARGENT && p.getPosition() != null) {
                placed++;
            }
        }
        int remaining = maxPions - placed;

        int width = getWidth();
        int height = getHeight();

        int gap = 10;
        int maxPossibleSizeByHeight = (height - (maxPions + 1) * gap) / maxPions;
        int maxPossibleSizeByWidth = (int) (width * 0.35);

        int size = Math.min(maxPossibleSizeByHeight, maxPossibleSizeByWidth);
        if (size <= 0)
            size = 10;

        int totalHeight = maxPions * size + (maxPions - 1) * gap;
        int startY = (height - totalHeight) / 2;
        int startX = width - size - 10;

        zonesPions.clear();

        for (int i = 0; i < remaining; i++) {

            int x = startX;
            int y = startY + i * (size + gap);

            Rectangle r = new Rectangle(x, y, size, size);
            zonesPions.add(r);

            if (dragging && i == draggedIndex)
                continue;

            g.drawImage(pionArgent, x, y, size, size, null);
        }
    }
}
