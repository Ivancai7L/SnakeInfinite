package mi.proyecto;

import com.badlogic.gdx.Game;

public class MiJuegoPrincipal extends Game {

    @Override
    public void create() {
        setScreen(new Menu(this));
    }

    public void mostrarMenu() {
        setScreen(new Menu(this));
    }

    public void mostrarJuego() {
        setScreen(new JuegoScreen(this));
    }

}
