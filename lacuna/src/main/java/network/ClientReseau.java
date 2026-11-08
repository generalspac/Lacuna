package network;

import java.io.*;
import java.net.*;
import java.util.function.Consumer;

/**
 * --------------------- CLIENT RÉSEAU -------------------------- //
 *
 * Gère la connexion côté invité dans une partie réseau.
 *
 * Cette classe permet :
 * - de se connecter à un hôte distant,
 * - d'envoyer des coups,
 * - de recevoir des messages réseau,
 * - de notifier un écouteur lors de la réception.
 */
public class ClientReseau {

    // --------------------- ATTRIBUTS -------------------------- //

    /**
     * Socket de connexion vers l'hôte.
     */
    private Socket socket;

    /**
     * Flux de sortie objet.
     */
    private ObjectOutputStream sortie;

    /**
     * Flux d'entrée objet.
     */
    private ObjectInputStream entree;

    /**
     * Callback appelé lors de la réception d'un coup réseau.
     */
    private Consumer<CoupReseau> ecouteur;

    // --------------------- CONNEXION -------------------------- //

    /**
     * Se connecte à l'hôte distant.
     *
     * Initialise les flux réseau puis démarre
     * l'écoute des messages entrants.
     *
     * @param ip    Adresse IP de l'hôte
     * @param port  Port réseau utilisé
     *
     * @throws IOException Erreur réseau lors de la connexion
     */
    public void connecter(String ip, int port) throws IOException {

        socket = new Socket(ip, port);

        // sortie avant entree pour éviter le deadlock
        sortie = new ObjectOutputStream(socket.getOutputStream());

        entree = new ObjectInputStream(socket.getInputStream());

        ecouterMessages();
    }

    // --------------------- ENVOI -------------------------- //

    /**
     * Envoie un coup à l'hôte distant.
     *
     * @param coup Coup réseau à envoyer
     *
     * @throws IOException Erreur lors de l'envoi
     */
    public void envoyerCoup(CoupReseau coup) throws IOException {

        if (sortie != null) {

            sortie.writeObject(coup);

            sortie.flush();
        }
    }

    // --------------------- ÉCOUTEUR -------------------------- //

    /**
     * Définit le callback appelé lors
     * de la réception d'un message réseau.
     *
     * @param ecouteur Fonction appelée à la réception
     */
    public void setEcouteur(Consumer<CoupReseau> ecouteur) {

        this.ecouteur = ecouteur;
    }

    // --------------------- ÉCOUTE DES MESSAGES -------------------------- //

    /**
     * Lance un thread dédié à l'écoute
     * des messages entrants.
     *
     * Chaque message reçu est transmis
     * à l'écouteur enregistré.
     */
    private void ecouterMessages() {

        new Thread(() -> {

            try {

                while (true) {

                    CoupReseau coup =
                            (CoupReseau) entree.readObject();

                    if (ecouteur != null) {

                        ecouteur.accept(coup);
                    }
                }

            } catch (IOException | ClassNotFoundException e) {

                e.printStackTrace();
            }

        }).start();
    }

    // --------------------- FERMETURE -------------------------- //

    /**
     * Ferme proprement la connexion réseau
     * ainsi que tous les flux associés.
     */
    public void fermer() {

        try {

            if (entree != null) {
                entree.close();
            }

            if (sortie != null) {
                sortie.close();
            }

            if (socket != null) {
                socket.close();
            }

        } catch (IOException e) {

            e.printStackTrace();
        }
    }
}