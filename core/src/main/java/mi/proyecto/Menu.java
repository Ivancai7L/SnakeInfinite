package mi.proyecto;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.Input;

public class Menu implements Screen {
    private MiJuegoPrincipal game;
    private SpriteBatch batch;
    private BitmapFont font;
    private Texture fondo;

    public Menu(MiJuegoPrincipal game) {
        this.game = game;
        batch = new SpriteBatch();
        font = new BitmapFont();
        // Cargar imagen de fondo desde assets
        fondo = new Texture("Fondosnake.jpg");
    }

    @Override
    public void show() { }

    @Override
    public void render(float delta) {
        // Limpiar pantalla
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        // Dibujar fondo
        batch.draw(fondo, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // Dibujar texto del menú
        font.getData().setScale(2);
        font.draw(batch, "Snake Infinite", Gdx.graphics.getWidth() / 2 - 100, Gdx.graphics.getHeight() / 2 + 40);
        font.draw(batch, "Presiona ENTER para jugar", Gdx.graphics.getWidth() / 2 - 150, Gdx.graphics.getHeight() / 2);
        font.draw(batch, "Presiona ESC para salir", Gdx.graphics.getWidth() / 2 - 150, Gdx.graphics.getHeight() / 2 - 60);
        batch.end();

        // Controles del menú
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            game.setScreen(new JuegoScreen(game));
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }
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
        font.dispose();
        fondo.dispose();
    }
}
