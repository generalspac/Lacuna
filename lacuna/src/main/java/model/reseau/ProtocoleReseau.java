package model.reseau;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import model.Fleur;
import model.Pion;

public final class ProtocoleReseau {
    public static final int PORT_DEFAUT = 5050;

    private static final String TYPE_ETAT = "ETAT ";

    private ProtocoleReseau() {
    }

    public static String creerMessageEtat(EtatReseau etat) {
        String corps = encoderEtat(etat);
        String base64 = Base64.getEncoder().encodeToString(corps.getBytes(StandardCharsets.UTF_8));
        return TYPE_ETAT + base64;
    }

    public static EtatReseau lireMessageEtat(String message) {
        if (message == null || !message.startsWith(TYPE_ETAT)) {
            return null;
        }
        String base64 = message.substring(TYPE_ETAT.length());
        String corps = new String(Base64.getDecoder().decode(base64), StandardCharsets.UTF_8);
        return decoderEtat(corps);
    }

    private static String encoderEtat(EtatReseau etat) {
        StringBuilder sb = new StringBuilder();
        ligne(sb, "JOUEUR", etat.joueurActuel);
        ligne(sb, "AVANTAGE", etat.avantageApplique + ";" + etat.phaseAvantage);
        ligne(sb, "ABANDON", etat.partieAbandonnee + ";" + etat.gagnantAbandonIndex);
        ligne(sb, "MESSAGE", proteger(etat.message));

        for (EtatReseau.JoueurReseau joueur : etat.joueurs) {
            ligne(sb, "J", proteger(joueur.nom) + ";" + joueur.type.name());
        }
        for (EtatReseau.PionReseau pion : etat.pions) {
            String pos = pion.x == null ? "null" : pion.x + ";" + pion.y;
            ligne(sb, "P", pion.type.name() + ";" + pos);
        }
        for (EtatReseau.FleurReseau fleur : etat.fleursPlateau) {
            ligne(sb, "F", fleur.type.name() + ";" + fleur.x + ";" + fleur.y);
        }
        for (EtatReseau.FleurGagneeReseau fleur : etat.fleursGagnees) {
            ligne(sb, "G", fleur.joueurIndex + ";" + fleur.type.name() + ";" + fleur.x + ";" + fleur.y);
        }
        return sb.toString();
    }

    private static EtatReseau decoderEtat(String texte) {
        EtatReseau etat = new EtatReseau();
        String[] lignes = texte.split("\\R");
        for (String ligne : lignes) {
            if (ligne.isEmpty()) {
                continue;
            }
            int pos = ligne.indexOf('=');
            if (pos < 0) {
                continue;
            }
            String cle = ligne.substring(0, pos);
            String valeur = ligne.substring(pos + 1);
            String[] morceaux = valeur.split(";", -1);

            switch (cle) {
                case "JOUEUR":
                    etat.joueurActuel = Integer.parseInt(valeur);
                    break;
                case "AVANTAGE":
                    etat.avantageApplique = Boolean.parseBoolean(morceaux[0]);
                    etat.phaseAvantage = morceaux.length > 1 && Boolean.parseBoolean(morceaux[1]);
                    break;
                case "ABANDON":
                    etat.partieAbandonnee = Boolean.parseBoolean(morceaux[0]);
                    etat.gagnantAbandonIndex = morceaux.length > 1 ? Integer.parseInt(morceaux[1]) : -1;
                    break;
                case "MESSAGE":
                    etat.message = deproteger(valeur);
                    break;
                case "J":
                    etat.joueurs.add(new EtatReseau.JoueurReseau(deproteger(morceaux[0]),
                            Pion.TypePion.valueOf(morceaux[1])));
                    break;
                case "P":
                    if ("null".equals(morceaux[1])) {
                        etat.pions.add(new EtatReseau.PionReseau(Pion.TypePion.valueOf(morceaux[0]), null, null));
                    } else {
                        etat.pions.add(new EtatReseau.PionReseau(Pion.TypePion.valueOf(morceaux[0]),
                                Integer.parseInt(morceaux[1]), Integer.parseInt(morceaux[2])));
                    }
                    break;
                case "F":
                    etat.fleursPlateau.add(new EtatReseau.FleurReseau(Fleur.TypeFleur.valueOf(morceaux[0]),
                            Integer.parseInt(morceaux[1]), Integer.parseInt(morceaux[2])));
                    break;
                case "G":
                    etat.fleursGagnees.add(new EtatReseau.FleurGagneeReseau(Integer.parseInt(morceaux[0]),
                            Fleur.TypeFleur.valueOf(morceaux[1]), Integer.parseInt(morceaux[2]),
                            Integer.parseInt(morceaux[3])));
                    break;
                default:
                    break;
            }
        }
        return etat;
    }

    private static void ligne(StringBuilder sb, String cle, Object valeur) {
        sb.append(cle).append('=').append(valeur).append('\n');
    }

    private static String proteger(String texte) {
        if (texte == null) {
            texte = "";
        }
        return Base64.getEncoder().encodeToString(texte.getBytes(StandardCharsets.UTF_8));
    }

    private static String deproteger(String texte) {
        return new String(Base64.getDecoder().decode(texte), StandardCharsets.UTF_8);
    }
}
