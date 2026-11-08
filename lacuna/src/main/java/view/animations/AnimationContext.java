package view.animations;

import model.Fleur;
import model.Joueur;
import model.Pion;

/**
 * --------------------- CONTEXTE D'ANIMATION -------------------------- //
 *
 * Contient l’ensemble des informations temporaires
 * utilisées par les animations du jeu.
 *
 * Cette classe centralise :
 * - l’état actuel des animations
 * - les éléments visuels de la phase 2
 * - les animations de victoire
 * - les effets visuels liés à l’IA
 */
public class AnimationContext {

    // --------------------- ÉTAT COURANT -------------------------- //

    /**
     * État actuel des animations.
     */
    private AnimationState state = AnimationState.AUCUNE;

    /**
     * Génération courante utilisée
     * pour invalider les anciennes animations.
     */
    private int generation = 0;

    /**
     * Progression actuelle d’une animation.
     */
    private double animationProgress = 1.0;

    // --------------------- VISUELS PHASE 2 -------------------------- //

    /**
     * Fleur actuellement illustrée
     * pendant la seconde phase.
     */
    private Fleur fleurPhase2Illustree = null;

    /**
     * Pion principal affiché
     * dans l’animation de phase 2.
     */
    private Pion pionPhase2Illustre = null;

    /**
     * Pion utilisé pour la comparaison
     * pendant la phase 2.
     */
    private Pion pionPhase2Compare = null;

    /**
     * Valeur utilisée pour créer
     * un effet de pulsation visuelle.
     */
    private int phase2ComparePulse = 0;

    // --------------------- VISUELS FIN DE PARTIE -------------------------- //

    /**
     * Joueur actuellement animé
     * comme gagnant.
     */
    private Joueur gagnantAnime = null;

    /**
     * Valeur utilisée pour l’effet
     * de pulsation du gagnant.
     */
    private int winnerPulse = 0;

    // --------------------- IA VISUELLE -------------------------- //

    /**
     * Indique si une sélection IA
     * est actuellement affichée visuellement.
     */
    private boolean selectionIAVisuelle = false;

    // --------------------- ÉTAT GÉNÉRAL -------------------------- //

    /**
     * Retourne l’état actuel des animations.
     *
     * @return État courant
     */
    public AnimationState getState() {
        return state;
    }

    /**
     * Modifie l’état actuel des animations.
     *
     * @param state Nouvel état
     */
    public void setState(AnimationState state) {
        this.state = state;
    }

    /**
     * Indique si une animation est en cours.
     *
     * @return true si une animation est active
     */
    public boolean isEnCours() {
        return state != AnimationState.AUCUNE;
    }

    // --------------------- GÉNÉRATION -------------------------- //

    /**
     * Crée une nouvelle génération d’animation.
     *
     * @return Nouvelle génération
     */
    public int nouvelleGeneration() {
        return ++generation;
    }

    /**
     * Annule les animations en cours
     * en invalidant leur génération.
     */
    public void annuler() {
        generation++;
    }

    /**
     * Retourne la génération actuelle.
     *
     * @return Génération courante
     */
    public int getGeneration() {
        return generation;
    }

    /**
     * Vérifie si une génération est encore valide.
     *
     * @param g Génération à tester
     *
     * @return true si la génération est valide
     */
    public boolean estGenerationValide(int g) {
        return g == generation;
    }

    // --------------------- VISUELS PHASE 2 -------------------------- //

    /**
     * Retourne la fleur illustrée.
     *
     * @return Fleur affichée
     */
    public Fleur getFleurPhase2Illustree() {
        return fleurPhase2Illustree;
    }

    /**
     * Retourne le pion illustré.
     *
     * @return Pion principal
     */
    public Pion getPionPhase2Illustre() {
        return pionPhase2Illustre;
    }

