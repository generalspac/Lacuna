package view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Graphics2D;
import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import global.Configuration;

public class Menu extends JPanel {

    public RoundedButton nouvellePartie;
    public RoundedButton chargerPartie;
    public RoundedButton leaderBoard;
    public RoundedButton regles;
    private Image backgroundImage;

    public Menu() {
        try {
            backgroundImage = ImageIO.read(Configuration.ouvre("menu_back.png"));
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'image de fond : " + e.getMessage());
        }

        LacunaGradientLabel title = new LacunaGradientLabel("LACUNA");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setPreferredSize(new Dimension(800, 180));
        title.setMaximumSize(new Dimension(800, 180));
        title.setMinimumSize(new Dimension(800, 180));
        setBackground(Color.white);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        nouvellePartie = new RoundedButton("Nouvelle Partie");
        chargerPartie = new RoundedButton("Charger Partie");
        leaderBoard = new RoundedButton("LeaderBoard");
        regles = new RoundedButton("Règles du jeu");

        nouvellePartie.setAlignmentX(Component.CENTER_ALIGNMENT);
        chargerPartie.setAlignmentX(Component.CENTER_ALIGNMENT);
        leaderBoard.setAlignmentX(Component.CENTER_ALIGNMENT);
        regles.setAlignmentX(Component.CENTER_ALIGNMENT);

        nouvellePartie.setBackground(new Color(18, 18, 18));
        nouvellePartie.setForeground(Color.WHITE);
        nouvellePartie.setOpaque(false);
        nouvellePartie.setFocusPainted(false);
        nouvellePartie.setBorderPainted(true);
        nouvellePartie.setMargin(new Insets(15, 30, 15, 30));
        nouvellePartie.setFont(new Font("Arial", Font.BOLD, 25));
        nouvellePartie.setContentAreaFilled(false);

        chargerPartie.setBackground(new Color(18, 18, 18));
        chargerPartie.setForeground(Color.WHITE);
        chargerPartie.setOpaque(false);
        chargerPartie.setFocusPainted(false);
        chargerPartie.setBorderPainted(true);
        chargerPartie.setMargin(new Insets(15, 30, 15, 30));
        chargerPartie.setFont(new Font("Arial", Font.BOLD, 25));

        leaderBoard.setBackground(new Color(18, 18, 18));
        leaderBoard.setForeground(Color.WHITE);
        leaderBoard.setOpaque(false);
        leaderBoard.setFocusPainted(false);
        leaderBoard.setBorderPainted(true);
        leaderBoard.setMargin(new Insets(15, 30, 15, 30));
        leaderBoard.setFont(new Font("Arial", Font.BOLD, 25));

        regles.setBackground(new Color(18, 18, 18));
        regles.setForeground(Color.WHITE);
        regles.setOpaque(false);
        regles.setFocusPainted(false);
        regles.setBorderPainted(true);
        regles.setMargin(new Insets(15, 30, 15, 30));
        regles.setFont(new Font("Arial", Font.BOLD, 25));

        nouvellePartie.setMaximumSize(new Dimension(400, 450));
        chargerPartie.setMaximumSize(new Dimension(400, 450));
        leaderBoard.setMaximumSize(new Dimension(400, 450));
        regles.setMaximumSize(new Dimension(400, 450));

        nouvellePartie.setActionCommand("nouvellePartie");
        chargerPartie.setActionCommand("chargerPartie");
        leaderBoard.setActionCommand("leaderBoard");
        regles.setActionCommand("regles");
        add(Box.createVerticalStrut(80));
        add(title);
        add(Box.createVerticalStrut(80));

        add(Box.createVerticalGlue());
        add(nouvellePartie);
        add(Box.createRigidArea(new Dimension(0, 30)));
        add(chargerPartie);
        add(Box.createRigidArea(new Dimension(0, 30)));
        add(leaderBoard);
        add(Box.createRigidArea(new Dimension(0, 30)));
        add(regles);
        add(Box.createVerticalGlue());

    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // Dessine l'image sur toute la fenêtre

        g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(new Color(0, 0, 0, 100));
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }
}
