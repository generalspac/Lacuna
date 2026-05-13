package view;

import javax.swing.*;
import java.awt.*;

public class TutorialOverlay extends JPanel {

    private JLabel titleLabel;
    private JLabel messageLabel;

    private JButton nextButton;
    private JButton backButton;
    private JButton skipButton;
    private JButton backMenu;

    public TutorialOverlay() {

        setLayout(new GridBagLayout());
        setBackground(new Color(0, 0, 0, 180));
        setOpaque(false);

        JPanel box = new JPanel();
        box.setLayout(new BorderLayout(20, 20));
        box.setPreferredSize(new Dimension(700, 350));
        box.setBackground(new Color(30, 30, 30));
        box.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        // ===== TITRE =====
        titleLabel = new JLabel("Tutoriel");
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));

        // ===== TEXTE =====
        messageLabel = new JLabel();
        messageLabel.setForeground(Color.WHITE);
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // ===== BOUTONS =====
        JPanel buttons = new JPanel(new FlowLayout());
        buttons.setOpaque(false);

        backButton = new JButton("Retour");
        nextButton = new JButton("Continuer");
        skipButton = new JButton("Passer");
        backMenu = new JButton("Retour au menu");

        buttons.add(backButton);
        buttons.add(skipButton);
        buttons.add(nextButton);
        buttons.add(backMenu);

        box.add(titleLabel, BorderLayout.NORTH);
        box.add(messageLabel, BorderLayout.CENTER);
        box.add(buttons, BorderLayout.SOUTH);

        add(box);

        setVisible(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
        super.paintComponent(g);
    }

    public void showMessage(
            String title,
            String message,
            Runnable onNext,
            Runnable onBack,
            Runnable onSkip,
            Runnable onBackMenu) {
        resetButtons();
        nextButton.setVisible(true);
        skipButton.setVisible(true);
        titleLabel.setText(title);
        messageLabel.setText(message);

        // reset listeners
        for (var l : nextButton.getActionListeners())
            nextButton.removeActionListener(l);

        for (var l : backButton.getActionListeners())
            backButton.removeActionListener(l);

        for (var l : skipButton.getActionListeners())
            skipButton.removeActionListener(l);

        for (var l : backMenu.getActionListeners())
            backMenu.removeActionListener(l);

        backMenu.addActionListener(e -> {
            setVisible(false);
            if (onBackMenu != null)
                onBackMenu.run();
        });

        nextButton.addActionListener(e -> {
            setVisible(false);
            if (onNext != null)
                onNext.run();
        });

        backButton.addActionListener(e -> {
            if (onBack != null)
                onBack.run();
        });

        skipButton.addActionListener(e -> {
            setVisible(false);
            if (onSkip != null)
                onSkip.run();
        });

        setVisible(true);
    }

    public void setNextButtonText(String text) {
        nextButton.setText(text);
    }

    public void setBackButtonVisible(boolean visible) {
        backButton.setVisible(visible);
    }

    private void resetButtons() {
        backButton.setVisible(false);
        nextButton.setVisible(false);
        backMenu.setVisible(false);
        skipButton.setVisible(false);

    }

    public void setSkipButtonVisible(boolean visible) {
        skipButton.setVisible(visible);
    }
}