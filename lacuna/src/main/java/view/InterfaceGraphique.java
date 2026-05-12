package view;

import model.Types;
import global.Configuration;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

import controller.ControleurBoutons;
import controller.EcouteurDeClavier;

import model.IA.NiveauxIA;

public class InterfaceGraphique implements Runnable {

    JFrame frame;
    boolean maximized = false;
    private Menu menu;

    @Override
    public void run() {
        frame = new JFrame("LACUNA");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1400, 1000);
        frame.setLocationRelativeTo(null);

        afficherMenu();

        frame.setVisible(true);
    }

    public void afficherMenu() {
        menu = new Menu();
        menu.nouvellePartie.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                lancerJeu();
            }
        });

        // Navigation vers les règles
        menu.regles.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                afficherRegles();
            }
        });

        frame.setContentPane(menu);
        frame.revalidate();
        frame.repaint();
    }

    public void afficherRegles() {
        Regles reglesView = new Regles();
        reglesView.retourMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                afficherMenu();
            }
        });

        frame.setContentPane(reglesView);
        frame.revalidate();
        frame.repaint();
    }

    public void lancerJeu() {

        JPanel panel = new JPanel();
        panel.setBackground(Color.BLACK);
        panel.setLayout(new GridLayout(5, 1, 15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Choisissez un mode");
        title.setForeground(Color.WHITE);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 26));

        String[] options = {
                "Humain vs Humain",
                "IA Facile",
                "IA Moyenne",
                "IA Difficile"
        };

        JComboBox<String> combo = new JComboBox<>(options);
        combo.setFont(new Font("Arial", Font.PLAIN, 20));
        combo.setPreferredSize(new Dimension(300, 50));

        panel.add(title);
        panel.add(combo);

        JOptionPane optionPane = new JOptionPane(
                panel,
                JOptionPane.PLAIN_MESSAGE,
                JOptionPane.OK_CANCEL_OPTION);

        JDialog dialog = optionPane.createDialog(frame, "LACUNA");

        dialog.setSize(500, 300);
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);

        Object selectedValue = optionPane.getInputValue();
        String choix = (String) combo.getSelectedItem();

        if (choix == null) {
            return; // Annulé, on reste sur le menu
        }

        // Afficher un écran de chargement temporaire

        JPanel loadingPanel = new JPanel(new BorderLayout());
        loadingPanel.setBackground(Color.BLACK);

        // ================= SPINNER =================
        JPanel spinner = new JPanel() {

            double angle = 0;

            {
                setOpaque(false);

                new javax.swing.Timer(16, e -> {
                    angle += 0.25;
                    repaint();
                }).start();
            }

            @Override
            protected void paintComponent(Graphics g) {

                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                int size = 50;

                int x = getWidth() / 2 - size / 2;
                int y = getHeight() / 2 - size / 2;

                // cercle de fond
                g2.setStroke(new BasicStroke(5f));
                g2.setColor(new Color(255, 255, 255, 40));
                g2.drawOval(x, y, size, size);

                // arc animé
                g2.setColor(Color.WHITE);

                int start = (int) (angle * 40) % 360;

                g2.drawArc(x, y, size, size, start, 90);

                g2.dispose();
            }
        };

        spinner.setPreferredSize(new Dimension(100, 100));

        // ================= TEXTE =================
        JLabel loadingLabel = new JLabel(
                "Chargement du plateau...",
                SwingConstants.CENTER);

        loadingLabel.setForeground(Color.WHITE);
        loadingLabel.setFont(
                new Font("Arial", Font.ITALIC, 24));

        // ================= PANEL CENTRAL =================
        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        spinner.setAlignmentX(Component.CENTER_ALIGNMENT);
        loadingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        center.add(Box.createVerticalGlue());
        center.add(spinner);
        center.add(Box.createRigidArea(new Dimension(0, 20)));
        center.add(loadingLabel);
        center.add(Box.createVerticalGlue());

        // ================= ASSEMBLAGE =================
        loadingPanel.add(center, BorderLayout.CENTER);

        frame.setContentPane(loadingPanel);
        frame.revalidate();
        frame.repaint();

        // Utiliser un SwingWorker pour ne pas bloquer l'EDT pendant l'initialisation
        SwingWorker<FlexPanel, Void> worker = new SwingWorker<>() {
            JeuGraphique aire;

            @Override
            protected FlexPanel doInBackground() throws Exception {
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

                JLabel scoreLabel = new JLabel("0 - 0", SwingConstants.CENTER);
                scoreLabel.setForeground(Color.WHITE);
                scoreLabel.setFont(new Font("Arial", Font.BOLD, 40));

                scoreHeader.add(scoreArgent);
                scoreHeader.add(scoreLabel);
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

                aire = new JeuGraphique(frame, scoreArgent, scoreOr, scoreLabel, instructionLabel, statusLabel);
                aire.setBackground(Color.BLACK);
                aire.setOpaque(true);

                // Appliquer le choix du mode
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

                // ================= BOUTONS UNDO/REDO/RESET =================
                FlexPanel buttonPanel = new FlexPanel();
                buttonPanel.setDirection(FlexPanel.Direction.ROW);
                buttonPanel.setJustify(FlexPanel.Justify.CENTER);
                buttonPanel.setAlign(FlexPanel.Align.CENTER);
                buttonPanel.setGap(20);
                buttonPanel.setPreferredSize(new Dimension(0, 40));
                buttonPanel.setBackground(Color.BLACK);

                ControleurBoutons controleurBoutons = new ControleurBoutons(aire);
                aire.addKeyListener(new EcouteurDeClavier(controleurBoutons));
                aire.setFocusable(true);

                JButton undoButton = new JButton("Undo");
                undoButton.addActionListener(e -> controleurBoutons.undo());

                JButton redoButton = new JButton("Redo");
                redoButton.addActionListener(e -> controleurBoutons.redo());

                JButton resetButton = new JButton("Reset");
                resetButton.addActionListener(e -> controleurBoutons.reset());

                buttonPanel.add(undoButton);
                buttonPanel.add(redoButton);
                buttonPanel.add(resetButton);

                // Bouton sauvegarder
                JButton sauvegarderButton = new JButton("Save");
                sauvegarderButton.addActionListener(e -> aire.jeu.sauvegarderPartie("sauvegarde.txt"));

                // Bouton charger
                JButton chargerButton = new JButton("Load");
                chargerButton.addActionListener(e -> {
                    aire.jeu.chargerPartie("sauvegarde.txt");
                    aire.refreshScores();
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

                return root;
            }

            @Override
            protected void done() {
                try {
                    FlexPanel root = get();
                    frame.setContentPane(root);

                    DragLayer dragLayer = new DragLayer();
                    dragLayer.setOpaque(false);
                    frame.setGlassPane(dragLayer);
                    frame.getGlassPane().setVisible(true);

                    frame.revalidate();
                    frame.repaint();
                    aire.requestFocusInWindow();
                    aire.refreshScores();
                } catch (Exception e) {
                    e.printStackTrace();
                    afficherMenu();
                }
            }
        };
        worker.execute();
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
