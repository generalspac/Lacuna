package model;

import java.util.List;

// Interface commune pour manipuler le jeu réel, ou une copie d'état IA
public interface EtatPlateau {
    List<Fleur> fleursPlateau();

    List<Pion> pionsPlateau();

    Cercle cerclePlateau();
}
