package view;

import javax.swing.*;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import java.awt.*;

public class Regles extends JPanel {

    public RoundedButton retourMenu;

    public Regles() {
        setBackground(Color.BLACK);
        setLayout(new BorderLayout());

        // Titre
        JLabel titre = new JLabel("Règles du Jeu", SwingConstants.CENTER);
        titre.setFont(new Font("Arial", Font.BOLD, 48));
        titre.setForeground(Color.WHITE);
        titre.setBorder(BorderFactory.createEmptyBorder(30, 0, 30, 0));
        add(titre, BorderLayout.NORTH);

        // Zone de texte pour les règles (JEditorPane pour HTML/CSS)
        JEditorPane htmlRegles = new JEditorPane();
        htmlRegles.setContentType("text/html");
        htmlRegles.setEditable(false);
        htmlRegles.setOpaque(false); // Pour voir le fond du JPanel
        
        // Configuration du style
        HTMLEditorKit kit = new HTMLEditorKit();
        htmlRegles.setEditorKit(kit);
        
        StyleSheet styleSheet = kit.getStyleSheet();
        styleSheet.addRule("body { color: #ffffff; font-family: 'Segoe UI', Arial, sans-serif; padding: 20px 60px; }");
        styleSheet.addRule("h2 { color: #00ffff; font-size: 26px; margin-top: 25px; border-bottom: 2px solid #00ffff; padding-bottom: 5px; }");
        styleSheet.addRule("p { font-size: 18px; line-height: 1.6; margin-bottom: 15px; }");
        styleSheet.addRule("ul { margin-left: 30px; }");
        styleSheet.addRule("li { font-size: 18px; margin-bottom: 12px; color: #e0e0e0; }");
        styleSheet.addRule("b { color: #ffd700; }"); // Accentuation en or
        styleSheet.addRule(".section { margin-bottom: 30px; }");

        String htmlContent = "<html><body>" +
            "<div class='section'>" +
            "<h2>But du jeu</h2>" +
            "<p>Le but de <b>Lacuna</b> est de capturer plus de fleurs que votre adversaire. La partie se déroule en deux phases distinctes où chaque décision compte.</p>" +
            "</div>" +
            
            "<div class='section'>" +
            "<h2>Phase 1 : Placement des pions</h2>" +
            "<ul>" +
            "<li>Les joueurs placent à tour de rôle un pion sur le plateau.</li>" +
            "<li>Un pion doit être placé sur une <b>ligne imaginaire</b> entre deux fleurs du <b>même type</b>.</li>" +
            "<li>Vous capturez alors immédiatement ces deux fleurs.</li>" +
            "<li><i>Astuce : Essayez de bloquer les lignes de votre adversaire tout en préparant les vôtres !</i></li>" +
            "</ul>" +
            "</div>" +
            
            "<div class='section'>" +
            "<h2>Phase 2 : Attribution des fleurs restantes</h2>" +
            "<ul>" +
            "<li>Une fois tous les pions placés, les fleurs restantes ne sont pas perdues !</li>" +
            "<li>Elles sont attribuées au joueur ayant le <b>pion le plus proche</b> de chaque fleur.</li>" +
            "<li>Cette phase est cruciale et peut totalement renverser l'issue du match.</li>" +
            "</ul>" +
            "</div>" +
            
            "<div class='section'>" +
            "<h2>Condition de Victoire</h2>" +
            "<p>Le gagnant est déterminé par la collection de fleurs :</p>" +
            "<ul>" +
            "<li>Le joueur qui possède la <b>majorité des fleurs</b> d'un type gagne ce type de fleur.</li>" +
            "<li>Le joueur qui gagne le <b>plus grand nombre de types de fleurs</b> différents gagne la partie.</li>" +
            "</ul>" +
            "</div>" +
            "</body></html>";

        htmlRegles.setText(htmlContent);

        JScrollPane scrollPane = new JScrollPane(htmlRegles);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getViewport().setBackground(Color.BLACK);
        add(scrollPane, BorderLayout.CENTER);

        // Bouton retour
        retourMenu = new RoundedButton("Retour au Menu");
        retourMenu.setBackground(new Color(18, 18, 18));
        retourMenu.setForeground(Color.WHITE);
        retourMenu.setPreferredSize(new Dimension(280, 60));
        retourMenu.setFont(new Font("Arial", Font.BOLD, 22));
        
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(Color.BLACK);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(30, 0, 40, 0));
        bottomPanel.add(retourMenu);
        add(bottomPanel, BorderLayout.SOUTH);
    }
}
