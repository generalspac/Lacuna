package view.animations;

import javax.swing.Timer;

public final class SwingTimers {

    private SwingTimers() {}

    public static void after(int delayMs, Runnable action) {

        Timer timer = new Timer(delayMs, e -> {
            ((Timer) e.getSource()).stop();

            if (action != null) {
                action.run();
            }
        });

        timer.setRepeats(false);
        timer.start();
    }
}