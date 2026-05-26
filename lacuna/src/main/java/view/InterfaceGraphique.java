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
import network.CoupReseau;
import network.ServeurReseau;
import view.component.DragLayer;
import view.component.FlexPanel;
import view.component.Menu;
import view.component.Regles;
import view.component.RoundedButton;
import view.component.RoundedCheckBox;
import view.component.RoundedTextField;
import view.component.Score;
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
        nettoyerGlassPane();
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
            afficherMenuChargerPartie();
        });

        frame.setContentPane(menu);
        frame.revalidate();
        frame.repaint();
    }

    public void afficherMenuChargerPartie() {
        DatabaseManager db = new DatabaseManager();
        List<DatabaseManager.PartieInfo> parties;
        try {
            db.ouvrirConnexion();
            parties = db.listerPartiesObjets();
            db.fermerConnexion();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(frame, "Erreur base de données : " + ex.getMessage());
            return;
        }

        if (parties.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Aucune partie sauvegardée.");
            return;
        }

        JDialog dialog = new JDialog(frame, "Charger une partie", true);
        dialog.setSize(600, 700);
        dialog.setLocationRelativeTo(frame);

        JPanel content = new JPanel() {
            private Image background;
            {
                try {
                    background = javax.imageio.ImageIO.read(global.Configuration.ouvre("image.png"));
                } catch (Exception ex) {}
            }
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (background != null) {
                    g.drawImage(background, 0, 0, getWidth(), getHeight(), this);
                }
                g.setColor(new Color(0, 0, 0, 180));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        content.setLayout(new BorderLayout());

        JLabel title = new JLabel("PARTIES SAUVEGARDÉES", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        content.add(title, BorderLayout.NORTH);

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(false);

        for (DatabaseManager.PartieInfo p : parties) {
            JPanel card = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(255, 255, 255, 20));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                    g2.setColor(new Color(255, 255, 255, 50));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
                    g2.dispose();
                }
            };
            card.setOpaque(false);
            card.setLayout(new BorderLayout(15, 10));
            card.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
            card.setMaximumSize(new Dimension(550, 80));

            JPanel textPanel = new JPanel(new GridLayout(2, 1));
            textPanel.setOpaque(false);
            
            JLabel nameLabel = new JLabel(p.nom);
            nameLabel.setForeground(new Color(255, 210, 90));
            nameLabel.setFont(new Font("Arial", Font.BOLD, 18));
            
            JLabel dateLabel = new JLabel(p.date);
            dateLabel.setForeground(Color.LIGHT_GRAY);
            dateLabel.setFont(new Font("Arial", Font.ITALIC, 14));
            
            textPanel.add(nameLabel);
            textPanel.add(dateLabel);
            
            card.add(textPanel, BorderLayout.CENTER);

            RoundedButton btnLoad = new RoundedButton("CHARGER");
            btnLoad.setPreferredSize(new Dimension(120, 35));
            btnLoad.addActionListener(ev -> {
                idPartieACharger = p.id;
                dialog.dispose();
                lancerJeu();
            });
            card.add(btnLoad, BorderLayout.EAST);

            listPanel.add(card);
            listPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        content.add(scroll, BorderLayout.CENTER);

        RoundedButton btnRetour = new RoundedButton("RETOUR");
        btnRetour.setPreferredSize(new Dimension(200, 45));
        btnRetour.addActionListener(ev -> dialog.dispose());
        
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        footer.add(btnRetour);
        content.add(footer, BorderLayout.SOUTH);

        dialog.setContentPane(content);
        dialog.setVisible(true);
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
        }, () -> {
            afficherMenu();
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
            JButton undoButton, redoButton, resetButton, sauvegarderButton, quitterButton, feedforwardButton;
            JComponent phase2PanelRef;
            JCheckBox checkAide;

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

                aire = new JeuGraphique(frame, scoreArgent, scoreOr, instructionLabel, statusLabel,
                        () -> afficherMenu());
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

                undoButton = new JButton("Undo");
                undoButton.setToolTipText("Raccourci : <-");
                undoButton.addActionListener(e -> controleurBoutons.undo());

                redoButton = new JButton("Redo");
                redoButton.setToolTipText("Raccourci : ->");
                redoButton.addActionListener(e -> controleurBoutons.redo());

                resetButton = new JButton("Reset");
                resetButton.setToolTipText("Raccourci : R");
                resetButton.addActionListener(e -> controleurBoutons.reset());

                JButton abandonButton = new JButton("Abandonner");
                abandonButton.setToolTipText("Raccourci : A");
                abandonButton.addActionListener(e -> controleurBoutons.abandonner());

                feedforwardButton = new JButton("Feedforward");
                feedforwardButton.setToolTipText("Raccourci : F");
                feedforwardButton.addActionListener(e -> controleurBoutons.feedforward());

                quitterButton = new JButton("Quitter");
                quitterButton.addActionListener(e -> {
                    int confirm = JOptionPane.showConfirmDialog(frame, "Êtes-vous sûr de vouloir quitter la partie ?",
                            "Confirmer", JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        aire.jeu.annulerIAEnCours();
                        aire.annulerAnimationsVisuelles();
                        afficherMenu();
                    }
                });

                checkAide = new RoundedCheckBox("Aide");
                checkAide.setSelected(false);

                checkAide.addActionListener(e -> {
                 
                    aire.setAfficherSuggestions(checkAide.isSelected());
                    aire.repaint();
                });
                buttonPanel.add(checkAide);

                JButton gitButton = new JButton("Guide");
                gitButton.setToolTipText("Raccourci : G");
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
                buttonPanel.add(feedforwardButton);
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
                double[] facteursVitessePhase2 = {4.0, 2.0, 1.0, 0.35, 0.12};
                sliderVitesse.addChangeListener(e -> {
                    double facteur = facteursVitessePhase2[sliderVitesse.getValue() - 1];
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

                new EcouteurDeClavier(controleurBoutons).installerSur(root);

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

    private void nettoyerGlassPane() {
        if (frame != null && frame.getGlassPane() instanceof DragLayer) {
            ((DragLayer) frame.getGlassPane()).cancelAnimations();
            frame.getGlassPane().setVisible(false);
        }
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

    };worker.execute();}}

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

    private JPanel createPlayerCard(String title, Color color) {

        JPanel panel = new JPanel() {

            @Override
            protected void paintComponent(Graphics g) {

                Graphics2D g2 = (Graphics2D) g;

                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(20, 20, 20, 220));

                g2.fillRoundRect(
                        0,
                        0,
                        getWidth(),
                        getHeight(),
                        40,
                        40);

                g2.setColor(color);

                g2.setStroke(new BasicStroke(3f));

                g2.drawRoundRect(
                        1,
                        1,
                        getWidth() - 2,
                        getHeight() - 2,
                        40,
                        40);

                super.paintComponent(g);
            }
        };

        panel.setOpaque(false);
        panel.setLayout(null);

        JLabel label = new JLabel(title, SwingConstants.CENTER);

        label.setForeground(color);

        label.setFont(new Font("Arial", Font.BOLD, 32));

        label.setBounds(0, 40, 300, 40);

        panel.add(label);

        return panel;
    }

    private void styleToggle(JToggleButton btn, Color color) {

        btn.setFocusPainted(false);

        btn.setFont(new Font("Arial", Font.BOLD, 18));

        btn.setForeground(Color.WHITE);

        btn.setBackground(new Color(40, 40, 40));

        btn.setBorder(BorderFactory.createLineBorder(color, 2));

        btn.addChangeListener(e -> {

            if (btn.isSelected()) {

                btn.setBackground(color);
                btn.setForeground(Color.BLACK);

            } else {

                btn.setBackground(new Color(40, 40, 40));
                btn.setForeground(Color.WHITE);
            }
        });
    }

    public void afficherMenuNomsPvP() {

        JPanel panel = creerPanelMenu();
        panel.setLayout(new GridBagLayout());

        // ================= CARTE CENTRALE =================

        JPanel card = new JPanel();
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 40), 2),
                BorderFactory.createEmptyBorder(35, 50, 35, 50)));

        // ================= TITRE =================

        JLabel titre = new JLabel("DUEL JOUEURS");
        titre.setForeground(Color.WHITE);
        titre.setFont(new Font("Arial", Font.BOLD, 36));
        titre.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ================= CHAMPS NOMS =================

        JLabel labelJ1 = new JLabel("Joueur 1");
        labelJ1.setForeground(new Color(255, 210, 90));
        labelJ1.setFont(new Font("Arial", Font.BOLD, 22));
        labelJ1.setAlignmentX(Component.CENTER_ALIGNMENT);

        RoundedTextField fieldJ1 = new RoundedTextField("Nom optionnel", 15);
        fieldJ1.setMaximumSize(new Dimension(320, 45));
        fieldJ1.setFont(new Font("Arial", Font.PLAIN, 18));
        fieldJ1.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel labelJ2 = new JLabel("Joueur 2");
        labelJ2.setForeground(new Color(180, 220, 255));
        labelJ2.setFont(new Font("Arial", Font.BOLD, 22));
        labelJ2.setAlignmentX(Component.CENTER_ALIGNMENT);

        RoundedTextField fieldJ2 = new RoundedTextField("Nom optionnel", 15);
        fieldJ2.setMaximumSize(new Dimension(320, 45));
        fieldJ2.setFont(new Font("Arial", Font.PLAIN, 18));
        fieldJ2.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ================= CHOIX COULEUR =================

        JLabel couleurTitre = new JLabel("Couleur du Joueur 1");
        couleurTitre.setForeground(Color.WHITE);
        couleurTitre.setFont(new Font("Arial", Font.BOLD, 20));
        couleurTitre.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel couleurPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 0));
        couleurPanel.setOpaque(false);

        JRadioButton rbOr = new JRadioButton("OR");
        rbOr.setOpaque(false);
        rbOr.setSelected(true);
        rbOr.setForeground(new Color(255, 210, 90));
        rbOr.setFont(new Font("Arial", Font.BOLD, 18));

        JRadioButton rbArgent = new JRadioButton("ARGENT");
        rbArgent.setOpaque(false);
        rbArgent.setForeground(new Color(180, 220, 255));
        rbArgent.setFont(new Font("Arial", Font.BOLD, 18));

        ButtonGroup bgCouleur = new ButtonGroup();
        bgCouleur.add(rbOr);
        bgCouleur.add(rbArgent);

        couleurPanel.add(rbOr);
        couleurPanel.add(rbArgent);

        // ================= QUI COMMENCE =================

        JLabel commenceTitre = new JLabel("Qui commence ?");
        commenceTitre.setForeground(Color.WHITE);
        commenceTitre.setFont(new Font("Arial", Font.BOLD, 20));
        commenceTitre.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel commencePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        commencePanel.setOpaque(false);

        JRadioButton rbRandom = new JRadioButton("Aléatoire");
        rbRandom.setOpaque(false);
        rbRandom.setSelected(true);
        rbRandom.setForeground(Color.WHITE);
        rbRandom.setFont(new Font("Arial", Font.BOLD, 17));

        JRadioButton rbStartOr = new JRadioButton("OR");
        rbStartOr.setOpaque(false);
        rbStartOr.setForeground(new Color(255, 210, 90));
        rbStartOr.setFont(new Font("Arial", Font.BOLD, 17));

        JRadioButton rbStartArgent = new JRadioButton("ARGENT");
        rbStartArgent.setOpaque(false);
        rbStartArgent.setForeground(new Color(180, 220, 255));
        rbStartArgent.setFont(new Font("Arial", Font.BOLD, 17));

        ButtonGroup bgCommence = new ButtonGroup();
        bgCommence.add(rbRandom);
        bgCommence.add(rbStartOr);
        bgCommence.add(rbStartArgent);

        commencePanel.add(rbRandom);
        commencePanel.add(rbStartOr);
        commencePanel.add(rbStartArgent);

        // ================= BOUTONS =================

        RoundedButton jouer = creerBoutonMenu("LANCER");
        RoundedButton retour = creerBoutonMenu("RETOUR");

        jouer.setAlignmentX(Component.CENTER_ALIGNMENT);
        retour.setAlignmentX(Component.CENTER_ALIGNMENT);

        jouer.setMaximumSize(new Dimension(260, 55));
        retour.setMaximumSize(new Dimension(260, 55));

        // ================= ACTIONS =================

        jouer.addActionListener(e -> {

            ParametresPartie p = new ParametresPartie();

            p.mode = "Humain vs Humain";

            // Attribution couleurs
            if (rbOr.isSelected()) {

                p.nomOr = fieldJ1.getText().trim();
                p.nomArgent = fieldJ2.getText().trim();

            } else {

                p.nomOr = fieldJ2.getText().trim();
                p.nomArgent = fieldJ1.getText().trim();
            }

            // Qui commence
            if (rbStartOr.isSelected()) {

                p.joueurQuiCommence = Pion.TypePion.OR;

            } else if (rbStartArgent.isSelected()) {

                p.joueurQuiCommence = Pion.TypePion.ARGENT;

            } else {

                p.joueurQuiCommence = Math.random() < 0.5
                        ? Pion.TypePion.OR
                        : Pion.TypePion.ARGENT;
            }

            lancerJeu(p);
        });

        retour.addActionListener(e -> afficherMenuNouvellePartie());

        // ================= AJOUTS =================

        card.add(titre);

        card.add(Box.createRigidArea(new Dimension(0, 30)));

        card.add(labelJ1);
        card.add(Box.createRigidArea(new Dimension(0, 8)));
        card.add(fieldJ1);

        card.add(Box.createRigidArea(new Dimension(0, 22)));

        card.add(labelJ2);
        card.add(Box.createRigidArea(new Dimension(0, 8)));
        card.add(fieldJ2);

        card.add(Box.createRigidArea(new Dimension(0, 28)));

        card.add(couleurTitre);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(couleurPanel);

        card.add(Box.createRigidArea(new Dimension(0, 28)));

        card.add(commenceTitre);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(commencePanel);

        card.add(Box.createRigidArea(new Dimension(0, 35)));

        card.add(jouer);

        card.add(Box.createRigidArea(new Dimension(0, 12)));

        card.add(retour);

        panel.add(card);

        frame.setContentPane(panel);
        frame.revalidate();
        frame.repaint();
    }

    public void afficherMenuChoixIA() {
        JPanel panel = creerPanelMenu();
        panel.setLayout(new GridBagLayout());

        // ================= CARTE CENTRALE =================
        JPanel card = new JPanel();
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 40), 2),
                BorderFactory.createEmptyBorder(35, 50, 35, 50)));

        // ================= TITRE =================
        JLabel titre = new JLabel("JOUEUR VS IA");
        titre.setForeground(Color.WHITE);
        titre.setFont(new Font("Arial", Font.BOLD, 36));
        titre.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ================= CHAMP NOM =================
        JLabel labelNom = new JLabel("Ton nom");
        labelNom.setForeground(new Color(255, 210, 90));
        labelNom.setFont(new Font("Arial", Font.BOLD, 22));
        labelNom.setAlignmentX(Component.CENTER_ALIGNMENT);

        RoundedTextField fieldNom = new RoundedTextField("Nom optionnel", 15);
        fieldNom.setMaximumSize(new Dimension(320, 45));
        fieldNom.setFont(new Font("Arial", Font.PLAIN, 18));
        fieldNom.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ================= QUI COMMENCE =================
        JLabel commenceTitre = new JLabel("Qui commence ?");
        commenceTitre.setForeground(Color.WHITE);
        commenceTitre.setFont(new Font("Arial", Font.BOLD, 20));
        commenceTitre.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel commencePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        commencePanel.setOpaque(false);

        JRadioButton rbRandom = new JRadioButton("Aléatoire");
        rbRandom.setOpaque(false);
        rbRandom.setSelected(true);
        rbRandom.setForeground(Color.WHITE);
        rbRandom.setFont(new Font("Arial", Font.BOLD, 17));

        JRadioButton rbHumain = new JRadioButton("Moi");
        rbHumain.setOpaque(false);
        rbHumain.setForeground(new Color(255, 210, 90));
        rbHumain.setFont(new Font("Arial", Font.BOLD, 17));

        JRadioButton rbIA = new JRadioButton("IA");
        rbIA.setOpaque(false);
        rbIA.setForeground(new Color(180, 220, 255));
        rbIA.setFont(new Font("Arial", Font.BOLD, 17));

        ButtonGroup bgCommence = new ButtonGroup();
        bgCommence.add(rbRandom);
        bgCommence.add(rbHumain);
        bgCommence.add(rbIA);

        commencePanel.add(rbRandom);
        commencePanel.add(rbHumain);
        commencePanel.add(rbIA);

        // ================= BOUTONS DIFFICULTÉ =================
        RoundedButton facile = creerBoutonMenu("IA Facile");
        RoundedButton moyen = creerBoutonMenu("IA Moyenne");
        RoundedButton difficile = creerBoutonMenu("IA Difficile");
        RoundedButton retour = creerBoutonMenu("RETOUR");

        facile.addActionListener(e -> {
            Pion.TypePion quiCommence = rbHumain.isSelected() ? Pion.TypePion.OR
                    : (rbIA.isSelected() ? Pion.TypePion.ARGENT : null);
            lancerJeuIA(NiveauxIA.FACILE, fieldNom.getText().trim(), quiCommence);
        });
        moyen.addActionListener(e -> {
            Pion.TypePion quiCommence = rbHumain.isSelected() ? Pion.TypePion.OR
                    : (rbIA.isSelected() ? Pion.TypePion.ARGENT : null);
            lancerJeuIA(NiveauxIA.MOYEN, fieldNom.getText().trim(), quiCommence);
        });
        difficile.addActionListener(e -> {
            Pion.TypePion quiCommence = rbHumain.isSelected() ? Pion.TypePion.OR
                    : (rbIA.isSelected() ? Pion.TypePion.ARGENT : null);
            lancerJeuIA(NiveauxIA.DIFFICILE, fieldNom.getText().trim(), quiCommence);
        });
        retour.addActionListener(e -> afficherMenuNouvellePartie());

        // ================= AJOUTS =================
        card.add(titre);
        card.add(Box.createRigidArea(new Dimension(0, 30)));
        card.add(labelNom);
        card.add(Box.createRigidArea(new Dimension(0, 8)));
        card.add(fieldNom);
        card.add(Box.createRigidArea(new Dimension(0, 25)));
        card.add(commenceTitre);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(commencePanel);
        card.add(Box.createRigidArea(new Dimension(0, 35)));
        card.add(facile);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(moyen);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(difficile);
        card.add(Box.createRigidArea(new Dimension(0, 20)));
        card.add(retour);

        panel.add(card);
        frame.setContentPane(panel);
        frame.revalidate();
        frame.repaint();
    }

    private void lancerJeuIA(NiveauxIA niveau, String nom, Pion.TypePion quiCommence) {
        ParametresPartie p = new ParametresPartie();
        if (niveau == NiveauxIA.FACILE)
            p.mode = "IA Facile";
        else if (niveau == NiveauxIA.MOYEN)
            p.mode = "IA Moyenne";
        else if (niveau == NiveauxIA.DIFFICILE)
            p.mode = "IA Difficile";

        p.nomHumain = nom;
        p.joueurQuiCommence = quiCommence;
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

