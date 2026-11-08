package view.animations;

import java.awt.event.ActionListener;

import javax.swing.Timer;

/**
 * Utilitaires centralisés pour la création des Timers Swing.
 */
public final class SwingTimers {

    private SwingTimers() {}

    /**
     * Lance un Timer exécuté une seule fois.
     */
    public static void startSingle(int delay, ActionListener action) {

        Timer timer = new Timer(delay, null);

        timer.addActionListener(e -> {
            timer.stop();
            action.actionPerformed(e);
        });

        timer.start();
    }

    /**
     * Lance un Timer répétitif.
     */
    public static Timer startRepeating(int delay, ActionListener action) {

        Timer timer = new Timer(delay, action);

        timer.start();

        return timer;
    }
}