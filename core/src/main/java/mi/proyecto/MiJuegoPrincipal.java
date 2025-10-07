package mi.proyecto;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class MiJuegoPrincipal extends Game {
    private SpriteBatch batch;
    private Snake snake;
    private Frutas frutas;

    @Override
    public void create() {
        batch = new SpriteBatch();
        snake = new Snake();
        frutas = new Frutas();
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0.3f, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        snake.draw(batch);
        frutas.draw(batch);
        batch.end();

        snake.update();
    }

    @Override
    public void dispose() {
        batch.dispose();
        snake.dispose();
        frutas.dispose();
    }
}
