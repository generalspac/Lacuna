package view;

import model.Types;
import global.Configuration;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;

import controller.ControleurBoutons;
import controller.EcouteurDeClavier;

import model.IA.NiveauxIA;

public class InterfaceGraphique implements Runnable {

    JFrame frame;
    boolean maximized = false;

    @Override
    public void run() {

        frame = new JFrame("LACUNA");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        DragLayer dragLayer = new DragLayer();
        dragLayer.setOpaque(false);

        frame.setGlassPane(dragLayer);
        frame.getGlassPane().setVisible(true);
        dragLayer.setVisible(true);

       

        // ================= ROOT FLEX (VERTICAL) =================
        FlexPanel root = new FlexPanel();
        root.setDirection(FlexPanel.Direction.COLUMN);
        root.setJustify(FlexPanel.Justify.START);
        root.setAlign(FlexPanel.Align.STRETCH);
        root.setBackground(Color.BLACK);

        // ================= ZONE SCORE (TOP - CENTRÉE) =================
        FlexPanel scoreHeader = new FlexPanel();
        scoreHeader.setDirection(FlexPanel.Direction.ROW);
        scoreHeader.setJustify(FlexPanel.Justify.CENTER);
        scoreHeader.setAlign(FlexPanel.Align.CENTER);
        scoreHeader.setPreferredSize(new Dimension(0, 200));
        scoreHeader.setGap(100);

        Score scoreArgent = new Score(Types.TypePion.ARGENT);
        scoreArgent.setPreferredSize(new Dimension(250, 320));

        Score scoreOr = new Score(Types.TypePion.OR);
        scoreOr.setPreferredSize(new Dimension(250, 320));

        JLabel iconArgent;
        JLabel iconOr;
        try {
            Image imgArgent = ImageIO.read(Configuration.ouvre("pion_argent.png"));
            Image scaledArgent = imgArgent.getScaledInstance(40, 40, Image.SCALE_SMOOTH);
            iconArgent = new JLabel(new ImageIcon(scaledArgent));

            Image imgOr = ImageIO.read(Configuration.ouvre("pion_or.png"));
            Image scaledOr = imgOr.getScaledInstance(40, 40, Image.SCALE_SMOOTH);
            iconOr = new JLabel(new ImageIcon(scaledOr));

        } catch (IOException e) {
            e.printStackTrace();
            iconArgent = new JLabel();
            iconOr = new JLabel();
        }

        JLabel scoreLabel = new JLabel("0 - 0", SwingConstants.CENTER);
        scoreLabel.setForeground(Color.WHITE);
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 40));

        // panel comme un score de foot
        JPanel scorePanel = new JPanel();
        scorePanel.setOpaque(false);

        scorePanel.add(iconArgent);
        scorePanel.add(scoreLabel);
        scorePanel.add(iconOr);
        scoreHeader.add(scoreArgent);
        scoreHeader.add(scorePanel);
        scoreHeader.add(scoreOr);

        // ================= MESSAGE D'INSTRUCTION =================
        JLabel instructionLabel = new JLabel("Avantage", SwingConstants.CENTER);
        instructionLabel.setForeground(Color.WHITE);
        instructionLabel.setFont(new Font("Arial", Font.BOLD, 24));
        instructionLabel.setPreferredSize(new Dimension(0, 50));
        instructionLabel.setBackground(Color.BLACK);
        instructionLabel.setOpaque(true);

        // ================= MESSAGE DE STATUT (Feedback)=================
        JLabel statusLabel = new JLabel("Bienvenue dans le jeu !", SwingConstants.CENTER);
        statusLabel.setForeground(Color.LIGHT_GRAY);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        statusLabel.setPreferredSize(new Dimension(0, 35));
        statusLabel.setBackground(Color.BLACK);
        statusLabel.setOpaque(true);

        // ================= ZONE JEU (ROW) =================
        FlexPanel gameRow = new FlexPanel();
        gameRow.setDirection(FlexPanel.Direction.ROW);
        gameRow.setJustify(FlexPanel.Justify.CENTER);
        gameRow.setAlign(FlexPanel.Align.STRETCH);
        gameRow.setBackground(Color.BLACK);

        JeuGraphique aire = new JeuGraphique(frame, scoreArgent, scoreOr, scoreLabel, instructionLabel, statusLabel);
        aire.setBackground(Color.BLACK);
        aire.setOpaque(true);
        
        // ==================== OPTIONS DE JEU =========================== //
        String[] options = {
            "Humain vs Humain",
            "IA Facile",
            "IA Moyenne",
            "IA Difficile"
            };

        String choix = (String) JOptionPane.showInputDialog(
            frame,
            "Choisissez un mode",
            "Lacuna",
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]);
            
        if (choix != null) {

            switch (choix) {

                case "IA Facile":
                    aire.jeu.activerIA(NiveauxIA.FACILE);
                    break;

                case "IA Moyenne":
                    aire.jeu.activerIA(NiveauxIA.MOYEN);
                    break;

                case "IA Difficile":
                    aire.jeu.activerIA(NiveauxIA.DIFFICILE);
                    break;

                default:
                    break;
            }
        }

        ///////////////////////////////////

        

        // Mettre à jour le statusLabel avec le joueur qui commence
        statusLabel.setText("Bienvenue ! " + aire.jeu.getJoueurActuel().getTypePion() + " commence.");
        instructionLabel.setText("Phase d'avantage : Joeur " + aire.jeu.getJoueurActuel().getTypePion() + " doit choisir une fleur");   


        // ================= BOUTONS UNDO/REDO/RESET =================
        FlexPanel buttonPanel = new FlexPanel();
        buttonPanel.setDirection(FlexPanel.Direction.ROW);
        buttonPanel.setJustify(FlexPanel.Justify.CENTER);
        buttonPanel.setAlign(FlexPanel.Align.CENTER);
        buttonPanel.setGap(20);
        buttonPanel.setPreferredSize(new Dimension(0, 40));
        buttonPanel.setBackground(Color.BLACK);


        ControleurBoutons controleurBoutons = new ControleurBoutons(aire);


        ////////// Pour le clavier//////////////
        aire.addKeyListener(new EcouteurDeClavier(controleurBoutons));
        aire.setFocusable(true);
        ///////////////////////////////////////////////////

       
        JButton undoButton = new JButton("Undo");
        undoButton.setPreferredSize(new Dimension(80, 40));
        undoButton.addActionListener(e -> controleurBoutons.undo());

        JButton redoButton = new JButton("Redo");
        redoButton.setPreferredSize(new Dimension(80, 40));
        redoButton.addActionListener(e -> controleurBoutons.redo());

        JButton resetButton = new JButton("Reset");
        resetButton.setPreferredSize(new Dimension(80, 40));
        resetButton.addActionListener(e -> controleurBoutons.reset());

        buttonPanel.add(undoButton);
        buttonPanel.add(redoButton);
        buttonPanel.add(resetButton);

        // Bouton sauvegarder
        JButton sauvegarderButton = new JButton("Save");
        sauvegarderButton.setPreferredSize(new Dimension(120, 40));
        sauvegarderButton.addActionListener(e -> aire.jeu.sauvegarderPartie("sauvegarde.txt"));

        // Bouton charger
        JButton chargerButton = new JButton("Load");
        chargerButton.setPreferredSize(new Dimension(120, 40));
        chargerButton.addActionListener(e -> {
            aire.jeu.chargerPartie("sauvegarde.txt");
            aire.refreshScores();
            aire.repaint();
        });

        buttonPanel.add(sauvegarderButton);
        buttonPanel.add(chargerButton);

        PionArgent leftPile = new PionArgent(frame, aire.jeu);
        leftPile.setPreferredSize(new Dimension(80, 600));
        leftPile.setBackground(Color.BLACK);

        PionOr rightPile = new PionOr(frame, aire.jeu);
        rightPile.setPreferredSize(new Dimension(80, 600));
        rightPile.setBackground(Color.BLACK);

        aire.setSidePiles(leftPile, rightPile);

        JPanel s1 = new JPanel();
        s1.setBackground(Color.BLACK);
        JPanel s2 = new JPanel();
        s2.setBackground(Color.BLACK);

        gameRow.add(s1);
        gameRow.setFlexGrow(s1, 1);
        gameRow.add(leftPile);
        gameRow.setFlexGrow(leftPile, 1);
        gameRow.add(aire);
        gameRow.setFlexGrow(aire, 4);
        gameRow.add(rightPile);
        gameRow.setFlexGrow(rightPile, 1);
        gameRow.add(s2);
        gameRow.setFlexGrow(s2, 1);

        // ================= ASSEMBLAGE FINAL =================
        root.add(scoreHeader);
        root.setFlexGrow(scoreHeader, 0);
        root.add(instructionLabel);
        root.setFlexGrow(instructionLabel, 0);
        root.add(statusLabel);
        root.setFlexGrow(statusLabel, 0);
        root.add(gameRow);
        root.setFlexGrow(gameRow, 1);
        root.add(buttonPanel);
        root.setFlexGrow(buttonPanel, 0);

        frame.add(root);

        // ================= FRAME =================
        frame.setSize(1400, 1000);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        aire.requestFocusInWindow();
    }

    public void toggleFullscreen() {
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice device = env.getDefaultScreenDevice();

        if (maximized) {
            device.setFullScreenWindow(null);
            maximized = false;
        } else {
            device.setFullScreenWindow(frame);
            maximized = true;
        }
    }
}
