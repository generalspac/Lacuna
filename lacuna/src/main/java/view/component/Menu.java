package view.component;

import java.awt.*;
import javax.imageio.ImageIO;
import javax.swing.*;

import global.Configuration;

/**
 * Menu principal du jeu Lacuna.
 *
 * Cette vue affiche les différents boutons permettant :
 * - de lancer une nouvelle partie,
 * - de charger une sauvegarde,
 * - d'accéder au tutoriel,
 * - d'afficher les règles,
 * - de lancer une partie réseau.
 *
 * Le menu affiche également une image de fond
 * avec un voile sombre appliqué par-dessus.
 */
public class Menu extends JPanel {

    /**
     * Bouton permettant de lancer une nouvelle partie.
     */
    public RoundedButton nouvellePartie;

    /**
     * Bouton permettant de charger une partie sauvegardée.
     */
    public RoundedButton chargerPartie;

    /**
     * Bouton permettant d'afficher les règles du jeu.
     */
    public RoundedButton regles;

    /**
     * Bouton permettant d'afficher le tutoriel.
     */
    public RoundedButton tutoriel;

    /**
     * Bouton permettant de lancer une partie multijoueur réseau.
     */
    public RoundedButton multijoueurReseau;

    /**
     * Image de fond du menu.
     */
    private Image backgroundImage;

    /**
     * Initialise le menu principal et configure
     * tous les composants graphiques.
     */
    public Menu() {

        try {
            backgroundImage = ImageIO.read(Configuration.ouvre("image.png"));
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'image de fond : " + e.getMessage());
        }

        setBackground(Color.white);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // =========================
        // Styles
        // =========================
        nouvellePartie = new RoundedButton("Nouvelle Partie");
        chargerPartie = new RoundedButton("Charger Partie");
        regles = new RoundedButton("Règles du jeu");
        tutoriel = new RoundedButton("Tutoriel");
        multijoueurReseau = new RoundedButton("Multijoueur Réseau");

        RoundedButton[] buttons = {
                nouvellePartie,
                chargerPartie,
                tutoriel,
                regles,
                multijoueurReseau
        };

        for (RoundedButton b : buttons) {

            b.setAlignmentX(Component.CENTER_ALIGNMENT);

            b.setBackground(Color.DARK_GRAY);
            b.setForeground(Color.WHITE);
            b.setMargin(new Insets(15, 30, 15, 30));
            b.setFont(new Font("Arial", Font.BOLD, 25));
            b.setMaximumSize(new Dimension(300, 5));

            // Désactive le rendu Swing par défaut
            // afin de conserver le rendu personnalisé.
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
        regles.setActionCommand("regles");
        tutoriel.setActionCommand("tutoriel");
        multijoueurReseau.setActionCommand("multijoueurReseau");

        // =========================
        // LAYOUT
        // =========================
        add(Box.createVerticalStrut(80));
        add(Box.createVerticalStrut(80));

        add(Box.createVerticalGlue());

        add(nouvellePartie);
        add(Box.createRigidArea(new Dimension(0, 1)));

        add(chargerPartie);
        add(Box.createRigidArea(new Dimension(0, 1)));

        add(tutoriel);
        add(Box.createRigidArea(new Dimension(0, 1)));

        add(regles);
        add(Box.createRigidArea(new Dimension(0, 1)));

        add(multijoueurReseau);

        add(Box.createVerticalGlue());
    }

    /**
     * Dessine le fond du menu ainsi qu'un voile sombre
     * par-dessus l'image de fond.
     *
     * @param g Contexte graphique utilisé pour le rendu
     */
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