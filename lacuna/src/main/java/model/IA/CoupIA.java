package model.IA;

import model.Coordonnees;
import model.Fleur;

// Représente un coup joué par l'IA ( deux fleurs  + la position du pion)

public class CoupIA {
    public Fleur fleur1;
    public Fleur fleur2;
    public Coordonnees position;

    public CoupIA(Fleur fleur1, Fleur fleur2, Coordonnees position) {
        this.fleur1 = fleur1;
        this.fleur2 = fleur2;
        this.position = position;
    }
}