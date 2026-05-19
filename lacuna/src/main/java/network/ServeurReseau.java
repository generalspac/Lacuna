package network;

import java.io.*;
import java.net.*;
import java.util.function.Consumer;


// Coté hote : ouvre un ServerSocket et gère les échanges avec l'autre joueur (aka. l'invité)

public class ServeurReseau {

    private ServerSocket serverSocket;
    private Socket socket;
    private ObjectOutputStream sortie;
    private ObjectInputStream entree;
    private Consumer<CoupReseau> ecouteur;

    // Ouvre le serveur sur le port donné et attend une connexion 
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


    // Envoie un coup au client distant
    public void envoyerCoup(CoupReseau coup) throws IOException {
        if (sortie != null) {
            sortie.writeObject(coup);
            sortie.flush(); // n'attends pas que le stream soit rempli envoie maintenant ! pour que le coup arrive instantanément chez l'adversaire
        }
    }

    // le callback appelé à chaque réception d'un message
    public void setEcouteur(Consumer<CoupReseau> ecouteur) {
        this.ecouteur = ecouteur;
    }

    // Boucle de lecture des messages entrants dans un thread dédié
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

    // Ferme proprement toutes les connexions
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