package testcontroller;

import controller.EcouteurDeSouris;
import model.Jeu;
import org.junit.jupiter.api.Test;
import view.JeuGraphique;

import javax.swing.JFrame;
import java.awt.event.MouseEvent;

import static org.junit.jupiter.api.Assertions.*;

public class ControllerTests {

    /**
     * Test de non-régression sur le comportement de l'écouteur de souris.
     * Vérifie qu'un clic hors du cercle de jeu ne modifie pas l'état du modèle.
     */
    @Test
    void testEcouteurDeSourisClicHorsCercleNeModifiePasLeJeu() {

        JFrame frame = new JFrame();

        JeuGraphique gui = new JeuGraphique(frame);

        gui.setSize(660, 360);

        Jeu game = gui.jeu;

        int initialPionCount = game.getPions().size();

        MouseEvent event = new MouseEvent(
                gui,
                MouseEvent.MOUSE_PRESSED,
                System.currentTimeMillis(),
                0,
                0,
                0,
                1,
                false
        );

        new EcouteurDeSouris(gui).mousePressed(event);

        assertEquals(
                initialPionCount,
                game.getPions().size(),
                "Un clic hors du cercle ne doit pas ajouter un pion"
        );

        assertNull(game.getFleurSelectionnee1());
        assertNull(game.getFleurSelectionnee2());
    }
}