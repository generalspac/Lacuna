/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

// Représente un pion OR ou ARGENT et sa position sur le plateau.
public class Pion {

    public enum TypePion {
        OR, ARGENT
    }

    private TypePion type;
    private Coordonnees position;

    public Pion(TypePion type, Coordonnees position) {
        this.type = type;
        this.position = position;
    }

    public TypePion getType() {
        return type;
    }

    public Coordonnees getPosition() {
        return position;
    }

    public void setType(TypePion type){
        this.type = type ;
    }
    
    public void setPosition(Coordonnees position){
        this.position = position ;
        
    }


    ///////////// AUTRES METHODES ///////////////
    public  static Pion copiePion(Pion p) {
        Coordonnees pos = p.getPosition();
        Coordonnees copiePos;

        if (pos == null) {
            copiePos = null;
        }
         else {
            copiePos = new Coordonnees(pos.getX(), pos.getY());
        }
        return new Pion(p.getType(), copiePos);
    }

    public char typePionToChar() {
        switch (type) {
            case OR:
                return 'X';
            case ARGENT:
                return 'A';
            default:
                return 'I';
        }
    }
}