    /**
     * Retourne le pion de comparaison.
     *
     * @return Pion comparé
     */
    public Pion getPionPhase2Compare() {
        return pionPhase2Compare;
    }

    /**
     * Retourne la valeur de pulsation
     * de comparaison.
     *
     * @return Valeur du pulse
     */
    public int getPhase2ComparePulse() {
        return phase2ComparePulse;
    }

    /**
     * Définit la fleur illustrée.
     *
     * @param f Fleur affichée
     */
    public void setFleurPhase2Illustree(Fleur f) {
        this.fleurPhase2Illustree = f;
    }

    /**
     * Définit le pion illustré.
     *
     * @param p Pion principal
     */
    public void setPionPhase2Illustre(Pion p) {
        this.pionPhase2Illustre = p;
    }

    /**
     * Définit le pion de comparaison.
     *
     * @param p Pion comparé
     */
    public void setPionPhase2Compare(Pion p) {
        this.pionPhase2Compare = p;
    }

    /**
     * Modifie la valeur de pulsation
     * de comparaison.
     *
     * @param v Nouvelle valeur
     */
    public void setPhase2ComparePulse(int v) {
        this.phase2ComparePulse = v;
    }

    /**
     * Incrémente la pulsation
     * de comparaison.
     *
     * @return Nouvelle valeur
     */
    public int incrementerPhase2ComparePulse() {
        return ++phase2ComparePulse;
    }

    /**
     * Réinitialise les données
     * visuelles de la phase 2.
     */
    public void clearPhase2() {

        fleurPhase2Illustree = null;
        pionPhase2Illustre = null;
        pionPhase2Compare = null;
        phase2ComparePulse = 0;
    }

    /**
     * Retourne la progression
     * actuelle de l’animation.
     *
     * @return Progression
     */
    public double getAnimationProgress() {
        return animationProgress;
    }

    /**
     * Modifie la progression
     * actuelle de l’animation.
     *
     * @param progress Nouvelle progression
     */
    public void setAnimationProgress(double progress) {
        this.animationProgress = progress;
    }

    // --------------------- VISUELS FIN DE PARTIE -------------------------- //

    /**
     * Retourne le joueur animé comme gagnant.
     *
     * @return Joueur gagnant
     */
    public Joueur getGagnantAnime() {
        return gagnantAnime;
    }

    /**
     * Retourne la valeur de pulsation
     * du gagnant.
     *
     * @return Valeur du pulse
     */
    public int getWinnerPulse() {
        return winnerPulse;
    }

    /**
     * Définit le joueur gagnant animé.
     *
     * @param j Joueur gagnant
     */
    public void setGagnantAnime(Joueur j) {
        this.gagnantAnime = j;
    }

    /**
     * Modifie la valeur de pulsation
     * du gagnant.
     *
     * @param v Nouvelle valeur
     */
    public void setWinnerPulse(int v) {
        this.winnerPulse = v;
    }

    /**
     * Incrémente la pulsation du gagnant.
     *
     * @return Nouvelle valeur
     */
    public int incrementerWinnerPulse() {
        return ++winnerPulse;
    }

    // --------------------- IA VISUELLE -------------------------- //

    /**
     * Indique si une sélection IA
     * est actuellement affichée.
     *
     * @return true si la sélection IA est visible
     */
    public boolean isSelectionIAVisuelle() {
        return selectionIAVisuelle;
    }

    /**
     * Active ou désactive
     * la sélection visuelle IA.
     *
     * @param b Nouvel état
     */
    public void setSelectionIAVisuelle(boolean b) {
        this.selectionIAVisuelle = b;
    }

    // --------------------- RESET COMPLET -------------------------- //

    /**
     * Réinitialise complètement
     * le contexte d’animation.
     */
    public void reset() {

        annuler();

        state = AnimationState.AUCUNE;

        selectionIAVisuelle = false;

        gagnantAnime = null;

        winnerPulse = 0;

        clearPhase2();
    }
}