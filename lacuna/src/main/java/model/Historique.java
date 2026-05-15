package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import global.Configuration;

// Gère l'historique (undo, redo,charger, enregistrer )
public class Historique {

    // pour l'undo redo
    private Stack<ActionJeu> undoStack = new Stack<>();
    private Stack<ActionJeu> redoStack = new Stack<ActionJeu>();

    ////////// la classe ActionJeu //////////////////
    public static class ActionJeu {// pour l'undo redo
        Pion pion;
        Coordonnees position;
        Fleur f1;
        Fleur f2;
        Joueur joueur;

        public ActionJeu(Pion pion, Fleur f1, Fleur f2, Joueur joueur) {
            this.pion = pion;
            //verifier que pion et sa position ne sont pas null avant de sauvegarder la position
            this.position = (pion != null && pion.getPosition() != null)
                    ? new Coordonnees(pion.getPosition().getX(), pion.getPosition().getY())
                    : null;
            this.f1 = f1;
            this.f2 = f2;
            this.joueur = joueur;
        }

        public Pion getPion() {
            return pion;
        }

        public Fleur getFleur1() {
            return f1;
        }

        public Fleur getFleur2() {
            return f2;
        }

        public Joueur getJoueur() {
            return joueur;
        }

        public Coordonnees getPosition() {
            return position;
        }
    }

    public void enregistrerAction(Pion pion, Fleur f1, Fleur f2, Joueur joueur) {
        ActionJeu action = new ActionJeu(pion, f1, f2, joueur);
        undoStack.push(action);
        redoStack.clear();
    }

    public void enregistrerActionAvantage(Fleur fleurChoisie, Joueur joueurCommence) {
        ActionJeu action = new ActionJeu(null, fleurChoisie, null, joueurCommence);
        undoStack.push(action);
    }

    public void clear() {
        undoStack.clear();
        redoStack.clear();
    }

    public Stack<ActionJeu> getUndoStack() {
        return undoStack;
    }

    public Stack<ActionJeu> getRedoStack() {
        return redoStack;
    }

    public void undo(Jeu jeu) {
        if (!this.undoStack.isEmpty()) {
            ActionJeu action = this.undoStack.pop();
            // Annuler l'action
            if (action.pion != null) {
                action.pion.setPosition(null); // Retirer le pion du plateau
                jeu.joueurPrecedent();
                jeu.setFeedbackMessage("Undo : le dernier pion a été retiré du plateau.");
            } else {
                // Annuler un avantage initial : remettre le jeu en phase de sélection
                jeu.setAvantageInitialApplique(false);
                jeu.setEnPhaseSelectionAvantage(true);
                jeu.setFeedbackMessage("Undo : avantage initial annulé. Choisissez une nouvelle fleur.");
            }

            if (action.f1 != null) {
                action.joueur.getFleursGagnees().remove(action.f1); // Retirer la fleur du joueur
                jeu.getFleurs().add(action.f1); // Remettre la fleur sur le plateau
            }
            if (action.f2 != null) {
                action.joueur.getFleursGagnees().remove(action.f2); // Retirer la fleur du joueur
                jeu.getFleurs().add(action.f2); // Remettre la fleur sur le plateau
            }
            this.redoStack.push(action); // Ajouter l'action annulée à la pile de redo
        } else {
            jeu.setFeedbackMessage("Rien à annuler.");
        }

        jeu.resetFleursSelectionnee1();
        jeu.resetFleursSelectionnee2();
    }

    public void redo(Jeu jeu) {
        if (!this.redoStack.isEmpty()) {
            ActionJeu action = this.redoStack.pop();
            if (action.pion != null) {
                // Refaire une action de placement de pion
                if (jeu.placePion(action.pion, action.position)) { // Replacer le pion à la position sauvegardée
                    if (action.f1 != null) {
                        action.joueur.ajouterFleur(action.f1); // Retirer la fleur du plateau et la donner au joueur
                        jeu.getFleurs().remove(action.f1);
                    }
                    if (action.f2 != null) {
                        action.joueur.ajouterFleur(action.f2); // Retirer la fleur du plateau et la donner au joueur
                        jeu.getFleurs().remove(action.f2);
                    }
                    jeu.joueurSuivant();
                    jeu.setFeedbackMessage("Redo : le coup a été refait.");
                    this.undoStack.push(action); // Ajouter l'action refaite à la pile d'undo
                } else {
                    jeu.setFeedbackMessage("Impossible de refaire l'action : placement du pion invalide.");
                }
            } else {
                // Refaire un avantage initial
                if (action.f1 != null) {
                    action.joueur.ajouterFleur(action.f1);
                    jeu.getFleurs().remove(action.f1);
                }
                if (action.f2 != null) {
                    action.joueur.ajouterFleur(action.f2);
                    jeu.getFleurs().remove(action.f2);
                }
                jeu.setAvantageInitialApplique(true);
                jeu.setEnPhaseSelectionAvantage(false);
                jeu.setFeedbackMessage("Redo : avantage initial réappliqué.");
                this.undoStack.push(action);
            }
        } else {
            jeu.setFeedbackMessage("Rien à refaire.");
        }

        jeu.resetFleursSelectionnee1();
        jeu.resetFleursSelectionnee2();
    }

