package mi.proyecto;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;

public class Menu implements Screen {
    private MiJuegoPrincipal game;
    private SpriteBatch batch;
    private BitmapFont font;
    private Texture fondo;
    private float tiempo; // 🔹 Variable para controlar el parpadeo

    public Menu(MiJuegoPrincipal game) {
        this.game = game;
        batch = new SpriteBatch();
        font = new BitmapFont();
        fondo = new Texture("Fondosnake.jpg");
        tiempo = 0; // Inicializa el contador de tiempo
    }

    @Override
    public void show() { }

    @Override
    public void render(float delta) {
        tiempo += delta;

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        batch.draw(fondo, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        font.getData().setScale(2);
        font.setColor(Color.WHITE);
        font.draw(batch, "Snake Infinite", Gdx.graphics.getWidth() / 2 - 100, Gdx.graphics.getHeight() / 2 + 40);

        // 🔹 Efecto de parpadeo con opacidad variable (entre 0 y 1)
        float alpha = (float) ((Math.sin(tiempo * 3) + 1) / 2);
        font.setColor(1, 1, 1, alpha); // RGB = blanco, alpha = transparencia
        font.draw(batch, "Presiona ENTER para jugar", Gdx.graphics.getWidth() / 2 - 150, Gdx.graphics.getHeight() / 2);

        // 🔹 Vuelve el color a opaco para el texto de salida
        font.setColor(Color.WHITE);
        font.draw(batch, "Presiona ESC para salir", Gdx.graphics.getWidth() / 2 - 150, Gdx.graphics.getHeight() / 2 - 60);

        batch.end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            game.setScreen(new JuegoScreen(game));
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }
    }

    @Override public void resize(int width, int height) { }
    @Override public void pause() { }
    @Override public void resume() { }
    @Override public void hide() { }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        fondo.dispose();
    }
}
