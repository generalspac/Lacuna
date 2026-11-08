/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.util.ArrayList;
import java.util.List;

// Représente une position avec les coordonnées (x,y)
public class Coordonnees implements Comparable<Coordonnees> {
    public int x;
    public int y;

    public Coordonnees(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /////////GETTERS ET SETTERS METHODES////////////

    public int getX(){
        return x;
    }
    public int getY(){
        return y;
    }

    public void setX(int x){
        this.x = x;
    }

    public void setY(int y){
        this.y = y;
    }
    /////////////////////

    public static Coordonnees copieCoordonnees(Coordonnees pos) {
        if (pos == null) {
            return null;
        }

        return new Coordonnees(pos.getX(), pos.getY());
    }

    // dist entre deux points de coordonnes
    public static double distance(Coordonnees a, Coordonnees b) {
        double dx = a.getX() - b.getX();
        double dy = a.getY() - b.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }

    public double distance(Coordonnees autre) {
        return distance(this, autre);
    }

    public static Coordonnees milieu(Coordonnees A, Coordonnees B) {

        int x = (A.getX() + B.getX()) / 2;
        int y = (A.getY() + B.getY()) / 2;

        return new Coordonnees(x, y);
    }

    public static boolean surSegment(Coordonnees A, Coordonnees B, Coordonnees C, double tolerance) {

        double ax = A.getX();
        double ay = A.getY();
        double bx = B.getX();
        double by = B.getY();
        double cx = C.getX();
        double cy = C.getY();

        // Vecteurs AB et AC
        double abx = bx - ax;
        double aby = by - ay;

        double acx = cx - ax;
        double acy = cy - ay;

        // Produit scalaire pour projection
        double ab2 = abx * abx + aby * aby;
        if (ab2 == 0)
            return false;

        double t = (acx * abx + acy * aby) / ab2;

        // Vérifie si projection est sur le segment
        if (t < 0 || t > 1)
            return false;

        // Point projeté sur AB
        double px = ax + t * abx;
        double py = ay + t * aby;

        // Distance C -> segment
        double dx = cx - px;
        double dy = cy - py;

        double distance = Math.sqrt(dx * dx + dy * dy);

        // tolérance (rayon des pièces)
        return distance <= tolerance;
    }

    public static boolean estExactementAuMilieu(Coordonnees A, Coordonnees B, Coordonnees C) {
        double ax = A.getX();
        double ay = A.getY();
        double bx = B.getX();
        double by = B.getY();
        double cx = C.getX();
        double cy = C.getY();

        double abx = bx - ax;
        double aby = by - ay;
        double acx = cx - ax;
        double acy = cy - ay;

        double ab2 = abx * abx + aby * aby;
        if (ab2 == 0)
            return false;

        double t = (acx * abx + acy * aby) / ab2;

        // Doit être environ au milieu (t=0.5)
        if (Math.abs(t - 0.5) > 0.1)
            return false;

        // Doit être sur la ligne (distance faible)
        double p_x = ax + t * abx;
        double p_y = ay + t * aby;
        double dx = cx - p_x;
        double dy = cy - p_y;
        double distance = Math.sqrt(dx * dx + dy * dy);

        return distance <= 15;
    }

    //Genère plusieurs positions possibles sur me segment [A,B] pour placer les pions
    public static List<Coordonnees> pointsDuSegment(Coordonnees A, Coordonnees B, int nbPointsSegment) {

        List<Coordonnees> points = new ArrayList<>();

        double ax = A.getX();
        double ay = A.getY();

        double bx = B.getX();
        double by = B.getY();

        //On découpe le segment en plusieurs morceaux
        for (int i = 1; i < nbPointsSegment; i++) {

            double t = (double) i / nbPointsSegment;

            int x = (int) (ax + t * (bx - ax));
            int y = (int) (ay + t * (by - ay));

            Coordonnees pos = new Coordonnees(x, y);

            points.add(pos);
        }

        return points;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Coordonnees that = (Coordonnees) obj;
        return x == that.x && y == that.y;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(x, y);
    }

    @Override
    public int compareTo(Coordonnees arg0) {
        if (this.x != arg0.x) {
            return Integer.compare(this.x, arg0.x);
        }
        return Integer.compare(this.y, arg0.y);
    }
}
