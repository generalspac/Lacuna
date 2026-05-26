package view.component;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class FlexPanel extends JPanel {

    public enum Direction { ROW, COLUMN }
    public enum Justify { START, CENTER, END, SPACE_BETWEEN }
    public enum Align { START, CENTER, END, STRETCH }

    private Direction direction = Direction.ROW;
    private Justify justify = Justify.CENTER;
    private Align align = Align.STRETCH;
    private int gap = 10;

    private final Map<Component, Integer> flexGrow = new HashMap<>();

    public FlexPanel() {
        setLayout(null);
        setBackground(Color.BLACK);
    }

    // ================= CONFIG =================
    public void setDirection(Direction d) { this.direction = d; }
    public void setJustify(Justify j) { this.justify = j; }
    public void setAlign(Align a) { this.align = a; }
    public void setGap(int g) { this.gap = g; }

    //NEW: flex-grow
    public void setFlexGrow(Component c, int value) {
        flexGrow.put(c, value);
    }

    // ================= LAYOUT ENGINE =================
    @Override
    public void doLayout() {

        Component[] comps = getComponents();
        if (comps.length == 0) return;

        int width = getWidth();
        int height = getHeight();

        if (width <= 0 || height <= 0) return;

        boolean row = direction == Direction.ROW;

        int totalFixed = 0;
        int totalFlex = 0;

        int count = comps.length;

        //calcul tailles fixes + flex
        for (int i = 0; i < count; i++) {

            Component c = comps[i];
            Dimension d = c.getPreferredSize();

            int grow = flexGrow.getOrDefault(c, 0);

            if (row) {
                if (grow == 0)
                    totalFixed += d.width;
                else
                    totalFlex += grow;
            } else {
                if (grow == 0)
                    totalFixed += d.height;
                else
                    totalFlex += grow;
            }
        }

        int space = row ? width : height;

        int cursorX = 0;
        int cursorY = 0;

        //CENTER ALIGN
        int offset = (justify == Justify.CENTER)
                ? (space - totalFixed - gap * (count - 1)) / 2
                : 0;

        // Correction: offset should only be applied if there's no flex components that take up space,
        // or if we really want to center the whole block of flex components.
        // Actually, if there are flex components, they will take up all remaining space, so justify: CENTER doesn't make much sense unless totalFlex is 0.
        if (totalFlex > 0) offset = 0;

        if (row) cursorX = offset;
        else cursorY = offset;

        // ================= PLACE COMPONENTS =================
        for (int i = 0; i < count; i++) {

            Component c = comps[i];
            Dimension d = c.getPreferredSize();

            int grow = flexGrow.getOrDefault(c, 0);

            if (row) {

                int w = (grow > 0)
                        ? (width - totalFixed - gap * (count - 1)) * grow / Math.max(1, totalFlex)
                        : d.width;

                int h = (align == Align.STRETCH) ? height : d.height;
                int y = (align == Align.CENTER) ? (height - h) / 2 : 0;

                c.setBounds(cursorX, y, w, h);
                cursorX += w + gap;

            } else {

                int h = (grow > 0)
                        ? (height - totalFixed - gap * (count - 1)) * grow / Math.max(1, totalFlex)
                        : d.height;

                int w = (align == Align.STRETCH) ? width : d.width;
                int x = (align == Align.CENTER) ? (width - w) / 2 : 0;

                c.setBounds(x, cursorY, w, h);
                cursorY += h + gap;
            }
        }
    }
}