package fuzzer.fuzzview;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;

import model.Cercle;
import model.Coordonnees;
import model.Fleur;
import model.Jeu;
import view.CoordonneeConverter;
import view.animations.AnimationContext;
import view.animations.AnimationState;

/**
 * Fuzzer destiné à tester la robustesse des conversions graphiques,
 * des animations et des interactions visuelles du jeu.
 */
public class FuzzJeuGraphique {

    /**
     * Point d'entrée du fuzzing.
     * Vérifie la cohérence des conversions model ↔ screen,
     * des états d'animation et des interactions jeu/vue.
     */
    public static void fuzzerTestOneInput(FuzzedDataProvider data) {

        // Construction du converter avec dimensions aléatoires
        int centreX    = data.consumeInt(50, 600);
        int centreY    = data.consumeInt(50, 600);
        int rayon      = data.consumeInt(30, 400);
        int compWidth  = data.consumeInt(100, 1920);
        int compHeight = data.consumeInt(100, 1080);

        Cercle cercle = new Cercle(new Coordonnees(centreX, centreY), rayon);
        CoordonneeConverter conv = new CoordonneeConverter(cercle, compWidth, compHeight);

        // Vérification des valeurs dérivées : jamais NaN ni incohérentes
        int size = conv.size();
        if (size != Math.min(compWidth, compHeight)) {
            throw new RuntimeException("size() incohérent");
        }
        if (Double.isNaN(conv.rayonScreen())) {
            throw new RuntimeException("NaN dans rayonScreen()");
        }
        if (conv.fleurSize() < 0) {
            throw new RuntimeException("fleurSize() négatif");
        }

        // Conversions modelToScreen : pas de NaN 
        int nbConversions = data.consumeInt(0, 20);
        for (int i = 0; i < nbConversions; i++) {
            int mx = data.consumeInt(-10000, 10000);
            int my = data.consumeInt(-10000, 10000);

            Coordonnees screen = conv.modelToScreen(mx, my);
            if (Double.isNaN(screen.getX()) || Double.isNaN(screen.getY())) {
                throw new RuntimeException("NaN dans modelToScreen(" + mx + "," + my + ")");
            }

            // Aller-retour : tolérance ±1 px due aux arrondis int
            Coordonnees back = conv.screenToModel(screen.getX(), screen.getY());
            if (Math.abs(mx - back.getX()) > 1 || Math.abs(my - back.getY()) > 1) {
                throw new RuntimeException("Aller-retour modelToScreen->screenToModel échoue");
            }
        }

        // Conversions screenToModel : pas de NaN
        int nbInv = data.consumeInt(0, 20);
        for (int i = 0; i < nbInv; i++) {
            int sx = data.consumeInt(-500, compWidth + 500);
            int sy = data.consumeInt(-500, compHeight + 500);

            Coordonnees model = conv.screenToModel(sx, sy);
            if (Double.isNaN(model.getX()) || Double.isNaN(model.getY())) {
                throw new RuntimeException("NaN dans screenToModel(" + sx + "," + sy + ")");
            }
        }

        // setDimensions : redimensionnement aléatoire
        int nbResize = data.consumeInt(0, 5);
        for (int i = 0; i < nbResize; i++) {
            int nw = data.consumeInt(100, 2560);
            int nh = data.consumeInt(100, 1600);
            conv.setDimensions(nw, nh);

            // Le centre du modèle doit toujours mapper sur le centre écran
            Coordonnees screen = conv.modelToScreen(centreX, centreY);
            if (screen.getX() != conv.centerX() || screen.getY() != conv.centerY()) {
                throw new RuntimeException("Centre modèle ne mappe plus sur centre écran après resize");
            }
        }

        // AnimationContext
        AnimationContext ctx = new AnimationContext();

        AnimationState[] etats = AnimationState.values();
        int nbTransitions = data.consumeInt(0, 30);
        for (int i = 0; i < nbTransitions; i++) {
            AnimationState s = etats[data.consumeInt(0, etats.length - 1)];
            ctx.setState(s);

            // isEnCours() doit être cohérent avec l'état
            boolean attendu = (s != AnimationState.AUCUNE);
            if (ctx.isEnCours() != attendu) {
                throw new RuntimeException("isEnCours() incohérent pour state=" + s);
            }
        }

        // AnimationContext : génération et annulation
        int nbGen = data.consumeInt(0, 20);
        int derniereGen = 0;
        for (int i = 0; i < nbGen; i++) {
            derniereGen = ctx.nouvelleGeneration();
        }

        if (derniereGen != ctx.getGeneration()) {
            throw new RuntimeException("getGeneration() ne correspond pas à nouvelleGeneration()");
        }

        if (derniereGen > 0 && !ctx.estGenerationValide(derniereGen)) {
            throw new RuntimeException("estGenerationValide() faux pour la génération courante");
        }

        int genAvantReset = ctx.getGeneration();
        ctx.reset();

        if (ctx.estGenerationValide(genAvantReset) && genAvantReset > 0) {
            throw new RuntimeException("reset() n'a pas invalidé la génération précédente");
        }

        if (ctx.getState() != AnimationState.AUCUNE) {
            throw new RuntimeException("reset() n'a pas remis l'état à AUCUNE");
        }

        // ToucherFleur + jouerCoup
        Jeu jeu = new Jeu(800, 600, new Cercle(new Coordonnees(400, 300), 280));

        if (!jeu.getFleurs().isEmpty()) {
            jeu.appliquerAvantageAvecFleur(jeu.getFleurs().get(0));
        }

        int nbClics = data.consumeInt(0, 20);
        for (int i = 0; i < nbClics; i++) {
            int cx = data.consumeInt(100, 700);
            int cy = data.consumeInt(50, 550);
            jeu.toucherFleur(new Coordonnees(cx, cy));
        }

        int nbCoupsJoues = data.consumeInt(0, 10);
        for (int i = 0; i < nbCoupsJoues; i++) {
            int cx = data.consumeInt(100, 700);
            int cy = data.consumeInt(50, 550);
            jeu.jouerCoupAvecFleursSelectionnees(new Coordonnees(cx, cy));
        }

        // Invariant : le score total de chaque joueur est toujours >= 0
        for (int j = 0; j < jeu.getNbJoueurs(); j++) {
            if (jeu.getJoueurs()[j].getScoreTotal() < 0) {
                throw new RuntimeException("Score négatif détecté pour joueur " + j);
            }
        }

        // Les fleurs du plateau ont toutes une position non nulle
        for (Fleur f : jeu.getFleurs()) {
            if (f.getPosition() == null) {
                throw new RuntimeException("Fleur sans position sur le plateau");
            }
        }

        // Crash sentinelle
        if (rayon == 66666) {
            throw new RuntimeException("Crash sentinelle FuzzJeuGraphique");
        }
    }
}