package view.render;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import model.ConstantesJeu;
import model.Coordonnees;
import model.Fleur;
import model.IA.CoupIA;
import model.IA.EtatJeu;
import model.IA.GenerateurCoup;
import model.Jeu;
import model.ReglesPlateau;
import view.JeuGraphique;

public class MoveRenderer {

    private final Jeu jeu;

    private final JeuGraphique vue;

    private boolean afficherCoupsPossibles = false;

    private Fleur.TypeFleur filtreCouleurCoups = null;

    private boolean feedforwardCacheValide = false;

    private final List<Fleur[]> feedforwardPairesNouvelles =
            new ArrayList<>();

    public MoveRenderer(
            Jeu jeu,
            JeuGraphique vue) {

        this.jeu = jeu;

        this.vue = vue;
    }

    /**
     * Active ou masque l'affichage des coups possibles pour guider le joueur.
     *
     * @param couleur couleur filtrée, ou {@code null} pour toutes les couleurs.
     * @param masquer {@code true} pour cacher l'aide.
     */
    public void afficherCoupsPossibles(
            Fleur.TypeFleur couleur,
            boolean masquer) {

        if (masquer) {

            afficherCoupsPossibles = false;

            filtreCouleurCoups = null;

            jeu.setFeedbackMessage(
                    "Affichage des coups possibles masqué.");

        } else {

            if (afficherCoupsPossibles
                    && filtreCouleurCoups == couleur) {

                afficherCoupsPossibles = false;

                filtreCouleurCoups = null;

                jeu.setFeedbackMessage(
                        "Affichage des coups possibles masqué.");

            } else {

                afficherCoupsPossibles = true;

                filtreCouleurCoups = couleur;

                String label =
                        (couleur == null)
                                ? "toutes les couleurs"
                                : couleur.name().toLowerCase();

                jeu.setFeedbackMessage(
                        "Affichage des coups possibles pour "
                                + label
                                + ".");
            }
        }

        vue.repaint();
    }

    /**
     * Dessine les coups possibles.
     *
     * @param g2 contexte graphique.
     * @param fleurSize taille fleur.
     */
    public void drawPossibleMoves(
            Graphics2D g2,
            int fleurSize) {

        if (!afficherCoupsPossibles)
            return;

        if (jeu.estTourIA()) {

            drawAICandidateMoves(g2);

            return;
        }

        Fleur f1 =
                jeu.getFleurSelectionnee1();

        Fleur f2 =
                jeu.getFleurSelectionnee2();

        List<Fleur[]> paires =
                jeu.pairesConnectees(
                        filtreCouleurCoups);

        g2.setColor(
                new Color(
                        0,
                        255,
                        255,
                        170));

        g2.setStroke(new BasicStroke(2));

        for (Fleur[] paire : paires) {

            Coordonnees p1 =
                    vue.modelToScreen(
                            paire[0].getPosition().x,
                            paire[0].getPosition().y);

            Coordonnees p2 =
                    vue.modelToScreen(
                            paire[1].getPosition().x,
                            paire[1].getPosition().y);

            g2.drawLine(
                    p1.getX(),
                    p1.getY(),
                    p2.getX(),
                    p2.getY());
        }

        if (f1 != null
                && f2 != null
                && jeu.fleursConnectees(
                        f1,
                        f2)) {

            Coordonnees p1 =
                    vue.modelToScreen(
                            f1.getPosition().x,
                            f1.getPosition().y);

            Coordonnees p2 =
                    vue.modelToScreen(
                            f2.getPosition().x,
                            f2.getPosition().y);

            Stroke old =
                    g2.getStroke();

            Color oldC =
                    g2.getColor();

            g2.setColor(
                    new Color(
                            255,
                            255,
                            255,
                            230));

            g2.setStroke(
                    new BasicStroke(4));

            g2.drawLine(
                    p1.getX(),
                    p1.getY(),
                    p2.getX(),
                    p2.getY());

            g2.setColor(oldC);

            g2.setStroke(old);
        }
    }

