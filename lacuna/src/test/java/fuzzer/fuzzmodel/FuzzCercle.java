package fuzzer.fuzzmodel;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;

import model.Cercle;
import model.Coordonnees;



public class FuzzCercleStandalone {


    public static void fuzzerTestOneInput(
            FuzzedDataProvider data
    ) {

        int centreX = data.consumeInt();
        int centreY = data.consumeInt();

        int rayon =
                data.consumeInt(0, 100000);

        int pointX = data.consumeInt();
        int pointY = data.consumeInt();

        Coordonnees centre =
                new Coordonnees(centreX, centreY);

        Cercle cercle =
                new Cercle(centre, rayon);

        Coordonnees point =
                new Coordonnees(pointX, pointY);

        cercle.aire();
        cercle.perimetre();

        double distance = cercle.distanceAuCentre(point);

        if (Double.isNaN(distance)) {
                throw new RuntimeException("NaN trouvé");
        }

        if (distance <= rayon) {
            cercle.contientPoint(point);
        }

        if (rayon == 42424242) {
            throw new RuntimeException(
                    "Crash trouvé"
            );
        }
    }
}