    // Sauvegarde l'état complet de la partie dans un fichier texte
    public void sauvegarderPartie(Jeu jeu, String cheminFichier) {
        try (java.io.BufferedWriter writer = new java.io.BufferedWriter(new java.io.FileWriter(cheminFichier))) {

            // idx de joueur actuel
            writer.write("JOUEUR_ACTUEL:");
            writer.newLine();
            writer.write(String.valueOf(jeu.currentPlayerIndex));
            writer.newLine();

            // flags de phase (avantage initial)
            writer.write("AVANTAGE:");
            writer.newLine();
            writer.write(jeu.isAvantageInitialApplique() + " " + jeu.isEnPhaseSelectionAvantage());
            writer.newLine();

            // etat de chaque pion : type + position (ou null si non placé)
            writer.write("PIONS:");
            writer.newLine();
            for (Pion p : jeu.getPions()) {
                if (p.getPosition() == null) {
                    writer.write(p.getType().name() + " null");
                } else {
                    writer.write(p.getType().name() + " " + p.getPosition().getX() + " " + p.getPosition().getY());
                }
                writer.newLine();
            }

            // fleurs encore prsentes sur le plateau
            writer.write("FLEURS_PLATEAU:");
            writer.newLine();
            for (Fleur f : jeu.getFleurs()) {
                writer.write(f.getType().name() + " " + f.getPosition().getX() + " " + f.getPosition().getY());
                writer.newLine();
            }

            // fleurs collectés par chaque joueur 
            writer.write("FLEURS_COLLECTEES:");
            writer.newLine();
            for (int i = 0; i < jeu.getJoueurs().length; i++) {
                for (Fleur f : jeu.getJoueurs()[i].getFleursGagnees()) {
                    writer.write(i + " " + f.getType().name() + " " + f.getPosition().getX() + " " + f.getPosition().getY());
                    writer.newLine();
                }
            }

            // Pile undo (du bas vers le haut pour recharger dans le meme ordre)
            writer.write("UNDO_STACK:");
            writer.newLine();
            List<ActionJeu> actions = new ArrayList<>(undoStack);
            for (ActionJeu action : actions) {
                // type et position du pion
                String pionStr = (action.pion == null || action.position == null) ? "null"
                        : action.pion.getType().name() + " " + action.position.getX() + " " + action.position.getY();

                // fleur f1 (ou null)
                String f1Str = (action.f1 == null) ? "null"
                        : action.f1.getType().name() + " " + action.f1.getPosition().getX() + " " + action.f1.getPosition().getY();

                // fleur f2 (ou null)
                String f2Str = (action.f2 == null) ? "null"
                        : action.f2.getType().name() + " " + action.f2.getPosition().getX() + " " + action.f2.getPosition().getY();

                // index du joueur qui a effectue l'action
                int joueurIndex = 0;
                for (int i = 0; i < jeu.getJoueurs().length; i++) {
                    if (jeu.getJoueurs()[i] == action.joueur) {
                        joueurIndex = i;
                        break;
                    }
                }

                writer.write(pionStr + " | " + f1Str + " | " + f2Str + " | " + joueurIndex);
                writer.newLine();
            }

            Configuration.debugeur("Partie sauvegardée dans : %s\n", cheminFichier);

        } catch (java.io.IOException e) {
            Configuration.debugeurErreur("Erreur lors de la sauvegarde : %s\n", e.getMessage());
        }
    }