    /**
     * Dessine les coups candidats IA.
     *
     * @param g2 contexte graphique.
     */
    private void drawAICandidateMoves(
            Graphics2D g2) {

        List<CoupIA> coups =
                GenerateurCoup.genererCoups(
                        EtatJeu.depuisJeu(
                                jeu,
                                jeu.currentPlayerIndex));

        Stroke oldStroke =
                g2.getStroke();

        Color oldColor =
                g2.getColor();

        g2.setStroke(
                new BasicStroke(2));

        for (CoupIA coup : coups) {

            if (coup == null
                    || coup.fleur1 == null
                    || coup.fleur2 == null
                    || coup.position == null) {

                continue;
            }

            if (filtreCouleurCoups != null
                    && coup.fleur1.getType()
                            != filtreCouleurCoups) {

                continue;
            }

            Coordonnees p1 =
                    vue.modelToScreen(
                            coup.fleur1.getPosition().x,
                            coup.fleur1.getPosition().y);

            Coordonnees p2 =
                    vue.modelToScreen(
                            coup.fleur2.getPosition().x,
                            coup.fleur2.getPosition().y);

            Coordonnees pion =
                    vue.modelToScreen(
                            coup.position.x,
                            coup.position.y);

            g2.setColor(
                    new Color(
                            0,
                            255,
                            255,
                            145));

            g2.drawLine(
                    p1.getX(),
                    p1.getY(),
                    p2.getX(),
                    p2.getY());

            g2.setColor(
                    new Color(
                            255,
                            255,
                            255,
                            210));

            g2.fillOval(
                    pion.getX() - 4,
                    pion.getY() - 4,
                    8,
                    8);
        }

        g2.setStroke(oldStroke);

        g2.setColor(oldColor);
    }

    /**
     * Invalide le cache feedforward.
     */
    public void invaliderFeedforwardCache() {

        feedforwardCacheValide = false;

        feedforwardPairesNouvelles.clear();
    }

    /**
     * Vérifie si deux fleurs sont identiques.
     *
     * @param a fleur 1.
     * @param b fleur 2.
     * @return true si identiques.
     */
    private boolean memeFleur(
            Fleur a,
            Fleur b) {

        if (a == null || b == null)
            return false;

        return a.getType() == b.getType()
                && a.getPosition().equals(
                        b.getPosition());
    }

