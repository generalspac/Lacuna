package view.animations;

import model.Fleur;
import model.Joueur;
import model.Pion;

public class AnimationContext {

    // -------------------------- ÉTAT COURANT --------------------------------- //
    private AnimationState state = AnimationState.AUCUNE;
    private int generation = 0;
    private double animationProgress = 1.0;

    // -------------------------- VISUELS PHASE 2 ------------------------------ //
    private Fleur fleurPhase2Illustree = null;
    private Pion pionPhase2Illustre = null;
    private Pion pionPhase2Compare = null;
    private int phase2ComparePulse = 0;

    // -------------------------- VISUELS FIN DE PARTIE ------------------------ //
    private Joueur gagnantAnime = null;
    private int winnerPulse = 0;

    // -------------------------- IA VISUELLE ---------------------------------- //
    private boolean selectionIAVisuelle = false;

    // -------------------------- ÉTAT GÉNÉRAL --------------------------------- //
    public AnimationState getState() { return state; }
    public void setState(AnimationState state) { this.state = state; }
    public boolean isEnCours() { return state != AnimationState.AUCUNE; }

    // -------------------------- GÉNÉRATION ----------------------------------- //

    public int nouvelleGeneration() { return ++generation; }
    public void annuler() { generation++; }
    public int getGeneration() { return generation; }
    public boolean estGenerationValide(int g) { return g == generation; }

    // -------------------------- VISUELS PHASE 2 ------------------------------ //
    public Fleur getFleurPhase2Illustree() { return fleurPhase2Illustree; }
    public Pion getPionPhase2Illustre() { return pionPhase2Illustre; }
    public Pion getPionPhase2Compare() { return pionPhase2Compare; }
    public int getPhase2ComparePulse() { return phase2ComparePulse; }
    public void setFleurPhase2Illustree(Fleur f) { this.fleurPhase2Illustree = f; }
    public void setPionPhase2Illustre(Pion p) { this.pionPhase2Illustre = p; }
    public void setPionPhase2Compare(Pion p) { this.pionPhase2Compare = p; }
    public void setPhase2ComparePulse(int v) { this.phase2ComparePulse = v; }
    public int incrementerPhase2ComparePulse() { return ++phase2ComparePulse; }
    public void clearPhase2() {
        fleurPhase2Illustree = null;
        pionPhase2Illustre = null;
        pionPhase2Compare = null;
        phase2ComparePulse = 0;
    }
    public double getAnimationProgress() {
        return animationProgress;
    }

    public void setAnimationProgress(double progress) {
        this.animationProgress = progress;
    }
    // -------------------------- VISUELS FIN DE PARTIE ------------------------ //

    public Joueur getGagnantAnime() { return gagnantAnime; }
    public int getWinnerPulse() { return winnerPulse; }
    public void setGagnantAnime(Joueur j) { this.gagnantAnime = j; }
    public void setWinnerPulse(int v) { this.winnerPulse = v; }
    public int incrementerWinnerPulse() { return ++winnerPulse; }

    // -------------------------- IA VISUELLE ---------------------------------- //
    public boolean isSelectionIAVisuelle() { return selectionIAVisuelle; }
    public void setSelectionIAVisuelle(boolean b) { this.selectionIAVisuelle = b; }

    // -------------------------- RESET COMPLET -------------------------------- //
    public void reset() {
        annuler();
        state = AnimationState.AUCUNE;
        selectionIAVisuelle = false;
        gagnantAnime = null;
        winnerPulse = 0;
        clearPhase2();
    }
}