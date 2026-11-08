package view;


import javax.swing.*;
import java.awt.*;

import model.Pion;
import view.component.RoundedButton;
import view.component.RoundedTextField;

/**
 * Vue de configuration d'une partie Joueur vs Joueur.
 * <p>
 * Permet :
 * <ul>
 *     <li>de saisir les noms des joueurs ;</li>
 *     <li>de choisir la couleur du joueur 1 ;</li>
 *     <li>de sélectionner le joueur qui commence.</li>
 * </ul>
 */
public class PvPConfigView extends JPanel {

    // ---------------------- CHAMPS ------------------------ //

    private final JFrame frame;

    public RoundedTextField fieldJ1;
    public RoundedTextField fieldJ2;

    public JRadioButton rbOr;
    public JRadioButton rbArgent;

    public JRadioButton rbRandom;
    public JRadioButton rbStartOr;
    public JRadioButton rbStartArgent;

    public RoundedButton jouer;
    public RoundedButton retour;

    // ---------------------- CONSTRUCTEUR ------------------------ //

    /**
     * Crée la vue de configuration PvP.
     *
     * @param frame fenêtre principale de l'application
     */
    public PvPConfigView(JFrame frame) {

        this.frame = frame;

        initialiserInterface();
    }

    // ---------------------- INITIALISATION ------------------------ //

    /**
     * Initialise l'interface graphique.
     */
    private void initialiserInterface() {

        setLayout(new GridBagLayout());

        JPanel card = creerCarte();

        add(card);
    }

    /**
     * Crée la carte centrale contenant les composants.
     *
     * @return carte principale
     */
    private JPanel creerCarte() {

        JPanel card = new JPanel();

        card.setOpaque(false);

        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                        new Color(255, 255, 255, 40),
                        2),
                BorderFactory.createEmptyBorder(
                        35,
                        50,
                        35,
                        50)));

        // ---------------------- TITRE ------------------------ //

        JLabel titre = new JLabel("DUEL JOUEURS");

        titre.setForeground(Color.WHITE);

        titre.setFont(new Font("Arial", Font.BOLD, 36));

        titre.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ---------------------- JOUEUR 1 ------------------------ //

        JLabel labelJ1 = new JLabel("Joueur 1");

        labelJ1.setForeground(new Color(255, 210, 90));

        labelJ1.setFont(new Font("Arial", Font.BOLD, 22));

        labelJ1.setAlignmentX(Component.CENTER_ALIGNMENT);

        fieldJ1 = new RoundedTextField("Nom optionnel", 15);

        fieldJ1.setMaximumSize(new Dimension(320, 45));

        fieldJ1.setFont(new Font("Arial", Font.PLAIN, 18));

        fieldJ1.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ---------------------- JOUEUR 2 ------------------------ //

        JLabel labelJ2 = new JLabel("Joueur 2");

        labelJ2.setForeground(new Color(180, 220, 255));

        labelJ2.setFont(new Font("Arial", Font.BOLD, 22));

        labelJ2.setAlignmentX(Component.CENTER_ALIGNMENT);

        fieldJ2 = new RoundedTextField("Nom optionnel", 15);

        fieldJ2.setMaximumSize(new Dimension(320, 45));

        fieldJ2.setFont(new Font("Arial", Font.PLAIN, 18));

        fieldJ2.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ---------------------- COULEUR ------------------------ //

        JLabel couleurTitre = new JLabel("Couleur du Joueur 1");

        couleurTitre.setForeground(Color.WHITE);

        couleurTitre.setFont(new Font("Arial", Font.BOLD, 20));

        couleurTitre.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel couleurPanel = new JPanel(
                new FlowLayout(
                        FlowLayout.CENTER,
                        25,
                        0));

        couleurPanel.setOpaque(false);

        rbOr = new JRadioButton("OR");

        rbOr.setOpaque(false);

        rbOr.setSelected(true);

        rbOr.setForeground(new Color(255, 210, 90));

        rbOr.setFont(new Font("Arial", Font.BOLD, 18));

        rbArgent = new JRadioButton("ARGENT");

        rbArgent.setOpaque(false);

        rbArgent.setForeground(new Color(180, 220, 255));

        rbArgent.setFont(new Font("Arial", Font.BOLD, 18));

        ButtonGroup bgCouleur = new ButtonGroup();

        bgCouleur.add(rbOr);

        bgCouleur.add(rbArgent);

        couleurPanel.add(rbOr);

        couleurPanel.add(rbArgent);

        // ---------------------- COMMENCE ------------------------ //

        JLabel commenceTitre = new JLabel("Qui commence ?");

        commenceTitre.setForeground(Color.WHITE);

        commenceTitre.setFont(new Font("Arial", Font.BOLD, 20));

        commenceTitre.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel commencePanel = new JPanel(
                new FlowLayout(
                        FlowLayout.CENTER,
                        20,
                        0));

        commencePanel.setOpaque(false);

        rbRandom = new JRadioButton("Aléatoire");

        rbRandom.setOpaque(false);

        rbRandom.setSelected(true);

        rbRandom.setForeground(Color.WHITE);

        rbRandom.setFont(new Font("Arial", Font.BOLD, 17));

        rbStartOr = new JRadioButton("OR");

        rbStartOr.setOpaque(false);

        rbStartOr.setForeground(new Color(255, 210, 90));

        rbStartOr.setFont(new Font("Arial", Font.BOLD, 17));

        rbStartArgent = new JRadioButton("ARGENT");

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

        // ---------------------- BOUTONS ------------------------ //

        jouer = creerBouton("LANCER");

        retour = creerBouton("RETOUR");

        // ---------------------- AJOUTS ------------------------ //

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

        return card;
    }

    // ---------------------- UTILITAIRES ------------------------ //

    /**
     * Crée un bouton stylisé.
     *
     * @param texte texte du bouton
     *
     * @return bouton créé
     */
    private RoundedButton creerBouton(String texte) {

        RoundedButton bouton = new RoundedButton(texte);

        bouton.setAlignmentX(Component.CENTER_ALIGNMENT);

        bouton.setBackground(Color.DARK_GRAY);

        bouton.setForeground(Color.WHITE);

        bouton.setMargin(new Insets(15, 30, 15, 30));

        bouton.setFont(new Font("Arial", Font.BOLD, 25));

        bouton.setMaximumSize(new Dimension(260, 55));

        bouton.setOpaque(false);

        bouton.setContentAreaFilled(false);

        bouton.setBorderPainted(false);

        bouton.setFocusPainted(false);

        return bouton;
    }

    // ---------------------- DONNÉES ------------------------ //

    /**
     * Retourne le type du joueur qui commence.
     *
     * @return type du joueur qui commence
     */
    public Pion.TypePion getJoueurQuiCommence() {

        if (rbStartOr.isSelected()) {

            return Pion.TypePion.OR;
        }

        if (rbStartArgent.isSelected()) {

            return Pion.TypePion.ARGENT;
        }

        return Math.random() < 0.5
                ? Pion.TypePion.OR
                : Pion.TypePion.ARGENT;
    }
}