package view;

import model.Pion;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import controller.ControleurBoutons;
import controller.EcouteurDeClavier;

import model.IA.NiveauxIA;
import model.Fleur;

public class InterfaceGraphique implements Runnable {

    JFrame frame;
    boolean maximized = false;
    private Menu menu;

    private static class ParametresPartie {
        String mode;
        String nomOr;
        String nomArgent;
        String nomHumain;
        Pion.TypePion joueurQuiCommence;
    }

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

        ParametresPartie parametres = demanderParametresPartie();
        if (parametres == null) {
            return;
        }
        String choix = parametres.mode;

        // Afficher un écran de chargement temporaire
        JPanel loadingPanel = new JPanel(new BorderLayout());
        loadingPanel.setBackground(Color.BLACK);
        JLabel loadingLabel = new JLabel("Chargement du plateau...", SwingConstants.CENTER);
        loadingLabel.setForeground(Color.WHITE);
        loadingLabel.setFont(new Font("Arial", Font.ITALIC, 24));
        loadingPanel.add(loadingLabel, BorderLayout.CENTER);
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

                Score scoreArgent = new Score(Pion.TypePion.ARGENT);
                scoreArgent.setPreferredSize(new Dimension(250, 320));

                Score scoreOr = new Score(Pion.TypePion.OR);
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

