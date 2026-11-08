package view.render;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Stroke;

import model.Coordonnees;
import model.Fleur;
import model.Jeu;
import model.Joueur;
import model.Pion;
import view.animations.AnimationContext;
import view.CoordonneeConverter;
import view.JeuGraphique;

public class OverlayRenderer {

    private final Jeu jeu;
    private final JeuGraphique vue;
    private final AnimationContext animCtx;
    private final CoordonneeConverter converter;

    public OverlayRenderer(
            Jeu jeu,
            JeuGraphique vue,
            AnimationContext animCtx,
            CoordonneeConverter converter) {

        this.jeu = jeu;
        this.vue = vue;
        this.animCtx = animCtx;
        this.converter = converter;
    }

    /**
     * Dessine la sélection courante du joueur.
     *
     * @param g2 contexte graphique.
     */
    public void drawSelection(Graphics2D g2) {

        Fleur f1 = jeu.getFleurSelectionnee1();

        Fleur f2 = jeu.getFleurSelectionnee2();

        boolean deuxFleurs = f1 != null && f2 != null;

        boolean connectees =
                deuxFleurs && jeu.fleursConnectees(f1, f2);

        g2.setColor(
                connectees
                        || !deuxFleurs
                        || animCtx.isSelectionIAVisuelle()
                                ? Color.YELLOW
                                : Color.RED);

        g2.setStroke(new BasicStroke(3));

        drawSelectionMarker(g2, f1);

        drawSelectionMarker(g2, f2);

        if (deuxFleurs) {

            Coordonnees p1 =
                    vue.modelToScreen(
                            f1.getPosition().x,
                            f1.getPosition().y);

            Coordonnees p2 =
                    vue.modelToScreen(
                            f2.getPosition().x,
                            f2.getPosition().y);

            Stroke old = g2.getStroke();

            if (!connectees && !animCtx.isSelectionIAVisuelle()) {

                g2.setStroke(
                        new BasicStroke(
                                4,
                                BasicStroke.CAP_ROUND,
                                BasicStroke.JOIN_ROUND,
                                1f,
                                new float[] { 10f, 8f },
                                0f));
            }

            g2.drawLine(
                    p1.getX(),
                    p1.getY(),
                    p2.getX(),
                    p2.getY());

            g2.setStroke(old);
        }
    }

    /**
     * Dessine le marqueur d'une fleur sélectionnée.
     *
     * @param g2 contexte graphique.
     * @param f fleur.
     */
    private void drawSelectionMarker(Graphics2D g2, Fleur f) {

        if (f == null || f.getPosition() == null)
            return;

        Coordonnees p =
                vue.modelToScreen(
                        f.getPosition().x,
                        f.getPosition().y);

        int sz =
                Math.max(
                        24,
                        (int) (converter.size() * 0.065));

        g2.drawOval(
                p.getX() - sz / 2,
                p.getY() - sz / 2,
                sz,
                sz);
    }

    /**
     * Dessine les aides de phase 2.
     *
     * @param g2 contexte graphique.
     */
    public void drawPhase2Illustration(Graphics2D g2) {

        Fleur fleurIllu =
                animCtx.getFleurPhase2Illustree();

        Pion pionIllu =
                animCtx.getPionPhase2Illustre();

        if (fleurIllu == null
                || pionIllu == null
                || fleurIllu.getPosition() == null
                || pionIllu.getPosition() == null)
            return;

        Coordonnees fp =
                vue.modelToScreen(
                        fleurIllu.getPosition().x,
                        fleurIllu.getPosition().y);

        Coordonnees pp =
                vue.modelToScreen(
                        pionIllu.getPosition().x,
                        pionIllu.getPosition().y);

        Stroke oldStroke = g2.getStroke();

        Color oldColor = g2.getColor();

        Pion pionCompare =
                animCtx.getPionPhase2Compare();

        boolean compare =
                pionCompare != null
                        && pionCompare.getPosition() != null;

        int rayonFleur =
                Math.max(
                        13,
                        (int) (converter.fleurSize() * 0.55));

        int rayonPion =
                Math.max(
                        15,
                        (int) (converter.fleurSize() * 0.62));

        int rayonCompare =
                Math.max(
                        13,
                        (int) (converter.fleurSize() * 0.55));

        g2.setStroke(
                new BasicStroke(
                        3,
                        BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND,
                        1f,
                        new float[] { 12f, 8f },
                        0f));

        if (compare) {

            Coordonnees pc =
                    vue.modelToScreen(
                            pionCompare.getPosition().x,
                            pionCompare.getPosition().y);

            float pulse =
                    (float) (Math.sin(
                            animCtx.getPhase2ComparePulse() * 0.8)
                            * 0.5 + 0.5);

            int alphaG =
                    190 + (int) (50 * pulse);

            int alphaC =
                    115 + (int) (65 * (1.0 - pulse));

            g2.setColor(
                    new Color(
                            255,
                            255,
                            255,
                            alphaC));

            g2.drawLine(
                    fp.getX(),
                    fp.getY(),
                    pc.getX(),
                    pc.getY());

            g2.setColor(
                    new Color(
                            255,
                            215,
                            0,
                            alphaG));

            g2.drawLine(
                    fp.getX(),
                    fp.getY(),
                    pp.getX(),
                    pp.getY());

            g2.setStroke(new BasicStroke(3));

            g2.setColor(
                    new Color(
                            255,
                            255,
                            255,
                            alphaC));

            g2.drawOval(
                    pc.getX() - rayonCompare,
                    pc.getY() - rayonCompare,
                    rayonCompare * 2,
                    rayonCompare * 2);

            g2.setColor(
                    new Color(
                            255,
                            215,
                            0,
                            alphaG));

            g2.drawOval(
                    pp.getX() - rayonPion,
                    pp.getY() - rayonPion,
                    rayonPion * 2,
                    rayonPion * 2);

        } else {

            g2.setColor(
                    new Color(
                            255,
                            215,
                            0,
                            230));

            g2.drawLine(
                    fp.getX(),
                    fp.getY(),
                    pp.getX(),
                    pp.getY());

            g2.setStroke(new BasicStroke(3));

            g2.drawOval(
                    pp.getX() - rayonPion,
                    pp.getY() - rayonPion,
                    rayonPion * 2,
                    rayonPion * 2);
        }

        g2.setColor(
                new Color(
                        255,
                        215,
                        0,
                        230));

        g2.setStroke(new BasicStroke(3));

        g2.drawOval(
                fp.getX() - rayonFleur,
                fp.getY() - rayonFleur,
                rayonFleur * 2,
                rayonFleur * 2);

        g2.setStroke(oldStroke);

        g2.setColor(oldColor);
    }

