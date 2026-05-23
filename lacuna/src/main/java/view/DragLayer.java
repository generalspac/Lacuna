package view;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DragLayer extends JComponent {

    private Image draggedImage;
    private Point mouse;
    private Object payload;

    private static class SpriteAnimation {
        Image image;
        Point start;
        Point end;
        long startTime;
        int durationMs;
        int size;
        Runnable onDone;

        SpriteAnimation(Image image, Point start, Point end, int durationMs, int size, Runnable onDone) {
            this.image = image;
            this.start = start;
            this.end = end;
            this.durationMs = durationMs;
            this.size = size;
            this.onDone = onDone;
            this.startTime = System.currentTimeMillis();
        }
    }

    private final List<SpriteAnimation> animations = new ArrayList<>();
    private Timer animationTimer;

    public void startDrag(Image img, Object payload, Point p) {
        this.draggedImage = img;
        this.payload = payload;
        this.mouse = p;
        setVisible(true);
        repaint();
    }

    public void updateMouse(Point p) {
        this.mouse = p;
        repaint();
    }

    public Object getPayload() {
        return payload;
    }

    public void clear() {
        draggedImage = null;
        mouse = null;
        payload = null;
        repaint();
    }

    public void cancelAnimations() {
        animations.clear();
        if (animationTimer != null) {
            animationTimer.stop();
        }
        clear();
    }

    public boolean hasAnimations() {
        return !animations.isEmpty();
    }

    public void animateImage(Image img, Point start, Point end, int durationMs, int size, Runnable onDone) {
        if (img == null || start == null || end == null) {
            if (onDone != null) onDone.run();
            return;
        }

        animations.add(new SpriteAnimation(img, start, end, durationMs, size, onDone));
        setVisible(true);

        if (animationTimer == null) {
            animationTimer = new Timer(16, e -> tickAnimations());
        }
        if (!animationTimer.isRunning()) {
            animationTimer.start();
        }
        repaint();
    }

    private void tickAnimations() {
        long now = System.currentTimeMillis();
        Iterator<SpriteAnimation> it = animations.iterator();
        while (it.hasNext()) {
            SpriteAnimation a = it.next();
            if (now - a.startTime >= a.durationMs) {
                it.remove();
                if (a.onDone != null) {
                    SwingUtilities.invokeLater(a.onDone);
                }
            }
        }

        if (animations.isEmpty() && animationTimer != null) {
            animationTimer.stop();
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        long now = System.currentTimeMillis();
        for (SpriteAnimation a : animations) {
            double t = Math.min(1.0, (now - a.startTime) / (double) a.durationMs);
            // ralentissement pour l'animation
            double eased = 1.0 - Math.pow(1.0 - t, 3);
            int x = (int) (a.start.x + (a.end.x - a.start.x) * eased);
            int y = (int) (a.start.y + (a.end.y - a.start.y) * eased);
            g2.drawImage(a.image, x - a.size / 2, y - a.size / 2, a.size, a.size, null);
        }

        if (draggedImage == null || mouse == null) return;

        int size = 40;
        g2.drawImage(draggedImage,
                mouse.x - size / 2,
                mouse.y - size / 2,
                size, size, null);
    }
}
