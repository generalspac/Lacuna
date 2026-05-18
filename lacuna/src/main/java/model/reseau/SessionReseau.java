package model.reseau;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class SessionReseau implements AutoCloseable {
    private final Socket socket;
    private final BufferedReader entree;
    private final PrintWriter sortie;
    private final Consumer<EtatReseau> receptionEtat;
    private final Consumer<String> changementStatut;
    private volatile boolean actif = true;

    private SessionReseau(Socket socket, Consumer<EtatReseau> receptionEtat,
            Consumer<String> changementStatut) throws IOException {
        this.socket = socket;
        this.receptionEtat = receptionEtat;
        this.changementStatut = changementStatut;
        this.entree = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        this.sortie = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
        demarrerLecture();
    }

    public static SessionReseau attendreJoueur(int port, Consumer<EtatReseau> receptionEtat,
            Consumer<String> changementStatut) throws IOException {
        notifier(changementStatut, "En attente d'un joueur sur le port " + port + "...");
        try (ServerSocket serveur = new ServerSocket(port)) {
            Socket socket = serveur.accept();
            notifier(changementStatut, "Joueur connecté : " + socket.getInetAddress().getHostAddress());
            return new SessionReseau(socket, receptionEtat, changementStatut);
        }
    }

    public static SessionReseau rejoindre(String hote, int port, Consumer<EtatReseau> receptionEtat,
            Consumer<String> changementStatut) throws IOException {
        notifier(changementStatut, "Connexion à " + hote + ":" + port + "...");
        Socket socket = new Socket(hote, port);
        notifier(changementStatut, "Connecté à l'hôte.");
        return new SessionReseau(socket, receptionEtat, changementStatut);
    }

    public void envoyerEtat(EtatReseau etat) {
        if (!actif) {
            return;
        }
        sortie.println(ProtocoleReseau.creerMessageEtat(etat));
    }

    public boolean estActif() {
        return actif && !socket.isClosed();
    }

    @Override
    public void close() {
        actif = false;
        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }

    private void demarrerLecture() {
        Thread lecture = new Thread(() -> {
            try {
                String ligne;
                while (actif && (ligne = entree.readLine()) != null) {
                    EtatReseau etat = ProtocoleReseau.lireMessageEtat(ligne);
                    if (etat != null && receptionEtat != null) {
                        receptionEtat.accept(etat);
                    }
                }
            } catch (IOException e) {
                if (actif) {
                    notifier(changementStatut, "Connexion réseau interrompue : " + e.getMessage());
                }
            } finally {
                actif = false;
            }
        }, "lacuna-reseau-lecture");
        lecture.setDaemon(true);
        lecture.start();
    }

    private static void notifier(Consumer<String> changementStatut, String message) {
        if (changementStatut != null) {
            changementStatut.accept(message);
        }
    }
}
