package testcontroller;
// import controller.EcouteurDeClavier;
import controller.EcouteurDeSouris;
import global.Configuration;
import model.Jeu;
import org.junit.jupiter.api.Test;
import view.JeuGraphique;

import javax.swing.JFrame;
import java.awt.event.MouseEvent;
import java.lang.reflect.Field;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class ControllerTests {

    /*
    @Test
    void testEcouteurDeClavierNeLancePasException() {
        EcouteurDeClavier listener = new EcouteurDeClavier();
        KeyEvent event = new KeyEvent(new Button(), KeyEvent.KEY_PRESSED,
                System.currentTimeMillis(), 0, KeyEvent.VK_ENTER, '\n');

        assertDoesNotThrow(() -> listener.keyPressed(event));
    }*/

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
                "Un clic hors du cercle ne doit pas ajouter de pion"
        );

        assertNull(game.getFleurSelectionnee1());

        assertNull(game.getFleurSelectionnee2());
    }
}
