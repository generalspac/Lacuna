package view;

import model.DatabaseManager;
import model.Pion;
import java.sql.SQLException;
import java.util.List;

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
    // id de la partie à charger depuis la DB après lancerJeu(), -1 si nouvelle partie
    private int idPartieACharger = -1;

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

        menu.tutoriel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                afficherTutoriel();
            }
        });

        // Affiche la liste des parties en base
        //  l'utilisateur choisit puis le jeu se lance
        menu.chargerPartie.addActionListener(e -> {
            DatabaseManager db = new DatabaseManager();
            try {
                db.ouvrirConnexion();
                List<String> parties = db.listerParties();
                db.fermerConnexion();
                if (parties.isEmpty()) { JOptionPane.showMessageDialog(frame, "Aucune partie sauvegardée."); return; }
                String[] options = parties.toArray(new String[0]);
                String choisi = (String) JOptionPane.showInputDialog(frame, "Choisissez une partie :",
                        "Charger une partie", JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
                if (choisi == null) return;
                idPartieACharger = Integer.parseInt(choisi.split(" - ")[0]);
                lancerJeu();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(frame, "Erreur base de données : " + ex.getMessage());
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

    public void afficherTutoriel() {

        IntroductionPanel intro = new IntroductionPanel(() -> {
            lancerJeuTutoriel();
        });

        frame.setContentPane(intro);
        frame.revalidate();
        frame.repaint();
    }

  

    public void lancerJeuTutoriel() {

       
        TutorialOverlay overlay = new TutorialOverlay();

        JeuTutoriel tutoriel = new JeuTutoriel(frame, overlay, () -> afficherMenu());

        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setLayout(new OverlayLayout(layeredPane));

        layeredPane.add(tutoriel, JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(overlay, JLayeredPane.PALETTE_LAYER);

        // IMPORTANT
        DragLayer dragLayer = new DragLayer();

        dragLayer.setOpaque(false);

        frame.setGlassPane(dragLayer);

        frame.getGlassPane().setVisible(true);

        frame.setContentPane(layeredPane);

        frame.revalidate();

        frame.repaint();

        tutoriel.startTutorial();
    }

    public void lancerJeu() {
        ParametresPartie parametres;
        if (idPartieACharger >= 0) {
            // chargement depuis la DB on baypasse la fenetre de configuration de jeu 
            parametres = new ParametresPartie();
            parametres.mode = "Humain vs Humain";
            parametres.joueurQuiCommence = null;
            parametres.nomOr = "";
            parametres.nomArgent = "";
            parametres.nomHumain = "";
        } else {
            parametres = demanderParametresPartie();
            if (parametres == null) return;
        }
        String choix = parametres.mode;

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
        spinner.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ================= TEXTE =================
        JLabel loadingLabel = new JLabel("Chargement du plateau...", SwingConstants.CENTER);
        loadingLabel.setForeground(Color.WHITE);
        loadingLabel.setFont(new Font("Arial", Font.ITALIC, 24));
        loadingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ================= CENTRE =================
        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        center.add(Box.createVerticalGlue());
        center.add(spinner);
        center.add(Box.createVerticalStrut(20));
        center.add(loadingLabel);
        center.add(Box.createVerticalGlue());

        // ================= AJOUT FINAL =================
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

                // ================= LAYOUT PRINCIPAL (SYMETRIQUE) =================
                FlexPanel layoutPrincipal = new FlexPanel();
                layoutPrincipal.setDirection(FlexPanel.Direction.ROW);
                layoutPrincipal.setJustify(FlexPanel.Justify.CENTER);
                layoutPrincipal.setAlign(FlexPanel.Align.STRETCH);
                layoutPrincipal.setBackground(Color.BLACK);

                // Score Argent (gauche haut)
                Score scoreArgent = new Score(Pion.TypePion.ARGENT);
                scoreArgent.setPreferredSize(new Dimension(250, 320));

                // Score Or (droite haut)
                Score scoreOr = new Score(Pion.TypePion.OR);
                scoreOr.setPreferredSize(new Dimension(250, 320));

                // ================= COLONNE GAUCHE (Score Argent + Pile Argent)
                // =================
                FlexPanel colonneGauche = new FlexPanel();
                colonneGauche.setDirection(FlexPanel.Direction.COLUMN);
                colonneGauche.setJustify(FlexPanel.Justify.CENTER);
                colonneGauche.setAlign(FlexPanel.Align.CENTER);
                colonneGauche.setPreferredSize(new Dimension(250, 0));
                colonneGauche.setBackground(Color.BLACK);

                colonneGauche.add(scoreArgent);
                colonneGauche.setFlexGrow(scoreArgent, 1);

                // ================= COLONNE DROITE (Score Or + Pile Or) =================
                FlexPanel colonneDroite = new FlexPanel();
                colonneDroite.setDirection(FlexPanel.Direction.COLUMN);
                colonneDroite.setJustify(FlexPanel.Justify.CENTER);
                colonneDroite.setAlign(FlexPanel.Align.CENTER);
                colonneDroite.setPreferredSize(new Dimension(250, 0));
                colonneDroite.setBackground(Color.BLACK);

                colonneDroite.add(scoreOr);
                colonneDroite.setFlexGrow(scoreOr, 1);

                // ================= CENTRE (AIRE DE JEU) =================
                FlexPanel gameCenter = new FlexPanel();
                gameCenter.setDirection(FlexPanel.Direction.COLUMN);
                gameCenter.setJustify(FlexPanel.Justify.CENTER);
                // gameCenter.setAlign(FlexPanel.Align.CENTER);
                gameCenter.setAlign(FlexPanel.Align.STRETCH);
                gameCenter.setBackground(Color.BLACK);

                aire = new JeuGraphique(frame, scoreArgent, scoreOr, instructionLabel, statusLabel);
                aire.setPreferredSize(new Dimension(900, 900));
                aire.setMinimumSize(new Dimension(700, 700));
                aire.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
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
                        aire.jeu.configurerHumainVsHumain(parametres.nomOr, parametres.nomArgent,
                                parametres.joueurQuiCommence);
                        break;
                }

                gameCenter.add(aire);
                gameCenter.setFlexGrow(aire, 1);

                // ================= BOUTONS UNDO/REDO/RESET =================
                FlexPanel buttonPanel = new FlexPanel();
                buttonPanel.setDirection(FlexPanel.Direction.ROW);
                buttonPanel.setJustify(FlexPanel.Justify.CENTER);
                buttonPanel.setAlign(FlexPanel.Align.CENTER);
                buttonPanel.setGap(20);
                buttonPanel.setPreferredSize(new Dimension(0, 40));
                buttonPanel.setBackground(Color.BLACK);

                ControleurBoutons controleurBoutons = new ControleurBoutons(aire);
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

                JButton gitButton = new JButton("Guide");
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

                // Bouton sauvegarder — sauvegarde dans la base SQLite via DatabaseManager
                JButton sauvegarderButton = new JButton("Save");
                sauvegarderButton.addActionListener(e -> {
                    String nom = JOptionPane.showInputDialog(frame, "Nom de la sauvegarde :", "Sauvegarder", JOptionPane.PLAIN_MESSAGE);
                    if (nom == null) return;
                    DatabaseManager db = new DatabaseManager();
                    try {
                        db.ouvrirConnexion();
                        db.sauvegarderPartie(aire.jeu, nom.isEmpty() ? "sans nom" : nom);
                        db.fermerConnexion();
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(frame, "Erreur sauvegarde : " + ex.getMessage());
                    }
                });

                buttonPanel.add(sauvegarderButton);

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

                // Piles latérales (maintenant en bas des colonnes)
                PionGraphique leftPile = new PionGraphique(frame, aire.jeu, Pion.TypePion.ARGENT);
                leftPile.setPreferredSize(new Dimension(250, 600));
                leftPile.setBackground(Color.BLACK);

                PionGraphique rightPile = new PionGraphique(frame, aire.jeu, Pion.TypePion.OR);
                rightPile.setPreferredSize(new Dimension(250, 600));
                rightPile.setBackground(Color.BLACK);

                aire.setSidePiles(leftPile, rightPile);

                // Finaliser les colonnes avec les piles
                colonneGauche.add(leftPile);
                colonneGauche.setFlexGrow(leftPile, 1);

                colonneDroite.add(rightPile);
                colonneDroite.setFlexGrow(rightPile, 1);

                // Assemblage symétrique du layout principal
                layoutPrincipal.add(colonneGauche);
                layoutPrincipal.setFlexGrow(colonneGauche, 0);
                layoutPrincipal.add(gameCenter);
                layoutPrincipal.setFlexGrow(gameCenter, 1);
                layoutPrincipal.add(colonneDroite);
                layoutPrincipal.setFlexGrow(colonneDroite, 0);

                // ================= ASSEMBLAGE FINAL =================
                root.add(instructionLabel);
                root.setFlexGrow(instructionLabel, 0);
                root.add(statusLabel);
                root.setFlexGrow(statusLabel, 0);
                root.add(layoutPrincipal);
                root.setFlexGrow(layoutPrincipal, 1);
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
                    // restaurer l'état de la partie chargée depuis la DB
                    if (idPartieACharger >= 0) {
                        DatabaseManager db = new DatabaseManager();
                        try {
                            db.ouvrirConnexion();
                            db.chargerPartie(aire.jeu, idPartieACharger);
                            db.fermerConnexion();
                        } catch (SQLException ex) {
                            JOptionPane.showMessageDialog(frame, "Erreur chargement : " + ex.getMessage());
                        }
                        idPartieACharger = -1;
                    }
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
                if(controleur.getvue().jeu.getFleurSelectionnee1() != null) {
                    controleur.toggleCoupsPossibles(controleur.getvue().jeu.getFleurSelectionnee1().getType(), false);
                } else {
                    controleur.toggleCoupsPossibles(null, false);
                }
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