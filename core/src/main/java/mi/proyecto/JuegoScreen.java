package mi.proyecto;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

public class JuegoScreen implements Screen {
    private MiJuegoPrincipal game;
    private Snake snake;
    private Frutas fruta;
    private BitmapFont font;
    private int puntaje;
    private boolean gameOver;

    public JuegoScreen(MiJuegoPrincipal game) {
        this.game = game;
        snake = new Snake();
        fruta = new Frutas();
        fruta.regenerar(snake);
        font = new BitmapFont();
        puntaje = 0;
        gameOver = false;
    }

    @Override
    public void render(float delta) {
        // actualizar
        if (!gameOver) {
            snake.update();

            // Colisión con fruta
            if (snake.getRect().overlaps(fruta.getRect())) {
                snake.crecer();
                fruta.regenerar(snake);
                puntaje++;
            }

            // Colisión consigo mismo
            if (snake.colisionaConCuerpo()) {
                gameOver = true;
            }
        }

        // dibujado
        Gdx.gl.glClearColor(0, 0.3f, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.batch.begin();
        fruta.draw(game.batch);
        snake.draw(game.batch);
        font.draw(game.batch, "Puntaje: " + puntaje, 20, Gdx.graphics.getHeight() - 20);
        if (gameOver) {
            font.draw(game.batch, "GAME OVER - ENTER para reiniciar",
                Gdx.graphics.getWidth()/2f - 180,
                Gdx.graphics.getHeight()/2f);
        }
        game.batch.end();

        // reiniciar si corresponde
        if (gameOver && Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ENTER)) {
            reiniciar();
        }
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ESCAPE)) {
            game.mostrarMenu();
        }
    }

    private void reiniciar() {
        snake.dispose();
        snake = new Snake();
        fruta.regenerar(snake);
        puntaje = 0;
        gameOver = false;
    }

    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void show() {}
    @Override public void hide() {}
    @Override public void dispose() {
        snake.dispose();
        fruta.dispose();
        font.dispose();
    }
}
