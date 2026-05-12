package fuzzer.fuzzmodel;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import model.Coordonnees;

public class FuzzCoordonnees {

    public static void fuzzerTestOneInput(
            FuzzedDataProvider data
    ) {

        int x1 = data.consumeInt();
        int y1 = data.consumeInt();

        int x2 = data.consumeInt();
        int y2 = data.consumeInt();

        Coordonnees c1 =
                new Coordonnees(x1, y1);

        Coordonnees c2 =
                new Coordonnees(x2, y2);

        // equals 

        if (c1.equals(c2)) {

            if (c1.hashCode() != c2.hashCode()) {

                throw new RuntimeException(
                        "equals/hashCode incohérents"
                );
            }
        }

        // compareTo

        int cmp1 = c1.compareTo(c2);
        int cmp2 = c2.compareTo(c1);

        if (cmp1 != -cmp2) {

            throw new RuntimeException(
                    "compareTo asymétrique"
            );
        }

        // compareTo avec equals

        if (c1.equals(c2) && cmp1 != 0) {

            throw new RuntimeException(
                    "equals mais compareTo != 0"
            );
        }

        // getters/setters

        c1.setX(x2);
        c1.setY(y2);

        if (c1.getX() != x2 ||
            c1.getY() != y2) {

            throw new RuntimeException(
                    "setters/getters incohérents"
            );
        }

        // NaN / overflow logique

        if (Integer.compare(
                c1.getX(),
                c2.getX()
        ) != Integer.signum(c1.compareTo(c2))
            &&
            c1.getX() != c2.getX()) {

            System.out.println(
                    "Possible incohérence compareTo"
            );
        }
    }
}