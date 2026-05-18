package model.reseau;

import java.util.ArrayList;
import java.util.List;

import model.Fleur;
import model.Pion;

public class EtatReseau {
    public int joueurActuel;
    public boolean avantageApplique;
    public boolean phaseAvantage;
    public boolean partieAbandonnee;
    public int gagnantAbandonIndex = -1;
    public String message = "";

    public final List<JoueurReseau> joueurs = new ArrayList<>();
    public final List<FleurReseau> fleursPlateau = new ArrayList<>();
    public final List<PionReseau> pions = new ArrayList<>();
    public final List<FleurGagneeReseau> fleursGagnees = new ArrayList<>();

    public static class JoueurReseau {
        public String nom;
        public Pion.TypePion type;

        public JoueurReseau(String nom, Pion.TypePion type) {
            this.nom = nom;
            this.type = type;
        }
    }

    public static class FleurReseau {
        public Fleur.TypeFleur type;
        public int x;
        public int y;

        public FleurReseau(Fleur.TypeFleur type, int x, int y) {
            this.type = type;
            this.x = x;
            this.y = y;
        }
    }

    public static class PionReseau {
        public Pion.TypePion type;
        public Integer x;
        public Integer y;

        public PionReseau(Pion.TypePion type, Integer x, Integer y) {
            this.type = type;
            this.x = x;
            this.y = y;
        }
    }

    public static class FleurGagneeReseau extends FleurReseau {
        public int joueurIndex;

        public FleurGagneeReseau(int joueurIndex, Fleur.TypeFleur type, int x, int y) {
            super(type, x, y);
            this.joueurIndex = joueurIndex;
        }
    }
}
