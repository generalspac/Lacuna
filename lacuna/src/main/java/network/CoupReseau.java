package network;

import java.io.Serializable;

/**
 * --------------------- COUP RÉSEAU -------------------------- //
 *
 * Représente un message échangé entre l'hôte
 * et l'invité lors d'une partie réseau.
 *
 * Cette classe est sérialisable afin de pouvoir
 * être transmise via les sockets Java.
 */
public class CoupReseau implements Serializable {

    // --------------------- TYPE DE MESSAGE -------------------------- //

    /**
     * Type du message réseau.
     *
     * Valeurs possibles :
     * - INIT
     * - AVANTAGE
     * - COUP
     * - ABANDON
     */
    public String typeMessage;

    // --------------------- INITIALISATION -------------------------- //

    /**
     * Indique si l'hôte commence la partie.
     *
     * true  = OR commence
     * false = ARGENT commence
     */
    public boolean hoteCommence;

    // --------------------- INFORMATIONS DU PION -------------------------- //

    /**
     * Type du pion joué.
     *
     * Valeurs possibles :
     * - OR
     * - ARGENT
     *
     * Peut être null pour un avantage.
     */
    public String typePion;

    /**
     * Position X du pion.
     */
    public int pionX;

    /**
     * Position Y du pion.
     */
    public int pionY;

    /**
     * Indique si aucun pion n'est joué.
     *
     * Utilisé pour les avantages.
     */
    public boolean pionNull;

    // --------------------- FLEUR 1 -------------------------- //

    /**
     * Type de la première fleur.
     */
    public String typeFleur1;

    /**
     * Position X de la première fleur.
     */
    public int fleur1X;

    /**
     * Position Y de la première fleur.
     */
    public int fleur1Y;

    // --------------------- FLEUR 2 -------------------------- //

    /**
     * Type de la seconde fleur.
     */
    public String typeFleur2;

    /**
     * Position X de la seconde fleur.
     */
    public int fleur2X;

    /**
     * Position Y de la seconde fleur.
     */
    public int fleur2Y;

    /**
     * Indique si la seconde fleur est absente.
     *
     * Utilisé notamment lors d'un avantage initial.
     */
    public boolean fleur2Null;
}