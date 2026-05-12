package view;

import javax.swing.*;
import java.awt.*;

public class Regles extends JPanel {

    public RoundedButton retourMenu;

    public Regles() {
        setBackground(Color.BLACK);
        setLayout(new BorderLayout());

        // Titre
        JLabel titre = new JLabel("Règles du Jeu", SwingConstants.CENTER);
        titre.setFont(new Font("Arial", Font.BOLD, 36));
        titre.setForeground(Color.WHITE);
        titre.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(titre, BorderLayout.NORTH);

        // Zone de texte pour les règles
        JTextArea texteRegles = new JTextArea();
        texteRegles.setBackground(Color.BLACK);
        texteRegles.setForeground(Color.WHITE);
        texteRegles.setFont(new Font("Arial", Font.PLAIN, 18));
        texteRegles.setEditable(false);
        texteRegles.setLineWrap(true);
        texteRegles.setWrapStyleWord(true);
        texteRegles.setMargin(new Insets(20, 50, 20, 50));

        texteRegles.setText(
            "Bienvenue dans Lacuna !\n\n" +
            "But du jeu :\n" +
            "Le but est de capturer plus de fleurs que votre adversaire.\n\n" +
            "Phase 1 : Placement des pions\n" +
            "- Les joueurs placent à tour de rôle un pion sur le plateau.\n" +
            "- Un pion doit être placé sur une ligne imaginaire entre deux fleurs du même type.\n" +
            "- Vous capturez alors ces deux fleurs.\n\n" +
            "Phase 2 : Attribution des fleurs restantes\n" +
            "- Une fois tous les pions placés, les fleurs restantes sont attribuées au joueur ayant le pion le plus proche.\n\n" +
            "Victoire :\n" +
            "- Le joueur qui possède la majorité des fleurs dans le plus grand nombre de types de fleurs gagne la partie."
        );

        JScrollPane scrollPane = new JScrollPane(texteRegles);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Color.BLACK);
        add(scrollPane, BorderLayout.CENTER);

        // Bouton retour
        retourMenu = new RoundedButton("Retour au Menu");
        retourMenu.setBackground(new Color(18, 18, 18));
        retourMenu.setForeground(Color.WHITE);
        retourMenu.setPreferredSize(new Dimension(200, 50));
        
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(Color.BLACK);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        bottomPanel.add(retourMenu);
        add(bottomPanel, BorderLayout.SOUTH);
    }
}