    /**
     * Recalcule le feedforward.
     */
    private void recalculerFeedforward() {

        Fleur s1 =
                jeu.getFleurSelectionnee1();

        Fleur s2 =
                jeu.getFleurSelectionnee2();

        if (s1 == null
                || s2 == null
                || s1.getPosition() == null
                || s2.getPosition() == null) {

            invaliderFeedforwardCache();

            feedforwardCacheValide = true;

            return;
        }

        if (!jeu.fleursConnectees(
                s1,
                s2)) {

            invaliderFeedforwardCache();

            feedforwardCacheValide = true;

            return;
        }

        feedforwardPairesNouvelles.clear();

        EtatJeu etatAvant =
                EtatJeu.depuisJeu(
                        jeu,
                        jeu.currentPlayerIndex);

        List<Fleur[]> anciennesPaires =
                new ArrayList<>();

        for (int i = 0;
                i < etatAvant.fleursRestantes.size();
                i++) {

            for (int j = i + 1;
                    j < etatAvant.fleursRestantes.size();
                    j++) {

                Fleur f1 =
                        etatAvant.fleursRestantes.get(i);

                Fleur f2 =
                        etatAvant.fleursRestantes.get(j);

                if (f1.getType() == f2.getType()
                        && ReglesPlateau.segmentLibre(
                                etatAvant,
                                f1.getPosition(),
                                f2.getPosition(),
                                ConstantesJeu.TOLERANCE_SEGMENT)) {

                    anciennesPaires.add(
                            new Fleur[] { f1, f2 });
                }
            }
        }

        CoupIA coup =
                new CoupIA(
                        etatAvant.trouverFleur(
                                jeu.getFleurSelectionnee1()),
                        etatAvant.trouverFleur(
                                jeu.getFleurSelectionnee2()),
                        Coordonnees.milieu(
                                jeu.getFleurSelectionnee1().getPosition(),
                                jeu.getFleurSelectionnee2().getPosition()));

        EtatJeu etatApres =
                etatAvant.appliquerCoup(coup);

        List<Fleur[]> nouvellesPaires =
                new ArrayList<>();

        for (int i = 0;
                i < etatApres.fleursRestantes.size();
                i++) {

            for (int j = i + 1;
                    j < etatApres.fleursRestantes.size();
                    j++) {

                Fleur f1 =
                        etatApres.fleursRestantes.get(i);

                Fleur f2 =
                        etatApres.fleursRestantes.get(j);

                if (f1.getType() == f2.getType()
                        && ReglesPlateau.segmentLibre(
                                etatApres,
                                f1.getPosition(),
                                f2.getPosition(),
                                ConstantesJeu.TOLERANCE_SEGMENT)) {

                    nouvellesPaires.add(
                            new Fleur[] { f1, f2 });
                }
            }
        }

        Set<String> anciennesSignatures =
                new HashSet<>();

        for (Fleur[] pa : anciennesPaires) {

            anciennesSignatures.add(
                    signature(pa));
        }

        for (Fleur[] pa : nouvellesPaires) {

            String sig =
                    signature(pa);

            if (!anciennesSignatures.contains(sig)) {

                feedforwardPairesNouvelles.add(pa);
            }
        }

        feedforwardCacheValide = true;
    }

    /**
     * Retourne la signature d'une paire.
     *
     * @param paire paire.
     * @return signature.
     */
    private String signature(Fleur[] paire) {

        if (paire == null
                || paire.length < 2
                || paire[0] == null
                || paire[1] == null
                || paire[0].getPosition() == null
                || paire[1].getPosition() == null) {

            return "";
        }

        Fleur a = paire[0];

        Fleur b = paire[1];

        String sA =
                a.getType()
                        + "@"
                        + a.getPosition().x
                        + ","
                        + a.getPosition().y;

        String sB =
                b.getType()
                        + "@"
                        + b.getPosition().x
                        + ","
                        + b.getPosition().y;

        return (sA.compareTo(sB) <= 0)
                ? (sA + "|" + sB)
                : (sB + "|" + sA);
    }

    /**
     * Affiche le feedforward.
     *
     * @param g2 contexte graphique.
     */
    public void afficheFeedforward(Graphics2D g2) {

        if (!feedforwardCacheValide) {

            recalculerFeedforward();
        }

        if (jeu.getFleurSelectionnee1() != null
                && jeu.getFleurSelectionnee2() != null) {

            g2.setColor(
                    new Color(
                            255,
                            0,
                            0,
                            150));

            g2.setStroke(
                    new BasicStroke(3));

            for (Fleur[] paire : feedforwardPairesNouvelles) {

                if ((memeFleur(
                        paire[0],
                        jeu.getFleurSelectionnee1())
                        && memeFleur(
                                paire[1],
                                jeu.getFleurSelectionnee2()))
                        || (memeFleur(
                                paire[1],
                                jeu.getFleurSelectionnee1())
                                && memeFleur(
                                        paire[0],
                                        jeu.getFleurSelectionnee2()))) {

                    continue;
                }

                Coordonnees p1 =
                        vue.modelToScreen(
                                paire[0].getPosition().x,
                                paire[0].getPosition().y);

                Coordonnees p2 =
                        vue.modelToScreen(
                                paire[1].getPosition().x,
                                paire[1].getPosition().y);

                g2.drawLine(
                        p1.getX(),
                        p1.getY(),
                        p2.getX(),
                        p2.getY());
            }
        }
    }
}