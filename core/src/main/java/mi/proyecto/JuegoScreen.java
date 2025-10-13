package mi.proyecto;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class JuegoScreen implements Screen {
    private MiJuegoPrincipal game;
    private SpriteBatch batch;
    private Snake snake;
    private Frutas frutas;

    public JuegoScreen(MiJuegoPrincipal game) {
        this.game = game;
        batch = new SpriteBatch();
        snake = new Snake();
        frutas = new Frutas();
    }

    @Override
    public void show() { }

    @Override
    public void render(float delta) {
        snake.update();

        Gdx.gl.glClearColor(0, 0.3f, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        snake.draw(batch);
        frutas.draw(batch);
        batch.end();
    }

    @Override
    public void resize(int width, int height) { }

    @Override
    public void pause() { }

    @Override
    public void resume() { }

    @Override
    public void hide() { }

    @Override
    public void dispose() {
        batch.dispose();
        snake.dispose();
        frutas.dispose();
    }
}
