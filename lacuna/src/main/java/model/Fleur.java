/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;



public class Fleur {
    public enum TypeFleur {
        ROUGE, BLEUE, JAUNE, VERTE, ORANGE, VIOLETTE, ROSE
    }

    private TypeFleur type; // Type de la fleur 
    private Coordonnees position; // Position de la fleur

    public Fleur(TypeFleur type, Coordonnees position) {
        this.type = type;
        this.position = position;
    }
    
    public Fleur(TypeFleur type) {
        this.type = type;
        position = new Coordonnees(0, 0);
    }

    public TypeFleur getType() {
        return type;
    }

    public Coordonnees getPosition() {
        return position;
    }
    
    public void setPosition(Coordonnees pos){
        if(pos == null) return;
        
        position.x = pos.x;
        position.y = pos.y;
    }


    //////////// AUTRES METHODES /////////////
    public static Fleur copieFleur(Fleur f) {
        Coordonnees pos = f.getPosition();
        return new Fleur(f.getType(), new Coordonnees(pos.getX(), pos.getY()));
    }


    

    public char typeFleurToChar() {
        switch (type) {
            case ROUGE:
                return 'R';
            case BLEUE:
                return 'B';
            case JAUNE:
                return 'J';
            case VERTE:
                return 'V';
            case ORANGE:
                return 'O';
            case VIOLETTE:
                return 'P';
            case ROSE:
                return 'S'; // S pour rose pour éviter la confusion avec rouge
            default:
                return 'I';
        }
    }   
}
