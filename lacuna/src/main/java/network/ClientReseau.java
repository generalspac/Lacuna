package network;

import java.io.*;
import java.net.*;
import java.util.function.Consumer;

// coté invité : se connecte à l'hote et gère les échanges

public class ClientReseau {

    private Socket socket;
    private ObjectOutputStream sortie;
    private ObjectInputStream entree;
    private Consumer<CoupReseau> ecouteur;

    // Se connecte à l'hote via son IP et son port
    public void connecter(String ip, int port) throws IOException {
        socket = new Socket(ip, port);
        // sortie avant entree pour éviter le deadlock
        sortie = new ObjectOutputStream(socket.getOutputStream());
        entree = new ObjectInputStream(socket.getInputStream());
        ecouterMessages();
    }

    // Envoie un coup à l'hote
    public void envoyerCoup(CoupReseau coup) throws IOException {
        if (sortie != null) {
            sortie.writeObject(coup);
            sortie.flush();
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}