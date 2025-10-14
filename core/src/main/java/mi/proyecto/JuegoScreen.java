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
    private int puntaje;

    public JuegoScreen(MiJuegoPrincipal game) {
        this.game = game;
        batch = new SpriteBatch();
        snake = new Snake();
        frutas = new Frutas();
        puntaje = 0;
    }

    @Override
    public void show() { }

    @Override
    public void render(float delta) {
        snake.update();

        Gdx.gl.glClearColor(0, 0.3f, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (colisionaConFruta()) {
            frutas.regenerar();
            puntaje++;
            System.out.println("Puntaje: " + puntaje);
        }

        batch.begin();
        snake.draw(batch);
        frutas.draw(batch);
        batch.end();
    }

    private boolean colisionaConFruta() {
        float snakeX = snake.getX();
        float snakeY = snake.getY();
        float snakeSize = snake.getSize();
        float fruitX = frutas.getX();
        float fruitY = frutas.getY();
        float fruitSize = frutas.getSize();

        // Colisión simple tipo AABB
        return snakeX < fruitX + fruitSize &&
            snakeX + snakeSize > fruitX &&
            snakeY < fruitY + fruitSize &&
            snakeY + snakeSize > fruitY;
    }

    @Override public void resize(int width, int height) { }
    @Override public void pause() { }
    @Override public void resume() { }
    @Override public void hide() { }

    @Override
    public void dispose() {
        batch.dispose();
        snake.dispose();
        frutas.dispose();
    }
}
