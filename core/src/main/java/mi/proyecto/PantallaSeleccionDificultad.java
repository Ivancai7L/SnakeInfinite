package mi.proyecto;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

public class PantallaSeleccionDificultad implements Screen {

    private final MiJuegoPrincipal juego;
    private BitmapFont font;
    private BitmapFont titleFont;

    public PantallaSeleccionDificultad(MiJuegoPrincipal juego) {
        this.juego = juego;
        System.out.println("PantallaSeleccionDificultad: Constructor llamado");
    }

    @Override
    public void show() {
        try {
            System.out.println("PantallaSeleccionDificultad: show() iniciado");

            font = new BitmapFont();
            font.setColor(Color.WHITE);
            font.getData().setScale(2);

            titleFont = new BitmapFont();
            titleFont.setColor(Color.YELLOW);
            titleFont.getData().setScale(3);

            System.out.println("PantallaSeleccionDificultad: show() completado");

        } catch (Exception e) {
            System.err.println("PantallaSeleccionDificultad: ERROR en show()");
            e.printStackTrace();
        }
    }

    @Override
    public void render(float delta) {
        try {
            Gdx.gl.glClearColor(0.2f, 0.2f, 0.3f, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

            juego.batch.begin();

            // Título
            titleFont.draw(juego.batch, "SELECCIONA DIFICULTAD",
                Gdx.graphics.getWidth() / 2f - 300,
                Gdx.graphics.getHeight() - 100);

            // Opciones
            font.draw(juego.batch, "1 - FACIL",
                Gdx.graphics.getWidth() / 2f - 100,
                Gdx.graphics.getHeight() / 2f + 100);

            font.draw(juego.batch, "2 - NORMAL",
                Gdx.graphics.getWidth() / 2f - 100,
                Gdx.graphics.getHeight() / 2f + 40);

            font.draw(juego.batch, "3 - DIFICIL",
                Gdx.graphics.getWidth() / 2f - 100,
                Gdx.graphics.getHeight() / 2f - 20);

            font.draw(juego.batch, "4 - IMPOSIBLE",
                Gdx.graphics.getWidth() / 2f - 100,
                Gdx.graphics.getHeight() / 2f - 80);

            juego.batch.end();

            // Detección de teclas
            if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.NUM_1)) {
                System.out.println("PantallaSeleccionDificultad: Tecla 1 presionada - FACIL");
                juego.dificultadSeleccionada = Dificultad.FACIL;
                System.out.println("PantallaSeleccionDificultad: Llamando a iniciarJuego()");
                juego.iniciarJuego();
            }

            if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.NUM_2)) {
                System.out.println("PantallaSeleccionDificultad: Tecla 2 presionada - NORMAL");
                juego.dificultadSeleccionada = Dificultad.NORMAL;
                System.out.println("PantallaSeleccionDificultad: Llamando a iniciarJuego()");
                juego.iniciarJuego();
            }

            if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.NUM_3)) {
                System.out.println("PantallaSeleccionDificultad: Tecla 3 presionada - DIFICIL");
                juego.dificultadSeleccionada = Dificultad.DIFICIL;
                System.out.println("PantallaSeleccionDificultad: Llamando a iniciarJuego()");
                juego.iniciarJuego();
            }

            if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.NUM_4)) {
                System.out.println("PantallaSeleccionDificultad: Tecla 4 presionada - IMPOSIBLE");
                juego.dificultadSeleccionada = Dificultad.IMPOSIBLE;
                System.out.println("PantallaSeleccionDificultad: Llamando a iniciarJuego()");
                juego.iniciarJuego();
            }

        } catch (Exception e) {
            System.err.println("PantallaSeleccionDificultad: ERROR en render()");
            e.printStackTrace();
        }
    }

    @Override
    public void resize(int width, int height) {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {
        System.out.println("PantallaSeleccionDificultad: hide() llamado");
        dispose();
    }

    @Override
    public void dispose() {
        try {
            System.out.println("PantallaSeleccionDificultad: dispose() llamado");
            if (font != null) font.dispose();
            if (titleFont != null) titleFont.dispose();
        } catch (Exception e) {
            System.err.println("PantallaSeleccionDificultad: ERROR en dispose()");
            e.printStackTrace();
        }
    }
}
