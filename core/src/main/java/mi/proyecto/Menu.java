package mi.proyecto;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.Input;

public class Menu implements Screen {
    private MiJuegoPrincipal game;
    private BitmapFont font;
    private Texture fondo;
    private Music musica;
    private float tiempo;

    public Menu(MiJuegoPrincipal game) {
        this.game = game;
        font = new BitmapFont();
        font.getData().setScale(2);
        font.setColor(Color.WHITE);


        fondo = new Texture("Fondosnake.jpg");
        musica = Gdx.audio.newMusic(Gdx.files.internal("Musicasnake.mp3"));
        musica.setLooping(true);
        musica.setVolume(0.5f);
        musica.play();

        tiempo = 0f;
    }

    @Override
    public void render(float delta) {
        tiempo += delta;

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.batch.begin();
        game.batch.draw(fondo, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // 📘 Título
        font.setColor(Color.WHITE);
        font.draw(game.batch, "SNAKE INFINITE",
            Gdx.graphics.getWidth() / 2f - 120,
            Gdx.graphics.getHeight() / 2f + 80);

        // ✨ Texto parpadeante
        float alpha = (float) ((Math.sin(tiempo * 3) + 1) / 2);
        font.setColor(1, 1, 1, alpha);
        font.draw(game.batch, "Presiona ENTER para jugar",
            Gdx.graphics.getWidth() / 2f - 160,
            Gdx.graphics.getHeight() / 2f + 20);

        // 📜 Texto fijo
        font.setColor(Color.WHITE);
        font.draw(game.batch, "Presiona ESC para salir",
            Gdx.graphics.getWidth() / 2f - 140,
            Gdx.graphics.getHeight() / 2f - 40);

        game.batch.end();

        // 🎮 Controles
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            game.mostrarJuego();
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            musica.stop();
            Gdx.app.exit();
        }
    }

    @Override public void show() {}
    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        font.dispose();
        fondo.dispose();
        musica.dispose();
        // ❌ No se debe cerrar game.batch aquí
    }
}
