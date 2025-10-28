package mi.proyecto;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class MiJuegoPrincipal extends Game {
    public SpriteBatch batch;

    @Override
    public void create() {
        batch = new SpriteBatch();
        setScreen(new Menu(this));
    }

    public void mostrarMenu() {
        setScreen(new Menu(this));
    }

    public void mostrarJuego() {
        setScreen(new JuegoScreen(this));
    }

    @Override
    public void dispose() {
        batch.dispose();
        super.dispose();
    }
}

