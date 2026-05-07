package model;

public class Cercle {
    
    private  Coordonnees centre;
    private int rayon;
    //double echelle = 0.5; // échelle    

    //CONSTRUCTEUR////

    public Cercle(Coordonnees centre, int rayon ){
        this.centre = centre;
        this.rayon = rayon ;
    }

    ////////////////////////////////////////

    ////////// GETTER(S) AND SETTER(S) METHODS ///////////////

    //======= GETTER(S)===========/
    public Coordonnees getCentre(){
        return centre;
    }
   
    public int getRayon(){
        return rayon;
    }

    //======= SETTER(S)===========/
    public void setCentre(Coordonnees centre){
        this.centre = centre;
    }

    public void setRayon(int rayon){
        this.rayon = rayon ;
    }


     ////////// AUTRES  METHODES ///////////////
     
    /*les methodes aire et perimetre sont ajoutées car vu qu'on est sur un objet de  type cercle,
        c'est les infos de base à savoir (peut être que ça nous aidera plus tard aussi ) */
    
    public double aire() {
        return Math.PI * rayon * rayon;
    }

    public double perimetre() {
        return 2 * Math.PI * rayon;
    }

    //connaitre la distance d'un point par rapport au centre 
    public double distanceAuCentre(Coordonnees p){
        int x = p.getX();
        int y = p.getY();
        
        int dx = x - centre.getX();
        int dy = y - centre.getY();
    //    System.out.println("Distance au centre: " + Math.sqrt(dx * dx + dy * dy));
        return (double) Math.sqrt(dx * dx + dy * dy);
    }

    // Vérifie si le cercle contient un point de coordonnes P
    //attention: on suppose ici que si le point est sur la circonference 
    //alors il n'appartient au cercle ( c'est pour celà on a mis <=  et non < )
    public boolean contientPoint(Coordonnees p){
        return distanceAuCentre(p) <= (double)rayon ;
    }

    // Cette methode permet de copier le cercle actuel (je suppose qu'elle nous aidera peut être pour le future)
    // je l'ai rajouté par intuition ( à l'équipe de voir)
    public Cercle copie(){
        return new Cercle(centre , rayon );
    }

    




}