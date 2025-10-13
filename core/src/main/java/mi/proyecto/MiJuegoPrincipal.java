package mi.proyecto;

import com.badlogic.gdx.Game;

public class MiJuegoPrincipal extends Game {

    @Override
    public void create() {
        // Inicia el juego 
        setScreen(new Menu(this));
    }
}
