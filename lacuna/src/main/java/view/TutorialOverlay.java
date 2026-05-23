package view;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import global.Configuration;

public class TutorialOverlay extends JPanel {

    private JLabel titleLabel;
    private JLabel messageLabel;
    private JLabel avatarLabel;

    private RoundedButton nextButton;
    private RoundedButton backButton;
    private RoundedButton skipButton;
    private RoundedButton backMenu;

    public TutorialOverlay() {

        setLayout(null);
        setBackground(new Color(0, 0, 0, 160));
        setOpaque(false);

        // =========================
        // BOITE DE DIALOGUE
        // =========================
        JPanel box = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Fond sombre translucide
                g2.setColor(new Color(25, 25, 25, 240));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 40, 40));
                
                // Bordure fine
                g2.setColor(new Color(80, 80, 80));
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 40, 40));
                
                g2.dispose();
            }
        };
        box.setLayout(null);
        box.setOpaque(false);
        box.setBounds(200, 150, 950, 400);

        // =========================
        // AVATAR
        // =========================
        try {
            Image img = ImageIO.read(Configuration.ouvre("avatar.png"));
            Image scaled = img.getScaledInstance(180, 180, Image.SCALE_SMOOTH);
            avatarLabel = new JLabel(new ImageIcon(scaled));
        } catch (Exception ex) {
            avatarLabel = new JLabel();
        }
        avatarLabel.setBounds(30, 80, 180, 180);
        box.add(avatarLabel);

        // =========================
        // TITRE
        // =========================
        titleLabel = new JLabel("Tutoriel");
        titleLabel.setBounds(240, 30, 630, 40);
        titleLabel.setForeground(new Color(140, 0, 255)); // Violet Lacuna
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        box.add(titleLabel);

        // =========================
        // MESSAGE (HTML)
        // =========================
        messageLabel = new JLabel();
        messageLabel.setBounds(240, 80, 630, 200);
        messageLabel.setForeground(Color.WHITE);
        messageLabel.setVerticalAlignment(SwingConstants.TOP);
        box.add(messageLabel);

        // =========================
        // BOUTONS
        // =========================
        backButton = new RoundedButton("Retour");
        skipButton = new RoundedButton("Passer");
        nextButton = new RoundedButton("Continuer");
        backMenu = new RoundedButton("Quitter");

        int btnW = 160;
        int btnH = 45;
        int startX = 240;
        int yPos = 310;
        int gap = 15;

        backButton.setBounds(startX, yPos, btnW, btnH);
        skipButton.setBounds(startX + (btnW + gap), yPos, btnW, btnH);
        nextButton.setBounds(startX + 2 * (btnW + gap), yPos, btnW, btnH);
        backMenu.setBounds(startX + 3 * (btnW + gap), yPos, btnW, btnH);

        // Style boutons
        RoundedButton[] btns = {backButton, skipButton, nextButton, backMenu};
        for (RoundedButton b : btns) {
            b.setFont(new Font("SansSerif", Font.BOLD, 16));
            b.setBackground(new Color(45, 45, 45));
            b.setForeground(Color.WHITE);
            box.add(b);
        }
        nextButton.setBackground(new Color(140, 0, 255)); // Accentuer le bouton Suivant

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
        if (onBackMenu != null) backMenu.setVisible(true);
        
        titleLabel.setText(title.toUpperCase());
        
        // Formatage HTML automatique pour le message
        String htmlMessage = "<html><div style='font-family: \"SansSerif\"; font-size: 16px; line-height: 1.4;'>"
                + message.replace("\n", "<br>")
                + "</div></html>";
        messageLabel.setText(htmlMessage);

        // Clear listeners
        for (var l : nextButton.getActionListeners()) nextButton.removeActionListener(l);
        for (var l : backButton.getActionListeners()) backButton.removeActionListener(l);
        for (var l : skipButton.getActionListeners()) skipButton.removeActionListener(l);
        for (var l : backMenu.getActionListeners()) backMenu.removeActionListener(l);

        nextButton.addActionListener(e -> { setVisible(false); if (onNext != null) onNext.run(); });
        backButton.addActionListener(e -> { if (onBack != null) onBack.run(); });
        skipButton.addActionListener(e -> { setVisible(false); if (onSkip != null) onSkip.run(); });
        backMenu.addActionListener(e -> { setVisible(false); if (onBackMenu != null) onBackMenu.run(); });

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