                // Appliquer les choix de départ faits dans le menu.
                switch (choix) {
                    case "IA Facile":
                        aire.jeu.activerIA(NiveauxIA.FACILE, parametres.nomHumain, parametres.joueurQuiCommence);
                        break;
                    case "IA Moyenne":
                        aire.jeu.activerIA(NiveauxIA.MOYEN, parametres.nomHumain, parametres.joueurQuiCommence);
                        break;
                    case "IA Difficile":
                        aire.jeu.activerIA(NiveauxIA.DIFFICILE, parametres.nomHumain, parametres.joueurQuiCommence);
                        break;
                    default:
                        aire.jeu.configurerHumainVsHumain(parametres.nomOr, parametres.nomArgent, parametres.joueurQuiCommence);
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

                installerRaccourcisClavier(root, controleurBoutons);

                JButton undoButton = new JButton("Undo");
                undoButton.addActionListener(e -> controleurBoutons.undo());

                JButton redoButton = new JButton("Redo");
                redoButton.addActionListener(e -> controleurBoutons.redo());

                JButton resetButton = new JButton("Reset");
                resetButton.addActionListener(e -> controleurBoutons.reset());

                JButton abandonButton = new JButton("Abandonner");
                abandonButton.addActionListener(e -> controleurBoutons.abandonner());

                JButton gitButton = new JButton("Git");
                gitButton.addActionListener(e -> {
                    Object[] options = new Object[Fleur.TypeFleur.values().length + 2];
                    options[0] = "Masquer";
                    options[1] = "Toutes";
                    for (int i = 0; i < Fleur.TypeFleur.values().length; i++) {
                        options[i + 2] = Fleur.TypeFleur.values()[i];
                    }
                    Object choix = JOptionPane.showInputDialog(frame,
                            "Afficher les coups possibles pour quelle couleur de fleurs ?",
                            "Couleur des fleurs",
                            JOptionPane.PLAIN_MESSAGE,
                            null,
                            options,
                            options[0]);
                    if (choix == null) {
                        return;
                    }
                    if ("Masquer".equals(choix)) {
                        controleurBoutons.toggleCoupsPossibles(null, true);
                    } else {
                        Fleur.TypeFleur couleur = null;
                        if (choix instanceof Fleur.TypeFleur) {
                            couleur = (Fleur.TypeFleur) choix;
                        }
                        controleurBoutons.toggleCoupsPossibles(couleur, false);
                    }
                });

                buttonPanel.add(undoButton);
                buttonPanel.add(redoButton);
                buttonPanel.add(resetButton);
                buttonPanel.add(abandonButton);
                buttonPanel.add(gitButton);

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

                // ================= CONTRÔLES PHASE 2 =================
                FlexPanel phase2Panel = new FlexPanel();
                phase2Panel.setDirection(FlexPanel.Direction.ROW);
                phase2Panel.setJustify(FlexPanel.Justify.CENTER);
                phase2Panel.setAlign(FlexPanel.Align.CENTER);
                phase2Panel.setGap(10);
                phase2Panel.setPreferredSize(new Dimension(0, 40));
                phase2Panel.setBackground(Color.BLACK);

                JLabel vitesseLabel = new JLabel("Vitesse Phase 2 :");
                vitesseLabel.setForeground(Color.LIGHT_GRAY);
                vitesseLabel.setFont(new Font("Arial", Font.PLAIN, 14));

                JLabel lentLabel = new JLabel("Lent");
                lentLabel.setForeground(Color.LIGHT_GRAY);
                lentLabel.setFont(new Font("Arial", Font.PLAIN, 12));

                // valeur 1 = lent (delai x4)
                // valeur 3 = normal (x1) 
                // valeur 5 = rapide (x0.25)
                
                JSlider sliderVitesse = new JSlider(JSlider.HORIZONTAL, 1, 5, 3);
                sliderVitesse.setPreferredSize(new Dimension(150, 30));
                sliderVitesse.setBackground(Color.BLACK);
                sliderVitesse.setForeground(Color.LIGHT_GRAY);
                sliderVitesse.addChangeListener(e -> {
                    double facteur = Math.pow(2.0, 3 - sliderVitesse.getValue());
                    aire.setFacteurVitesse(facteur);
                });

                JLabel rapideLabel = new JLabel("Rapide");
                rapideLabel.setForeground(Color.LIGHT_GRAY);
                rapideLabel.setFont(new Font("Arial", Font.PLAIN, 12));

                JButton skipButton = new JButton("Skip");
                skipButton.addActionListener(e -> aire.skipAnimationPhase2());

                phase2Panel.add(vitesseLabel);
                phase2Panel.add(lentLabel);
                phase2Panel.add(sliderVitesse);
                phase2Panel.add(rapideLabel);
                phase2Panel.add(skipButton);

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
                root.add(phase2Panel);
                root.setFlexGrow(phase2Panel, 0);

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
                    if (aire.jeu.estTourIA()) {
                        aire.jeu.jouerAvantageIA();
                        aire.refreshScoresEtTourIA();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    afficherMenu();
                }
            }
        };
        worker.execute();
    }


    private ParametresPartie demanderParametresPartie() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 12, 12));
        panel.setBackground(Color.BLACK);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        String[] modes = { "Humain vs Humain", "IA Facile", "IA Moyenne", "IA Difficile" };
        JComboBox<String> modeCombo = new JComboBox<>(modes);

        JTextField nomOrField = new JTextField();
        JTextField nomArgentField = new JTextField();
        JTextField nomHumainField = new JTextField();

        JComboBox<String> commenceHumain = new JComboBox<>(new String[] { "Aléatoire", "OR", "ARGENT" });
        JComboBox<String> commenceIA = new JComboBox<>(new String[] { "Aléatoire", "Humain", "IA" });

        panel.add(labelBlanc("Mode"));
        panel.add(modeCombo);
        panel.add(labelBlanc("Nom joueur OR (optionnel)"));
        panel.add(nomOrField);
        panel.add(labelBlanc("Nom joueur ARGENT (optionnel)"));
        panel.add(nomArgentField);
        panel.add(labelBlanc("Nom humain vs IA (optionnel)"));
        panel.add(nomHumainField);
        panel.add(labelBlanc("Qui commence ?"));
        panel.add(commenceHumain);

        modeCombo.addActionListener(e -> {
            boolean ia = !"Humain vs Humain".equals(modeCombo.getSelectedItem());
            nomOrField.setEnabled(!ia);
            nomArgentField.setEnabled(!ia);
            nomHumainField.setEnabled(ia);
            panel.remove(9);
            panel.add(ia ? commenceIA : commenceHumain, 9);
            panel.revalidate();
            panel.repaint();
        });

        nomHumainField.setEnabled(false);

        int result = JOptionPane.showConfirmDialog(frame, panel, "Nouvelle partie", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return null;
        }

        ParametresPartie p = new ParametresPartie();
        p.mode = (String) modeCombo.getSelectedItem();
        p.nomOr = nomOrField.getText();
        p.nomArgent = nomArgentField.getText();
        p.nomHumain = nomHumainField.getText();

        boolean modeIA = !"Humain vs Humain".equals(p.mode);
        String choixDepart = (String) (modeIA ? commenceIA.getSelectedItem() : commenceHumain.getSelectedItem());
        p.joueurQuiCommence = convertirChoixDepart(choixDepart, modeIA);
        return p;
    }

    private JLabel labelBlanc(String texte) {
        JLabel label = new JLabel(texte);
        label.setForeground(Color.WHITE);
        return label;
    }

    private Pion.TypePion convertirChoixDepart(String choix, boolean modeIA) {
        if (choix == null || "Aléatoire".equals(choix)) {
            return null;
        }
        if (modeIA) {
            return "IA".equals(choix) ? Pion.TypePion.ARGENT : Pion.TypePion.OR;
        }
        return "ARGENT".equals(choix) ? Pion.TypePion.ARGENT : Pion.TypePion.OR;
    }

    private void installerRaccourcisClavier(JComponent cible, ControleurBoutons controleur) {
       // key Bindings
        InputMap inputMap = cible.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = cible.getActionMap();

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "undo");
        actionMap.put("undo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controleur.undo();
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "redo");
        actionMap.put("redo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controleur.redo();
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0), "reset");
        actionMap.put("reset", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controleur.reset();
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_G, 0), "guides");
        actionMap.put("guides", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controleur.toggleCoupsPossibles(null, false);
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, 0), "abandon");
        actionMap.put("abandon", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controleur.abandonner();
            }
        });
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