    // Charge l'état d'une partie depuis un fichier texte et reconstruit le jeu
    public void chargerPartie(Jeu jeu, String cheminFichier) {
        try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(cheminFichier))) {

            // Remise à zéro de l'état courant
            jeu.getPions().clear();
            jeu.getFleurs().clear();
            clear();
            jeu.resetFleursSelectionnee1();
            jeu.resetFleursSelectionnee2();
            jeu.initPlayers(); // recrée les joueurs avec fleursGagnees vides

            String ligne;
            String section = "";

            while ((ligne = reader.readLine()) != null) {
                ligne = ligne.trim();
                if (ligne.isEmpty()) continue;

                // Détection des sections
                if (ligne.equals("JOUEUR_ACTUEL:"))       { section = "JOUEUR_ACTUEL";       continue; }
                if (ligne.equals("AVANTAGE:"))             { section = "AVANTAGE";             continue; }
                if (ligne.equals("PIONS:"))                { section = "PIONS";                continue; }
                if (ligne.equals("FLEURS_PLATEAU:"))       { section = "FLEURS_PLATEAU";       continue; }
                if (ligne.equals("FLEURS_COLLECTEES:"))    { section = "FLEURS_COLLECTEES";    continue; }
                if (ligne.equals("UNDO_STACK:"))           { section = "UNDO_STACK";           continue; }

                switch (section) {

                    case "JOUEUR_ACTUEL":
                        jeu.currentPlayerIndex = Integer.parseInt(ligne);
                        break;

                    case "AVANTAGE": {
                        String[] parts = ligne.split(" ");
                        jeu.setAvantageInitialApplique(Boolean.parseBoolean(parts[0]));
                        jeu.setEnPhaseSelectionAvantage(Boolean.parseBoolean(parts[1]));
                        break;
                    }

                    case "PIONS": {
                        String[] parts = ligne.split(" ");
                        Pion.TypePion type = Pion.TypePion.valueOf(parts[0]);
                        Coordonnees pos = null;
                        if (!parts[1].equals("null")) {
                            pos = new Coordonnees(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
                        }
                        jeu.getPions().add(new Pion(type, pos));
                        break;
                    }

                    case "FLEURS_PLATEAU": {
                        String[] parts = ligne.split(" ");
                        Fleur.TypeFleur type = Fleur.TypeFleur.valueOf(parts[0]);
                        Coordonnees pos = new Coordonnees(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
                        jeu.getFleurs().add(new Fleur(type, pos));
                        break;
                    }

                    case "FLEURS_COLLECTEES": {
                        String[] parts = ligne.split(" ");
                        int joueurIndex = Integer.parseInt(parts[0]);
                        Fleur.TypeFleur type = Fleur.TypeFleur.valueOf(parts[1]);
                        Coordonnees pos = new Coordonnees(Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
                        jeu.getJoueurs()[joueurIndex].ajouterFleur(new Fleur(type, pos));
                        break;
                    }

                    case "UNDO_STACK": {

                        String[] segments = ligne.split(" \\| ");
                        if (segments.length < 4) break;

                        Pion pion = null;

                        // Lecture du pion
                        if (!segments[0].trim().equals("null")) {
                            String[] pionParts = segments[0].trim().split(" ");
                            Pion.TypePion pionType = Pion.TypePion.valueOf(pionParts[0]);
                            int px = Integer.parseInt(pionParts[1]);
                            int py = Integer.parseInt(pionParts[2]);

                            // Recherche du pion dans la liste par type et position
                            for (Pion p : jeu.getPions()) {
                                if (p.getType() == pionType && p.getPosition() != null
                                        && p.getPosition().getX() == px && p.getPosition().getY() == py) {
                                    pion = p;
                                    break;
                                }
                            }
                            if (pion == null) break; // pion introuvable, on ignore cette action
                        }

                        // Index du joueur
                        int joueurIndex = Integer.parseInt(segments[3].trim());
                        Joueur joueur = jeu.getJoueurs()[joueurIndex];

                        // Lecture de f1 
                        Fleur f1 = null;
                        String seg1 = segments[1].trim();
                        if (!seg1.equals("null")) {
                            String[] f1Parts = seg1.split(" ");
                            Fleur.TypeFleur f1Type = Fleur.TypeFleur.valueOf(f1Parts[0]);
                            int f1x = Integer.parseInt(f1Parts[1]);
                            int f1y = Integer.parseInt(f1Parts[2]);
                            // Recherche dans les fleurs collectées du joueur
                            for (Fleur f : joueur.getFleursGagnees()) {
                                if (f.getType() == f1Type
                                        && f.getPosition().getX() == f1x
                                        && f.getPosition().getY() == f1y) {
                                    f1 = f;
                                    break;
                                }
                            }
                        }

                        // Lecture de f2 (peut être null)
                        Fleur f2 = null;
                        String seg2 = segments[2].trim();
                        if (!seg2.equals("null")) {
                            String[] f2Parts = seg2.split(" ");
                            Fleur.TypeFleur f2Type = Fleur.TypeFleur.valueOf(f2Parts[0]);
                            int f2x = Integer.parseInt(f2Parts[1]);
                            int f2y = Integer.parseInt(f2Parts[2]);
                            // Recherche dans les fleurs collectées du joueur
                            for (Fleur f : joueur.getFleursGagnees()) {
                                if (f.getType() == f2Type
                                        && f.getPosition().getX() == f2x
                                        && f.getPosition().getY() == f2y) {
                                    f2 = f;
                                    break;
                                }
                            }
                        }

                        undoStack.push(new ActionJeu(pion, f1, f2, joueur));
                        break;
                    }
                }
            }

            Configuration.debugeur("Partie chargée depuis : %s\n", cheminFichier);

        } catch (java.io.IOException e) {
            Configuration.debugeurErreur("Erreur lors du chargement : %s\n", e.getMessage());
        }
    }
}
