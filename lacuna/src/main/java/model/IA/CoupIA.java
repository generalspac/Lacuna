package model.IA;

import model.Coordonnees;
import model.Fleur;

/**
 * Représente un coup complet choisi par l'IA.
 *
 * Un coup relie deux fleurs de même couleur et place un pion sur leur segment.
 */
public class CoupIA {
    public Fleur fleur1;
    public Fleur fleur2;
    public Coordonnees position;

    /**
     * Crée un coup IA.
     *
     * @param fleur1 première fleur à collecter.
     * @param fleur2 seconde fleur à collecter.
     * @param position position du pion entre les deux fleurs.
     */
    public CoupIA(Fleur fleur1, Fleur fleur2, Coordonnees position) {
        this.fleur1 = fleur1;
        this.fleur2 = fleur2;
        this.position = position;
    }
}
