package view;

import model.DatabaseManager;
import model.Pion;
import java.sql.SQLException;
import java.util.List;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import controller.ControleurBoutons;

import model.IA.NiveauxIA;
import model.Fleur;
import network.CoupReseau;
import network.ServeurReseau;
import network.ClientReseau;
import java.util.concurrent.CountDownLatch;

public class InterfaceGraphique implements Runnable {

    JFrame frame;
    boolean maximized = false;
    private Menu menu;
    // id de la partie à charger depuis la DB après lancerJeu(), -1 si nouvelle
    // partie
    private int idPartieACharger = -1;

    // Réseau : stockés avant d'appeler lancerJeu() pour le mode réseau
    private Object gestionnaireReseau = null; // ServeurReseau ou ClientReseau
    private Pion.TypePion typeLocalReseau = null; // type du joueur local
    private boolean hoteCommencea = false; // true si OR (hôte) commence
    private ParametresPartie parametresPresets = null; // bypass du dialogue de config
    private int graineReseau = 0; // graine partagée pour synchroniser les plateaux

    private static class ParametresPartie {
        String mode;
        String nomOr;
        String nomArgent;
        String nomHumain;
        NiveauxIA niveauOr;
        NiveauxIA niveauArgent;
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
                afficherMenuNouvellePartie();
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