    /**
     * Dessine l'overlay d'attente réseau.
     *
     * @param g2 contexte graphique.
     */
    public void drawAttenteAdversaireOverlay(Graphics2D g2) {

        Color oldColor = g2.getColor();

        Font oldFont = g2.getFont();

        g2.setColor(
                new Color(
                        0,
                        0,
                        0,
                        130));

        g2.fillRect(
                0,
                0,
                vue.getWidth(),
                vue.getHeight());

        String texte =
                "En attente de l'adversaire…";

        g2.setFont(
                oldFont.deriveFont(
                        Font.BOLD,
                        26f));

        g2.setColor(Color.WHITE);

        FontMetrics fm =
                g2.getFontMetrics();

        g2.drawString(
                texte,
                (vue.getWidth() - fm.stringWidth(texte)) / 2,
                vue.getHeight() / 2);

        g2.setColor(oldColor);

        g2.setFont(oldFont);
    }

    /**
     * Dessine l'overlay de victoire.
     *
     * @param g2 contexte graphique.
     */
    public void drawWinnerOverlay(Graphics2D g2) {

        Joueur gagnant =
                animCtx.getGagnantAnime();

        int pulse =
                animCtx.getWinnerPulse();

        if (gagnant == null && pulse <= 0)
            return;

        String texte =
                (gagnant == null)
                        ? "ÉGALITÉ !"
                        : jeu.nomJoueurPourAffichage(gagnant)
                                + " GAGNE !";

        int alpha =
                Math.min(
                        210,
                        70 + pulse * 8);

        int w =
                Math.min(
                        vue.getWidth() - 60,
                        420);

        int h = 100;

        int x =
                (vue.getWidth() - w) / 2;

        int y = 28;

        Color oldColor = g2.getColor();

        Font oldFont = g2.getFont();

        Stroke oldStroke = g2.getStroke();

        g2.setColor(
                new Color(
                        0,
                        0,
                        0,
                        alpha));

        g2.fillRoundRect(
                x,
                y,
                w,
                h,
                28,
                28);

        g2.setColor(
                new Color(
                        255,
                        215,
                        0,
                        230));

        g2.setStroke(new BasicStroke(4));

        g2.drawRoundRect(
                x,
                y,
                w,
                h,
                28,
                28);

        float taille =
                30f
                        + (float) (Math.sin(
                                pulse * 0.7) * 4.0);

        g2.setFont(
                oldFont.deriveFont(
                        Font.BOLD,
                        taille));

        FontMetrics fm =
                g2.getFontMetrics();

        g2.setColor(Color.WHITE);

        g2.drawString(
                texte,
                x + (w - fm.stringWidth(texte)) / 2,
                y + (h + fm.getAscent()) / 2 - 10);

        g2.setFont(
                oldFont.deriveFont(
                        Font.BOLD,
                        14f));

        String detail =
                "Couleurs gagnées : "
                        + jeu.getJoueurs()[0].nbCouleursGagnees()
                        + " - "
                        + jeu.getJoueurs()[1].nbCouleursGagnees();

        FontMetrics fm2 =
                g2.getFontMetrics();

        g2.drawString(
                detail,
                x + (w - fm2.stringWidth(detail)) / 2,
                y + h - 18);

        g2.setColor(oldColor);

        g2.setFont(oldFont);

        g2.setStroke(oldStroke);
    }
}