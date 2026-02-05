package mi.proyecto;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class JuegoScreen implements Screen {

    private static final float VELOCIDAD_BASE = 50f;
    private static final float TAMANIO_SERPIENTE = 20f;
    private static final float ESCALA_FONT = 2f;

    private final MiJuegoPrincipal juego;
    private Snake snake;
    private Frutas fruta;
    private BitmapFont font;

    private int puntuacion;
    private boolean juegoTerminado;

    public JuegoScreen(MiJuegoPrincipal juego) {
        System.out.println("JuegoScreen: Constructor llamado");
        this.juego = juego;
        System.out.println("JuegoScreen: Constructor completado");
    }

    @Override
    public void show() {
        try {
            if (juego.dificultadSeleccionada == null) {
                juego.dificultadSeleccionada = Dificultad.NORMAL;
                System.out.println("Advertencia: No se seleccionó dificultad. Usando NORMAL por defecto.");
            }

            float velocidad = juego.dificultadSeleccionada.getVelocidad() * VELOCIDAD_BASE;

            snake = new Snake(velocidad, TAMANIO_SERPIENTE);
            System.out.println("Snake creada con velocidad: " + velocidad);

            fruta = new Frutas();
            System.out.println("Frutas creadas");

            font = new BitmapFont();
            font.setColor(Color.WHITE);
            font.getData().setScale(ESCALA_FONT);

            puntuacion = 0;
            juegoTerminado = false;

            System.out.println("JuegoScreen inicializado correctamente");

        } catch (Exception e) {
            System.err.println("ERROR al inicializar JuegoScreen:");
            e.printStackTrace();
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    juego.mostrarMenu();
                }
            });
        }
    }

    @Override
    public void render(float delta) {
        try {
            Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

            if (!juegoTerminado && snake != null) {
                manejarInput();
                snake.actualizar(delta);
                verificarColisionConFruta();
                verificarColisionConBordes();
                verificarColisionConCuerpo();
            }

            juego.batch.begin();

            if (fruta != null) {
                fruta.dibujar(juego.batch);
            }

            if (snake != null) {
                snake.dibujar(juego.batch);
            }

            if (font != null) {
                font.draw(juego.batch, "Puntuación: " + puntuacion, 20, Gdx.graphics.getHeight() - 20);
                font.draw(juego.batch, "ESC para volver al menú", 20, Gdx.graphics.getHeight() - 50);
            }

            if (juegoTerminado && font != null) {
                font.draw(juego.batch, "GAME OVER! Presiona ESPACIO para reiniciar",
                    Gdx.graphics.getWidth() / 2f - 400,
                    Gdx.graphics.getHeight() / 2f);
            }

            juego.batch.end();

            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                juego.mostrarMenu();
            }

            if (juegoTerminado && Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                show();
            }

        } catch (Exception e) {
            System.err.println("ERROR en render de JuegoScreen:");
            e.printStackTrace();
        }
    }

    private void manejarInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            snake.cambiarDireccion(Direccion.ARRIBA);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            snake.cambiarDireccion(Direccion.ABAJO);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            snake.cambiarDireccion(Direccion.IZQUIERDA);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            snake.cambiarDireccion(Direccion.DERECHA);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            snake.cambiarDireccion(Direccion.ARRIBA);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            snake.cambiarDireccion(Direccion.ABAJO);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.A)) {
            snake.cambiarDireccion(Direccion.IZQUIERDA);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.D)) {
            snake.cambiarDireccion(Direccion.DERECHA);
        }
    }

    private void verificarColisionConFruta() {
        if (snake == null || fruta == null) return;

        Vector2 cabeza = snake.getCabeza();
        Rectangle areaCabeza = new Rectangle(cabeza.x, cabeza.y, snake.getTamano(), snake.getTamano());

        if (areaCabeza.overlaps(fruta.getRect())) {
            snake.comer();
            fruta.regenerar();
            puntuacion += 10;
            System.out.println("Fruta comida! Puntuación: " + puntuacion);
        }
    }

    private void verificarColisionConBordes() {
        if (snake == null) return;

        Vector2 cabeza = snake.getCabeza();

        if (cabeza.x < 0 || cabeza.x > Gdx.graphics.getWidth() - snake.getTamano() ||
            cabeza.y < 0 || cabeza.y > Gdx.graphics.getHeight() - snake.getTamano()) {
            juegoTerminado = true;
            System.out.println("Game Over! Puntuación final: " + puntuacion);
        }
    }

    private void verificarColisionConCuerpo() {
        if (snake == null) return;

        if (snake.getCuerpo().size() < 8) {
            return;
        }

        Vector2 cabeza = snake.getCabeza();
        boolean esCabeza = true;
        int segmentosIgnorados = 0;
        for (Vector2 segmento : snake.getCuerpo()) {
            if (esCabeza) {
                esCabeza = false;
                continue;
            }
            if (segmentosIgnorados < 3) {
                segmentosIgnorados++;
                continue;
            }
            if (cabeza.dst(segmento) < snake.getTamano() * 0.35f) {
                juegoTerminado = true;
                System.out.println("Game Over! Puntuación final: " + puntuacion);
                return;
            }
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
        dispose();
    }

    @Override
    public void dispose() {
        try {
            if (snake != null) snake.dispose();
            if (fruta != null) fruta.dispose();
            if (font != null) font.dispose();
        } catch (Exception e) {
            System.err.println("Error al hacer dispose: " + e.getMessage());
        }
    }
}
