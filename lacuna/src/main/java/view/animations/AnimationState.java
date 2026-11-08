package view.animations;

/**
 * --------------------- ÉTATS D'ANIMATION -------------------------- //
 *
 * Représente les différents états possibles
 * des animations du jeu.
 */
public enum AnimationState {

    /**
     * Aucune animation active.
     */
    AUCUNE,

    /**
     * Animation d’un coup joué par l’IA.
     */
    COUP_IA,

    /**
     * Animation de transition
     * vers la seconde phase.
     */
    PHASE2,

    /**
     * Animation de victoire
     * du joueur gagnant.
     */
    GAGNANT
}