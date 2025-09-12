package mi.proyecto;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

public class MiJuegoPrincipal extends ApplicationAdapter {
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
        ScreenUtils.clear(0, 0, 0, 1);

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
