package network;

import java.io.Serializable;

public class CoupReseau implements Serializable {

    public String typeMessage; // INIT, AVANTAGE, COUP, ABANDON 

    // INIT Uniquement 
    public boolean hoteCommence; // true = OR commence, false = Argent commence

    // AVANTAGE ou COUP 

    public String typePion;   // "OR" ou "ARGENT" (null si avantage)
    public int pionX, pionY;  // position du pion (0,0 si null)
    public boolean pionNull;  // true si coup avantage (pas de pion)

    // fleur 1
    public String typeFleur1;
    public int fleur1X, fleur1Y;

    // fleur 2 (null si avantage initial)
    public String typeFleur2;
    public int fleur2X, fleur2Y;
    public boolean fleur2Null;


}