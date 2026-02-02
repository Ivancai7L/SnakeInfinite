package mi.proyecto;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class MiJuegoPrincipal extends Game {

    public SpriteBatch batch;
    public Dificultad dificultadSeleccionada;

    @Override
    public void create() {
        try {
            System.out.println("MiJuegoPrincipal: create() iniciado");
            batch = new SpriteBatch();
            System.out.println("MiJuegoPrincipal: SpriteBatch creado");
            mostrarMenu();
            System.out.println("MiJuegoPrincipal: create() completado");
        } catch (Exception e) {
            System.err.println("MiJuegoPrincipal: ERROR en create()");
            e.printStackTrace();
        }
    }

    public void mostrarMenu() {
        try {
            System.out.println("MiJuegoPrincipal: mostrarMenu() llamado");
            setScreen(new Menu(this));
            System.out.println("MiJuegoPrincipal: Menu establecido");
        } catch (Exception e) {
            System.err.println("MiJuegoPrincipal: ERROR en mostrarMenu()");
            e.printStackTrace();
        }
    }

    public void mostrarSeleccionDificultad() {
        try {
            System.out.println("MiJuegoPrincipal: mostrarSeleccionDificultad() llamado");
            setScreen(new PantallaSeleccionDificultad(this));
            System.out.println("MiJuegoPrincipal: PantallaSeleccionDificultad establecida");
        } catch (Exception e) {
            System.err.println("MiJuegoPrincipal: ERROR en mostrarSeleccionDificultad()");
            e.printStackTrace();
        }
    }

    public void iniciarJuego() {
        try {
            System.out.println("MiJuegoPrincipal: iniciarJuego() llamado");
            System.out.println("MiJuegoPrincipal: Dificultad seleccionada: " + dificultadSeleccionada);
            setScreen(new JuegoScreen(this));
            System.out.println("MiJuegoPrincipal: JuegoScreen establecido");
        } catch (Exception e) {
            System.err.println("MiJuegoPrincipal: ERROR en iniciarJuego()");
            e.printStackTrace();
        }
    }

    public void mostrarJuego() {
        setScreen(new JuegoScreen(this));
    }

    @Override
    public void dispose() {
        try {
            System.out.println("MiJuegoPrincipal: dispose() llamado");
            batch.dispose();
        } catch (Exception e) {
            System.err.println("MiJuegoPrincipal: ERROR en dispose()");
            e.printStackTrace();
        }
    }


}
