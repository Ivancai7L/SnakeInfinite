package mi.proyecto;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.Random;

public class JuegoScreen implements Screen {
    private final MiJuegoPrincipal game;
    private OrthographicCamera camara;
    private Snake snake;
    private ArrayList<Frutas> frutas;
    private BitmapFont font;
    private int puntaje;
    private boolean gameOver;
    private Music musica;
    private Random random;

    private static final int CANTIDAD_FRUTAS = 25;

    public JuegoScreen(MiJuegoPrincipal game) {
        this.game = game;
        camara = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camara.setToOrtho(false);

        snake = new Snake();
        frutas = new ArrayList<>();
        random = new Random();

        for (int i = 0; i < CANTIDAD_FRUTAS; i++) {
            Frutas f = new Frutas();
            f.regenerar();
            frutas.add(f);
        }

        font = new BitmapFont();
        font.setColor(Color.WHITE);
        puntaje = 0;
        gameOver = false;

        musica = Gdx.audio.newMusic(Gdx.files.internal("Musicasnake.mp3"));
        musica.setLooping(true);
        musica.setVolume(0.4f);
        musica.play();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0f, 0.25f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (!gameOver) {
            snake.update(delta);

            Vector2 cabeza = snake.getCabeza();
            for (Frutas f : frutas) {
                if (f.getRect().contains(cabeza.x, cabeza.y)) {
                    snake.crecer();
                    puntaje++;
                    f.regenerar();
                }
            }

            if (colisionaConCuerpo()) {
                gameOver = true;
                musica.stop();
            }
        }

        camara.position.set(snake.getCabeza().x, snake.getCabeza().y, 0);
        camara.update();
        game.batch.setProjectionMatrix(camara.combined);

        game.batch.begin();
        for (Frutas f : frutas) f.draw(game.batch);
        snake.draw(game.batch);

        font.draw(game.batch, "Puntaje: " + puntaje,
            camara.position.x - Gdx.graphics.getWidth() / 2f + 20,
            camara.position.y + Gdx.graphics.getHeight() / 2f - 20);

        if (gameOver) {
            font.draw(game.batch, "GAME OVER - ENTER para reiniciar",
                camara.position.x - 180, camara.position.y);
        }

        game.batch.end();

        if (gameOver && Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) reiniciar();
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            musica.stop();
            game.mostrarMenu();
        }
    }

    private boolean colisionaConCuerpo() {
        Vector2 cabeza = snake.getCabeza();
        ArrayList<Vector2> cuerpo = snake.getCuerpo();
        for (int i = 5; i < cuerpo.size(); i++) {
            if (cabeza.dst(cuerpo.get(i)) < 10f) return true;
        }
        return false;
    }

    private void reiniciar() {
        snake.dispose();
        snake = new Snake();
        frutas.clear();
        for (int i = 0; i < CANTIDAD_FRUTAS; i++) {
            Frutas f = new Frutas();
            f.regenerar();
            frutas.add(f);
        }
        puntaje = 0;
        gameOver = false;
        musica.play();
    }

    @Override public void resize(int width, int height) { camara.setToOrtho(false, width, height); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void show() {}
    @Override public void hide() {}
    @Override public void dispose() {
        snake.dispose();
        for (Frutas f : frutas) f.dispose();
        font.dispose();
        musica.dispose();
    }
}
