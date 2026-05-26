package view;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

import global.Configuration;
import view.component.RoundedButton;

public class IntroductionPanel extends JPanel {

    private String texte = "BIENVENUE DANS LACUNA.\n\n"
            + "Dans ce tutoriel, vous allez apprendre à :\n"
            + "• Sélectionner des fleurs stratégiquement\n"
            + "• Placer des pions pour capturer des zones\n"
            + "• Et remporter un maximum de couleurs !";

    private String texteAffiche = "";

    private JLabel texteLabel;
    private final Runnable onQuit;

    public IntroductionPanel(Runnable onFinish, Runnable onQuit) {
        this.onQuit = onQuit;
        setLayout(null);
        setBackground(new Color(15, 15, 15));

        // =========================
        // AVATAR
        // =========================
        ImageIcon avatarIcon;

        try {
            avatarIcon = new ImageIcon(
                    ImageIO.read(Configuration.ouvre("avatar.png")));
        } catch (Exception ex) {
            avatarIcon = new ImageIcon();
        }

        Image image = avatarIcon.getImage().getScaledInstance(
                220,
                220,
                Image.SCALE_SMOOTH);

        ImageIcon resizedAvatar = new ImageIcon(image);

        JLabel avatarLabel = new JLabel(resizedAvatar);

        // Position avatar
        avatarLabel.setBounds(60, 300, 220, 220);

        add(avatarLabel);

        // =========================
        // BULLE DE DIALOGUE
        // =========================
        JPanel bulle = new JPanel() {

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                // Ombre plus douce
                g2.setColor(new Color(0, 0, 0, 80));
                g2.fillRoundRect(10, 10, getWidth() - 10, getHeight() - 10, 50, 50);

                // Fond blanc avec un léger dégradé
                GradientPaint gp = new GradientPaint(0, 0, Color.WHITE, 0, getHeight(), new Color(245, 245, 250));
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 12, getHeight() - 12, 50, 50));

                // Bordure discrète
                g2.setColor(new Color(200, 200, 200));
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 12, getHeight() - 12, 50, 50));

                g2.dispose();
            }
        };

        bulle.setLayout(new BorderLayout());
        bulle.setOpaque(false);

        // Position bulle
        bulle.setBounds(260, 100, 850, 380);

        // =========================
        // TEXTE
        // =========================
        texteLabel = new JLabel();
        texteLabel.setForeground(new Color(40, 40, 40));
        texteLabel.setHorizontalAlignment(SwingConstants.CENTER);
        texteLabel.setVerticalAlignment(SwingConstants.CENTER);
        
        // Ajustement des marges pour compenser l'ombre et centrer parfaitement
        texteLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 10));

        bulle.add(texteLabel, BorderLayout.CENTER);
        add(bulle);

        // =========================
        // BOUTON CONTINUER
        // =========================
        RoundedButton continuer = new RoundedButton("CONTINUER");
        continuer.setBounds(575, 520, 250, 65);
        continuer.setFont(new Font("SansSerif", Font.BOLD, 22));
        continuer.setBackground(Color.BLACK);
        continuer.setForeground(Color.WHITE);

        continuer.addActionListener(e -> {
            if (onFinish != null) {
                onFinish.run();
            }
        });

        add(continuer);

        // =========================
        // BOUTON QUITTER
        // =========================
        RoundedButton quitter = new RoundedButton("QUITTER");
        quitter.setBounds(575, 605, 250, 45);
        quitter.setFont(new Font("SansSerif", Font.BOLD, 18));
        quitter.setBackground(new Color(120, 30, 30));
        quitter.setForeground(Color.WHITE);

        quitter.addActionListener(e -> {
            if (onQuit != null) {
                onQuit.run();
            }
        });

        add(quitter);

        // =========================
        // ANIMATION TEXTE
        // =========================
        lancerAnimationTexte();
    }

    private void lancerAnimationTexte() {
        Timer timer = new Timer(20, null);
        final int[] index = { 0 };

        timer.addActionListener(e -> {
            if (index[0] < texte.length()) {
                texteAffiche += texte.charAt(index[0]);
                
                String display = texteAffiche.replace("\n", "<br>");
                
                // Formatage dynamique
                display = display.replace("LACUNA", "<span style='color: #8C00FF; font-weight: bold;'>LACUNA</span>");
                display = display.replace("fleurs", "<span style='color: #0078FF; font-weight: bold;'>fleurs</span>");
                display = display.replace("pions", "<span style='color: #FFB400; font-weight: bold;'>pions</span>");
                display = display.replace("couleurs", "<span style='color: #FF3C3C; font-weight: bold;'>couleurs</span>");
                
                // Titre plus grand
                if (display.contains("LACUNA.")) {
                    display = display.replace("BIENVENUE DANS <span style='color: #8C00FF; font-weight: bold;'>LACUNA</span>.", 
                        "<div style='font-size: 28px; margin-bottom: 15px; color: #000000;'>BIENVENUE DANS <span style='color: #8C00FF;'>LACUNA</span></div>");
                }

                // Supprimé width fixe pour laisser Swing gérer le centrage automatique via BorderLayout.CENTER
                String html = "<html><div style='text-align: center; font-family: \"Segoe UI\", \"SansSerif\"; font-size: 19px; line-height: 1.5;'>"
                        + display
                        + "</div></html>";

                texteLabel.setText(html);
                index[0]++;
            } else {
                timer.stop();
            }
        });
        timer.start();
    }
}
