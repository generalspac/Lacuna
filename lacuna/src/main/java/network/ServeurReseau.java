package network;

import java.io.*;
import java.net.*;
import java.util.function.Consumer;

/**
 * --------------------- SERVEUR RÉSEAU -------------------------- //
 *
 * Gère la connexion côté hôte dans une partie réseau.
 *
 * Cette classe permet :
 * - d’ouvrir un serveur socket
 * - d’attendre la connexion d’un joueur distant
 * - d’envoyer des messages réseau
 * - de recevoir les coups du client
 */
public class ServeurReseau {

    // --------------------- ATTRIBUTS -------------------------- //

    /**
     * Serveur socket utilisé pour attendre
     * la connexion d’un client.
     */
    private ServerSocket serverSocket;

    /**
     * Socket de communication avec le client.
     */
    private Socket socket;

    /**
     * Flux de sortie utilisé pour envoyer
     * les objets réseau.
     */
    private ObjectOutputStream sortie;

    /**
     * Flux d’entrée utilisé pour recevoir
     * les objets réseau.
     */
    private ObjectInputStream entree;

    /**
     * Callback appelé lors de la réception
     * d’un message réseau.
     */
    private Consumer<CoupReseau> ecouteur;

    // --------------------- DÉMARRAGE -------------------------- //

    /**
     * Ouvre le serveur réseau sur le port donné
     * puis attend la connexion d’un client.
     *
     * La connexion est gérée dans un thread dédié.
     *
     * @param port Port utilisé par le serveur
     *
     * @throws IOException Erreur lors de la création du serveur
     */
    public void demarrer(int port) throws IOException {

        serverSocket = new ServerSocket(port);

        new Thread(() -> {

            try {

                socket = serverSocket.accept();

                // sortie avant entree pour éviter le deadlock
                sortie = new ObjectOutputStream(socket.getOutputStream());
                entree = new ObjectInputStream(socket.getInputStream());

                ecouterMessages();

            } catch (IOException e) {

                e.printStackTrace();
            }

        }).start();
    }

    // --------------------- ENVOI -------------------------- //

    /**
     * Envoie un coup au joueur distant.
     *
     * @param coup Coup réseau à envoyer
     *
     * @throws IOException Erreur lors de l’envoi
     */
    public void envoyerCoup(CoupReseau coup) throws IOException {

        if (sortie != null) {

            sortie.writeObject(coup);

            // envoie immédiat du message
            sortie.flush();
        }
    }

    // --------------------- ÉCOUTEUR -------------------------- //

    /**
     * Définit le callback appelé lors
     * de la réception d’un message.
     *
     * @param ecouteur Fonction exécutée
     *                  à chaque réception
     */
    public void setEcouteur(Consumer<CoupReseau> ecouteur) {
        this.ecouteur = ecouteur;
    }

    // --------------------- RÉCEPTION -------------------------- //

    /**
     * Lance un thread chargé d’écouter
     * les messages entrants.
     *
     * Chaque message reçu est transmis
     * à l’écouteur enregistré.
     */
    private void ecouterMessages() {

        new Thread(() -> {

            try {

                while (true) {

                    CoupReseau coup = (CoupReseau) entree.readObject();

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
     * Ferme proprement toutes les connexions réseau.
     */
    public void fermer() {

        try {

            if (entree != null) entree.close();
            if (sortie != null) sortie.close();
            if (socket != null) socket.close();
            if (serverSocket != null) serverSocket.close();

        } catch (IOException e) {

            e.printStackTrace();
        }
    }
}