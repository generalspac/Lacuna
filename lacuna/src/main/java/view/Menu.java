package view;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.*;

import global.Configuration;

public class Menu extends JPanel {

    public RoundedButton nouvellePartie;
    public RoundedButton chargerPartie;
    public RoundedButton leaderBoard;
    public RoundedButton regles;
    public RoundedButton tutoriel;
    public RoundedButton multijoueurReseau;

    private Image backgroundImage;

    public Menu() {

        try {
            backgroundImage = ImageIO.read(Configuration.ouvre("image.png"));
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'image de fond : " + e.getMessage());
        }

        setBackground(Color.white);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        ////LacunaGradientLabel title = new LacunaGradientLabel("LACUNA");
        ////title.setAlignmentX(Component.CENTER_ALIGNMENT);
        ////title.setPreferredSize(new Dimension(800, 180));
        ////title.setMaximumSize(new Dimension(800, 180));
        ////title.setMinimumSize(new Dimension(800, 180));

        // =========================
        // Styles
        // =========================
        nouvellePartie = new RoundedButton("Nouvelle Partie");
        chargerPartie = new RoundedButton("Charger Partie");
        leaderBoard = new RoundedButton("LeaderBoard");
        regles = new RoundedButton("Règles du jeu");
        tutoriel = new RoundedButton("Tutoriel");
        multijoueurReseau = new RoundedButton("Multijoueur Réseau");

        RoundedButton[] buttons = {
                nouvellePartie,
                chargerPartie,
                tutoriel,
                leaderBoard,
                regles,
                multijoueurReseau
        };

        for (RoundedButton b : buttons) {
            b.setAlignmentX(Component.CENTER_ALIGNMENT);

            // ===== TES STYLES CONSERVÉS =====
            b.setBackground(Color.DARK_GRAY);
            b.setForeground(Color.WHITE);
            b.setMargin(new Insets(15, 30, 15, 30));
            b.setFont(new Font("Arial", Font.BOLD, 25));
            b.setMaximumSize(new Dimension(300, 5));

            // ===== CORRECTION IMPORTANTE =====
            // ne PAS laisser Swing casser ton custom painting
            b.setOpaque(false);
            b.setContentAreaFilled(false);
            b.setBorderPainted(false);
            b.setFocusPainted(false);
        }

        // =========================
        // ACTION COMMANDS
        // =========================
        nouvellePartie.setActionCommand("nouvellePartie");
        chargerPartie.setActionCommand("chargerPartie");
        leaderBoard.setActionCommand("leaderBoard");
        regles.setActionCommand("regles");
        tutoriel.setActionCommand("tutoriel");
        multijoueurReseau.setActionCommand("multijoueurReseau");

        // =========================
        // LAYOUT (IDENTIQUE)
        // =========================
        add(Box.createVerticalStrut(80));
        //add(title);
        add(Box.createVerticalStrut(80));

        add(Box.createVerticalGlue());
        add(nouvellePartie);
        add(Box.createRigidArea(new Dimension(0, 1)));
        add(chargerPartie);
        add(Box.createRigidArea(new Dimension(0, 1)));
        add(tutoriel);
        add(Box.createRigidArea(new Dimension(0, 1)));
        add(leaderBoard);
        add(Box.createRigidArea(new Dimension(0, 1)));
        add(regles);
        add(Box.createRigidArea(new Dimension(0, 1)));
        add(multijoueurReseau);
        add(Box.createVerticalGlue());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }

        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(new Color(0, 0, 0, 100));
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }
}