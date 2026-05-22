package model.IA;

import java.util.ArrayList;
import java.util.List;

import model.Coordonnees;
import model.Cercle;
import model.EtatPlateau;
import model.Fleur;
import model.Jeu;
import model.Joueur;
import model.Pion;

// Copie l'état du plateau pour l'algo minimax


public class EtatJeu implements EtatPlateau {

    // Fleurs encore sur le plateau
    public List<Fleur> fleursRestantes;

    // Pions déjà placés sur le plateau
    public List<Pion> pions;

    // Fleurs collectées par chaque joueur
    public List<Fleur>[] fleursParJoueur;

    // Types de pion de chaque joueur
    public Pion.TypePion[] typesPionsJoueurs;

    // Index du joueur courant ( 0 ou 1 )
    public int joueurCourant;

    public EtatJeu() {
        fleursRestantes = new ArrayList<>();
        pions = new ArrayList<>();
        fleursParJoueur = new ArrayList[2];
        fleursParJoueur[0] = new ArrayList<>();
        fleursParJoueur[1] = new ArrayList<>();
        typesPionsJoueurs = new Pion.TypePion[2];
    }

    @Override
    public List<Fleur> fleursPlateau() {
        return fleursRestantes;
    }

    @Override
    public List<Pion> pionsPlateau() {
        return pions;
    }

    @Override
    public Cercle cerclePlateau() {
        return null;
    }

    public static EtatJeu depuisJeu(Jeu jeu, int joueurCourant) {
        EtatJeu etat = new EtatJeu();

        for (Fleur f : jeu.getFleurs()) {
            etat.fleursRestantes.add(f);
        }

        for (Pion p : jeu.getPions()) {
            etat.pions.add(Pion.copiePion(p));
        }

        Joueur[] joueurs = jeu.getJoueurs();
        for (int i = 0; i < joueurs.length && i < 2; i++) {
            etat.typesPionsJoueurs[i] = joueurs[i].getTypePion();

            for (Fleur f : joueurs[i].getFleursGagnees()) {
                etat.fleursParJoueur[i].add(f);
            }
        }

        etat.joueurCourant = joueurCourant;
        return etat;
    }

    public EtatJeu copie() {
        EtatJeu copie = new EtatJeu();

        copie.fleursRestantes.addAll(this.fleursRestantes);

        for (Pion p : pions) {
            copie.pions.add(Pion.copiePion(p));
        }

        for (int i = 0; i < 2; i++) {
            copie.typesPionsJoueurs[i] = typesPionsJoueurs[i];
            copie.fleursParJoueur[i].addAll(this.fleursParJoueur[i]);
        }

        copie.joueurCourant = joueurCourant;
        return copie;
    }

    public EtatJeu appliquerCoup(CoupIA coup) {
        EtatJeu suivant = copie();
        // On recherche la fleur f1 et f2 du coup 
        Fleur f1 = suivant.trouverFleur(coup.fleur1);
        Fleur f2 = suivant.trouverFleur(coup.fleur2);

        if (f1 == null || f2 == null) {
            return suivant;
        }

        Pion.TypePion typePion = suivant.typePionDuJoueur(suivant.joueurCourant);
        Pion pionLibre = suivant.getPionLibre(typePion);

        if (pionLibre != null) {
            pionLibre.setPosition(new Coordonnees(coup.position.x, coup.position.y));
        }

        // Suppression des fleurs gagnées dans le plateau
        // Collection des fleurs gagnees par le joueur courant
        suivant.fleursRestantes.remove(f1);
        suivant.fleursRestantes.remove(f2);
        suivant.fleursParJoueur[suivant.joueurCourant].add(f1);
        suivant.fleursParJoueur[suivant.joueurCourant].add(f2);

        suivant.joueurCourant = 1 - suivant.joueurCourant; // MÀJ index joueurs
        return suivant;
    }

    public boolean estTerminal() {
        if (fleursRestantes.size() < 2) {
            return true;
        }

        for (Pion p : pions) {
            if (p.getPosition() == null) {
                return false;
            }
        }

        return true;
    }

    public Pion getPionLibre(Pion.TypePion type) {
        for (Pion p : pions) {
            if (p.getType() == type && p.getPosition() == null) {
                return p;
            }
        }

        return null;
    }

    public Pion.TypePion typePionDuJoueur(int indexJoueur) {
        return typesPionsJoueurs[indexJoueur];
    }

    public Fleur trouverFleur(Fleur recherchee) {
        for (Fleur f : fleursRestantes) {
            if (memeFleur(f, recherchee)) {
                return f;
            }
        }

        return null;
    }

    private static boolean memeFleur(Fleur a, Fleur b) {
        return a.getType() == b.getType() && a.getPosition().equals(b.getPosition());
    }
}
