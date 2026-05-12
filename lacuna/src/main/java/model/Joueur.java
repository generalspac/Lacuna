package model;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public abstract class Joueur {
    private static final int NB_COULEURS = 6;
    protected String nom;
    protected Types.TypePion typePion;
    protected List<Fleur> fleursGagnees;

    public Joueur(String nom, Types.TypePion typePion) {
        this.nom = nom;
        this.typePion = typePion;
        this.fleursGagnees = new ArrayList<>();
    }

    public String getNom() {
        return nom;
    }

    public Types.TypePion getTypePion() {
        return typePion;
    }

    public List<Fleur> getFleursGagnees() {
        return fleursGagnees;
    }

    public void ajouterFleur(Fleur fleur) {
        fleursGagnees.add(fleur);
    }

    public int getScoreTotal() {
        return fleursGagnees.size();
    }

    public int nbFleursDeCouleur(Types.TypeFleur type) {
        int compteur = 0;

        for (Fleur f : fleursGagnees) {
            if (f.getType() == type) {
                compteur++;
            }
        }

        return compteur;
    }

    public int nbCouleursGagnees() {
        int compteur = 0;

        for (Types.TypeFleur type : Types.TypeFleur.values()) {
            if (nbFleursDeCouleur(type) >= 4) {
                compteur++;
            }
        }

        return compteur;
    }



    public boolean aGagne() {
        return nbCouleursGagnees() >= 4 ; 
    }

    public void reset() {
        fleursGagnees.clear();
    }



    
    //public abstract Coup choisirCoup(Game game);
}