        menu.multijoueurReseau.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                lancerJeuReseau();
            }
        });

        // Affiche la liste des parties en base
        // l'utilisateur choisit puis le jeu se lance
        menu.chargerPartie.addActionListener(e -> {
            DatabaseManager db = new DatabaseManager();
            try {
                db.ouvrirConnexion();
                List<String> parties = db.listerParties();
                db.fermerConnexion();
                if (parties.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Aucune partie sauvegardée.");
                    return;
                }
                String[] options = parties.toArray(new String[0]);
                String choisi = (String) JOptionPane.showInputDialog(frame, "Choisissez une partie :",
                        "Charger une partie", JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
                if (choisi == null)
                    return;
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
        if (parametresPresets != null) {
            // mode réseau : paramètres préparés par lancerJeuReseau()
            parametres = parametresPresets;
            parametresPresets = null;
        } else if (idPartieACharger >= 0) {
            // chargement depuis la DB on baypasse la fenetre de configuration de jeu
            parametres = new ParametresPartie();
            parametres.mode = "Humain vs Humain";
            parametres.joueurQuiCommence = null;
            parametres.nomOr = "";
            parametres.nomArgent = "";
            parametres.nomHumain = "";
        } else {
            // Dans le nouveau flux, on affiche le menu de choix de mode
            afficherMenuNouvellePartie();
            return;
        }
        lancerJeu(parametres);
    }

    public void lancerJeu(ParametresPartie parametres) {
        if (parametres == null)
            return;
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
            JButton undoButton, redoButton, resetButton, sauvegarderButton, quitterButton;
            JComponent phase2PanelRef;

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
                    case "IA vs IA":
                        aire.jeu.configurerIAContreIA(parametres.niveauOr, parametres.niveauArgent,
                                parametres.joueurQuiCommence);
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

                undoButton = new JButton("Undo");
                undoButton.addActionListener(e -> controleurBoutons.undo());

                redoButton = new JButton("Redo");
                redoButton.addActionListener(e -> controleurBoutons.redo());

                resetButton = new JButton("Reset");
                resetButton.addActionListener(e -> controleurBoutons.reset());

                JButton abandonButton = new JButton("Abandonner");
                abandonButton.addActionListener(e -> controleurBoutons.abandonner());

                quitterButton = new JButton("Quitter");
                quitterButton.addActionListener(e -> {
                    int confirm = JOptionPane.showConfirmDialog(frame, "Êtes-vous sûr de vouloir quitter la partie ?",
                            "Confirmer", JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        aire.jeu.annulerIAEnCours();
                        afficherMenu();
                    }
                });

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
                sauvegarderButton = new JButton("Save");
                sauvegarderButton.addActionListener(e -> {
                    if (aire.isAnimationEnCours()) {
                        JOptionPane.showMessageDialog(frame,
                                "Attendez la fin de l'animation avant de sauvegarder la partie.");
                        return;
                    }
                    aire.jeu.annulerIAEnCours();
                    String nom = JOptionPane.showInputDialog(frame, "Nom de la sauvegarde :", "Sauvegarder",
                            JOptionPane.PLAIN_MESSAGE);
                    if (nom == null) {
                        aire.refreshScoresEtTourIA();
                        return;
                    }
                    DatabaseManager db = new DatabaseManager();
                    try {
                        db.ouvrirConnexion();
                        db.sauvegarderPartie(aire.jeu, nom.isEmpty() ? "sans nom" : nom);
                        db.fermerConnexion();
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(frame, "Erreur sauvegarde : " + ex.getMessage());
                    } finally {
                        aire.refreshScoresEtTourIA();
                    }
                });

                buttonPanel.add(sauvegarderButton);
                buttonPanel.add(quitterButton);

                // ================= CONTRÔLES PHASE 2 =================
                FlexPanel phase2Panel = new FlexPanel();
                phase2PanelRef = phase2Panel;
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
                    // Phase 2 : cachée en phase 1, affichée quand elle devient disponible
                    phase2PanelRef.setVisible(false);
                    aire.setPhase2Panel(phase2PanelRef);

                    // Configuration réseau si une connexion est en attente
                    if (gestionnaireReseau != null) {
                        Object gestionnaire = gestionnaireReseau;
                        Pion.TypePion typeLocal = typeLocalReseau;
                        gestionnaireReseau = null;
                        typeLocalReseau = null;
                        // Cacher les boutons non applicables en mode réseau
                        undoButton.setVisible(false);
                        redoButton.setVisible(false);
                        resetButton.setVisible(false);
                        sauvegarderButton.setVisible(false);
                        // Synchroniser le plateau avec la graine partagée pour que les deux
                        // machines aient exactement les mêmes positions de fleurs
                        aire.jeu.reinitialiserFleurs(graineReseau);
                        aire.jeu.currentPlayerIndex = hoteCommencea ? 0 : 1;
                        aire.installerReseau(gestionnaire, typeLocal);
                    } else {
                        aire.refreshScores();
                        if (aire.jeu.estTourIA()) {
                            aire.jeu.jouerAvantageIA();
                            aire.refreshScoresEtTourIA();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    afficherMenu();
                }
            }
        };
        worker.execute();
    }

    // Lance la connexion réseau (hôte ou invité) puis démarre le jeu
    private void lancerJeuReseau() {
        String[] roles = { "Hôte (attendre un joueur)", "Invité (se connecter)" };
        int roleChoisi = JOptionPane.showOptionDialog(frame, "Choisissez votre rôle :",
                "Multijoueur Réseau", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, roles, roles[0]);
        if (roleChoisi < 0)
            return;

        boolean estHote = (roleChoisi == 0);

        if (estHote) {
            // --- Côté hôte ---
            String portStr = JOptionPane.showInputDialog(frame, "Port d'écoute :", "12345");
            if (portStr == null)
                return;
            int port;
            try {
                port = Integer.parseInt(portStr.trim());
            } catch (NumberFormatException e) {
                port = 12345;
            }

            String[] departs = { "OR (toi)", "ARGENT (l'invité)" };
            int departChoisi = JOptionPane.showOptionDialog(frame, "Qui commence ?",
                    "Départ", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                    null, departs, departs[0]);
            if (departChoisi < 0)
                return;
            final boolean hoteCommence = (departChoisi == 0);
            final int portFinal = port;
            final int graineFinal = new java.util.Random().nextInt();

            // Écran d'attente
            afficherEcranAttente("En attente d'un joueur sur le port " + port + "…");

            ServeurReseau serveur = new ServeurReseau();

            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    // Attendre que l'invité envoie PRET avant d'envoyer INIT
                    CountDownLatch latch = new CountDownLatch(1);
                    serveur.setEcouteur(coup -> {
                        if ("PRET".equals(coup.typeMessage))
                            latch.countDown();
                    });
                    serveur.demarrer(portFinal);
                    latch.await();

                    // Envoyer le message INIT à l'invité (avec la graine partagée)
                    CoupReseau init = new CoupReseau();
                    init.typeMessage = "INIT";
                    init.hoteCommence = hoteCommence;
                    init.fleur1X = graineFinal;
                    serveur.envoyerCoup(init);
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        // Préparer le lancement du jeu en mode réseau
                        gestionnaireReseau = serveur;
                        typeLocalReseau = Pion.TypePion.OR;
                        hoteCommencea = hoteCommence;
                        graineReseau = graineFinal;
                        parametresPresets = new ParametresPartie();
                        parametresPresets.mode = "Réseau";
                        parametresPresets.nomOr = "Hôte";
                        parametresPresets.nomArgent = "Invité";
                        parametresPresets.nomHumain = "";
                        parametresPresets.joueurQuiCommence = null;
                        lancerJeu();
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(frame, "Erreur réseau : " + e.getMessage());
                        afficherMenu();
                    }
                }
            };
            worker.execute();

        } else {
            // --- Côté invité ---
            String ip = JOptionPane.showInputDialog(frame, "Adresse IP de l'hôte :", "127.0.0.1");
            if (ip == null)
                return;
            String portStr = JOptionPane.showInputDialog(frame, "Port :", "12345");
            if (portStr == null)
                return;
            int port;
            try {
                port = Integer.parseInt(portStr.trim());
            } catch (NumberFormatException e) {
                port = 12345;
            }
            final String ipFinal = ip.trim().isEmpty() ? "127.0.0.1" : ip.trim();
            final int portFinal = port;

            afficherEcranAttente("Connexion à " + ipFinal + ":" + portFinal + "…");

            ClientReseau client = new ClientReseau();

            SwingWorker<CoupReseau, Void> worker = new SwingWorker<CoupReseau, Void>() {
                @Override
                protected CoupReseau doInBackground() throws Exception {
                    client.connecter(ipFinal, portFinal);

                    // Configurer l'écouteur avant d'envoyer PRET pour ne pas rater INIT
                    CountDownLatch latch = new CountDownLatch(1);
                    CoupReseau[] initHolder = new CoupReseau[1];
                    client.setEcouteur(coup -> {
                        if ("INIT".equals(coup.typeMessage)) {
                            initHolder[0] = coup;
                            latch.countDown();
                        }
                    });

                    // Signaler à l'hôte qu'on est prêt
                    CoupReseau pret = new CoupReseau();
                    pret.typeMessage = "PRET";
                    client.envoyerCoup(pret);

                    latch.await();
                    return initHolder[0];
                }

                @Override
                protected void done() {
                    try {
                        CoupReseau init = get();
                        gestionnaireReseau = client;
                        typeLocalReseau = Pion.TypePion.ARGENT;
                        hoteCommencea = init.hoteCommence;
                        graineReseau = init.fleur1X;
                        parametresPresets = new ParametresPartie();
                        parametresPresets.mode = "Réseau";
                        parametresPresets.nomOr = "Hôte";
                        parametresPresets.nomArgent = "Invité";
                        parametresPresets.nomHumain = "";
                        parametresPresets.joueurQuiCommence = null;
                        lancerJeu();
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(frame, "Erreur réseau : " + e.getMessage());
                        afficherMenu();
                    }
                }
            };
            worker.execute();
        }
    }

    // Affiche un écran d'attente simple avec un message
    private void afficherEcranAttente(String message) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.BLACK);
        JLabel label = new JLabel(message, SwingConstants.CENTER);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.BOLD, 22));
        panel.add(label, BorderLayout.CENTER);
        frame.setContentPane(panel);
        frame.revalidate();
        frame.repaint();
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
                if (controleur.getvue().jeu.getFleurSelectionnee1() != null) {
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

    public void afficherMenuNouvellePartie() {

        JPanel panel = creerPanelMenu();

        RoundedButton pvp = creerBoutonMenu("Joueur 1 vs Joueur 2");
        RoundedButton pve = creerBoutonMenu("Joueur vs IA");
        RoundedButton eve = creerBoutonMenu("IA vs IA");
        RoundedButton retour = creerBoutonMenu("Retour");

        pvp.addActionListener(e -> afficherMenuNomsPvP());

        pve.addActionListener(e -> afficherMenuChoixIA());

        eve.addActionListener(e -> {

            JDialog dialog = new JDialog(frame, "IA vs IA", true);

            dialog.setSize(900, 550);
            dialog.setLocationRelativeTo(frame);
            dialog.setUndecorated(false);
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

            String[] niveaux = {
                    "FACILE",
                    "MOYEN",
                    "DIFFICILE"
            };

            final JList<String> listeOr = new JList<>(niveaux);
            final JList<String> listeArgent = new JList<>(niveaux);

            DefaultListCellRenderer centerRenderer = new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                        boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    setHorizontalAlignment(SwingConstants.CENTER);
                    setOpaque(isSelected);
                    return this;
                }
            };

            listeOr.setCellRenderer(centerRenderer);
            listeArgent.setCellRenderer(centerRenderer);

            JPanel panelDialog = new JPanel() {

                private Image background;

                {
                    try {
                        background = javax.imageio.ImageIO.read(
                                global.Configuration.ouvre("image.png"));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

                @Override
                protected void paintComponent(Graphics g) {

                    super.paintComponent(g);

                    Graphics2D g2 = (Graphics2D) g;

                    g2.setRenderingHint(
                            RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);

                    if (background != null) {
                        g2.drawImage(background, 0, 0, getWidth(), getHeight(), this);
                    }

                    g2.setColor(new Color(0, 0, 0, 170));
                    g2.fillRect(0, 0, getWidth(), getHeight());

                    g2.setColor(new Color(100, 180, 255));
                    g2.setStroke(new BasicStroke(4f));

                    Font font = new Font("Arial", Font.BOLD, 90);
                    g2.setFont(font);

                    FontMetrics fm = g2.getFontMetrics();

                    String texte = "VS";

                    int textWidth = fm.stringWidth(texte);
                    int textHeight = fm.getAscent();

                    int cx = getWidth() / 2;
                    int cy = getHeight() / 2;

                    int x = cx - textWidth / 2;
                    int y = cy + textHeight / 2;

                    GradientPaint gp = new GradientPaint(
                            0,
                            0,
                            Color.RED,
                            0,
                            120,
                            Color.ORANGE);

                    g2.setPaint(gp);

                    g2.drawString(texte, x, y);
                 

                    // Dessin des traits pour IA OR
                    int selOr = listeOr.getSelectedIndex();
                    if (selOr != -1) {
                        g2.setStroke(new BasicStroke(3f));
                        g2.setColor(new Color(255, 180, 50, 180));
                        int yItem = 130 + selOr * 70 + 35;
                        int xListRight = 70 + 250;

                        // Chemin orthogonal vers le VS (horizontal puis vertical puis horizontal)
                        g2.drawLine(xListRight, yItem, cx - 100, yItem);
                        g2.drawLine(cx - 100, yItem, cx - 100, cy);
                        g2.drawLine(cx - 100, cy, cx - 80, cy);
                    }

                    // Dessin des traits pour IA ARGENT
                    int selArgent = listeArgent.getSelectedIndex();
                    if (selArgent != -1) {
                        g2.setStroke(new BasicStroke(3f));
                        g2.setColor(new Color(120, 180, 255, 180));
                        int yItem = 130 + selArgent * 70 + 35;
                        int xListLeft = 580;

                        // Chemin orthogonal vers le VS (horizontal puis vertical puis horizontal)
                        g2.drawLine(xListLeft, yItem, cx + 100, yItem);
                        g2.drawLine(cx + 100, yItem, cx + 100, cy);
                        g2.drawLine(cx + 100, cy, cx + 80, cy);
                    }
                }
            };

            listeOr.addListSelectionListener(ev -> panelDialog.repaint());
            listeArgent.addListSelectionListener(ev -> panelDialog.repaint());

            panelDialog.setLayout(null);

            JLabel labelOr = new JLabel("IA OR", SwingConstants.CENTER);
            labelOr.setForeground(new Color(255, 200, 50));
            labelOr.setFont(new Font("Arial", Font.BOLD, 40));
            labelOr.setBounds(45, 40, 300, 50);
            panelDialog.add(labelOr);

            listeOr.setSelectedIndex(1);
            listeOr.setFont(new Font("Arial", Font.BOLD, 35));
            listeOr.setBackground(new Color(0, 0, 0, 0));
            listeOr.setForeground(Color.WHITE);
            listeOr.setSelectionBackground(new Color(255, 180, 50));
            listeOr.setFixedCellHeight(70);
            listeOr.setBounds(70, 130, 250, 250);
            panelDialog.add(listeOr);

            JLabel labelArgent = new JLabel("IA ARGENT", SwingConstants.CENTER);
            labelArgent.setForeground(new Color(180, 220, 255));
            labelArgent.setFont(new Font("Arial", Font.BOLD, 40));
            labelArgent.setBounds(555, 40, 300, 50);
            panelDialog.add(labelArgent);

            listeArgent.setSelectedIndex(1);
            listeArgent.setFont(new Font("Arial", Font.BOLD, 35));
            listeArgent.setBackground(new Color(0, 0, 0, 0));
            listeArgent.setForeground(Color.WHITE);
            listeArgent.setSelectionBackground(new Color(120, 180, 255));
            listeArgent.setFixedCellHeight(70);
            listeArgent.setBounds(580, 130, 250, 250);
            panelDialog.add(listeArgent);

            RoundedButton lancer = new RoundedButton("LANCER");
            lancer.setFont(new Font("Arial", Font.BOLD, 28));
            lancer.setBounds(180, 420, 250, 50);
            lancer.addActionListener(ev -> {
                ParametresPartie p = new ParametresPartie();
                p.mode = "IA vs IA";
                p.niveauOr = convertirNiveauIA(listeOr.getSelectedValue());
                p.niveauArgent = convertirNiveauIA(listeArgent.getSelectedValue());
                p.joueurQuiCommence = null;
                dialog.dispose();
                lancerJeu(p);
            });
            panelDialog.add(lancer);

            RoundedButton retourDialog = new RoundedButton("RETOUR");
            retourDialog.setFont(new Font("Arial", Font.BOLD, 28));
            retourDialog.setBounds(470, 420, 250, 50);
            retourDialog.addActionListener(ev -> {
                dialog.dispose();
            });
            panelDialog.add(retourDialog);

            dialog.setContentPane(panelDialog);
            dialog.setVisible(true);
        });

        retour.addActionListener(e -> afficherMenu());

        panel.add(Box.createVerticalGlue());
        panel.add(pvp);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(pve);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(eve);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(retour);
        panel.add(Box.createVerticalGlue());

        frame.setContentPane(panel);
        frame.revalidate();
        frame.repaint();
    }

    private NiveauxIA convertirNiveauIA(String txt) {

        switch (txt) {

            case "FACILE":
                return NiveauxIA.FACILE;

            case "DIFFICILE":
                return NiveauxIA.DIFFICILE;

            default:
                return NiveauxIA.MOYEN;
        }
    }

    public void afficherMenuNomsPvP() {
        JPanel panel = creerPanelMenu();

        JLabel labelJ1 = new JLabel("Nom Joueur 1 :");
        labelJ1.setForeground(Color.WHITE);
        labelJ1.setFont(new Font("Arial", Font.BOLD, 20));
        labelJ1.setAlignmentX(Component.CENTER_ALIGNMENT);

        RoundedTextField fieldJ1 = new RoundedTextField("Nom optionnel", 15);
        fieldJ1.setMaximumSize(new Dimension(300, 40));
        fieldJ1.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel labelJ2 = new JLabel("Nom Joueur 2 :");
        labelJ2.setForeground(Color.WHITE);
        labelJ2.setFont(new Font("Arial", Font.BOLD, 20));
        labelJ2.setAlignmentX(Component.CENTER_ALIGNMENT);

        RoundedTextField fieldJ2 = new RoundedTextField("Nom optionnel", 15);
        fieldJ2.setMaximumSize(new Dimension(300, 40));
        fieldJ2.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel colorChoicePanel = new JPanel();
        colorChoicePanel.setOpaque(false);
        colorChoicePanel.setLayout(new FlowLayout());

        JLabel colorLabel = new JLabel("Joueur 1 est :");
        colorLabel.setForeground(Color.WHITE);
        colorLabel.setFont(new Font("Arial", Font.BOLD, 18));

        JRadioButton rbOr = new JRadioButton("OR");
        rbOr.setForeground(new Color(255, 200, 50));
        rbOr.setOpaque(false);
        rbOr.setSelected(true);
        rbOr.setFont(new Font("Arial", Font.BOLD, 18));

        JRadioButton rbArgent = new JRadioButton("ARGENT");
        rbArgent.setForeground(new Color(180, 220, 255));
        rbArgent.setOpaque(false);
        rbArgent.setFont(new Font("Arial", Font.BOLD, 18));

        ButtonGroup bg = new ButtonGroup();
        bg.add(rbOr);
        bg.add(rbArgent);

        colorChoicePanel.add(colorLabel);
        colorChoicePanel.add(rbOr);
        colorChoicePanel.add(rbArgent);
        colorChoicePanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        RoundedButton jouer = creerBoutonMenu("Jouer");
        RoundedButton retour = creerBoutonMenu("Retour");

        jouer.addActionListener(e -> {
            ParametresPartie p = new ParametresPartie();
            p.mode = "Humain vs Humain";
            p.joueurQuiCommence = null;
            if (rbOr.isSelected()) {
                p.nomOr = fieldJ1.getText().trim();
                p.nomArgent = fieldJ2.getText().trim();
            } else {
                p.nomOr = fieldJ2.getText().trim();
                p.nomArgent = fieldJ1.getText().trim();
            }
            p.nomHumain = "";
            lancerJeu(p);
        });

        retour.addActionListener(e -> afficherMenuNouvellePartie());

        panel.add(Box.createVerticalGlue());
        panel.add(labelJ1);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(fieldJ1);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        panel.add(labelJ2);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(fieldJ2);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        panel.add(colorChoicePanel);
        panel.add(Box.createRigidArea(new Dimension(0, 25)));
        panel.add(jouer);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(retour);
        panel.add(Box.createVerticalGlue());

        frame.setContentPane(panel);
        frame.revalidate();
        frame.repaint();
    }

    public void afficherMenuChoixIA() {
        JPanel panel = creerPanelMenu();

        RoundedButton facile = creerBoutonMenu("IA Facile");
        RoundedButton moyen = creerBoutonMenu("IA Moyenne");
        RoundedButton difficile = creerBoutonMenu("IA Difficile");
        RoundedButton retour = creerBoutonMenu("Retour");

        facile.addActionListener(e -> lancerJeuIA(NiveauxIA.FACILE));
        moyen.addActionListener(e -> lancerJeuIA(NiveauxIA.MOYEN));
        difficile.addActionListener(e -> lancerJeuIA(NiveauxIA.DIFFICILE));
        retour.addActionListener(e -> afficherMenuNouvellePartie());

        panel.add(Box.createVerticalGlue());
        panel.add(facile);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(moyen);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(difficile);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(retour);
        panel.add(Box.createVerticalGlue());

        frame.setContentPane(panel);
        frame.revalidate();
        frame.repaint();
    }

    private void lancerJeuIA(NiveauxIA niveau) {
        ParametresPartie p = new ParametresPartie();
        if (niveau == NiveauxIA.FACILE)
            p.mode = "IA Facile";
        else if (niveau == NiveauxIA.MOYEN)
            p.mode = "IA Moyenne";
        else if (niveau == NiveauxIA.DIFFICILE)
            p.mode = "IA Difficile";

        p.nomHumain = "";
        p.joueurQuiCommence = null;
        lancerJeu(p);
    }

    private RoundedButton creerBoutonMenu(String texte) {
        RoundedButton b = new RoundedButton(texte);
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        b.setBackground(Color.DARK_GRAY);
        b.setForeground(Color.WHITE);
        b.setMargin(new Insets(15, 30, 15, 30));
        b.setFont(new Font("Arial", Font.BOLD, 25));
        b.setMaximumSize(new Dimension(300, 50));

        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        return b;
    }

    private JPanel creerPanelMenu() {
        JPanel panel = new JPanel() {
            private Image backgroundImage;
            {
                try {
                    backgroundImage = javax.imageio.ImageIO.read(global.Configuration.ouvre("image.png"));
                } catch (Exception e) {
                }
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
        };
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        return panel;
    }
}
